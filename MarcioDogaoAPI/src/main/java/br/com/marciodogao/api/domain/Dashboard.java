package br.com.marciodogao.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Tabela DashBoard: histórico de relatórios. A API grava uma linha a cada fechamento de caixa. */
@Entity
@Table(name = "DashBoard")
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodDash")
    private Integer codDash;

    @Column(name = "Consulta_Informa", nullable = false, length = 100)
    private String consultaInforma;

    @Column(name = "Relatorio_Caixa", precision = 10, scale = 2)
    private BigDecimal relatorioCaixa;

    protected Dashboard() {
    }

    public Dashboard(String consultaInforma, BigDecimal relatorioCaixa) {
        this.consultaInforma = consultaInforma.length() > 100 ? consultaInforma.substring(0, 100) : consultaInforma;
        this.relatorioCaixa = relatorioCaixa;
    }

    public Integer getCodDash() { return codDash; }
    public String getConsultaInforma() { return consultaInforma; }
    public BigDecimal getRelatorioCaixa() { return relatorioCaixa; }
}
