package br.com.marciodogao.api.domain;

public enum Turno {
    MANHA("Manhã"),
    NOITE("Noite");

    private final String descricao;

    Turno(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
