package com.pedidos.infrastructure.adapter.in.rest;

import com.pedidos.domain.exception.ConflictoException;
import com.pedidos.domain.exception.CredencialesInvalidasException;
import com.pedidos.domain.exception.RecursoNoEncontradoException;
import com.pedidos.domain.exception.ReglaNegocioException;
import com.pedidos.infrastructure.adapter.in.rest.dto.ErrorResponse;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(RestResponse.Status.NOT_FOUND, "NO_ENCONTRADO", e);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> conflicto(ConflictoException e) {
        return respuesta(RestResponse.Status.CONFLICT, "CONFLICTO", e);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> credenciales(CredencialesInvalidasException e) {
        return respuesta(RestResponse.Status.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", e);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        return respuesta(RestResponse.Status.BAD_REQUEST, "SOLICITUD_INVALIDA", e);
    }

    private static RestResponse<ErrorResponse> respuesta(RestResponse.Status status, String codigo, Exception e) {
        return RestResponse.status(status, new ErrorResponse(codigo, e.getMessage()));
    }
}
