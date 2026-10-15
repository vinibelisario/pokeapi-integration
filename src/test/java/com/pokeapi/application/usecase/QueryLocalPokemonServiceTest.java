package com.pokeapi.application.usecase;

import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.LocalPokemonPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryLocalPokemonServiceTest {

    private static final LocalPokemon BULBASAUR = new LocalPokemon(
            1, "bulbasaur", "https://art.test/1.png", "A strange seed.",
            "Bulbizarre", "Kanto", List.of("starter"));

    @Mock
    private LocalPokemonRepository repository;

    private QueryLocalPokemonService service;

    @BeforeEach
    void setUp() {
        service = new QueryLocalPokemonService(repository);
    }

    @Test
    void getByIdReturnsTheStoredPokemon() {
        when(repository.findById(1)).thenReturn(Optional.of(BULBASAUR));

        assertThat(service.getById(1)).isEqualTo(BULBASAUR);
    }

    @Test
    void getByIdThrowsNotFoundWhenNotStored() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listReturnsPageFromRepository() {
        when(repository.findPage(0, 20)).thenReturn(List.of(BULBASAUR));
        when(repository.count()).thenReturn(1L);

        LocalPokemonPage page = service.list(0, 20);

        assertThat(page.content()).containsExactly(BULBASAUR);
        assertThat(page.page()).isEqualTo(0);
        assertThat(page.size()).isEqualTo(20);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void listRejectsInvalidPageOrSize() {
        assertThatThrownBy(() -> service.list(-1, 20)).isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> service.list(0, 0)).isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> service.list(0, 101)).isInstanceOf(InvalidDataException.class);

        verifyNoInteractions(repository);
    }
}