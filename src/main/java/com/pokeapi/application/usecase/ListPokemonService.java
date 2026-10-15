package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.model.PokemonPage;

public class ListPokemonService implements ListPokemonUseCase {

    static final int MAX_SIZE = 100;

    private final PokemonCatalogGateway gateway;

    public ListPokemonService(PokemonCatalogGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PokemonPage listPokemon(int page, int size) {
        if (page < 0) {
            throw new InvalidDataException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidDataException("size must be between 1 and " + MAX_SIZE);
        }
        return gateway.fetchPage(page, size);
    }
}