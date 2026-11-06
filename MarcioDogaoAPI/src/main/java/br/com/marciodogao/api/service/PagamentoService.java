package br.com.marciodogao.api.service;

import br.com.marciodogao.api.domain.Caixa;
import br.com.marciodogao.api.domain.Comanda;
import br.com.marciodogao.api.domain.FormaPagamento;
import br.com.marciodogao.api.domain.MovimentacaoCaixa;
import br.com.marciodogao.api.domain.Pagamento;
import br.com.marciodogao.api.domain.Pedido;
import br.com.marciodogao.api.domain.Turno;
import br.com.marciodogao.api.dto.ComandaDtos.PagamentoResponse;
import br.com.marciodogao.api.exception.RegraNegocioException;
import br.com.marciodogao.api.repository.MovimentacaoCaixaRepository;
import br.com.marciodogao.api.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * REGRAS DE PAGAMENTO:
 * - Só se recebe com caixa aberto.
 * - Cada pedido é pago uma única vez (dá para pagar a comanda inteira ou pedido a pedido,
 *   ex.: uma parte no PIX e outra no dinheiro).
 * - O valor cobrado é calculado pela API (preço do turno), gravado em Pagamento.Valor_Pago
 *   e lançado como entrada em Movimentacao_Caixa.
 */
@Service
public class PagamentoService {

    private final ComandaService comandas;
    private final CaixaService caixas;
    private final PagamentoRepository pagamentos;
    private final MovimentacaoCaixaRepository movimentacoes;

    public PagamentoService(ComandaService comandas, CaixaService caixas,
                            PagamentoRepository pagamentos, MovimentacaoCaixaRepository movimentacoes) {
        this.comandas = comandas;
        this.caixas = caixas;
        this.pagamentos = pagamentos;
        this.movimentacoes = movimentacoes;
    }

    /** Paga todos os pedidos ainda não pagos da comanda. */
    @Transactional
    public PagamentoResponse pagarComanda(Integer codComan, FormaPagamento forma) {
        Comanda comanda = comandas.obter(codComan);
        List<Pedido> pendentes = comanda.getPedidos().stream().filter(p -> !p.isPago()).toList();
        if (pendentes.isEmpty()) {
            throw new RegraNegocioException(comanda.getPedidos().isEmpty()
                    ? "A comanda nº " + comanda.getNumeroComanda() + " não tem pedidos para pagar."
                    : "A comanda nº " + comanda.getNumeroComanda() + " já está totalmente paga.");
        }
        return registrar(comanda, pendentes, forma);
    }

    @Transactional
    public PagamentoResponse pagarPedido(Integer codComan, Integer codPed, FormaPagamento forma) {
        Comanda comanda = comandas.obter(codComan);
        Pedido pedido = comandas.pedidoDaComanda(comanda, codPed);
        if (pedido.isPago()) {
            throw new RegraNegocioException("O pedido " + codPed + " já foi pago.");
        }
        return registrar(comanda, List.of(pedido), forma);
    }

    private PagamentoResponse registrar(Comanda comanda, List<Pedido> aPagar, FormaPagamento forma) {
        Caixa caixa = caixas.caixaAbertoObrigatorio();
        Turno turno = comandas.turnoDa(comanda);

        BigDecimal total = BigDecimal.ZERO;
        List<Integer> pagos = new ArrayList<>();
        for (Pedido pedido : aPagar) {
            BigDecimal valor = Dinheiro.duasCasas(pedido.valor(turno));
            pagamentos.save(new Pagamento(pedido, caixa, forma, valor, comanda.getMesa()));
            total = total.add(valor);
            pagos.add(pedido.getCodPed());
        }
        movimentacoes.save(MovimentacaoCaixa.entrada(caixa, total));
        comanda.recalcularValor(turno);

        return new PagamentoResponse(comanda.getCodComan(), caixa.getCodCaix(), forma, total, pagos, comanda.status());
    }
}
