package br.com.marciodogao.api.service;

import br.com.marciodogao.api.domain.Cardapio;
import br.com.marciodogao.api.domain.Comanda;
import br.com.marciodogao.api.domain.Login;
import br.com.marciodogao.api.domain.Pagamento;
import br.com.marciodogao.api.domain.Pedido;
import br.com.marciodogao.api.domain.PedidoFeito;
import br.com.marciodogao.api.domain.StatusComanda;
import br.com.marciodogao.api.domain.Turno;
import br.com.marciodogao.api.dto.ComandaDtos.AbrirComandaRequest;
import br.com.marciodogao.api.dto.ComandaDtos.ComandaResponse;
import br.com.marciodogao.api.dto.ComandaDtos.ItemRequest;
import br.com.marciodogao.api.dto.ComandaDtos.ItemResponse;
import br.com.marciodogao.api.dto.ComandaDtos.NovoPedidoRequest;
import br.com.marciodogao.api.dto.ComandaDtos.PedidoResponse;
import br.com.marciodogao.api.exception.NaoEncontradoException;
import br.com.marciodogao.api.exception.RegraNegocioException;
import br.com.marciodogao.api.repository.ComandaRepository;
import br.com.marciodogao.api.repository.LoginRepository;
import br.com.marciodogao.api.repository.MesaRepository;
import br.com.marciodogao.api.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REGRAS DA COMANDA:
 * - Não pode haver duas comandas ABERTAS com o mesmo número, nem duas na mesma mesa.
 * - O horário de abertura define o turno e, portanto, o preço (manhã / noite) de todos os itens.
 * - Comanda FECHADA (tudo pago) não recebe novos pedidos.
 * - Pedido pago não pode ser alterado nem removido; comanda com algum pagamento não pode ser cancelada.
 * - Valor_Pedido da comanda é sempre recalculado pela API (nunca vem do app).
 */
@Service
public class ComandaService {

    private final ComandaRepository comandas;
    private final PedidoRepository pedidos;
    private final LoginRepository logins;
    private final CardapioService cardapio;
    private final TurnoService turnos;
    private final MesaRepository mesas;

    public ComandaService(ComandaRepository comandas, PedidoRepository pedidos, LoginRepository logins,
                          CardapioService cardapio, TurnoService turnos, MesaRepository mesas) {
        this.mesas = mesas;
        this.comandas = comandas;
        this.pedidos = pedidos;
        this.logins = logins;
        this.cardapio = cardapio;
        this.turnos = turnos;
    }

    @Transactional
    public ComandaResponse abrir(AbrirComandaRequest req, Integer codLog) {
        String numero = req.numeroComanda().trim();
        boolean jaAberta = comandas.findByNumeroComanda(numero).stream().anyMatch(c -> c.status() == StatusComanda.ABERTA);
        if (jaAberta) {
            throw new RegraNegocioException("A comanda nº " + numero + " já está aberta. Use outra ou feche a atual.");
        }
        Login funcionario = logins.findById(codLog)
                .orElseThrow(() -> new NaoEncontradoException("Funcionário logado não encontrado."));

        String mesa = req.mesa() == null || req.mesa().isBlank() ? null : req.mesa().trim();
        if (mesa != null) {
            if (mesas.count() > 0 && !mesas.existsByNumero(mesa)) {
                throw new RegraNegocioException("A mesa " + mesa + " não está cadastrada.");
            }
            boolean mesaOcupada = comandas.findAbertas().stream().anyMatch(c -> mesa.equals(c.getMesa()));
            if (mesaOcupada) {
                throw new RegraNegocioException("A mesa " + mesa + " já tem uma comanda aberta.");
            }
        }
        Comanda comanda = new Comanda(numero, mesa, turnos.agora(), nomeDoFuncionario(req.nomeFuncionario(), funcionario), funcionario);
        return resposta(comandas.save(comanda));
    }

