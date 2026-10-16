package com.pedidos.domain.model;

import com.pedidos.domain.exception.ReglaNegocioException;
import java.util.Locale;
import java.util.regex.Pattern;
final class Correos {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Correos() {
    }

    static String normalizar(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new ReglaNegocioException("El campo 'correo' es obligatorio");
        }
        String limpio = correo.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO.matcher(limpio).matches()) {
            throw new ReglaNegocioException("El correo electrónico no tiene un formato válido");
        }
        return limpio;
    }
}
