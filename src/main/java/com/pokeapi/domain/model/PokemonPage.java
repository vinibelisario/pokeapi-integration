package com.pokeapi.domain.model;

import java.util.List;

public record PokemonPage(
        List<Pokemon> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public PokemonPage {
        content = List.copyOf(content);
    }
}
