package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Caixa;
import br.com.marciodogao.api.domain.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    @Query("select pg from Pagamento pg join fetch pg.pedido p join fetch p.comanda where pg.caixa = :caixa")
    List<Pagamento> findByCaixa(Caixa caixa);
}
