package br.com.marciodogao.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Tabela Movimentacao_Caixa: toda entrada (pagamentos, suprimento) e saída (sangria, despesas). */
@Entity
@Table(name = "Movimentacao_Caixa")
public class MovimentacaoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodMov")
    private Integer codMov;

    @Column(name = "Valores_Entrada", nullable = false, precision = 10, scale = 2)
    private BigDecimal valoresEntrada;

    @Column(name = "Valores_Saida", precision = 10, scale = 2)
    private BigDecimal valoresSaida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodCaix")
    private Caixa caixa;

    protected MovimentacaoCaixa() {
    }

    private MovimentacaoCaixa(Caixa caixa, BigDecimal entrada, BigDecimal saida) {
        this.caixa = caixa;
        this.valoresEntrada = entrada;
        this.valoresSaida = saida;
    }

    public static MovimentacaoCaixa entrada(Caixa caixa, BigDecimal valor) {
        return new MovimentacaoCaixa(caixa, valor, BigDecimal.ZERO);
    }

    public static MovimentacaoCaixa saida(Caixa caixa, BigDecimal valor) {
        return new MovimentacaoCaixa(caixa, BigDecimal.ZERO, valor);
    }

    public Integer getCodMov() { return codMov; }
    public BigDecimal getValoresEntrada() { return valoresEntrada; }
    public BigDecimal getValoresSaida() { return valoresSaida == null ? BigDecimal.ZERO : valoresSaida; }
    public Caixa getCaixa() { return caixa; }
}
