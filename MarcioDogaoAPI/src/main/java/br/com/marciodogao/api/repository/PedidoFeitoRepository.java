package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Caixa;
import br.com.marciodogao.api.domain.Cardapio;
import br.com.marciodogao.api.domain.PedidoFeito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoFeitoRepository extends JpaRepository<PedidoFeito, Integer> {

    boolean existsByCardapio(Cardapio cardapio);

    /** Produtos mais vendidos (itens de pedidos pagos no caixa informado). */
    @Query("""
            select new br.com.marciodogao.api.repository.ProdutoVendido(pf.cardapio.codCar, pf.cardapio.produto, count(pf))
            from PedidoFeito pf
            where exists (select pg from Pagamento pg where pg.pedido = pf.pedido and pg.caixa = :caixa)
            group by pf.cardapio.codCar, pf.cardapio.produto
            order by count(pf) desc
            """)
    List<ProdutoVendido> maisVendidos(Caixa caixa);
}
