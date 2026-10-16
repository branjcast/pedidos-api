package com.pedidos.application.port.in;

import com.pedidos.domain.model.CanalPedido;
import com.pedidos.domain.model.Cliente;
import com.pedidos.domain.model.EstatusPedido;
import java.math.BigDecimal;

public record DatosPedido(
        CanalPedido canal,
        Cliente cliente,
        String direccionEnvio,
        String codigoProducto,
        int cantidad,
        BigDecimal precio,
        EstatusPedido estatus) {
}
