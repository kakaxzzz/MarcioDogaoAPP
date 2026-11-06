package br.com.marciodogao.api.exception;

/** Registro não existe. Retorna HTTP 404. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
