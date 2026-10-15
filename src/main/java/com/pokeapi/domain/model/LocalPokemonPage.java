package com.pokeapi.domain.model;

import java.util.List;

public record LocalPokemonPage(
        List<LocalPokemon> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public LocalPokemonPage {
        content = List.copyOf(content);
    }
}