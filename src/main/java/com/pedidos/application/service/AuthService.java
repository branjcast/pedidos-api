package com.pedidos.application.service;

import com.pedidos.application.port.in.AuthUseCase;
import com.pedidos.application.port.in.TokenAcceso;
import com.pedidos.application.port.out.PasswordHasherPort;
import com.pedidos.application.port.out.TokenGeneratorPort;
import com.pedidos.application.port.out.UsuarioRepositoryPort;
import com.pedidos.domain.exception.CredencialesInvalidasException;
import com.pedidos.domain.model.Usuario;
import java.util.Locale;

public class AuthService implements AuthUseCase {

    private static final String MENSAJE = "Correo o contraseña incorrectos";

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;
    private final TokenGeneratorPort tokens;

    public AuthService(UsuarioRepositoryPort usuarios, PasswordHasherPort hasher, TokenGeneratorPort tokens) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Override
    public TokenAcceso login(String correo, String password) {
        if (correo == null || password == null) {
            throw new CredencialesInvalidasException(MENSAJE);
        }
        Usuario usuario = usuarios.buscarPorCorreo(correo.trim().toLowerCase(Locale.ROOT))
                .filter(Usuario::activo)
                .filter(u -> hasher.coincide(password, u.passwordHash()))
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE));
        return tokens.generar(usuario);
    }
}
