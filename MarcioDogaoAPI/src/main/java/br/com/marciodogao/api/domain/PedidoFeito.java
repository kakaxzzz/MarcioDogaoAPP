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

/** Tabela Pedido_Feito: cada linha é UMA unidade de um produto do cardápio dentro do pedido. */
@Entity
@Table(name = "Pedido_Feito")
public class PedidoFeito {

    public static final String VARIACAO_PADRAO = "Padrão";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodPed_Fei")
    private Integer codPedFei;

    /** Ex.: "sem cebola", "molho extra". Coluna com acento, por isso entre crases. */
    @Column(name = "`Variação_pedido`", nullable = false, length = 100)
    private String variacaoPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodCar")
    private Cardapio cardapio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodPed")
    private Pedido pedido;

    protected PedidoFeito() {
    }

    PedidoFeito(Pedido pedido, Cardapio cardapio, String variacao) {
        this.pedido = pedido;
        this.cardapio = cardapio;
        this.variacaoPedido = variacao == null || variacao.isBlank() ? VARIACAO_PADRAO : variacao.trim();
    }

    public BigDecimal precoUnitario(Turno turno) {
        return cardapio.getPreco().valorNoTurno(turno);
    }

    public Integer getCodPedFei() { return codPedFei; }
    public String getVariacaoPedido() { return variacaoPedido; }
    public Cardapio getCardapio() { return cardapio; }
    public Pedido getPedido() { return pedido; }
}
