package com.pokeapi.application.port.in;

import com.pokeapi.domain.model.LocalPokemon;

public interface SyncPokemonUseCase {

    LocalPokemon syncPokemon(String idOrName);
}