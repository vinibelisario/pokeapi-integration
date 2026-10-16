package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.DeleteLocalPokemonUseCase;
import com.pokeapi.application.port.in.QueryLocalPokemonUseCase;
import com.pokeapi.application.port.in.SyncPokemonUseCase;
import com.pokeapi.application.port.in.UpdateLocalPokemonUseCase;
import com.pokeapi.application.port.out.TokenGateway;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.LocalPokemonPage;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocalPokemonController.class)
@Import(SecurityConfig.class)
@WithMockUser
class LocalPokemonControllerTest {

    private static final LocalPokemon BULBASAUR = new LocalPokemon(
            1, "bulbasaur", "https://art.test/1.png", "A strange seed.",
            "Bulbizarre", "Kanto", List.of("starter"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenGateway tokenGateway;

    @MockitoBean
    private SyncPokemonUseCase syncPokemonUseCase;

    @MockitoBean
    private QueryLocalPokemonUseCase queryLocalPokemonUseCase;

    @MockitoBean
    private UpdateLocalPokemonUseCase updateLocalPokemonUseCase;

    @MockitoBean
    private DeleteLocalPokemonUseCase deleteLocalPokemonUseCase;

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

    @Test
    void updateLocalPokemonReturns200WithTheUpdatedCopy() throws Exception {
        LocalPokemon updated = new LocalPokemon(
                1, "bulbasaur", "https://art.test/1.png", "A strange seed.",
                "Bulbizarre", "Kanto", List.of("starter"));
        when(updateLocalPokemonUseCase.update(anyInt(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/local/pokemon/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"localizedName":"Bulbizarre","region":"Kanto","tags":["starter"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("Kanto"))
                .andExpect(jsonPath("$.tags[0]").value("starter"));
    }

    @Test
    void updateLocalPokemonReturns404WhenNotStored() throws Exception {
        when(updateLocalPokemonUseCase.update(anyInt(), any()))
                .thenThrow(new ResourceNotFoundException("Pokemon 99 not found"));

        mockMvc.perform(put("/api/local/pokemon/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"localizedName":"Bulbizarre","region":"Kanto","tags":[]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void updateLocalPokemonReturns400WhenValidationFails() throws Exception {
        when(updateLocalPokemonUseCase.update(anyInt(), any()))
                .thenThrow(new InvalidDataException("region must be at most 100 characters"));

        mockMvc.perform(put("/api/local/pokemon/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"localizedName":"Bulbizarre","region":"Kanto","tags":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid data"));
    }

    @Test
    void updateLocalPokemonReturns400ForMalformedJson() throws Exception {
        mockMvc.perform(put("/api/local/pokemon/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocalPokemonReturns204() throws Exception {
        mockMvc.perform(delete("/api/local/pokemon/{id}", 1))
                .andExpect(status().isNoContent());

        verify(deleteLocalPokemonUseCase).delete(1);
    }

    @Test
    void deleteLocalPokemonReturns404WhenNotStored() throws Exception {
        doThrow(new ResourceNotFoundException("Pokemon 99 not found"))
                .when(deleteLocalPokemonUseCase).delete(99);

        mockMvc.perform(delete("/api/local/pokemon/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }
}