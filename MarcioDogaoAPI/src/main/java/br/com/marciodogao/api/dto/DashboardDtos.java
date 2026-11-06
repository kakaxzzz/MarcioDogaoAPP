package br.com.marciodogao.api.dto;

import br.com.marciodogao.api.repository.ProdutoVendido;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    /** Painel em tempo real do caixa aberto. */
    public record DashboardResponse(
            boolean caixaAberto,
            Integer codCaix,
            BigDecimal faturamento,
            BigDecimal saldoCaixa,
            int comandasAbertas,
            int comandasAtendidas,
            BigDecimal ticketMedio,
            Map<String, BigDecimal> faturamentoPorTurno,
            Map<String, BigDecimal> faturamentoPorFormaPagamento,
            List<ProdutoVendido> maisVendidos) {
    }

    public record DashboardHistoricoResponse(Integer codDash, String descricao, BigDecimal faturamento) {
    }
}
