package com.pokeapi.presentation.controller;

import com.pokeapi.application.exception.ExternalServiceException;
import com.pokeapi.application.port.in.GetPokemonDetailUseCase;
import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.application.port.out.TokenGateway;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.model.Ability;
import com.pokeapi.domain.model.Pokemon;
import com.pokeapi.domain.model.PokemonPage;
import com.pokeapi.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PokemonController.class)
@Import(SecurityConfig.class)
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListPokemonUseCase listPokemonUseCase;

    @MockitoBean
    private PokemonCatalogGateway pokemonCatalogGateway;

    @MockitoBean
    private PokemonDetailGateway pokemonDetailGateway;

    @MockitoBean
    private GetPokemonDetailUseCase getPokemonDetailUseCase;

    @MockitoBean
    private TokenGateway tokenGateway;

    @Test
    void listPokemonReturns200WithPageContent() throws Exception {
        Pokemon bulbasaur = new Pokemon(1, "bulbasaur", "https://img/1.png", "Seed Pokémon", 6.9,
                List.of(new Ability("overgrow", false)));
        when(listPokemonUseCase.listPokemon(0, 20))
                .thenReturn(new PokemonPage(List.of(bulbasaur), 0, 20, 1302, 66));

        mockMvc.perform(get("/api/pokemon").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.content[0].spriteUrl").value("https://img/1.png"))
                .andExpect(jsonPath("$.content[0].category").value("Seed Pokémon"))
                .andExpect(jsonPath("$.content[0].weightKg").value(6.9))
                .andExpect(jsonPath("$.content[0].abilities[0].name").value("overgrow"))
                .andExpect(jsonPath("$.content[0].abilities[0].hidden").value(false))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1302))
                .andExpect(jsonPath("$.totalPages").value(66));
    }

    @Test
    void listPokemonUsesPageZeroAndSizeTwentyWhenNotInformed() throws Exception {
        when(listPokemonUseCase.listPokemon(0, 20))
                .thenReturn(new PokemonPage(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk());

        verify(listPokemonUseCase).listPokemon(0, 20);
    }

    @Test
    void listPokemonReturns400WhenPageIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listPokemonReturns400WhenUseCaseRejectsSize() throws Exception {
        when(listPokemonUseCase.listPokemon(0, 200))
                .thenThrow(new InvalidDataException("size must be between 1 and 100"));

        mockMvc.perform(get("/api/pokemon").param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid data"))
                .andExpect(jsonPath("$.detail").value("size must be between 1 and 100"));
    }

    @Test
    void listPokemonReturns502WhenPokeApiFails() throws Exception {
        when(listPokemonUseCase.listPokemon(anyInt(), anyInt()))
                .thenThrow(new ExternalServiceException("PokeAPI unavailable", null));

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("External service error"));
    }
}