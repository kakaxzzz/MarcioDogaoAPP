package br.com.marciodogao.api.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Tabela Cardapio: produtos vendidos. O preço fica na tabela Preco (manhã / noite). */
@Entity
@Table(name = "Cardapio")
public class Cardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodCar")
    private Integer codCar;

    @Column(name = "Produto", nullable = false, length = 100)
    private String produto;

    @OneToOne(mappedBy = "cardapio", cascade = CascadeType.ALL, orphanRemoval = true)
    private Preco preco;

    protected Cardapio() {
    }

    public Cardapio(String produto) {
        this.produto = produto;
    }

    public Integer getCodCar() { return codCar; }
    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }
    public Preco getPreco() { return preco; }

    public void definirPreco(Preco preco) {
        this.preco = preco;
        preco.setCardapio(this);
    }
}
