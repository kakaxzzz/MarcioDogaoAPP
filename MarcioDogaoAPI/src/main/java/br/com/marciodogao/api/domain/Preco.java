package br.com.marciodogao.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Tabela Preco: preço de um produto no turno da manhã e no turno da noite. */
@Entity
@Table(name = "Preco")
public class Preco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodPer")
    private Integer codPer;

    @Column(name = "Manha", nullable = false, precision = 10, scale = 2)
    private BigDecimal manha;

    @Column(name = "Noturno", precision = 10, scale = 2)
    private BigDecimal noturno;

    @OneToOne
    @JoinColumn(name = "CodCar")
    private Cardapio cardapio;

    protected Preco() {
    }

    public Preco(BigDecimal manha, BigDecimal noturno) {
        this.manha = manha;
        this.noturno = noturno;
    }

    /** Preço do turno. Se não houver preço noturno, vale o da manhã. */
    public BigDecimal valorNoTurno(Turno turno) {
        return turno == Turno.NOITE && noturno != null ? noturno : manha;
    }

    public Integer getCodPer() { return codPer; }
    public BigDecimal getManha() { return manha; }
    public void setManha(BigDecimal manha) { this.manha = manha; }
    public BigDecimal getNoturno() { return noturno; }
    public void setNoturno(BigDecimal noturno) { this.noturno = noturno; }
    public Cardapio getCardapio() { return cardapio; }
    void setCardapio(Cardapio cardapio) { this.cardapio = cardapio; }
}
