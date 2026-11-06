package br.com.marciodogao.api.exception;

import org.springframework.http.HttpStatus;

/** Login inválido (401) ou ação não permitida (403). */
public class AcessoException extends RuntimeException {

    private final HttpStatus status;

    private AcessoException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public static AcessoException credenciaisInvalidas() {
        return new AcessoException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }

    public static AcessoException negado(String mensagem) {
        return new AcessoException(HttpStatus.FORBIDDEN, mensagem);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
