package br.com.marciodogao.api.dto;

import br.com.marciodogao.api.domain.FormaPagamento;
import br.com.marciodogao.api.domain.StatusComanda;
import br.com.marciodogao.api.domain.Turno;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public final class ComandaDtos {

    private ComandaDtos() {
    }

    /** Numero_Comanda e Mesa são CHAR(2) no banco: até 2 caracteres (ex.: "7", "15"). */
    public record AbrirComandaRequest(
            @NotBlank(message = "Informe o número da comanda.") @Size(max = 2, message = "Número da comanda com até 2 caracteres.") String numeroComanda,
            @Size(max = 2, message = "Mesa com até 2 caracteres.") String mesa,
            @Size(max = 50) String nomeFuncionario) {
    }

    public record NovoPedidoRequest(
            @Size(max = 70, message = "Observação com até 70 caracteres.") String observacao,
            @NotEmpty(message = "O pedido precisa de pelo menos um item.") List<@Valid ItemRequest> itens) {
    }

    public record ItemRequest(
            @NotNull(message = "Informe o produto (codCar).") Integer codCar,
            @Min(value = 1, message = "Quantidade mínima é 1.") @Max(value = 50, message = "Quantidade máxima por item é 50.") Integer quantidade,
            @Size(max = 100) String variacao) {
    }

    public record PagamentoRequest(
            @NotNull(message = "Informe a forma de pagamento: DINHEIRO, PIX, CARTAO_CREDITO ou CARTAO_DEBITO.") FormaPagamento formaPagamento) {
    }

    public record ComandaResponse(
            Integer codComan,
            String numeroComanda,
            String mesa,
            LocalTime horarioPedido,
            Turno turno,
            String nomeFuncionario,
            Integer codLog,
            StatusComanda status,
            BigDecimal valorTotal,
            BigDecimal valorPago,
            BigDecimal valorPendente,
            List<PedidoResponse> pedidos) {
    }

    public record PedidoResponse(
            Integer codPed,
            String observacao,
            boolean pago,
            String formaPagamento,
            BigDecimal valor,
            List<ItemResponse> itens) {
    }

    public record ItemResponse(Integer codPedFei, Integer codCar, String produto, String variacao, BigDecimal precoUnitario) {
    }

    public record PagamentoResponse(
            Integer codComan,
            Integer codCaix,
            FormaPagamento formaPagamento,
            BigDecimal valorPago,
            List<Integer> pedidosPagos,
            StatusComanda statusComanda) {
    }
}
