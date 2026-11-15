package br.com.marciodogao.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tabela Mesa: as mesas do salao. Numero e CHAR(2), igual a Comanda.Mesa. */
@Entity
@Table(name = "Mesa")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodMesa")
    private Integer codMesa;

    @Column(name = "Numero", nullable = false, length = 2, unique = true)
    private String numero;

    protected Mesa() {
    }

    public Mesa(String numero) {
        this.numero = numero;
    }

    public Integer getCodMesa() { return codMesa; }
    public String getNumero() { return numero == null ? null : numero.trim(); }
}
