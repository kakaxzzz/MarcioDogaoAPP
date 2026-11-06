package br.com.marciodogao.api.controller;

import br.com.marciodogao.api.domain.StatusComanda;
import br.com.marciodogao.api.dto.ComandaDtos.AbrirComandaRequest;
import br.com.marciodogao.api.dto.ComandaDtos.ComandaResponse;
import br.com.marciodogao.api.dto.ComandaDtos.NovoPedidoRequest;
import br.com.marciodogao.api.dto.ComandaDtos.PagamentoRequest;
import br.com.marciodogao.api.dto.ComandaDtos.PagamentoResponse;
import br.com.marciodogao.api.service.ComandaService;
import br.com.marciodogao.api.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comandas")
@Tag(name = "Comandas, pedidos e pagamentos")
public class ComandaController {

    private final ComandaService comandas;
    private final PagamentoService pagamentos;

    public ComandaController(ComandaService comandas, PagamentoService pagamentos) {
        this.comandas = comandas;
        this.pagamentos = pagamentos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abrir comanda (o horário de agora define o turno / preço)")
    public ComandaResponse abrir(@Valid @RequestBody AbrirComandaRequest req, @AuthenticationPrincipal Jwt jwt) {
        return comandas.abrir(req, UsuarioLogado.codLog(jwt));
    }

    @GetMapping
    @Operation(summary = "Listar comandas ABERTA (padrão) ou FECHADA")
    public List<ComandaResponse> listar(@RequestParam(defaultValue = "ABERTA") StatusComanda status) {
        return comandas.listar(status);
    }

    @GetMapping("/{codComan}")
    public ComandaResponse buscar(@PathVariable Integer codComan) {
        return comandas.buscar(codComan);
    }

    @DeleteMapping("/{codComan}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancelar comanda (só se nada foi pago)")
    public void cancelar(@PathVariable Integer codComan) {
        comandas.cancelar(codComan);
    }

    @PostMapping("/{codComan}/pedidos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Lançar pedido com itens do cardápio")
    public ComandaResponse adicionarPedido(@PathVariable Integer codComan, @Valid @RequestBody NovoPedidoRequest req) {
        return comandas.adicionarPedido(codComan, req);
    }

    @DeleteMapping("/{codComan}/pedidos/{codPed}")
    @Operation(summary = "Remover pedido ainda não pago")
    public ComandaResponse removerPedido(@PathVariable Integer codComan, @PathVariable Integer codPed) {
        return comandas.removerPedido(codComan, codPed);
    }

    @DeleteMapping("/{codComan}/pedidos/{codPed}/itens/{codPedFei}")
    @Operation(summary = "Remover um item de um pedido ainda não pago")
    public ComandaResponse removerItem(@PathVariable Integer codComan, @PathVariable Integer codPed, @PathVariable Integer codPedFei) {
        return comandas.removerItem(codComan, codPed, codPedFei);
    }

    @PostMapping("/{codComan}/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pagar tudo o que falta na comanda")
    public PagamentoResponse pagarComanda(@PathVariable Integer codComan, @Valid @RequestBody PagamentoRequest req) {
        return pagamentos.pagarComanda(codComan, req.formaPagamento());
    }

    @PostMapping("/{codComan}/pedidos/{codPed}/pagamento")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pagar só um pedido (para dividir a conta em formas de pagamento diferentes)")
    public PagamentoResponse pagarPedido(@PathVariable Integer codComan, @PathVariable Integer codPed, @Valid @RequestBody PagamentoRequest req) {
        return pagamentos.pagarPedido(codComan, codPed, req.formaPagamento());
    }
}
