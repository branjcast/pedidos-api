package com.pedidos.domain.model;

import com.pedidos.domain.exception.ReglaNegocioException;
import java.time.Instant;

public record Usuario(
        String id,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String correo,
        String passwordHash,
        Rol rol,
        boolean activo,
        Instant creadoEn) {

    public Usuario {
        if (nombre == null || nombre.isBlank() || apellidoPaterno == null || apellidoPaterno.isBlank()) {
            throw new ReglaNegocioException("Nombre y apellido paterno son obligatorios");
        }
        if (rol == null) {
            throw new ReglaNegocioException("El rol es obligatorio");
        }
        nombre = nombre.trim();
        apellidoPaterno = apellidoPaterno.trim();
        apellidoMaterno = apellidoMaterno == null || apellidoMaterno.isBlank() ? null : apellidoMaterno.trim();
        correo = Correos.normalizar(correo);
    }
}
