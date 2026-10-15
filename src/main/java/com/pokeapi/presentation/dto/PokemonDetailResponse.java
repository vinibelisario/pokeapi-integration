package com.pokeapi.presentation.dto;

import com.pokeapi.domain.model.EvolutionNode;
import com.pokeapi.domain.model.PokemonDetail;

import java.util.List;

public record PokemonDetailResponse(
        int id,
        String name,
        String imageUrl,
        List<StatResponse> stats,
        String description,
        EvolutionNodeResponse evolutionChain) {

    public static PokemonDetailResponse from(PokemonDetail detail) {
        List<StatResponse> stats = detail.stats().stream()
                .map(s -> new StatResponse(s.name(), s.baseValue()))
                .toList();
        return new PokemonDetailResponse(
                detail.id(),
                detail.name(),
                detail.imageUrl(),
                stats,
                detail.description(),
                EvolutionNodeResponse.from(detail.evolutionChain()));
    }

    public record StatResponse(String name, int baseValue) {
    }

    public record EvolutionNodeResponse(int id, String name, List<EvolutionNodeResponse> evolvesTo) {

        public static EvolutionNodeResponse from(EvolutionNode node) {
            return new EvolutionNodeResponse(
                    node.id(),
                    node.name(),
                    node.evolvesTo().stream()
                            .map(EvolutionNodeResponse::from)
                            .toList());
        }
    }
}