package com.pedidos.infrastructure.adapter.in.rest.dto;

import com.pedidos.domain.model.EstatusPedido;
import jakarta.validation.constraints.NotNull;

public record EstatusRequest(@NotNull EstatusPedido estatus) {
}
