package com.pedidos.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Datos del cliente")
public record ClienteDto(
        @Schema(example = "María") @NotBlank @Size(max = 100) String nombre,
        @Schema(example = "González") @NotBlank @Size(max = 100) String apellidoPaterno,
        @Schema(description = "Opcional", example = "Pérez") @Size(max = 100) String apellidoMaterno,
        @Schema(example = "maria.gonzalez@correo.com") @NotBlank @Email @Size(max = 150) String correo) {
}
