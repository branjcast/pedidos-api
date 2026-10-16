package com.pedidos.infrastructure.adapter.in.rest;

import com.pedidos.application.port.in.DatosPedido;
import com.pedidos.application.port.in.DatosUsuario;
import com.pedidos.domain.model.Cliente;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Pedido;
import com.pedidos.domain.model.Sugerencia;
import com.pedidos.domain.model.Usuario;
import com.pedidos.infrastructure.adapter.in.rest.dto.ClienteDto;
import com.pedidos.infrastructure.adapter.in.rest.dto.PaginaResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.PedidoRequest;
import com.pedidos.infrastructure.adapter.in.rest.dto.PedidoResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.SugerenciaResponse;
import com.pedidos.infrastructure.adapter.in.rest.dto.UsuarioRequest;
import com.pedidos.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import java.util.function.Function;

final class RestMapper {

    static final String SIN_RESULTADOS = "No se tienen resultados que coincidan con los criterios de búsqueda";

    private RestMapper() {
    }

    static DatosPedido aDatos(PedidoRequest r) {
        ClienteDto c = r.cliente();
        return new DatosPedido(
                r.canal(),
                new Cliente(c.nombre(), c.apellidoPaterno(), c.apellidoMaterno(), c.correo()),
                r.direccionEnvio(),
                r.codigoProducto(),
                r.cantidad(),
                r.precio(),
                r.estatus());
    }

    static PedidoResponse aRespuesta(Pedido p) {
        Cliente c = p.cliente();
        return new PedidoResponse(
                p.id(),
                p.canal(),
                new ClienteDto(c.nombre(), c.apellidoPaterno(), c.apellidoMaterno(), c.correo()),
                p.direccionEnvio(),
                p.codigoProducto(),
                p.cantidad(),
                p.precio(),
                p.total(),
                p.estatus(),
                p.creadoEn(),
                p.actualizadoEn());
    }

    static SugerenciaResponse aRespuesta(Sugerencia s) {
        return new SugerenciaResponse(s.campo(), s.valor());
    }

    static DatosUsuario aDatos(UsuarioRequest r) {
        return new DatosUsuario(
                r.nombre(), r.apellidoPaterno(), r.apellidoMaterno(), r.correo(), r.password(), r.rol(), r.activo());
    }

    static UsuarioResponse aRespuesta(Usuario u) {
        return new UsuarioResponse(
                u.id(), u.nombre(), u.apellidoPaterno(), u.apellidoMaterno(), u.correo(), u.rol(), u.activo(), u.creadoEn());
    }

    static <D, R> PaginaResponse<R> aPagina(Pagina<D> pagina, Function<D, R> mapeo, boolean informarSinResultados) {
        return new PaginaResponse<>(
                pagina.items().stream().map(mapeo).toList(),
                pagina.page(),
                pagina.size(),
                pagina.total(),
                pagina.totalPaginas(),
                informarSinResultados && pagina.total() == 0 ? SIN_RESULTADOS : null);
    }
}
