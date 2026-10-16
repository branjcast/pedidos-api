package com.pedidos.application.port.out;

import com.pedidos.application.port.in.TokenAcceso;
import com.pedidos.domain.model.Usuario;

public interface TokenGeneratorPort {

    TokenAcceso generar(Usuario usuario);
}
