package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.SyncPokemonUseCase;
import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.PokemonDetail;

import java.util.List;

public class SyncPokemonService implements SyncPokemonUseCase {

    private final PokemonDetailGateway pokemonDetailGateway;
    private final LocalPokemonRepository repository;

    public SyncPokemonService(PokemonDetailGateway pokemonDetailGateway,
                              LocalPokemonRepository repository) {
        this.pokemonDetailGateway = pokemonDetailGateway;
        this.repository = repository;
    }

    @Override
    public LocalPokemon syncPokemon(String idOrName) {
        PokemonDetail detail = pokemonDetailGateway.fetchDetail(idOrName)
                .orElseThrow(() -> new ResourceNotFoundException("Pokemon " + idOrName + " not found"));

        LocalPokemon toSave = repository.findById(detail.id())
                .map(existing -> refresh(existing, detail))
                .orElseGet(() -> create(detail));

        return repository.save(toSave);
    }

    private static LocalPokemon create(PokemonDetail detail) {
        return new LocalPokemon(
                detail.id(), detail.name(), detail.imageUrl(), detail.description(),
                null, null, List.of());
    }

    private static LocalPokemon refresh(LocalPokemon existing, PokemonDetail detail) {
        return new LocalPokemon(
                detail.id(), detail.name(), detail.imageUrl(), detail.description(),
                existing.localizedName(), existing.region(), existing.tags());
    }
}