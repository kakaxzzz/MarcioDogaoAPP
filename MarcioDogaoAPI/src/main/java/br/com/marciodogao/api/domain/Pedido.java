package br.com.marciodogao.api.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Tabela Pedido: uma rodada de pedido dentro da comanda. Os itens ficam em Pedido_Feito. */
@Entity
@Table(name = "Pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodPed")
    private Integer codPed;

    /** Observação geral do pedido (ex.: "para viagem", "trazer junto"). */
    @Nationalized
    @Column(name = "o_que_vai_ser_pedido", length = 70)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodComan")
    private Comanda comanda;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("codPedFei")
    private List<PedidoFeito> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido")
    private List<Pagamento> pagamentos = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Comanda comanda, String observacao) {
        this.comanda = comanda;
        this.observacao = observacao;
        comanda.getPedidos().add(this);
    }

    public void adicionarItem(Cardapio produto, String variacao) {
        itens.add(new PedidoFeito(this, produto, variacao));
    }

    public BigDecimal valor(Turno turno) {
        return itens.stream().map(i -> i.precoUnitario(turno)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Pedido pago vale o que foi cobrado (Valor_Pago), mesmo que o preço do cardápio mude depois. */
    public BigDecimal valorCobrado(Turno turno) {
        return isPago()
                ? pagamentos.stream().map(Pagamento::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add)
                : valor(turno);
    }

    public boolean isPago() {
        return !pagamentos.isEmpty();
    }

    public Integer getCodPed() { return codPed; }
    public String getObservacao() { return observacao; }
    public Comanda getComanda() { return comanda; }
    public List<PedidoFeito> getItens() { return itens; }
    public List<Pagamento> getPagamentos() { return pagamentos; }
}
