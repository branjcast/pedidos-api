package com.pedidos.application.port.in;

import com.pedidos.domain.model.EstatusPedido;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Pedido;
import com.pedidos.domain.model.Sugerencia;
import java.util.List;

public interface PedidoUseCase {

    Pedido crear(DatosPedido datos);

    Pedido obtener(String id);

    Pagina<Pedido> buscar(FiltroPedidos filtro);

    Pedido actualizar(String id, DatosPedido datos);

    Pedido cambiarEstatus(String id, EstatusPedido estatus);

    void eliminar(String id);
    List<Sugerencia> sugerir(String termino, int limite);
}
