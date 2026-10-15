package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.QueryLocalPokemonUseCase;
import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.LocalPokemonPage;

import java.util.List;

public class QueryLocalPokemonService implements QueryLocalPokemonUseCase {

    static final int MAX_SIZE = 100;

    private final LocalPokemonRepository repository;

    public QueryLocalPokemonService(LocalPokemonRepository repository) {
        this.repository = repository;
    }

    @Override
    public LocalPokemon getById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pokemon " + id + " not found"));
    }

    @Override
    public LocalPokemonPage list(int page, int size) {
        if (page < 0) {
            throw new InvalidDataException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidDataException("size must be between 1 and " + MAX_SIZE);
        }

        List<LocalPokemon> content = repository.findPage(page, size);
        long total = repository.count();
        int totalPages = (int) ((total + size - 1) / size);

        return new LocalPokemonPage(content, page, size, total, totalPages);
    }
}