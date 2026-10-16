package com.pedidos.domain.model;

import com.pedidos.domain.exception.ReglaNegocioException;

public record Cliente(String nombre, String apellidoPaterno, String apellidoMaterno, String correo) {

    public Cliente {
        nombre = requerido(nombre, "nombre");
        apellidoPaterno = requerido(apellidoPaterno, "apellidoPaterno");
        apellidoMaterno = apellidoMaterno == null || apellidoMaterno.isBlank() ? null : apellidoMaterno.trim();
        correo = Correos.normalizar(correo);
    }
    public String nombreCompleto() {
        StringBuilder sb = new StringBuilder(nombre).append(' ').append(apellidoPaterno);
        if (apellidoMaterno != null) {
            sb.append(' ').append(apellidoMaterno);
        }
        return sb.toString();
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }
}
