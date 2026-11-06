package br.com.marciodogao.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tabela Login: funcionários que usam o app. CodLog não é IDENTITY, a API gera o próximo número. */
@Entity
@Table(name = "Login")
public class Login {

    @Id
    @Column(name = "CodLog")
    private Integer codLog;

    @Column(name = "Email", nullable = false, length = 100)
    private String email;

    /** Hash BCrypt, nunca a senha em texto. */
    @Column(name = "Senha", length = 100)
    private String senha;

    protected Login() {
    }

    public Login(Integer codLog, String email, String senhaHash) {
        this.codLog = codLog;
        this.email = email;
        this.senha = senhaHash;
    }

    public Integer getCodLog() { return codLog; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
