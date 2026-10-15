package com.pokedex.infrastructure.pokeapi;

import com.pokedex.application.exception.ExternalServiceException;
import com.pokedex.domain.model.Ability;
import com.pokedex.domain.model.Pokemon;
import com.pokedex.domain.model.PokemonPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests the PokeAPI adapter against canned JSON, without calling the real PokeAPI.
 */
class PokeApiPokemonCatalogTest {

    private static final String BASE_URL = "https://pokeapi.test/api/v2";

    private static final String LIST_JSON = """
            {
              "count": 1302,
              "next": "https://pokeapi.co/api/v2/pokemon?offset=1&limit=1",
              "previous": null,
              "results": [
                { "name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/" }
              ]
            }
            """;

    private static final String DETAIL_JSON = """
            {
              "id": 1,
              "name": "bulbasaur",
              "weight": 69,
              "sprites": { "front_default": "https://img.test/1.png" },
              "abilities": [
                { "ability": { "name": "overgrow" }, "is_hidden": false },
                { "ability": { "name": "chlorophyll" }, "is_hidden": true }
              ],
              "species": { "name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/" }
            }
            """;

    private static final String SPECIES_JSON = """
            {
              "genera": [
                { "genus": "Pokémon de semilla", "language": { "name": "es" } },
                { "genus": "Seed Pokémon", "language": { "name": "en" } }
              ]
            }
            """;

    private MockRestServiceServer server;
    private PokeApiPokemonCatalog catalog;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        catalog = new PokeApiPokemonCatalog(builder, BASE_URL);
    }

    @Test
    void fetchPageMapsPokeApiResponseToDomainModel() {
        server.expect(requestTo(BASE_URL + "/pokemon?offset=0&limit=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(LIST_JSON, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur"))
                .andRespond(withSuccess(DETAIL_JSON, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/pokemon-species/bulbasaur"))
                .andRespond(withSuccess(SPECIES_JSON, MediaType.APPLICATION_JSON));

        PokemonPage page = catalog.fetchPage(0, 1);

        assertThat(page.page()).isEqualTo(0);
        assertThat(page.size()).isEqualTo(1);
        assertThat(page.totalElements()).isEqualTo(1302);
        assertThat(page.totalPages()).isEqualTo(1302);

        Pokemon pokemon = page.content().get(0);
        assertThat(pokemon.id()).isEqualTo(1);
        assertThat(pokemon.name()).isEqualTo("bulbasaur");
        assertThat(pokemon.spriteUrl()).isEqualTo("https://img.test/1.png");
        assertThat(pokemon.category()).isEqualTo("Seed Pokémon");
        assertThat(pokemon.weightKg()).isEqualTo(6.9);
        assertThat(pokemon.abilities()).containsExactly(
                new Ability("overgrow", false),
                new Ability("chlorophyll", true));

        server.verify();
    }

    @Test
    void fetchPageWrapsPokeApiErrorsInExternalServiceException() {
        server.expect(requestTo(BASE_URL + "/pokemon?offset=0&limit=1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> catalog.fetchPage(0, 1))
                .isInstanceOf(ExternalServiceException.class);
    }
}