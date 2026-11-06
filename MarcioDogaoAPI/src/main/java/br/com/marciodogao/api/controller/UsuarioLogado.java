package br.com.marciodogao.api.controller;

import org.springframework.security.oauth2.jwt.Jwt;

final class UsuarioLogado {

    private UsuarioLogado() {
    }

    /** O "sub" do token é o CodLog do funcionário. */
    static Integer codLog(Jwt jwt) {
        return Integer.valueOf(jwt.getSubject());
    }
}
