package br.com.marciodogao.api.domain;

/**
 * Não existe coluna de status no banco: a comanda está FECHADA quando tem pedidos
 * e todos eles já foram pagos; caso contrário está ABERTA.
 */
public enum StatusComanda {
    ABERTA,
    FECHADA
}
