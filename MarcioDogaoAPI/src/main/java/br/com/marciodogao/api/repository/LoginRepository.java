package br.com.marciodogao.api.repository;

import br.com.marciodogao.api.domain.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Integer> {

    Optional<Login> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select coalesce(max(l.codLog), 0) from Login l")
    int maiorCodLog();
}
