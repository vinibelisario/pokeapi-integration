package com.pokeapi.presentation.dto;

import com.pokeapi.domain.model.Pokemon;

import java.util.List;

public record PokemonResponse(
        int id,
        String name,
        String spriteUrl,
        String category,
        double weightKg,
        List<AbilityResponse> abilities) {

    public static PokemonResponse from(Pokemon pokemon) {
        List<AbilityResponse> abilities = pokemon.abilities().stream()
                .map(a -> new AbilityResponse(a.name(), a.hidden()))
                .toList();
        return new PokemonResponse(
                pokemon.id(),
                pokemon.name(),
                pokemon.spriteUrl(),
                pokemon.category(),
                pokemon.weightKg(),
                abilities);
    }

    public record AbilityResponse(String name, boolean hidden) {
    }
}