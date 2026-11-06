package br.com.marciodogao.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class CaixaDtos {

    private CaixaDtos() {
    }

    public enum TipoMovimentacao {
        /** Suprimento: dinheiro colocado no caixa. */
        ENTRADA,
        /** Sangria / despesa: dinheiro retirado do caixa. */
        SAIDA
    }

    public record AbrirCaixaRequest(
            @NotNull(message = "Informe o valor de abertura (troco inicial).")
            @DecimalMin(value = "0.00", message = "O valor de abertura não pode ser negativo.")
            @Digits(integer = 8, fraction = 2) BigDecimal valorAbertura) {
    }

    public record MovimentacaoRequest(
            @NotNull(message = "Informe o tipo: ENTRADA ou SAIDA.") TipoMovimentacao tipo,
            @NotNull(message = "Informe o valor.") @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
            @Digits(integer = 8, fraction = 2) BigDecimal valor) {
    }

    public record MovimentacaoResponse(Integer codMov, BigDecimal entrada, BigDecimal saida) {
    }

    public record CaixaResponse(
            Integer codCaix,
            boolean aberto,
            BigDecimal valorAbertura,
            BigDecimal totalEntradas,
            BigDecimal totalSaidas,
            BigDecimal saldo,
            BigDecimal valorFechamento,
            String relacoesPagamento,
            Map<String, BigDecimal> recebidoPorTurno,
            Map<String, BigDecimal> recebidoPorFormaPagamento,
            List<MovimentacaoResponse> movimentacoes) {
    }

    public record CaixaHistoricoResponse(
            Integer codCaix,
            boolean aberto,
            BigDecimal valorAbertura,
            BigDecimal valorFechamento,
            String relacoesPagamento) {
    }
}
