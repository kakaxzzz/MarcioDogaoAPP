package br.com.marciodogao.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegistrarRequest(
            @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 100) String email,
            @NotBlank(message = "Informe a senha.") @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.") String senha) {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail.") String email,
            @NotBlank(message = "Informe a senha.") String senha) {
    }

    public record TokenResponse(String token, String tipo, Instant expiraEm, Integer codLog, String email) {
    }

    public record UsuarioResponse(Integer codLog, String email) {
    }
}
