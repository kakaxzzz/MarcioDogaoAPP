package br.com.marciodogao.api.dto;

import br.com.marciodogao.api.domain.Turno;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class CardapioDtos {

    private CardapioDtos() {
    }

    /** precoNoite é opcional: sem ele, à noite vale o preço da manhã. */
    public record ProdutoRequest(
            @NotBlank(message = "Informe o nome do produto.") @Size(max = 100) String produto,
            @NotNull(message = "Informe o preço da manhã.") @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
            @Digits(integer = 8, fraction = 2, message = "Use no máximo 2 casas decimais.") BigDecimal precoManha,
            @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
            @Digits(integer = 8, fraction = 2, message = "Use no máximo 2 casas decimais.") BigDecimal precoNoite) {
    }

    public record ProdutoResponse(
            Integer codCar,
            String produto,
            BigDecimal precoManha,
            BigDecimal precoNoite,
            Turno turnoAtual,
            BigDecimal precoAtual) {
    }
}
