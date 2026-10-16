package com.pedidos.application.port.in;

public record TokenAcceso(String token, String tipo, long expiraEnSegundos) {
}
