package br.com.marciodogao.api.service;

import br.com.marciodogao.api.config.RestauranteProperties;
import br.com.marciodogao.api.domain.Turno;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TurnoServiceTest {

    private final TurnoService turnos = new TurnoService(
            new RestauranteProperties("America/Sao_Paulo", "18:00", "05:00", null, null), Clock.systemUTC());

    @Test
    void manhaDas5AteAs18() {
        assertThat(turnos.turnoDe(LocalTime.of(5, 0))).isEqualTo(Turno.MANHA);
        assertThat(turnos.turnoDe(LocalTime.of(12, 30))).isEqualTo(Turno.MANHA);
        assertThat(turnos.turnoDe(LocalTime.of(17, 59, 59))).isEqualTo(Turno.MANHA);
    }

    @Test
    void noiteAtravessaAMeiaNoite() {
        assertThat(turnos.turnoDe(LocalTime.of(18, 0))).isEqualTo(Turno.NOITE);
        assertThat(turnos.turnoDe(LocalTime.of(23, 59))).isEqualTo(Turno.NOITE);
        assertThat(turnos.turnoDe(LocalTime.of(0, 0))).isEqualTo(Turno.NOITE);
        assertThat(turnos.turnoDe(LocalTime.of(4, 59))).isEqualTo(Turno.NOITE);
    }
}
