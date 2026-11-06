package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Cardapio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardapioRepository extends JpaRepository<Cardapio, Integer> {

    List<Cardapio> findAllByOrderByProdutoAsc();

    boolean existsByProdutoIgnoreCase(String produto);

    boolean existsByProdutoIgnoreCaseAndCodCarNot(String produto, Integer codCar);
}
