package com.pedidos.infrastructure.adapter.in.rest.dto;

import com.pedidos.domain.model.Rol;
import java.time.Instant;

public record UsuarioResponse(
        String id,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String correo,
        Rol rol,
        boolean activo,
        Instant creadoEn) {
}
