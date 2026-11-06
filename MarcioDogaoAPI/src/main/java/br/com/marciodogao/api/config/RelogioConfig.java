package br.com.marciodogao.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelogioConfig {

    /** Relógio no fuso do restaurante (os testes trocam por um relógio fixo). */
    @Bean
    public Clock relogio(RestauranteProperties props) {
        return Clock.system(ZoneId.of(props.fusoHorario()));
    }
}
