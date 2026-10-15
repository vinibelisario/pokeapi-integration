package com.pokeapi.application.port.out;

import com.pokeapi.domain.model.PokemonDetail;

import java.util.Optional;

public interface PokemonDetailGateway {

    Optional<PokemonDetail> fetchDetail(String idOrName);
}