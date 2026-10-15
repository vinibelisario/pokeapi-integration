package com.pokeapi.infrastructure.pokeapi;

import com.pokeapi.application.exception.ExternalServiceException;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.domain.model.Ability;
import com.pokeapi.domain.model.Pokemon;
import com.pokeapi.domain.model.PokemonPage;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.AbilitySlot;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.Genus;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.ListResponse;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.NamedResource;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.PokemonDetail;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.SpeciesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Component
public class PokeApiPokemonCatalog implements PokemonCatalogGateway {

    private final RestClient restClient;

    public PokeApiPokemonCatalog(RestClient.Builder builder,
                                 @Value("${pokeapi.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public PokemonPage fetchPage(int page, int size) {
        try {
            ListResponse list = restClient.get()
                    .uri("/pokemon?offset={offset}&limit={limit}", page * size, size)
                    .retrieve()
                    .body(ListResponse.class);

            if (list == null || list.results() == null) {
                throw new ExternalServiceException("Empty response from PokeAPI", null);
            }

            List<Pokemon> content = new ArrayList<>();
            for (NamedResource entry : list.results()) {
                content.add(fetchPokemon(entry.name()));
            }

            int totalPages = (int) ((list.count() + size - 1) / size);
            return new PokemonPage(content, page, size, list.count(), totalPages);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Failed to fetch Pokemon list from PokeAPI", e);
        }
    }

    private Pokemon fetchPokemon(String name) {
        PokemonDetail detail = restClient.get()
                .uri("/pokemon/{name}", name)
                .retrieve()
                .body(PokemonDetail.class);

        SpeciesResponse species = restClient.get()
                .uri("/pokemon-species/{name}", name)
                .retrieve()
                .body(SpeciesResponse.class);

        String spriteUrl = detail.sprites() != null ? detail.sprites().frontDefault() : null;

        List<Ability> abilities = detail.abilities().stream()
                .map(slot -> toAbility(slot))
                .toList();

        return new Pokemon(
                detail.id(),
                detail.name(),
                spriteUrl,
                englishCategory(species),
                detail.weight() / 10.0,   // PokeAPI returns hectograms
                abilities);
    }

    private static Ability toAbility(AbilitySlot slot) {
        return new Ability(slot.ability().name(), slot.hidden());
    }

    private static String englishCategory(SpeciesResponse species) {
        return species.genera().stream()
                .filter(genus -> genus.language() != null && "en".equals(genus.language().name()))
                .map(Genus::genus)
                .findFirst()
                .orElse("Unknown");
    }
}