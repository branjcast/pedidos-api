package com.pedidos.application.port.out;

public interface PasswordHasherPort {

    String hash(String passwordPlano);

    boolean coincide(String passwordPlano, String hash);
}
