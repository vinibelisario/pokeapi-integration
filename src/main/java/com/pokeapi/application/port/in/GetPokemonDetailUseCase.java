package com.pokeapi.application.port.in;

import com.pokeapi.domain.model.PokemonDetail;

public interface GetPokemonDetailUseCase {

    PokemonDetail getPokemonDetail(String idOrName);
}