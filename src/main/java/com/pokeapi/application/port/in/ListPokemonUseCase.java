package com.pokeapi.application.port.in;

import com.pokeapi.domain.model.PokemonPage;

public interface ListPokemonUseCase {

    PokemonPage listPokemon(int page, int size);
}