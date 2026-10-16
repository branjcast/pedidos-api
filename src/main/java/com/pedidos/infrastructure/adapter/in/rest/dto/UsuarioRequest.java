package com.pedidos.infrastructure.adapter.in.rest.dto;

import com.pedidos.domain.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Datos de usuario")
public record UsuarioRequest(
        @Schema(example = "Luis") @NotBlank @Size(max = 100) String nombre,
        @Schema(example = "Ramírez") @NotBlank @Size(max = 100) String apellidoPaterno,
        @Schema(description = "Opcional", example = "Soto") @Size(max = 100) String apellidoMaterno,
        @Schema(example = "luis.ramirez@correo.com") @NotBlank @Email @Size(max = 150) String correo,
        @Schema(description = "Obligatoria al crear (mín. 8). En actualización, si se omite se conserva la actual.", example = "Secreta123!")
                @Size(max = 100) String password,
        @Schema(description = "Por defecto USER") Rol rol,
        @Schema(description = "Por defecto true") Boolean activo) {
}
