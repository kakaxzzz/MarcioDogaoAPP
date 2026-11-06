package br.com.marciodogao.api.controller;

import br.com.marciodogao.api.dto.AuthDtos.LoginRequest;
import br.com.marciodogao.api.dto.AuthDtos.RegistrarRequest;
import br.com.marciodogao.api.dto.AuthDtos.TokenResponse;
import br.com.marciodogao.api.dto.AuthDtos.UsuarioResponse;
import br.com.marciodogao.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Login")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    @Operation(summary = "Entrar e receber o token JWT (envie depois em Authorization: Bearer <token>)")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar funcionário (o primeiro é livre; os demais exigem estar logado)")
    public UsuarioResponse registrar(@Valid @RequestBody RegistrarRequest req, @AuthenticationPrincipal Jwt jwt) {
        return auth.registrar(req, jwt == null ? null : UsuarioLogado.codLog(jwt));
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do funcionário logado")
    public UsuarioResponse eu(@AuthenticationPrincipal Jwt jwt) {
        return auth.usuario(UsuarioLogado.codLog(jwt));
    }
}
