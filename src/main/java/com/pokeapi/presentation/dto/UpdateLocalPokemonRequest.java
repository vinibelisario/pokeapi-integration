package com.pokeapi.presentation.dto;

import java.util.List;

public record UpdateLocalPokemonRequest(String localizedName, String region, List<String> tags) {
}