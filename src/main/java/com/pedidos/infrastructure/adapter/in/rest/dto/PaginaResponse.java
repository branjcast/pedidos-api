package com.pedidos.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaginaResponse<T>(
        List<T> items,
        int page,
        int size,
        long total,
        int totalPaginas,
        @Schema(description = "Se informa cuando no hay resultados que coincidan con la búsqueda") String mensaje) {
}
