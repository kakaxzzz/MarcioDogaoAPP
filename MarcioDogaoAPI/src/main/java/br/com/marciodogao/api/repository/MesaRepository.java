package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MesaRepository extends JpaRepository<Mesa, Integer> {

    List<Mesa> findAllByOrderByNumeroAsc();

    boolean existsByNumero(String numero);
}
