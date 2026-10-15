package com.pokeapi.application.usecase;

import com.pokeapi.application.exception.ExternalServiceException;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.model.Pokemon;
import com.pokeapi.domain.model.PokemonPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPokemonServiceTest {

    @Mock
    private PokemonCatalogGateway gateway;

    private ListPokemonService service;

    @BeforeEach
    void setUp() {
        service = new ListPokemonService(gateway);
    }

    @Test
    void listPokemonReturnsThePageProvidedByTheGateway() {
        Pokemon bulbasaur = new Pokemon(1, "bulbasaur", "sprite.png", "Seed Pokémon", 6.9, List.of());
        PokemonPage expected = new PokemonPage(List.of(bulbasaur), 0, 20, 1302, 66);
        when(gateway.fetchPage(0, 20)).thenReturn(expected);

        PokemonPage result = service.listPokemon(0, 20);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void listPokemonAcceptsTheMaximumSizeOfOneHundred() {
        PokemonPage expected = new PokemonPage(List.of(), 0, 100, 0, 0);
        when(gateway.fetchPage(0, 100)).thenReturn(expected);

        assertThat(service.listPokemon(0, 100)).isEqualTo(expected);
    }

    @Test
    void listPokemonRejectsNegativePage() {
        assertThatThrownBy(() -> service.listPokemon(-1, 20))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("page");

        verifyNoInteractions(gateway);
    }

    @Test
    void listPokemonRejectsSizeBelowOne() {
        assertThatThrownBy(() -> service.listPokemon(0, 0))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("size");

        verifyNoInteractions(gateway);
    }

    @Test
    void listPokemonRejectsSizeAboveOneHundred() {
        assertThatThrownBy(() -> service.listPokemon(0, 101))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("size");

        verifyNoInteractions(gateway);
    }

    @Test
    void listPokemonPropagatesExternalServiceFailure() {
        when(gateway.fetchPage(0, 20))
                .thenThrow(new ExternalServiceException("PokeAPI unavailable", null));

        assertThatThrownBy(() -> service.listPokemon(0, 20))
                .isInstanceOf(ExternalServiceException.class);
    }
}