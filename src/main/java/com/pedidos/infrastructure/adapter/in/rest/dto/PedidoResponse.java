package com.pedidos.infrastructure.adapter.in.rest.dto;

import com.pedidos.domain.model.CanalPedido;
import com.pedidos.domain.model.EstatusPedido;
import java.math.BigDecimal;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record PedidoResponse(
        String id,
        CanalPedido canal,
        ClienteDto cliente,
        String direccionEnvio,
        String codigoProducto,
        int cantidad,
        BigDecimal precio,
        @Schema(description = "precio x cantidad") BigDecimal total,
        EstatusPedido estatus,
        Instant creadoEn,
        Instant actualizadoEn) {
}
