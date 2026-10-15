package com.pokeapi.presentation.dto;

import com.pokeapi.domain.model.LocalPokemonPage;

import java.util.List;

public record LocalPokemonPageResponse(List<LocalPokemonResponse> content, int page, int size,
                                       long totalElements, int totalPages) {

    public static LocalPokemonPageResponse from(LocalPokemonPage page) {
        return new LocalPokemonPageResponse(
                page.content().stream().map(LocalPokemonResponse::from).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}