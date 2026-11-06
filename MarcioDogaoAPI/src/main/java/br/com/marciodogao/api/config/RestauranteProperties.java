package br.com.marciodogao.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Configurações do bloco "restaurante" do application.yml. */
@ConfigurationProperties("restaurante")
public record RestauranteProperties(
        String fusoHorario,
        String turnoNoturnoInicio,
        String turnoNoturnoFim,
        Jwt jwt,
        Cors cors) {

    public record Jwt(String secret, int expiracaoHoras) {
    }

    public record Cors(List<String> origensPermitidas) {
    }
}
