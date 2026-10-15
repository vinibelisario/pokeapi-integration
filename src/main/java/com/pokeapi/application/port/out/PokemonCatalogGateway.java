package com.pokeapi.application.port.out;

import com.pokeapi.domain.model.PokemonPage;

public interface PokemonCatalogGateway {

    PokemonPage fetchPage(int page, int size);
}