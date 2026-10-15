package com.pokeapi.application.port.in;

import com.pokeapi.domain.model.LocalPokemon;

public interface UpdateLocalPokemonUseCase {

    LocalPokemon update(int id, UpdateLocalPokemonCommand command);
}