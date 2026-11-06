package br.com.marciodogao.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabela Caixa. Abertura_Caixa = troco inicial. Fechamento_Caixa fica NULL enquanto o caixa
 * está aberto e recebe o saldo final quando fecha.
 */
@Entity
@Table(name = "Caixa")
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodCaix")
    private Integer codCaix;

    @Column(name = "Abertura_Caixa", nullable = false, precision = 10, scale = 2)
    private BigDecimal aberturaCaixa;

    @Column(name = "Fechamento_Caixa", precision = 10, scale = 2)
    private BigDecimal fechamentoCaixa;

    /** Resumo do fechamento: total da manhã, da noite e saídas. */
    @Column(name = "`Relações_de_Pagamento_Final_da_Manha_e_Noite`", length = 100)
    private String relacoesPagamento;

    protected Caixa() {
    }

    public Caixa(BigDecimal aberturaCaixa) {
        this.aberturaCaixa = aberturaCaixa;
    }

    public boolean isAberto() {
        return fechamentoCaixa == null;
    }

    public void fechar(BigDecimal saldoFinal, String relacoes) {
        this.fechamentoCaixa = saldoFinal;
        this.relacoesPagamento = relacoes.length() > 100 ? relacoes.substring(0, 100) : relacoes;
    }

    public Integer getCodCaix() { return codCaix; }
    public BigDecimal getAberturaCaixa() { return aberturaCaixa; }
    public BigDecimal getFechamentoCaixa() { return fechamentoCaixa; }
    public String getRelacoesPagamento() { return relacoesPagamento; }
}
