package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.DeleteLocalPokemonUseCase;
import com.pokeapi.application.port.in.QueryLocalPokemonUseCase;
import com.pokeapi.application.port.in.SyncPokemonUseCase;
import com.pokeapi.application.port.in.UpdateLocalPokemonUseCase;
import com.pokeapi.application.port.out.TokenGateway;
import com.pokeapi.domain.model.LocalPokemonPage;
import com.pokeapi.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocalPokemonController.class)
@Import(SecurityConfig.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SyncPokemonUseCase syncUseCase;

    @MockitoBean
    private QueryLocalPokemonUseCase queryUseCase;

    @MockitoBean
    private UpdateLocalPokemonUseCase updateUseCase;

    @MockitoBean
    private DeleteLocalPokemonUseCase deleteUseCase;

    @MockitoBean
    private TokenGateway tokenGateway;

    @Test
    void protectedRouteWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/local/pokemon"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteWithInvalidTokenReturns401() throws Exception {
        when(tokenGateway.validate("bad")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/local/pokemon").header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteWithValidTokenReturns200() throws Exception {
        when(tokenGateway.validate("good")).thenReturn(Optional.of("alice"));
        when(queryUseCase.list(0, 20)).thenReturn(new LocalPokemonPage(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/local/pokemon").header("Authorization", "Bearer good"))
                .andExpect(status().isOk());
    }
}