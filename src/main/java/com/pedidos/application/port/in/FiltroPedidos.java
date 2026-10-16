package com.pedidos.application.port.in;

import com.pedidos.domain.model.CanalPedido;
import com.pedidos.domain.model.EstatusPedido;

public record FiltroPedidos(String termino, EstatusPedido estatus, CanalPedido canal, int page, int size) {
}
