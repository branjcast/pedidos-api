package com.pedidos.infrastructure.config;

import com.pedidos.application.port.in.DatosUsuario;
import com.pedidos.application.port.in.UsuarioUseCase;
import com.pedidos.application.port.out.UsuarioRepositoryPort;
import com.pedidos.domain.model.Rol;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AdminBootstrap {

    private static final Logger LOG = Logger.getLogger(AdminBootstrap.class);

    private final UsuarioUseCase usuarios;
    private final UsuarioRepositoryPort repositorio;
    private final Optional<String> correo;
    private final Optional<String> password;

    public AdminBootstrap(
            UsuarioUseCase usuarios,
            UsuarioRepositoryPort repositorio,
            @ConfigProperty(name = "app.admin.email") Optional<String> correo,
            @ConfigProperty(name = "app.admin.password") Optional<String> password) {
        this.usuarios = usuarios;
        this.repositorio = repositorio;
        this.correo = correo;
        this.password = password;
    }

    void alIniciar(@Observes StartupEvent evento) {
        if (correo.isEmpty() || password.isEmpty()) {
            LOG.info("ADMIN_EMAIL/ADMIN_PASSWORD no configurados: no se crea administrador inicial");
            return;
        }
        String email = correo.get().trim().toLowerCase();
        if (repositorio.buscarPorCorreo(email).isPresent()) {
            return;
        }
        usuarios.crear(new DatosUsuario("Administrador", "Sistema", null, email, password.get(), Rol.ADMIN, true));
        LOG.infof("Administrador inicial creado: %s", email);
    }
}
