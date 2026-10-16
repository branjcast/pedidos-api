package com.pedidos.infrastructure.adapter.in.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@ApplicationPath("/")
@OpenAPIDefinition(
        info = @Info(
                title = "API de Gestión de Pedidos",
                version = "1.0.0",
                description = "Pedidos realizados desde la aplicación móvil y el sitio web. "
                        + "Para probar en Swagger: obtén un token en POST /api/auth/login y pulsa 'Authorize'.",
                contact = @Contact(name = "Equipo de Pedidos")),
        tags = {
            @Tag(name = "Autenticación", description = "Login con JWT"),
            @Tag(name = "Pedidos", description = "CRUD de pedidos y búsqueda con typeahead"),
            @Tag(name = "Usuarios", description = "CRUD de usuarios (solo ADMIN)")
        })
@SecurityScheme(
        securitySchemeName = "jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token obtenido en /api/auth/login")
public class OpenApiConfig extends Application {
}
