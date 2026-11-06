package br.com.marciodogao.api.service;

import br.com.marciodogao.api.domain.Caixa;
import br.com.marciodogao.api.domain.Pagamento;
import br.com.marciodogao.api.dto.DashboardDtos.DashboardHistoricoResponse;
import br.com.marciodogao.api.dto.DashboardDtos.DashboardResponse;
import br.com.marciodogao.api.repository.ComandaRepository;
import br.com.marciodogao.api.repository.DashboardRepository;
import br.com.marciodogao.api.repository.PagamentoRepository;
import br.com.marciodogao.api.repository.PedidoFeitoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Painel do dono: faturamento do caixa aberto, ticket médio, mais vendidos e histórico dos fechamentos. */
@Service
public class DashboardService {

    private static final int TOP_PRODUTOS = 10;

    private final CaixaService caixas;
    private final PagamentoRepository pagamentos;
    private final ComandaRepository comandas;
    private final PedidoFeitoRepository itens;
    private final DashboardRepository dashboards;
    private final RelatorioPagamentos relatorio;

    public DashboardService(CaixaService caixas, PagamentoRepository pagamentos, ComandaRepository comandas,
                            PedidoFeitoRepository itens, DashboardRepository dashboards, RelatorioPagamentos relatorio) {
        this.caixas = caixas;
        this.pagamentos = pagamentos;
        this.comandas = comandas;
        this.itens = itens;
        this.dashboards = dashboards;
        this.relatorio = relatorio;
    }

    @Transactional(readOnly = true)
    public DashboardResponse painel() {
        int abertas = comandas.findAbertas().size();
        Optional<Caixa> aberto = caixas.caixaAberto();
        if (aberto.isEmpty()) {
            return new DashboardResponse(false, null, Dinheiro.duasCasas(null), Dinheiro.duasCasas(null), abertas, 0,
                    Dinheiro.duasCasas(null), relatorio.porTurno(List.of()), relatorio.porForma(List.of()), List.of());
        }
        Caixa caixa = aberto.get();
        List<Pagamento> recebidos = pagamentos.findByCaixa(caixa);
        BigDecimal faturamento = relatorio.total(recebidos);
        int atendidas = (int) recebidos.stream().map(p -> p.getPedido().getComanda().getCodComan()).distinct().count();
        BigDecimal ticket = atendidas == 0 ? Dinheiro.duasCasas(null)
                : faturamento.divide(BigDecimal.valueOf(atendidas), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> porTurno = relatorio.porTurno(recebidos);
        Map<String, BigDecimal> porForma = relatorio.porForma(recebidos);
        var top = itens.maisVendidos(caixa).stream().limit(TOP_PRODUTOS).toList();

        return new DashboardResponse(true, caixa.getCodCaix(), faturamento, caixas.saldo(caixa), abertas, atendidas,
                ticket, porTurno, porForma, top);
    }

    @Transactional(readOnly = true)
    public List<DashboardHistoricoResponse> historico() {
        return dashboards.findAllByOrderByCodDashDesc().stream()
                .map(d -> new DashboardHistoricoResponse(d.getCodDash(), d.getConsultaInforma(), d.getRelatorioCaixa()))
                .toList();
    }
}
