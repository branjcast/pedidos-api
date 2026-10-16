package com.pedidos.infrastructure.adapter.in.rest;

import com.pedidos.application.port.in.FiltroPedidos;
import com.pedidos.application.port.in.PedidoUseCase;
import com.pedidos.domain.model.CanalPedido;
import com.pedidos.domain.model.EstatusPedido;
import com.pedidos.domain.model.Pedido;
import com.pedidos.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.EstatusRequest;
import com.pedidos.infrastructure.adapter.in.rest.dto.PaginaResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.PedidoRequest;
import com.pedidos.infrastructure.adapter.in.rest.dto.PedidoResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.SugerenciaResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.TypeaheadResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
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
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
@Tag(name = "Pedidos")
@SecurityRequirement(name = "jwt")
@APIResponse(responseCode = "401", description = "Sin token o token inválido")
public class PedidoResource {

    private final PedidoUseCase pedidos;

    public PedidoResource(PedidoUseCase pedidos) {
        this.pedidos = pedidos;
    }

    @POST
    @Operation(summary = "Crear pedido")
    @APIResponse(responseCode = "201", description = "Pedido creado",
            content = @Content(schema = @Schema(implementation = PedidoResponse.class)))
    @APIResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response crear(@Valid PedidoRequest request, @Context UriInfo uriInfo) {
        Pedido creado = pedidos.crear(RestMapper.aDatos(request));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(creado.id()).build())
                .entity(RestMapper.aRespuesta(creado))
                .build();
    }

    @GET
    @Operation(
            summary = "Listar pedidos",
            description = "Lista paginada, más recientes primero. El parámetro 'q' busca (sin distinguir mayúsculas) en código de "
                    + "producto, nombre completo del cliente y correo. Si no hay coincidencias, 'mensaje' indica que no se "
                    + "tienen resultados que coincidan con los criterios de búsqueda.")
    public PaginaResponse<PedidoResponse> listar(
            @Parameter(description = "Texto a buscar; use el valor elegido en el typeahead") @QueryParam("q") String q,
            @QueryParam("estatus") EstatusPedido estatus,
            @QueryParam("canal") CanalPedido canal,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        var resultado = pedidos.buscar(new FiltroPedidos(q, estatus, canal, page, size));
        boolean hayCriterios = (q != null && !q.isBlank()) || estatus != null || canal != null;
        return RestMapper.aPagina(resultado, RestMapper::aRespuesta, hayCriterios);
    }

    @GET
    @Path("/typeahead")
    @Operation(
            summary = "Sugerencias del typeahead",
            description = "Devuelve opciones (código de producto, cliente, correo) que contienen el texto escrito. "
                    + "Llamar conforme el usuario escribe (se recomienda debounce). Si no hay coincidencias, la lista va vacía "
                    + "y 'mensaje' trae: \"No se tienen resultados que coincidan con los criterios de búsqueda\".")
    public TypeaheadResponse typeahead(
            @QueryParam("q") String q,
            @QueryParam("limite") @DefaultValue("10") @Min(1) @Max(20) int limite) {
        List<SugerenciaResponse> sugerencias =
                pedidos.sugerir(q, limite).stream().map(RestMapper::aRespuesta).toList();
        boolean escribio = q != null && !q.isBlank();
        return new TypeaheadResponse(
                q, sugerencias, escribio && sugerencias.isEmpty() ? RestMapper.SIN_RESULTADOS : null);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Consultar pedido por id")
    @APIResponse(responseCode = "404", description = "No existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PedidoResponse obtener(@PathParam("id") String id) {
        return RestMapper.aRespuesta(pedidos.obtener(id));
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar pedido", description = "Reemplaza los datos del pedido.")
    @APIResponse(responseCode = "404", description = "No existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PedidoResponse actualizar(@PathParam("id") String id, @Valid PedidoRequest request) {
        return RestMapper.aRespuesta(pedidos.actualizar(id, RestMapper.aDatos(request)));
    }

    @PATCH
    @Path("/{id}/estatus")
    @Operation(summary = "Cambiar el estatus del pedido")
    public PedidoResponse cambiarEstatus(@PathParam("id") String id, @Valid EstatusRequest request) {
        return RestMapper.aRespuesta(pedidos.cambiarEstatus(id, request.estatus()));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Eliminar pedido", description = "Solo ADMIN.")
    @APIResponse(responseCode = "204", description = "Eliminado")
    @APIResponse(responseCode = "403", description = "Requiere rol ADMIN")
    @APIResponse(responseCode = "404", description = "No existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response eliminar(@PathParam("id") String id) {
        pedidos.eliminar(id);
        return Response.noContent().build();
    }
}
