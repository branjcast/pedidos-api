package com.pedidos.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Error estándar de la API")
public record ErrorResponse(
        @Schema(example = "NO_ENCONTRADO") String codigo,
        @Schema(example = "No existe el pedido 123") String mensaje) {
}
