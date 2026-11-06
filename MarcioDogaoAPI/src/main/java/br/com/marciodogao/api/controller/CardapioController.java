package br.com.marciodogao.api.controller;

import br.com.marciodogao.api.dto.CardapioDtos.ProdutoRequest;
import br.com.marciodogao.api.dto.CardapioDtos.ProdutoResponse;
import br.com.marciodogao.api.service.CardapioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cardapio")
@Tag(name = "Cardápio")
public class CardapioController {

    private final CardapioService cardapio;

    public CardapioController(CardapioService cardapio) {
        this.cardapio = cardapio;
    }

    @GetMapping
    @Operation(summary = "Listar produtos com o preço do turno atual (público)")
    public List<ProdutoResponse> listar() {
        return cardapio.listar();
    }

    @GetMapping("/{codCar}")
    public ProdutoResponse buscar(@PathVariable Integer codCar) {
        return cardapio.buscar(codCar);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar produto com preço da manhã e da noite")
    public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest req) {
        return cardapio.criar(req);
    }

    @PutMapping("/{codCar}")
    public ProdutoResponse atualizar(@PathVariable Integer codCar, @Valid @RequestBody ProdutoRequest req) {
        return cardapio.atualizar(codCar, req);
    }

    @DeleteMapping("/{codCar}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir produto (só se nunca foi vendido)")
    public void excluir(@PathVariable Integer codCar) {
        cardapio.excluir(codCar);
    }
}
