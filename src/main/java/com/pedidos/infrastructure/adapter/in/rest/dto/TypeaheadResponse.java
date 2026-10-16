package com.pedidos.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TypeaheadResponse(
        String termino,
        List<SugerenciaResponse> sugerencias,
        @Schema(description = "Presente solo cuando no hay coincidencias",
                example = "No se tienen resultados que coincidan con los criterios de búsqueda") String mensaje) {
}
