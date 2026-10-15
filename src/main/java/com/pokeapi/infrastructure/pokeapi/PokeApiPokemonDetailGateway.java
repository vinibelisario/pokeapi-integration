package com.pokeapi.infrastructure.pokeapi;

import com.pokeapi.application.exception.ExternalServiceException;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.domain.model.EvolutionNode;
import com.pokeapi.domain.model.PokemonDetail;
import com.pokeapi.domain.model.Stat;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.ChainLink;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.EvolutionChainResponse;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.FlavorText;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.Sprites;
import com.pokeapi.infrastructure.pokeapi.PokeApiModels.SpeciesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class PokeApiPokemonDetailGateway implements PokemonDetailGateway {

    private final RestClient restClient;

    public PokeApiPokemonDetailGateway(RestClient.Builder builder,
                                       @Value("${pokeapi.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<PokemonDetail> fetchDetail(String idOrName) {
        try {
            PokeApiModels.PokemonDetail pokemon = restClient.get()
                    .uri("/pokemon/{idOrName}", idOrName)
                    .retrieve()
                    .body(PokeApiModels.PokemonDetail.class);

            SpeciesResponse species = restClient.get()
                    .uri("/pokemon-species/{name}", pokemon.species().name())
                    .retrieve()
                    .body(SpeciesResponse.class);

            EvolutionChainResponse chain = restClient.get()
                    .uri("/evolution-chain/{id}", idFromUrl(species.evolutionChain().url()))
                    .retrieve()
                    .body(EvolutionChainResponse.class);

            return Optional.of(toDomain(pokemon, species, chain));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ExternalServiceException("Failed to fetch Pokemon details from PokeAPI", e);
        }
    }

    private static PokemonDetail toDomain(PokeApiModels.PokemonDetail pokemon,
                                          SpeciesResponse species,
                                          EvolutionChainResponse chain) {
        return new PokemonDetail(
                pokemon.id(),
                pokemon.name(),
                imageUrl(pokemon.sprites()),
                pokemon.stats().stream()
                        .map(slot -> new Stat(slot.stat().name(), slot.baseStat()))
                        .toList(),
                englishDescription(species),
                toNode(chain.chain()));
    }

    private static String imageUrl(Sprites sprites) {
        if (sprites == null) {
            return null;
        }
        if (sprites.other() != null
                && sprites.other().officialArtwork() != null
                && sprites.other().officialArtwork().frontDefault() != null) {
            return sprites.other().officialArtwork().frontDefault();
        }
        return sprites.frontDefault();
    }

    private static String englishDescription(SpeciesResponse species) {
        return species.flavorTextEntries().stream()
                .filter(entry -> entry.language() != null && "en".equals(entry.language().name()))
                .map(FlavorText::flavorText)
                .findFirst()
                .map(text -> text.replaceAll("\\s+", " ").trim())
                .orElse(null);
    }

    private static EvolutionNode toNode(ChainLink link) {
        return new EvolutionNode(
                idFromUrl(link.species().url()),
                link.species().name(),
                link.evolvesTo().stream()
                        .map(PokeApiPokemonDetailGateway::toNode)
                        .toList());
    }

    private static int idFromUrl(String url) {
        String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }
}