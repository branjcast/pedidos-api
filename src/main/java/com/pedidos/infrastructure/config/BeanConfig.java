package com.pedidos.infrastructure.config;

import com.pedidos.application.port.in.AuthUseCase;
import com.pedidos.application.port.in.PedidoUseCase;
import com.pedidos.application.port.in.UsuarioUseCase;
import com.pedidos.application.port.out.PasswordHasherPort;
import com.pedidos.application.port.out.PedidoRepositoryPort;
import com.pedidos.application.port.out.TokenGeneratorPort;
import com.pedidos.application.port.out.UsuarioRepositoryPort;
import com.pedidos.application.service.AuthService;
import com.pedidos.application.service.PedidoService;
import com.pedidos.application.service.UsuarioService;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.time.Clock;

@Dependent
public class BeanConfig {

    @Produces
    @Singleton
    Clock reloj() {
        return Clock.systemUTC();
    }

    @Produces
    @Singleton
    PedidoUseCase pedidoUseCase(PedidoRepositoryPort repositorio, Clock reloj) {
        return new PedidoService(repositorio, reloj);
    }

    @Produces
    @Singleton
    UsuarioUseCase usuarioUseCase(UsuarioRepositoryPort repositorio, PasswordHasherPort hasher, Clock reloj) {
        return new UsuarioService(repositorio, hasher, reloj);
    }

    @Produces
    @Singleton
    AuthUseCase authUseCase(UsuarioRepositoryPort usuarios, PasswordHasherPort hasher, TokenGeneratorPort tokens) {
        return new AuthService(usuarios, hasher, tokens);
    }
}
