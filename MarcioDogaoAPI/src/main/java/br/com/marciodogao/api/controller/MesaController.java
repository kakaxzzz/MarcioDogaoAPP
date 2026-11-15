package br.com.marciodogao.api.controller;

import br.com.marciodogao.api.config.RestauranteProperties;
import br.com.marciodogao.api.domain.Turno;
import br.com.marciodogao.api.service.MesaService;
import br.com.marciodogao.api.service.MesaService.MesaResponse;
import br.com.marciodogao.api.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Mesas")
public class MesaController {

    public record NovaMesaRequest(String numero) {
    }

    /** demoEmail / demoSenha só vêm preenchidos no perfil "demo" (banco em memória com dados de exemplo). */
    public record InfoResponse(boolean demo, Turno turnoAtual, String inicioNoite, String demoEmail, String demoSenha) {
    }

    private final MesaService mesas;
    private final TurnoService turnos;
    private final RestauranteProperties props;
    private final Environment ambiente;

    public MesaController(MesaService mesas, TurnoService turnos, RestauranteProperties props, Environment ambiente) {
        this.mesas = mesas;
        this.turnos = turnos;
        this.props = props;
        this.ambiente = ambiente;
    }

    @GetMapping("/mesas")
    @Operation(summary = "Mesas do salão, com a comanda aberta de cada uma (se houver)")
    public List<MesaResponse> listar() {
        return mesas.listar();
    }

    @PostMapping("/mesas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar mesa")
    public MesaResponse criar(@RequestBody NovaMesaRequest req) {
        return mesas.criar(req.numero());
    }

    @GetMapping("/info")
    @Operation(summary = "Turno atual e modo de demonstração (público, usado pela tela de login)")
    public InfoResponse info() {
        boolean demo = ambiente.acceptsProfiles(Profiles.of("demo"));
        return new InfoResponse(demo, turnos.turnoAtual(), props.turnoNoturnoInicio(),
                demo ? ambiente.getProperty("demo.email") : null,
                demo ? ambiente.getProperty("demo.senha") : null);
    }
}
