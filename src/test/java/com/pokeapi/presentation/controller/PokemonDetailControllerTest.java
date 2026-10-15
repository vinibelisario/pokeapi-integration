package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.GetPokemonDetailUseCase;
import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.EvolutionNode;
import com.pokeapi.domain.model.PokemonDetail;
import com.pokeapi.domain.model.Stat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PokemonController.class)
class PokemonDetailControllerTest {

    private static final PokemonDetail BULBASAUR = new PokemonDetail(
            1,
            "bulbasaur",
            "https://art.test/1.png",
            List.of(new Stat("hp", 45)),
            "A strange seed was planted on its back at birth.",
            new EvolutionNode(1, "bulbasaur", List.of(
                    new EvolutionNode(2, "ivysaur", List.of()))));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetPokemonDetailUseCase getPokemonDetailUseCase;

    // The test context loads every use case bean, so their gateways must exist too.
    @MockitoBean
    private ListPokemonUseCase listPokemonUseCase;

    @MockitoBean
    private PokemonCatalogGateway pokemonCatalogGateway;

    @MockitoBean
    private PokemonDetailGateway pokemonDetailGateway;

    @Test
    void getPokemonDetailReturns200WithDetails() throws Exception {
        when(getPokemonDetailUseCase.getPokemonDetail("bulbasaur")).thenReturn(BULBASAUR);

        mockMvc.perform(get("/api/pokemon/{idOrName}", "bulbasaur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("bulbasaur"))
                .andExpect(jsonPath("$.imageUrl").value("https://art.test/1.png"))
                .andExpect(jsonPath("$.description").value("A strange seed was planted on its back at birth."))
                .andExpect(jsonPath("$.stats[0].name").value("hp"))
                .andExpect(jsonPath("$.stats[0].baseValue").value(45))
                .andExpect(jsonPath("$.evolutionChain.name").value("bulbasaur"))
                .andExpect(jsonPath("$.evolutionChain.evolvesTo[0].name").value("ivysaur"));
    }

    @Test
    void getPokemonDetailReturns404WhenPokemonDoesNotExist() throws Exception {
        when(getPokemonDetailUseCase.getPokemonDetail("missingno"))
                .thenThrow(new ResourceNotFoundException("Pokemon missingno not found"));

        mockMvc.perform(get("/api/pokemon/{idOrName}", "missingno"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void getPokemonDetailReturns400ForInvalidIdentifier() throws Exception {
        when(getPokemonDetailUseCase.getPokemonDetail("bad_name"))
                .thenThrow(new InvalidDataException("identifier may only contain letters, digits and hyphens"));

        mockMvc.perform(get("/api/pokemon/{idOrName}", "bad_name"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid data"));
    }
}