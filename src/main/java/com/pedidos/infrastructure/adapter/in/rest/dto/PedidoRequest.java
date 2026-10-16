package com.pedidos.infrastructure.adapter.in.rest.dto;

import com.pedidos.domain.model.CanalPedido;
import com.pedidos.domain.model.EstatusPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Datos para crear o reemplazar un pedido")
public record PedidoRequest(
        @Schema(description = "Origen del pedido", example = "APP") @NotNull CanalPedido canal,
        @NotNull @Valid ClienteDto cliente,
        @Schema(example = "Av. Reforma 222, Col. Juárez, CDMX, 06600") @NotBlank @Size(max = 300) String direccionEnvio,
        @Schema(example = "SKU-1001") @NotBlank @Size(max = 50) String codigoProducto,
        @Schema(example = "2") @NotNull @Min(1) Integer cantidad,
        @Schema(description = "Precio unitario", example = "199.90") @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @Schema(description = "Opcional. En creación el valor por defecto es PENDIENTE; en actualización conserva el actual.")
                EstatusPedido estatus) {
}
