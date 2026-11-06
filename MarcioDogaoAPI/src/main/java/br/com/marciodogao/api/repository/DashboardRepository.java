package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardRepository extends JpaRepository<Dashboard, Integer> {

    List<Dashboard> findAllByOrderByCodDashDesc();
}
