package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
}
