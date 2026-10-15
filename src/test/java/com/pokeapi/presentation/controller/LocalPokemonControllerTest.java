package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.QueryLocalPokemonUseCase;
import com.pokeapi.application.port.in.SyncPokemonUseCase;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.LocalPokemonPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocalPokemonController.class)
class LocalPokemonControllerTest {

    private static final LocalPokemon BULBASAUR = new LocalPokemon(
            1, "bulbasaur", "https://art.test/1.png", "A strange seed.",
            "Bulbizarre", "Kanto", List.of("starter"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SyncPokemonUseCase syncPokemonUseCase;

    @MockitoBean
    private QueryLocalPokemonUseCase queryLocalPokemonUseCase;

    @Test
    void syncPokemonReturns200WithTheStoredCopy() throws Exception {
        when(syncPokemonUseCase.syncPokemon("bulbasaur")).thenReturn(BULBASAUR);

        mockMvc.perform(post("/api/local/pokemon/sync/{idOrName}", "bulbasaur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("bulbasaur"))
                .andExpect(jsonPath("$.localizedName").value("Bulbizarre"))
                .andExpect(jsonPath("$.region").value("Kanto"))
                .andExpect(jsonPath("$.tags[0]").value("starter"));
    }

    @Test
    void syncPokemonReturns404WhenPokeApiDoesNotHaveIt() throws Exception {
        when(syncPokemonUseCase.syncPokemon("missingno"))
                .thenThrow(new ResourceNotFoundException("Pokemon missingno not found"));

        mockMvc.perform(post("/api/local/pokemon/sync/{idOrName}", "missingno"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void getLocalPokemonReturns200() throws Exception {
        when(queryLocalPokemonUseCase.getById(1)).thenReturn(BULBASAUR);

        mockMvc.perform(get("/api/local/pokemon/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bulbasaur"));
    }

    @Test
    void getLocalPokemonReturns404WhenNotStored() throws Exception {
        when(queryLocalPokemonUseCase.getById(99))
                .thenThrow(new ResourceNotFoundException("Pokemon 99 not found"));

        mockMvc.perform(get("/api/local/pokemon/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void listLocalPokemonReturns200WithPage() throws Exception {
        when(queryLocalPokemonUseCase.list(0, 20))
                .thenReturn(new LocalPokemonPage(List.of(BULBASAUR), 0, 20, 1, 1));

        mockMvc.perform(get("/api/local/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}