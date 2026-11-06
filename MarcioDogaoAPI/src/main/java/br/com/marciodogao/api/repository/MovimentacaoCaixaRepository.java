package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Caixa;
import br.com.marciodogao.api.domain.MovimentacaoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface MovimentacaoCaixaRepository extends JpaRepository<MovimentacaoCaixa, Integer> {

    List<MovimentacaoCaixa> findByCaixaOrderByCodMov(Caixa caixa);

    @Query("select coalesce(sum(m.valoresEntrada), 0) from MovimentacaoCaixa m where m.caixa = :caixa")
    BigDecimal totalEntradas(Caixa caixa);

    @Query("select coalesce(sum(m.valoresSaida), 0) from MovimentacaoCaixa m where m.caixa = :caixa")
    BigDecimal totalSaidas(Caixa caixa);
}
