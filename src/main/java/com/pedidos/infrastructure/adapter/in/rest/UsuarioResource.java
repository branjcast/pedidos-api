package com.pedidos.infrastructure.adapter.in.rest;

import com.pedidos.application.port.in.UsuarioUseCase;
import com.pedidos.domain.model.Usuario;
import com.pedidos.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.PaginaResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.UsuarioRequest;
import com.pedidos.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Tag(name = "Usuarios")
@SecurityRequirement(name = "jwt")
@APIResponse(responseCode = "401", description = "Sin token o token inválido")
@APIResponse(responseCode = "403", description = "Requiere rol ADMIN")
public class UsuarioResource {

    private final UsuarioUseCase usuarios;

    public UsuarioResource(UsuarioUseCase usuarios) {
        this.usuarios = usuarios;
    }

    @POST
    @Operation(summary = "Crear usuario")
    @APIResponse(responseCode = "201", description = "Usuario creado")
    @APIResponse(responseCode = "409", description = "El correo ya existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response crear(@Valid UsuarioRequest request, @Context UriInfo uriInfo) {
        Usuario creado = usuarios.crear(RestMapper.aDatos(request));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(creado.id()).build())
                .entity(RestMapper.aRespuesta(creado))
                .build();
    }

    @GET
    @Operation(summary = "Listar usuarios")
    public PaginaResponse<UsuarioResponse> listar(
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        return RestMapper.aPagina(usuarios.listar(page, size), RestMapper::aRespuesta, false);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Consultar usuario por id")
    @APIResponse(responseCode = "404", description = "No existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UsuarioResponse obtener(@PathParam("id") String id) {
        return RestMapper.aRespuesta(usuarios.obtener(id));
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar usuario")
    @APIResponse(responseCode = "404", description = "No existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "409", description = "El correo ya existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UsuarioResponse actualizar(@PathParam("id") String id, @Valid UsuarioRequest request) {
        return RestMapper.aRespuesta(usuarios.actualizar(id, RestMapper.aDatos(request)));
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar usuario")
    @APIResponse(responseCode = "204", description = "Eliminado")
    @APIResponse(responseCode = "404", description = "No existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response eliminar(@PathParam("id") String id) {
        usuarios.eliminar(id);
        return Response.noContent().build();
    }
}
