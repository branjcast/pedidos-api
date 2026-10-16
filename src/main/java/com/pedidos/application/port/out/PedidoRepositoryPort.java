package com.pedidos.application.port.out;

import com.pedidos.application.port.in.FiltroPedidos;
import com.pedidos.domain.model.Pagina;
import com.pedidos.domain.model.Pedido;
import com.pedidos.domain.model.Sugerencia;
import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {

    Pedido guardar(Pedido pedido);

    Optional<Pedido> buscarPorId(String id);

    Pagina<Pedido> buscar(FiltroPedidos filtro);

    boolean eliminar(String id);

    List<Sugerencia> sugerir(String termino, int limite);
}
