package com.pedidos.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record LoginRequest(
        @Schema(example = "admin@pedidos.local") @NotBlank String correo,
        @Schema(example = "Admin123!") @NotBlank String password) {
}
