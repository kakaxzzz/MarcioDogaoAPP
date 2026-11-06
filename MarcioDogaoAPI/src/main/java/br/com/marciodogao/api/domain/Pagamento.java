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

/** Tabela Pagamento: pagamento de um pedido, registrado no caixa que estava aberto. */
@Entity
@Table(name = "Pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodPag")
    private Integer codPag;

    @Column(name = "Forma_Pagamento", nullable = false, length = 100)
    private String formaPagamento;

    @Column(name = "Mesa_Pagamento", length = 10)
    private String mesaPagamento;

    @Column(name = "Valor_Pago", precision = 10, scale = 2)
    private BigDecimal valorPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodPed")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodCaix")
    private Caixa caixa;

    protected Pagamento() {
    }

    public Pagamento(Pedido pedido, Caixa caixa, FormaPagamento forma, BigDecimal valorPago, String mesaPagamento) {
        this.pedido = pedido;
        this.caixa = caixa;
        this.formaPagamento = forma.name();
        this.valorPago = valorPago;
        this.mesaPagamento = mesaPagamento;
        pedido.getPagamentos().add(this);
    }

    public Integer getCodPag() { return codPag; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getMesaPagamento() { return mesaPagamento; }
    public BigDecimal getValorPago() { return valorPago == null ? BigDecimal.ZERO : valorPago; }
    public Pedido getPedido() { return pedido; }
    public Caixa getCaixa() { return caixa; }
}
