package com.pedidos.application.service;

import com.pedidos.application.port.in.DatosPedido;
import com.pedidos.application.port.in.FiltroPedidos;
import com.pedidos.application.port.in.PedidoUseCase;
import com.pedidos.application.port.out.PedidoRepositoryPort;
import com.pedidos.domain.exception.RecursoNoEncontradoException;
import com.pedidos.domain.model.EstatusPedido;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Pedido;
import com.pedidos.domain.model.Sugerencia;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public class PedidoService implements PedidoUseCase {

    static final int TAMANO_MAXIMO = 100;
    static final int LIMITE_SUGERENCIAS_MAXIMO = 20;

    private final PedidoRepositoryPort repositorio;
    private final Clock reloj;

    public PedidoService(PedidoRepositoryPort repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public Pedido crear(DatosPedido datos) {
        Instant ahora = reloj.instant();
        Pedido pedido = new Pedido(
                UUID.randomUUID().toString(),
                datos.canal(),
                datos.cliente(),
                datos.direccionEnvio(),
                datos.codigoProducto(),
                datos.cantidad(),
                datos.precio(),
                datos.estatus() == null ? EstatusPedido.PENDIENTE : datos.estatus(),
                ahora,
                ahora);
        return repositorio.guardar(pedido);
    }

    @Override
    public Pedido obtener(String id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido " + id));
    }

    @Override
    public Pagina<Pedido> buscar(FiltroPedidos filtro) {
        int size = Math.max(1, Math.min(filtro.size(), TAMANO_MAXIMO));
        int page = Math.max(0, filtro.page());
        String termino = filtro.termino() == null || filtro.termino().isBlank() ? null : filtro.termino().trim();
        return repositorio.buscar(new FiltroPedidos(termino, filtro.estatus(), filtro.canal(), page, size));
    }

    @Override
    public Pedido actualizar(String id, DatosPedido datos) {
        Pedido actual = obtener(id);
        Pedido actualizado = new Pedido(
                actual.id(),
                datos.canal(),
                datos.cliente(),
                datos.direccionEnvio(),
                datos.codigoProducto(),
                datos.cantidad(),
                datos.precio(),
                datos.estatus() == null ? actual.estatus() : datos.estatus(),
                actual.creadoEn(),
                reloj.instant());
        return repositorio.guardar(actualizado);
    }

    @Override
    public Pedido cambiarEstatus(String id, EstatusPedido estatus) {
        return repositorio.guardar(obtener(id).conEstatus(estatus, reloj.instant()));
    }

    @Override
    public void eliminar(String id) {
        if (!repositorio.eliminar(id)) {
            throw new RecursoNoEncontradoException("No existe el pedido " + id);
        }
    }

    @Override
    public List<Sugerencia> sugerir(String termino, int limite) {
        if (termino == null || termino.isBlank()) {
            return List.of();
        }
        int tope = Math.max(1, Math.min(limite, LIMITE_SUGERENCIAS_MAXIMO));
        return repositorio.sugerir(termino.trim(), tope);
    }
}
