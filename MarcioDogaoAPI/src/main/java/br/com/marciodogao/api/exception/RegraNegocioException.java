package br.com.marciodogao.api.exception;

/** Regra do restaurante violada (ex.: pagar sem caixa aberto). Retorna HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
