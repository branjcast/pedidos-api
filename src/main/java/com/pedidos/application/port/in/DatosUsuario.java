package com.pedidos.application.port.in;

import com.pedidos.domain.model.Rol;
public record DatosUsuario(
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String correo,
        String password,
        Rol rol,
        Boolean activo) {
}
