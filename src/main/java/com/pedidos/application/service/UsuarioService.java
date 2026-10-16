package com.pedidos.application.service;

import com.pedidos.application.port.in.DatosUsuario;
import com.pedidos.application.port.in.UsuarioUseCase;
import com.pedidos.application.port.out.PasswordHasherPort;
import com.pedidos.application.port.out.UsuarioRepositoryPort;
import com.pedidos.domain.exception.ConflictoException;
import com.pedidos.domain.exception.RecursoNoEncontradoException;
import com.pedidos.domain.exception.ReglaNegocioException;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Rol;
import com.pedidos.domain.model.Usuario;
import java.time.Clock;
import java.util.UUID;

public class UsuarioService implements UsuarioUseCase {

    static final int PASSWORD_MINIMO = 8;

    private final UsuarioRepositoryPort repositorio;
    private final PasswordHasherPort hasher;
    private final Clock reloj;

    public UsuarioService(UsuarioRepositoryPort repositorio, PasswordHasherPort hasher, Clock reloj) {
        this.repositorio = repositorio;
        this.hasher = hasher;
        this.reloj = reloj;
    }

    @Override
    public Usuario crear(DatosUsuario datos) {
        validarPassword(datos.password());
        Usuario nuevo = new Usuario(
                UUID.randomUUID().toString(),
                datos.nombre(),
                datos.apellidoPaterno(),
                datos.apellidoMaterno(),
                datos.correo(),
                hasher.hash(datos.password()),
                datos.rol() == null ? Rol.USER : datos.rol(),
                datos.activo() == null || datos.activo(),
                reloj.instant());
        if (repositorio.buscarPorCorreo(nuevo.correo()).isPresent()) {
            throw new ConflictoException("Ya existe un usuario con el correo " + nuevo.correo());
        }
        return repositorio.guardar(nuevo);
    }

    @Override
    public Usuario obtener(String id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + id));
    }

    @Override
    public Pagina<Usuario> listar(int page, int size) {
        return repositorio.listar(Math.max(0, page), Math.max(1, Math.min(size, 100)));
    }

    @Override
    public Usuario actualizar(String id, DatosUsuario datos) {
        Usuario actual = obtener(id);
        String hash = actual.passwordHash();
        if (datos.password() != null && !datos.password().isBlank()) {
            validarPassword(datos.password());
            hash = hasher.hash(datos.password());
        }
        Usuario actualizado = new Usuario(
                actual.id(),
                datos.nombre(),
                datos.apellidoPaterno(),
                datos.apellidoMaterno(),
                datos.correo(),
                hash,
                datos.rol() == null ? actual.rol() : datos.rol(),
                datos.activo() == null ? actual.activo() : datos.activo(),
                actual.creadoEn());
        repositorio.buscarPorCorreo(actualizado.correo())
                .filter(otro -> !otro.id().equals(actual.id()))
                .ifPresent(otro -> {
                    throw new ConflictoException("Ya existe un usuario con el correo " + actualizado.correo());
                });
        return repositorio.guardar(actualizado);
    }

    @Override
    public void eliminar(String id) {
        if (!repositorio.eliminar(id)) {
            throw new RecursoNoEncontradoException("No existe el usuario " + id);
        }
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < PASSWORD_MINIMO) {
            throw new ReglaNegocioException("La contraseña debe tener al menos " + PASSWORD_MINIMO + " caracteres");
        }
    }
}