    @Transactional(readOnly = true)
    public List<ComandaResponse> listar(StatusComanda status) {
        List<Comanda> lista = status == StatusComanda.FECHADA ? comandas.findFechadas() : comandas.findAbertas();
        return lista.stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public ComandaResponse buscar(Integer codComan) {
        return resposta(obter(codComan));
    }

    @Transactional
    public ComandaResponse adicionarPedido(Integer codComan, NovoPedidoRequest req) {
        Comanda comanda = obter(codComan);
        if (comanda.status() == StatusComanda.FECHADA) {
            throw new RegraNegocioException("A comanda nº " + comanda.getNumeroComanda() + " já foi paga e está fechada. Abra uma nova comanda.");
        }
        String obs = req.observacao() == null || req.observacao().isBlank() ? null : req.observacao().trim();
        Pedido pedido = new Pedido(comanda, obs);
        for (ItemRequest item : req.itens()) {
            Cardapio produto = cardapio.obter(item.codCar());
            if (produto.getPreco() == null) {
                throw new RegraNegocioException("\"" + produto.getProduto() + "\" está sem preço cadastrado.");
            }
            int quantidade = item.quantidade() == null ? 1 : item.quantidade();
            // Pedido_Feito não tem coluna de quantidade: cada unidade é uma linha.
            for (int i = 0; i < quantidade; i++) {
                pedido.adicionarItem(produto, item.variacao());
            }
        }
        pedidos.save(pedido);
        comanda.recalcularValor(turnoDa(comanda));
        return resposta(comanda);
    }

    @Transactional
    public ComandaResponse removerPedido(Integer codComan, Integer codPed) {
        Comanda comanda = obter(codComan);
        Pedido pedido = pedidoDaComanda(comanda, codPed);
        exigirNaoPago(pedido);
        comanda.getPedidos().remove(pedido);
        pedidos.delete(pedido);
        comanda.recalcularValor(turnoDa(comanda));
        return resposta(comanda);
    }

    @Transactional
    public ComandaResponse removerItem(Integer codComan, Integer codPed, Integer codPedFei) {
        Comanda comanda = obter(codComan);
        Pedido pedido = pedidoDaComanda(comanda, codPed);
        exigirNaoPago(pedido);
        PedidoFeito item = pedido.getItens().stream()
                .filter(i -> i.getCodPedFei().equals(codPedFei))
                .findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Item " + codPedFei + " não pertence ao pedido " + codPed + "."));
        pedido.getItens().remove(item);
        if (pedido.getItens().isEmpty()) {
            comanda.getPedidos().remove(pedido);
            pedidos.delete(pedido);
        }
        comanda.recalcularValor(turnoDa(comanda));
        return resposta(comanda);
    }

    @Transactional
    public void cancelar(Integer codComan) {
        Comanda comanda = obter(codComan);
        if (comanda.temPagamento()) {
            throw new RegraNegocioException("A comanda nº " + comanda.getNumeroComanda() + " já tem pagamento e não pode ser cancelada.");
        }
        pedidos.deleteAll(comanda.getPedidos());
        comandas.delete(comanda);
    }

    // ---- usado também por PagamentoService / CaixaService ----

    Comanda obter(Integer codComan) {
        return comandas.findById(codComan)
                .orElseThrow(() -> new NaoEncontradoException("Comanda " + codComan + " não encontrada."));
    }

    Pedido pedidoDaComanda(Comanda comanda, Integer codPed) {
        return comanda.getPedidos().stream()
                .filter(p -> p.getCodPed().equals(codPed))
                .findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Pedido " + codPed + " não pertence à comanda " + comanda.getCodComan() + "."));
    }

    Turno turnoDa(Comanda comanda) {
        return turnos.turnoDe(comanda.getHorarioPedido());
    }

    ComandaResponse resposta(Comanda c) {
        Turno turno = turnoDa(c);
        List<PedidoResponse> pedidosResp = c.getPedidos().stream().map(p -> new PedidoResponse(
                p.getCodPed(),
                p.getObservacao(),
                p.isPago(),
                p.getPagamentos().stream().map(Pagamento::getFormaPagamento).distinct().collect(Collectors.joining(", ")),
                Dinheiro.duasCasas(p.valorCobrado(turno)),
                p.getItens().stream().map(i -> new ItemResponse(
                        i.getCodPedFei(),
                        i.getCardapio().getCodCar(),
                        i.getCardapio().getProduto(),
                        i.getVariacaoPedido(),
                        Dinheiro.duasCasas(i.precoUnitario(turno)))).toList()
        )).toList();

        BigDecimal total = Dinheiro.duasCasas(c.getValorPedido());
        BigDecimal pago = Dinheiro.duasCasas(c.getPedidos().stream()
                .filter(Pedido::isPago).map(p -> p.valorCobrado(turno)).reduce(BigDecimal.ZERO, BigDecimal::add));
        return new ComandaResponse(c.getCodComan(), c.getNumeroComanda(), c.getMesa(), c.getHorarioPedido(), turno,
                c.getNomeFuncionario(), c.getLogin() == null ? null : c.getLogin().getCodLog(), c.status(),
                total, pago, total.subtract(pago), pedidosResp);
    }

    private static void exigirNaoPago(Pedido pedido) {
        if (pedido.isPago()) {
            throw new RegraNegocioException("O pedido " + pedido.getCodPed() + " já foi pago e não pode ser alterado.");
        }
    }

    private static String nomeDoFuncionario(String informado, Login funcionario) {
        String nome = informado == null || informado.isBlank()
                ? funcionario.getEmail().substring(0, Math.max(funcionario.getEmail().indexOf('@'), 0))
                : informado.trim();
        if (nome.isBlank()) {
            nome = funcionario.getEmail();
        }
        return nome.length() > 50 ? nome.substring(0, 50) : nome;
    }
}
