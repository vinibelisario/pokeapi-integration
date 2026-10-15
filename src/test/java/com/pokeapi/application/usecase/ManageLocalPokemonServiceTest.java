package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.UpdateLocalPokemonCommand;
import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.LocalPokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageLocalPokemonServiceTest {

    private static final LocalPokemon STORED = new LocalPokemon(
            1, "bulbasaur", "https://art.test/1.png", "A strange seed.",
            "Bulbizarre", "Kanto", List.of("starter"));

    @Mock
    private LocalPokemonRepository repository;

    private ManageLocalPokemonService service;

    @BeforeEach
    void setUp() {
        service = new ManageLocalPokemonService(repository);
    }

    @Test
    void updateReplacesEditableFieldsAndKeepsPokeApiFields() {
        when(repository.findById(1)).thenReturn(Optional.of(STORED));
        when(repository.save(any(LocalPokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon result = service.update(1,
                new UpdateLocalPokemonCommand("Bulbasaur FR", "Johto", List.of("grass", "poison")));

        assertThat(result.localizedName()).isEqualTo("Bulbasaur FR");
        assertThat(result.region()).isEqualTo("Johto");
        assertThat(result.tags()).containsExactly("grass", "poison");
        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.imageUrl()).isEqualTo("https://art.test/1.png");
        assertThat(result.description()).isEqualTo("A strange seed.");
    }

    @Test
    void updateThrowsNotFoundWhenNotStored() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99, validCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(repository, never()).save(any(LocalPokemon.class));
    }

    @Test
    void updateAllowsClearingOptionalFields() {
        when(repository.findById(1)).thenReturn(Optional.of(STORED));
        when(repository.save(any(LocalPokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon result = service.update(1, new UpdateLocalPokemonCommand(null, null, List.of()));

        assertThat(result.localizedName()).isNull();
        assertThat(result.region()).isNull();
        assertThat(result.tags()).isEmpty();
    }

    @Test
    void updateRejectsBlankLocalizedName() {
        assertRejected(new UpdateLocalPokemonCommand("   ", "Kanto", List.of()), "localizedName");
    }

    @Test
    void updateRejectsRegionLongerThanOneHundredCharacters() {
        assertRejected(new UpdateLocalPokemonCommand(null, "a".repeat(101), List.of()), "region");
    }

    @Test
    void updateRejectsMoreThanTwentyTags() {
        List<String> tags = IntStream.range(0, 21).mapToObj(i -> "tag" + i).toList();

        assertRejected(new UpdateLocalPokemonCommand(null, null, tags), "tags");
    }

    @Test
    void updateRejectsTagsWithInvalidFormat() {
        assertRejected(new UpdateLocalPokemonCommand(null, null, List.of("Fire Type!")), "tags");
    }

    @Test
    void deleteRemovesTheStoredPokemon() {
        when(repository.findById(1)).thenReturn(Optional.of(STORED));

        service.delete(1);

        verify(repository).deleteById(1);
    }

    @Test
    void deleteThrowsNotFoundWhenNotStored() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(anyInt());
    }

    private void assertRejected(UpdateLocalPokemonCommand command, String fieldName) {
        assertThatThrownBy(() -> service.update(1, command))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining(fieldName);

        verifyNoInteractions(repository);
    }

    private static UpdateLocalPokemonCommand validCommand() {
        return new UpdateLocalPokemonCommand("Bulbizarre", "Kanto", List.of("starter"));
    }
}