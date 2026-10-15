package com.pokeapi.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class PokeApiModels {

    private PokeApiModels() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListResponse(long count, List<NamedResource> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NamedResource(String name, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PokemonDetail(int id, String name, int weight, Sprites sprites,
                                List<AbilitySlot> abilities) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sprites(@JsonProperty("front_default") String frontDefault) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AbilitySlot(NamedResource ability, @JsonProperty("is_hidden") boolean hidden) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpeciesResponse(List<Genus> genera) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genus(String genus, NamedResource language) {
    }
}