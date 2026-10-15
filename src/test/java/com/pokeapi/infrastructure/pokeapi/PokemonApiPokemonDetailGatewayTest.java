package com.pokeapi.infrastructure.pokeapi;

import com.pokeapi.domain.model.EvolutionNode;
import com.pokeapi.domain.model.PokemonDetail;
import com.pokeapi.domain.model.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PokeApiPokemonDetailGatewayTest {

    private static final String BASE_URL = "https://pokeapi.test/api/v2";

    private static final String DETAIL_JSON = """
            {
              "id": 1,
              "name": "bulbasaur",
              "weight": 69,
              "sprites": {
                "front_default": "https://img.test/1.png",
                "other": { "official-artwork": { "front_default": "https://art.test/1.png" } }
              },
              "abilities": [
                { "ability": { "name": "overgrow" }, "is_hidden": false }
              ],
              "stats": [
                { "base_stat": 45, "stat": { "name": "hp" } },
                { "base_stat": 49, "stat": { "name": "attack" } }
              ],
              "species": { "name": "bulbasaur", "url": "https://pokeapi.test/api/v2/pokemon-species/1/" }
            }
            """;

    private static final String SPECIES_JSON = """
            {
              "flavor_text_entries": [
                { "flavor_text": "Una semilla extraña.", "language": { "name": "es" } },
                { "flavor_text": "A strange seed was\\nplanted on its back at\\fbirth.", "language": { "name": "en" } }
              ],
              "evolution_chain": { "url": "https://pokeapi.test/api/v2/evolution-chain/1/" }
            }
            """;

    private static final String EVOLUTION_JSON = """
            {
              "id": 1,
              "chain": {
                "species": { "name": "bulbasaur", "url": "https://pokeapi.test/api/v2/pokemon-species/1/" },
                "evolves_to": [
                  {
                    "species": { "name": "ivysaur", "url": "https://pokeapi.test/api/v2/pokemon-species/2/" },
                    "evolves_to": [
                      {
                        "species": { "name": "venusaur", "url": "https://pokeapi.test/api/v2/pokemon-species/3/" },
                        "evolves_to": []
                      }
                    ]
                  }
                ]
              }
            }
            """;

    private MockRestServiceServer server;
    private PokeApiPokemonDetailGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new PokeApiPokemonDetailGateway(builder, BASE_URL);
    }

    @Test
    void fetchDetailMapsPokemonSpeciesAndEvolutionChain() {
        server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(DETAIL_JSON, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/pokemon-species/bulbasaur"))
                .andRespond(withSuccess(SPECIES_JSON, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/evolution-chain/1"))
                .andRespond(withSuccess(EVOLUTION_JSON, MediaType.APPLICATION_JSON));

        PokemonDetail detail = gateway.fetchDetail("bulbasaur").orElseThrow();

        assertThat(detail.id()).isEqualTo(1);
        assertThat(detail.name()).isEqualTo("bulbasaur");
        assertThat(detail.imageUrl()).isEqualTo("https://art.test/1.png");
        assertThat(detail.stats()).containsExactly(new Stat("hp", 45), new Stat("attack", 49));
        assertThat(detail.description()).isEqualTo("A strange seed was planted on its back at birth.");
        assertThat(detail.evolutionChain()).isEqualTo(
                new EvolutionNode(1, "bulbasaur", List.of(
                        new EvolutionNode(2, "ivysaur", List.of(
                                new EvolutionNode(3, "venusaur", List.of()))))));

        server.verify();
    }

    @Test
    void fetchDetailReturnsEmptyWhenPokemonDoesNotExist() {
        server.expect(requestTo(BASE_URL + "/pokemon/missingno"))
                .andRespond(withResourceNotFound());

        assertThat(gateway.fetchDetail("missingno")).isEmpty();

        server.verify();
    }
}