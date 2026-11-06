package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Integer> {

    Optional<Caixa> findFirstByFechamentoCaixaIsNull();

    List<Caixa> findAllByOrderByCodCaixDesc();
}
