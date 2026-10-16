package com.pedidos.infrastructure.adapter.out.security;

import com.pedidos.application.port.out.PasswordHasherPort;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BcryptPasswordHasher implements PasswordHasherPort {

    @Override
    public String hash(String passwordPlano) {
        return BcryptUtil.bcryptHash(passwordPlano);
    }

    @Override
    public boolean coincide(String passwordPlano, String hash) {
        return BcryptUtil.matches(passwordPlano, hash);
    }
}
