package com.pokeapi.application.port.out;

import com.pokeapi.domain.model.LocalPokemon;

import java.util.List;
import java.util.Optional;

public interface LocalPokemonRepository {

    Optional<LocalPokemon> findById(int id);

    LocalPokemon save(LocalPokemon pokemon);

    List<LocalPokemon> findPage(int page, int size);

    long count();
}