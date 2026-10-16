package com.pedidos.infrastructure.adapter.in.rest;

import com.pedidos.application.port.in.AuthUseCase;
import com.pedidos.application.port.in.TokenAcceso;
import com.pedidos.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.pedidos.infrastructure.adapter.in.rest.dto.TokenResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticación")
public class AuthResource {

    private final AuthUseCase auth;

    public AuthResource(AuthUseCase auth) {
        this.auth = auth;
    }

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Iniciar sesión", description = "Valida correo y contraseña y devuelve un JWT.")
    @APIResponse(responseCode = "200", description = "Login correcto",
            content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @APIResponse(responseCode = "401", description = "Credenciales incorrectas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public TokenResponse login(@Valid LoginRequest request) {
        TokenAcceso t = auth.login(request.correo(), request.password());
        return new TokenResponse(t.token(), t.tipo(), t.expiraEnSegundos());
    }
}
