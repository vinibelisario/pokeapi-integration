package com.pokeapi.domain.model;

import java.util.List;

public record Pokemon(
        int id,
        String name,
        String spriteUrl,
        String category,
        double weightKg,
        List<Ability> abilities) {

    public Pokemon {
        abilities = List.copyOf(abilities);
    }
}