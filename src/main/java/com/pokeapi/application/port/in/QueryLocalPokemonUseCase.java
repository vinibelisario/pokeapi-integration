package com.pokeapi.application.port.in;

import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.LocalPokemonPage;

public interface QueryLocalPokemonUseCase {

    LocalPokemon getById(int id);

    LocalPokemonPage list(int page, int size);
}