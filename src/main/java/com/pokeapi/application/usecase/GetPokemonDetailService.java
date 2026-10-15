package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.GetPokemonDetailUseCase;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.PokemonDetail;

import java.util.Locale;
import java.util.regex.Pattern;

public class GetPokemonDetailService implements GetPokemonDetailUseCase {

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("[a-z0-9-]+");

    private final PokemonDetailGateway gateway;

    public GetPokemonDetailService(PokemonDetailGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PokemonDetail getPokemonDetail(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) {
            throw new InvalidDataException("identifier must not be blank");
        }

        String normalized = idOrName.trim().toLowerCase(Locale.ROOT);
        if (!VALID_IDENTIFIER.matcher(normalized).matches()) {
            throw new InvalidDataException("identifier may only contain letters, digits and hyphens");
        }

        return gateway.fetchDetail(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Pokemon " + normalized + " not found"));
    }
}