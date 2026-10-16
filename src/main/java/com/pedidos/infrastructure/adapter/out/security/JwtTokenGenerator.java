package com.pedidos.infrastructure.adapter.out.security;

import com.pedidos.application.port.in.TokenAcceso;
import com.pedidos.application.port.out.TokenGeneratorPort;
import com.pedidos.domain.model.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

//RS256 para certs
@ApplicationScoped
public class JwtTokenGenerator implements TokenGeneratorPort {

    private final String emisor;
    private final long minutos;

    public JwtTokenGenerator(
            @ConfigProperty(name = "mp.jwt.verify.issuer") String emisor,
            @ConfigProperty(name = "app.jwt.expiration-minutes", defaultValue = "60") long minutos) {
        this.emisor = emisor;
        this.minutos = minutos;
    }

    @Override
    public TokenAcceso generar(Usuario usuario) {
        Duration vigencia = Duration.ofMinutes(minutos);
        String token = Jwt.issuer(emisor)
                .subject(usuario.id())
                .upn(usuario.correo())
                .groups(Set.of(usuario.rol().name()))
                .claim("nombre", usuario.nombre() + " " + usuario.apellidoPaterno())
                .expiresIn(vigencia)
                .sign();
        return new TokenAcceso(token, "Bearer", vigencia.toSeconds());
    }
}
