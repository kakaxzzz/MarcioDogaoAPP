package br.com.marciodogao.api.service;

import br.com.marciodogao.api.config.RestauranteProperties;
import br.com.marciodogao.api.domain.Login;
import br.com.marciodogao.api.dto.AuthDtos.LoginRequest;
import br.com.marciodogao.api.dto.AuthDtos.RegistrarRequest;
import br.com.marciodogao.api.dto.AuthDtos.TokenResponse;
import br.com.marciodogao.api.dto.AuthDtos.UsuarioResponse;
import br.com.marciodogao.api.exception.AcessoException;
import br.com.marciodogao.api.exception.NaoEncontradoException;
import br.com.marciodogao.api.exception.RegraNegocioException;
import br.com.marciodogao.api.repository.LoginRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * REGRAS:
 * - O primeiro funcionário pode se cadastrar sem login; depois disso só um funcionário logado cadastra outro.
 * - E-mail único (sem diferenciar maiúsculas). Senha guardada com BCrypt.
 */
@Service
public class AuthService {

    private final LoginRepository logins;
    private final PasswordEncoder senhas;
    private final JwtEncoder jwt;
    private final Clock relogio;
    private final Duration validade;

    public AuthService(LoginRepository logins, PasswordEncoder senhas, JwtEncoder jwt, Clock relogio, RestauranteProperties props) {
        this.logins = logins;
        this.senhas = senhas;
        this.jwt = jwt;
        this.relogio = relogio;
        this.validade = Duration.ofHours(props.jwt().expiracaoHoras());
    }

    @Transactional
    public UsuarioResponse registrar(RegistrarRequest req, Integer codLogAutenticado) {
        if (codLogAutenticado == null && logins.count() > 0) {
            throw AcessoException.negado("Somente um funcionário logado pode cadastrar outro funcionário.");
        }
        String email = req.email().trim().toLowerCase();
        if (logins.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe um funcionário com o e-mail " + email + ".");
        }
        Login novo = logins.save(new Login(logins.maiorCodLog() + 1, email, senhas.encode(req.senha())));
        return new UsuarioResponse(novo.getCodLog(), novo.getEmail());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        Login login = logins.findByEmailIgnoreCase(req.email().trim())
                .filter(l -> l.getSenha() != null && senhas.matches(req.senha(), l.getSenha()))
                .orElseThrow(AcessoException::credenciaisInvalidas);

        Instant agora = relogio.instant();
        Instant expira = agora.plus(validade);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("marciodogao-api")
                .issuedAt(agora)
                .expiresAt(expira)
                .subject(login.getCodLog().toString())
                .claim("email", login.getEmail())
                .build();
        String token = jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expira, login.getCodLog(), login.getEmail());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse usuario(Integer codLog) {
        Login login = logins.findById(codLog).orElseThrow(() -> new NaoEncontradoException("Funcionário não encontrado."));
        return new UsuarioResponse(login.getCodLog(), login.getEmail());
    }
}
