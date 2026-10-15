package com.pokeapi.application.port.in;

import java.util.List;

public record UpdateLocalPokemonCommand(String localizedName, String region, List<String> tags) {
}