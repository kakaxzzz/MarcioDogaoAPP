package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ComandaRepository extends JpaRepository<Comanda, Integer> {

    /** Aberta = sem pedidos, ou com pelo menos um pedido sem pagamento. */
    @Query("""
            select c from Comanda c
            where not exists (select p from Pedido p where p.comanda = c)
               or exists (select p from Pedido p where p.comanda = c
                          and not exists (select pg from Pagamento pg where pg.pedido = p))
            order by c.codComan
            """)
    List<Comanda> findAbertas();

    /** Fechada = tem pedidos e todos foram pagos. */
    @Query("""
            select c from Comanda c
            where exists (select p from Pedido p where p.comanda = c)
              and not exists (select p from Pedido p where p.comanda = c
                              and not exists (select pg from Pagamento pg where pg.pedido = p))
            order by c.codComan desc
            """)
    List<Comanda> findFechadas();

    List<Comanda> findByNumeroComanda(String numeroComanda);
}
