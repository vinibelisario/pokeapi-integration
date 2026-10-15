package com.pokeapi.application.usecase;

import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.EvolutionNode;
import com.pokeapi.domain.model.PokemonDetail;
import com.pokeapi.domain.model.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPokemonDetailServiceTest {

    private static final PokemonDetail BULBASAUR = new PokemonDetail(
            1,
            "bulbasaur",
            "https://art.test/1.png",
            List.of(new Stat("hp", 45)),
            "A strange seed was planted on its back at birth.",
            new EvolutionNode(1, "bulbasaur", List.of()));

    @Mock
    private PokemonDetailGateway gateway;

    private GetPokemonDetailService service;

    @BeforeEach
    void setUp() {
        service = new GetPokemonDetailService(gateway);
    }

    @Test
    void getPokemonDetailReturnsTheDetailProvidedByTheGateway() {
        when(gateway.fetchDetail("bulbasaur")).thenReturn(Optional.of(BULBASAUR));

        assertThat(service.getPokemonDetail("bulbasaur")).isEqualTo(BULBASAUR);
    }

    @Test
    void getPokemonDetailAcceptsANumericId() {
        when(gateway.fetchDetail("1")).thenReturn(Optional.of(BULBASAUR));

        assertThat(service.getPokemonDetail("1")).isEqualTo(BULBASAUR);
    }

    @Test
    void getPokemonDetailNormalizesNameToLowerCaseAndTrims() {
        when(gateway.fetchDetail("bulbasaur")).thenReturn(Optional.of(BULBASAUR));

        service.getPokemonDetail("  Bulbasaur ");

        verify(gateway).fetchDetail("bulbasaur");
    }

    @Test
    void getPokemonDetailThrowsNotFoundWhenThePokemonDoesNotExist() {
        when(gateway.fetchDetail("missingno")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPokemonDetail("missingno"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missingno");
    }

    @Test
    void getPokemonDetailRejectsBlankIdentifier() {
        assertThatThrownBy(() -> service.getPokemonDetail("   "))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("identifier");

        verifyNoInteractions(gateway);
    }

    @Test
    void getPokemonDetailRejectsIdentifierWithInvalidCharacters() {
        assertThatThrownBy(() -> service.getPokemonDetail("bulba saur!"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("identifier");

        verifyNoInteractions(gateway);
    }
}