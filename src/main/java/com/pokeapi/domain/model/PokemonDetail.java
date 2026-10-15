package com.pokeapi.domain.model;

import java.util.List;

public record PokemonDetail(
        int id,
        String name,
        String imageUrl,
        List<Stat> stats,
        String description,
        EvolutionNode evolutionChain) {

    public PokemonDetail {
        stats = List.copyOf(stats);
    }
}