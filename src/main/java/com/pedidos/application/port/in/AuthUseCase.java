package com.pedidos.application.port.in;

public interface AuthUseCase {
    TokenAcceso login(String correo, String password);
}
