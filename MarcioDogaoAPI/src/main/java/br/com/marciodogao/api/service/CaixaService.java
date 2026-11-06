package br.com.marciodogao.api.service;

import br.com.marciodogao.api.domain.Caixa;
import br.com.marciodogao.api.domain.Comanda;
import br.com.marciodogao.api.domain.Dashboard;
import br.com.marciodogao.api.domain.MovimentacaoCaixa;
import br.com.marciodogao.api.domain.Pagamento;
import br.com.marciodogao.api.dto.CaixaDtos.AbrirCaixaRequest;
import br.com.marciodogao.api.dto.CaixaDtos.CaixaHistoricoResponse;
import br.com.marciodogao.api.dto.CaixaDtos.CaixaResponse;
import br.com.marciodogao.api.dto.CaixaDtos.MovimentacaoRequest;
import br.com.marciodogao.api.dto.CaixaDtos.MovimentacaoResponse;
import br.com.marciodogao.api.dto.CaixaDtos.TipoMovimentacao;
import br.com.marciodogao.api.exception.NaoEncontradoException;
import br.com.marciodogao.api.exception.RegraNegocioException;
import br.com.marciodogao.api.repository.CaixaRepository;
import br.com.marciodogao.api.repository.ComandaRepository;
import br.com.marciodogao.api.repository.DashboardRepository;
import br.com.marciodogao.api.repository.MovimentacaoCaixaRepository;
import br.com.marciodogao.api.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REGRAS DO CAIXA:
 * - Só pode existir um caixa aberto por vez.
 * - Saldo = abertura + entradas - saídas. Não dá para tirar (sangria) mais do que o saldo.
 * - Não fecha com pedido sem pagamento.
 * - No fechamento: Fechamento_Caixa = saldo final, o campo de relações recebe o resumo manhã/noite
 *   e uma linha é gravada na tabela DashBoard.
 */
@Service
public class CaixaService {

    private final CaixaRepository caixas;
    private final MovimentacaoCaixaRepository movimentacoes;
    private final PagamentoRepository pagamentos;
    private final ComandaRepository comandas;
    private final DashboardRepository dashboards;
    private final RelatorioPagamentos relatorio;

    public CaixaService(CaixaRepository caixas, MovimentacaoCaixaRepository movimentacoes, PagamentoRepository pagamentos,
                        ComandaRepository comandas, DashboardRepository dashboards, RelatorioPagamentos relatorio) {
        this.caixas = caixas;
        this.movimentacoes = movimentacoes;
        this.pagamentos = pagamentos;
        this.comandas = comandas;
        this.dashboards = dashboards;
        this.relatorio = relatorio;
    }

    @Transactional
    public CaixaResponse abrir(AbrirCaixaRequest req) {
        caixas.findFirstByFechamentoCaixaIsNull().ifPresent(c -> {
            throw new RegraNegocioException("Já existe um caixa aberto (nº " + c.getCodCaix() + "). Feche-o antes de abrir outro.");
        });
        return resumo(caixas.save(new Caixa(Dinheiro.duasCasas(req.valorAbertura()))));
    }

    @Transactional(readOnly = true)
    public CaixaResponse atual() {
        return resumo(caixaAbertoObrigatorio());
    }

    @Transactional(readOnly = true)
    public CaixaResponse buscar(Integer codCaix) {
        return resumo(caixas.findById(codCaix).orElseThrow(() -> new NaoEncontradoException("Caixa " + codCaix + " não encontrado.")));
    }

    @Transactional(readOnly = true)
    public List<CaixaHistoricoResponse> historico() {
        return caixas.findAllByOrderByCodCaixDesc().stream()
                .map(c -> new CaixaHistoricoResponse(c.getCodCaix(), c.isAberto(), c.getAberturaCaixa(), c.getFechamentoCaixa(), c.getRelacoesPagamento()))
                .toList();
    }

    @Transactional
    public CaixaResponse movimentar(MovimentacaoRequest req) {
        Caixa caixa = caixaAbertoObrigatorio();
        BigDecimal valor = Dinheiro.duasCasas(req.valor());
        if (req.tipo() == TipoMovimentacao.SAIDA) {
            BigDecimal saldo = saldo(caixa);
            if (valor.compareTo(saldo) > 0) {
                throw new RegraNegocioException("Saída de " + Dinheiro.formatar(valor) + " maior que o saldo do caixa (" + Dinheiro.formatar(saldo) + ").");
            }
            movimentacoes.save(MovimentacaoCaixa.saida(caixa, valor));
        } else {
            movimentacoes.save(MovimentacaoCaixa.entrada(caixa, valor));
        }
        return resumo(caixa);
    }

    @Transactional
    public CaixaResponse fechar() {
        Caixa caixa = caixaAbertoObrigatorio();

        List<String> pendentes = comandas.findAbertas().stream()
                .filter(c -> c.getPedidos().stream().anyMatch(p -> !p.isPago()))
                .map(Comanda::getNumeroComanda)
                .toList();
        if (!pendentes.isEmpty()) {
            throw new RegraNegocioException("Não é possível fechar o caixa: comanda(s) com pedido sem pagamento: "
                    + String.join(", ", pendentes) + ".");
        }

        List<Pagamento> recebidos = pagamentos.findByCaixa(caixa);
        Map<String, BigDecimal> porTurno = relatorio.porTurno(recebidos);
        BigDecimal saidas = movimentacoes.totalSaidas(caixa);
        String relacoes = "Manhã: " + Dinheiro.formatar(porTurno.get("MANHA"))
                + " | Noite: " + Dinheiro.formatar(porTurno.get("NOITE"))
                + " | Saídas: " + Dinheiro.formatar(saidas);

        caixa.fechar(saldo(caixa), relacoes);
        dashboards.save(new Dashboard("Fechamento do caixa nº " + caixa.getCodCaix() + " - " + recebidos.size() + " pagamento(s)",
                relatorio.total(recebidos)));
        return resumo(caixa);
    }

    /** Usado pelo PagamentoService: não se recebe sem caixa aberto. */
    Caixa caixaAbertoObrigatorio() {
        return caixaAberto().orElseThrow(() -> new RegraNegocioException("Nenhum caixa aberto. Abra o caixa antes de continuar."));
    }

    Optional<Caixa> caixaAberto() {
        return caixas.findFirstByFechamentoCaixaIsNull();
    }

    BigDecimal saldo(Caixa caixa) {
        return Dinheiro.duasCasas(caixa.getAberturaCaixa()
                .add(movimentacoes.totalEntradas(caixa))
                .subtract(movimentacoes.totalSaidas(caixa)));
    }

    private CaixaResponse resumo(Caixa caixa) {
        List<Pagamento> recebidos = pagamentos.findByCaixa(caixa);
        List<MovimentacaoResponse> movs = movimentacoes.findByCaixaOrderByCodMov(caixa).stream()
                .map(m -> new MovimentacaoResponse(m.getCodMov(), Dinheiro.duasCasas(m.getValoresEntrada()), Dinheiro.duasCasas(m.getValoresSaida())))
                .collect(Collectors.toList());
        return new CaixaResponse(
                caixa.getCodCaix(),
                caixa.isAberto(),
                Dinheiro.duasCasas(caixa.getAberturaCaixa()),
                Dinheiro.duasCasas(movimentacoes.totalEntradas(caixa)),
                Dinheiro.duasCasas(movimentacoes.totalSaidas(caixa)),
                saldo(caixa),
                caixa.getFechamentoCaixa(),
                caixa.getRelacoesPagamento(),
                relatorio.porTurno(recebidos),
                relatorio.porForma(recebidos),
                movs);
    }
}
