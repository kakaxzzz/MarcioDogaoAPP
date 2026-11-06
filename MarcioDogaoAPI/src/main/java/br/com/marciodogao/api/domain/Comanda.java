package br.com.marciodogao.api.domain;

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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Tabela Comanda: a "ficha" de uma mesa / cliente. Agrupa os pedidos. */
@Entity
@Table(name = "Comanda")
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodComan")
    private Integer codComan;

    @Column(name = "Horario_Pedido", nullable = false)
    private LocalTime horarioPedido;

    @Column(name = "Numero_Comanda", length = 2)
    private String numeroComanda;

    @Column(name = "Mesa", length = 2)
    private String mesa;

    /** Total da comanda (soma de todos os pedidos), recalculado pela API a cada alteração. */
    @Column(name = "Valor_Pedido", precision = 10, scale = 2)
    private BigDecimal valorPedido;

    @Column(name = "Nome_funcionario", length = 50)
    private String nomeFuncionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodLog")
    private Login login;

    @OneToMany(mappedBy = "comanda")
    @OrderBy("codPed")
    private List<Pedido> pedidos = new ArrayList<>();

    protected Comanda() {
    }

    public Comanda(String numeroComanda, String mesa, LocalTime horarioPedido, String nomeFuncionario, Login login) {
        this.numeroComanda = numeroComanda;
        this.mesa = mesa;
        this.horarioPedido = horarioPedido;
        this.nomeFuncionario = nomeFuncionario;
        this.login = login;
        this.valorPedido = BigDecimal.ZERO;
    }

    public StatusComanda status() {
        boolean todosPagos = !pedidos.isEmpty() && pedidos.stream().allMatch(Pedido::isPago);
        return todosPagos ? StatusComanda.FECHADA : StatusComanda.ABERTA;
    }

    public boolean temPagamento() {
        return pedidos.stream().anyMatch(Pedido::isPago);
    }

    public void recalcularValor(Turno turno) {
        this.valorPedido = pedidos.stream().map(p -> p.valorCobrado(turno)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getCodComan() { return codComan; }
    public LocalTime getHorarioPedido() { return horarioPedido; }
    /** CHAR(2) no banco vem com espaço à direita ("5 "), por isso o trim. */
    public String getNumeroComanda() { return numeroComanda == null ? null : numeroComanda.trim(); }
    public String getMesa() { return mesa == null ? null : mesa.trim(); }
    public BigDecimal getValorPedido() { return valorPedido == null ? BigDecimal.ZERO : valorPedido; }
    public String getNomeFuncionario() { return nomeFuncionario; }
    public Login getLogin() { return login; }
    public List<Pedido> getPedidos() { return pedidos; }
}
