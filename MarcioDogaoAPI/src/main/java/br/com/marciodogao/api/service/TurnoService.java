package br.com.marciodogao.api.service;

import br.com.marciodogao.api.config.RestauranteProperties;
import br.com.marciodogao.api.domain.Turno;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * REGRA: o preço depende do turno em que a comanda foi aberta.
 * Entre turno-noturno-inicio e turno-noturno-fim (padrão 18:00 às 05:00) vale o preço "Noturno".
 */
@Service
public class TurnoService {

    private final LocalTime inicioNoite;
    private final LocalTime fimNoite;
    private final Clock relogio;

    public TurnoService(RestauranteProperties props, Clock relogio) {
        this.inicioNoite = LocalTime.parse(props.turnoNoturnoInicio());
        this.fimNoite = LocalTime.parse(props.turnoNoturnoFim());
        this.relogio = relogio;
    }

    public Turno turnoDe(LocalTime horario) {
        boolean noite = inicioNoite.isAfter(fimNoite)
                // atravessa a meia-noite (ex.: 18:00 -> 05:00)
                ? !horario.isBefore(inicioNoite) || horario.isBefore(fimNoite)
                : !horario.isBefore(inicioNoite) && horario.isBefore(fimNoite);
        return noite ? Turno.NOITE : Turno.MANHA;
    }

    public LocalTime agora() {
        return LocalTime.now(relogio).truncatedTo(ChronoUnit.SECONDS);
    }

    public Turno turnoAtual() {
        return turnoDe(agora());
    }
}
