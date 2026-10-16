package com.pedidos.application.port.in;

import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Usuario;

public interface UsuarioUseCase {

    Usuario crear(DatosUsuario datos);

    Usuario obtener(String id);

    Pagina<Usuario> listar(int page, int size);
    Usuario actualizar(String id, DatosUsuario datos);
    void eliminar(String id);
}
