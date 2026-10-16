package com.pedidos.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record SugerenciaResponse(
        @Schema(description = "Campo donde coincidió", example = "codigoProducto") String campo,
        @Schema(description = "Texto sugerido, listo para usarse como filtro (q)", example = "SKU-1001") String valor) {
}
