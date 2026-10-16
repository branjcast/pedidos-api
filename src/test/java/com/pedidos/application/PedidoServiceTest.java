package com.pedidos.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pedidos.application.port.in.DatosPedido;
import com.pedidos.application.port.in.FiltroPedidos;
import com.pedidos.application.port.out.PedidoRepositoryPort;
import com.pedidos.application.service.PedidoService;
import com.pedidos.domain.exception.RecursoNoEncontradoException;
import com.pedidos.domain.exception.ReglaNegocioException;
import com.pedidos.domain.model.CanalPedido;
import com.pedidos.domain.model.Cliente;
import com.pedidos.domain.model.EstatusPedido;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Pedido;
import com.pedidos.domain.model.Sugerencia;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PedidoServiceTest {

    private final Clock reloj = Clock.fixed(Instant.parse("2026-10-13T12:00:00Z"), ZoneOffset.UTC);
    private final MemoriaRepo repo = new MemoriaRepo();
    private final PedidoService servicio = new PedidoService(repo, reloj);

    private DatosPedido datos(int cantidad, EstatusPedido estatus) {
        return new DatosPedido(
                CanalPedido.WEB,
                new Cliente("María", "González", null, "Maria@Correo.com"),
                "Av. Reforma 222",
                "SKU-1001",
                cantidad,
                new BigDecimal("199.90"),
                estatus);
    }

    @Test
    void crearAsignaPendienteYNormalizaCorreo() {
        Pedido p = servicio.crear(datos(2, null));
        assertEquals(EstatusPedido.PENDIENTE, p.estatus());
        assertEquals("maria@correo.com", p.cliente().correo());
        assertEquals(new BigDecimal("399.80"), p.total());
    }

    @Test
    void cantidadInvalidaSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(datos(0, null)));
    }

    @Test
    void actualizarConservaEstatusSiNoSeEnvia() {
        Pedido p = servicio.crear(datos(1, EstatusPedido.CONFIRMADO));
        Pedido u = servicio.actualizar(p.id(), datos(5, null));
        assertEquals(EstatusPedido.CONFIRMADO, u.estatus());
        assertEquals(5, u.cantidad());
        assertEquals(p.creadoEn(), u.creadoEn());
    }

    @Test
    void cambiarEstatusYEliminar() {
        Pedido p = servicio.crear(datos(1, null));
        assertEquals(EstatusPedido.ENVIADO, servicio.cambiarEstatus(p.id(), EstatusPedido.ENVIADO).estatus());
        servicio.eliminar(p.id());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(p.id()));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.eliminar(p.id()));
    }

    @Test
    void sugerirConTextoVacioNoConsultaRepositorio() {
        assertTrue(servicio.sugerir("   ", 10).isEmpty());
        assertEquals(0, repo.consultasSugerir);
    }

    @Test
    void sugerirAcotaElLimite() {
        servicio.sugerir("sku", 500);
        assertEquals(20, repo.ultimoLimite);
    }

    @Test
    void buscarAcotaPaginacion() {
        servicio.buscar(new FiltroPedidos("  ", null, null, -3, 9999));
        assertEquals(0, repo.ultimoFiltro.page());
        assertEquals(100, repo.ultimoFiltro.size());
        assertEquals(null, repo.ultimoFiltro.termino());
    }

    static class MemoriaRepo implements PedidoRepositoryPort {
        final Map<String, Pedido> datos = new LinkedHashMap<>();
        int consultasSugerir;
        int ultimoLimite;
        FiltroPedidos ultimoFiltro;

        @Override public Pedido guardar(Pedido p) { datos.put(p.id(), p); return p; }
        @Override public Optional<Pedido> buscarPorId(String id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Pagina<Pedido> buscar(FiltroPedidos f) {
            ultimoFiltro = f;
            return new Pagina<>(List.copyOf(datos.values()), f.page(), f.size(), datos.size());
        }
        @Override public boolean eliminar(String id) { return datos.remove(id) != null; }
        @Override public List<Sugerencia> sugerir(String t, int limite) {
            consultasSugerir++;
            ultimoLimite = limite;
            return List.of();
        }
    }
}
