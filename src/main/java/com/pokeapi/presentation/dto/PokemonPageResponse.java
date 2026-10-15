package com.pokeapi.presentation.dto;

import com.pokeapi.domain.model.PokemonPage;

import java.util.List;

public record PokemonPageResponse(
        List<PokemonResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static PokemonPageResponse from(PokemonPage page) {
        List<PokemonResponse> content = page.content().stream()
                .map(PokemonResponse::from)
                .toList();
        return new PokemonPageResponse(
                content,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}