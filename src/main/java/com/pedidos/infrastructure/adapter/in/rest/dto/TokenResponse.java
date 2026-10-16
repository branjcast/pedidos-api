package com.pedidos.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "JWT para enviar en 'Authorization: Bearer <token>'") String token,
        @Schema(example = "Bearer") String tipo,
        @Schema(description = "Vigencia en segundos", example = "3600") long expiraEnSegundos) {
}
