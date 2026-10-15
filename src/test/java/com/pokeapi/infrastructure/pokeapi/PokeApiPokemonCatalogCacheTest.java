package com.pokeapi.infrastructure.pokeapi;

import com.pokeapi.application.exception.ExternalServiceException;
import com.pokeapi.domain.model.PokemonPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringJUnitConfig(PokeApiPokemonCatalogCacheTest.TestConfig.class)
class PokeApiPokemonCatalogCacheTest {

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
                { "ability": { "name": "overgrow" }, "is_hidden": false }
              ],
              "species": { "name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/" }
            }
            """;

    private static final String SPECIES_JSON = """
            {
              "genera": [
                { "genus": "Seed Pokémon", "language": { "name": "en" } }
              ]
            }
            """;

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    static class TestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        MockRestServiceServer mockRestServiceServer(RestClient.Builder restClientBuilder) {
            return MockRestServiceServer.bindTo(restClientBuilder).build();
        }

        @Bean
        PokeApiPokemonCatalog pokeApiPokemonCatalog(RestClient.Builder restClientBuilder,
                                                    MockRestServiceServer mockRestServiceServer) {
            return new PokeApiPokemonCatalog(restClientBuilder, BASE_URL);
        }

        @Bean
        org.springframework.cache.CacheManager cacheManager() {
            return new CaffeineCacheManager("pokemonPages");
        }
    }

    @Autowired
    private PokeApiPokemonCatalog catalog;

    @Autowired
    private MockRestServiceServer server;

    @BeforeEach
    void resetServer() {
        server.reset();
    }

    @Test
    void fetchPageCallsPokeApiOnlyOnceForRepeatedRequests() {
        expectBulbasaurPage();

        PokemonPage first = catalog.fetchPage(0, 1);
        PokemonPage second = catalog.fetchPage(0, 1);

        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @Test
    void fetchPageDoesNotCacheFailures() {
        server.expect(once(), requestTo(BASE_URL + "/pokemon?offset=0&limit=1"))
                .andRespond(withServerError());
        expectBulbasaurPage();

        assertThatThrownBy(() -> catalog.fetchPage(0, 1))
                .isInstanceOf(ExternalServiceException.class);

        PokemonPage page = catalog.fetchPage(0, 1);

        assertThat(page.content()).hasSize(1);
        server.verify();
    }

    private void expectBulbasaurPage() {
        server.expect(once(), requestTo(BASE_URL + "/pokemon?offset=0&limit=1"))
                .andRespond(withSuccess(LIST_JSON, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(BASE_URL + "/pokemon/bulbasaur"))
                .andRespond(withSuccess(DETAIL_JSON, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(BASE_URL + "/pokemon-species/bulbasaur"))
                .andRespond(withSuccess(SPECIES_JSON, MediaType.APPLICATION_JSON));
    }
}