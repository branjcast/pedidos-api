package com.pedidos.domain.model;

import java.util.List;

public record Pagina<T>(List<T> items, int page, int size, long total) {

    public int totalPaginas() {
        return size == 0 ? 0 : (int) Math.ceil((double) total / size);
    }
}
