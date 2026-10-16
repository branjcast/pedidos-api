package com.pedidos.application.port.out;

import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(String id);

    Optional<Usuario> buscarPorCorreo(String correo);

    Pagina<Usuario> listar(int page, int size);

    boolean eliminar(String id);
}
