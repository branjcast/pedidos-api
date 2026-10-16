package com.pedidos.domain.model;

import com.pedidos.domain.exception.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.Instant;
public record Pedido(
        String id,
        CanalPedido canal,
        Cliente cliente,
        String direccionEnvio,
        String codigoProducto,
        int cantidad,
        BigDecimal precio,
        EstatusPedido estatus,
        Instant creadoEn,
        Instant actualizadoEn) {

    public Pedido {
        if (canal == null || cliente == null || estatus == null) {
            throw new ReglaNegocioException("Canal, cliente y estatus son obligatorios");
        }
        if (direccionEnvio == null || direccionEnvio.isBlank()) {
            throw new ReglaNegocioException("La dirección de envío es obligatoria");
        }
        if (codigoProducto == null || codigoProducto.isBlank()) {
            throw new ReglaNegocioException("El código del producto es obligatorio");
        }
        if (cantidad < 1) {
            throw new ReglaNegocioException("La cantidad debe ser mayor o igual a 1");
        }
        if (precio == null || precio.signum() < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo");
        }
        direccionEnvio = direccionEnvio.trim();
        codigoProducto = codigoProducto.trim();
    }

    public BigDecimal total() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public Pedido conEstatus(EstatusPedido nuevo, Instant ahora) {
        return new Pedido(id, canal, cliente, direccionEnvio, codigoProducto, cantidad, precio, nuevo, creadoEn, ahora);
    }
}
