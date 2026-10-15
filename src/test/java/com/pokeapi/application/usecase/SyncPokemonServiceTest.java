package com.pokeapi.application.usecase;

import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.EvolutionNode;
import com.pokeapi.domain.model.LocalPokemon;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncPokemonServiceTest {

    private static final PokemonDetail BULBASAUR_FROM_POKEAPI = new PokemonDetail(
            1,
            "bulbasaur",
            "https://art.test/1.png",
            List.of(new Stat("hp", 45)),
            "A strange seed was planted on its back at birth.",
            new EvolutionNode(1, "bulbasaur", List.of()));

    @Mock
    private PokemonDetailGateway pokemonDetailGateway;

    @Mock
    private LocalPokemonRepository repository;

    private SyncPokemonService service;

    @BeforeEach
    void setUp() {
        service = new SyncPokemonService(pokemonDetailGateway, repository);
    }

    @Test
    void syncPokemonCreatesLocalCopyWhenNotStoredYet() {
        when(pokemonDetailGateway.fetchDetail("bulbasaur")).thenReturn(Optional.of(BULBASAUR_FROM_POKEAPI));
        when(repository.findById(1)).thenReturn(Optional.empty());
        when(repository.save(any(LocalPokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon result = service.syncPokemon("bulbasaur");

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.imageUrl()).isEqualTo("https://art.test/1.png");
        assertThat(result.description()).isEqualTo("A strange seed was planted on its back at birth.");
        assertThat(result.localizedName()).isNull();
        assertThat(result.tags()).isEmpty();
        verify(repository).save(result);
    }

    @Test
    void syncPokemonUpdatesPokeApiFieldsAndKeepsProprietaryFields() {
        LocalPokemon stored = new LocalPokemon(
                1, "bulbasaur", "old.png", "old description",
                "Bulbizarre", "Kanto", List.of("starter"));
        when(pokemonDetailGateway.fetchDetail("bulbasaur")).thenReturn(Optional.of(BULBASAUR_FROM_POKEAPI));
        when(repository.findById(1)).thenReturn(Optional.of(stored));
        when(repository.save(any(LocalPokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon result = service.syncPokemon("bulbasaur");

        assertThat(result.imageUrl()).isEqualTo("https://art.test/1.png");
        assertThat(result.description()).isEqualTo("A strange seed was planted on its back at birth.");
        assertThat(result.localizedName()).isEqualTo("Bulbizarre");
        assertThat(result.region()).isEqualTo("Kanto");
        assertThat(result.tags()).containsExactly("starter");
    }

    @Test
    void syncPokemonThrowsNotFoundWhenPokeApiDoesNotHaveIt() {
        when(pokemonDetailGateway.fetchDetail("missingno")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.syncPokemon("missingno"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missingno");

        verify(repository, never()).save(any(LocalPokemon.class));
    }
}