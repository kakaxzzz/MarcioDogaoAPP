package br.com.marciodogao.api.controller;

import br.com.marciodogao.api.dto.CaixaDtos.AbrirCaixaRequest;
import br.com.marciodogao.api.dto.CaixaDtos.CaixaHistoricoResponse;
import br.com.marciodogao.api.dto.CaixaDtos.CaixaResponse;
import br.com.marciodogao.api.dto.CaixaDtos.MovimentacaoRequest;
import br.com.marciodogao.api.service.CaixaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/caixa")
@Tag(name = "Caixa")
public class CaixaController {

    private final CaixaService caixa;

    public CaixaController(CaixaService caixa) {
        this.caixa = caixa;
    }

    @PostMapping("/abrir")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abrir o caixa com o troco inicial")
    public CaixaResponse abrir(@Valid @RequestBody AbrirCaixaRequest req) {
        return caixa.abrir(req);
    }

    @GetMapping("/atual")
    @Operation(summary = "Resumo do caixa aberto: saldo, entradas, saídas, manhã/noite e formas de pagamento")
    public CaixaResponse atual() {
        return caixa.atual();
    }

    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Lançar suprimento (ENTRADA) ou sangria/despesa (SAIDA)")
    public CaixaResponse movimentar(@Valid @RequestBody MovimentacaoRequest req) {
        return caixa.movimentar(req);
    }

    @PostMapping("/fechar")
    @Operation(summary = "Fechar o caixa (exige todas as comandas pagas)")
    public CaixaResponse fechar() {
        return caixa.fechar();
    }

    @GetMapping
    @Operation(summary = "Histórico de caixas")
    public List<CaixaHistoricoResponse> historico() {
        return caixa.historico();
    }

    @GetMapping("/{codCaix}")
    public CaixaResponse buscar(@PathVariable Integer codCaix) {
        return caixa.buscar(codCaix);
    }
}
