package com.pokeapi.domain.model;

import java.util.List;

public record EvolutionNode(int id, String name, List<EvolutionNode> evolvesTo) {

    public EvolutionNode {
        evolvesTo = List.copyOf(evolvesTo);
    }
}