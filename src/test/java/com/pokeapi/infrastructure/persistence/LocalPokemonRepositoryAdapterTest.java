package com.pokeapi.infrastructure.persistence;

import com.pokeapi.domain.model.LocalPokemon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(LocalPokemonRepositoryAdapter.class)
class LocalPokemonRepositoryAdapterTest {

    @Autowired
    private LocalPokemonRepositoryAdapter adapter;

    @Test
    void saveAndFindByIdKeepsEveryField() {
        LocalPokemon pokemon = new LocalPokemon(
                1, "bulbasaur", "https://art.test/1.png", "A strange seed.",
                "Bulbizarre", "Kanto", List.of("starter", "grass"));

        adapter.save(pokemon);

        assertThat(adapter.findById(1)).contains(pokemon);
    }

    @Test
    void saveKeepsEmptyProprietaryFields() {
        LocalPokemon pokemon = new LocalPokemon(
                25, "pikachu", "https://art.test/25.png", "Mouse Pokemon.",
                null, null, List.of());

        adapter.save(pokemon);

        assertThat(adapter.findById(25)).contains(pokemon);
    }

    @Test
    void findByIdReturnsEmptyWhenNotStored() {
        assertThat(adapter.findById(999)).isEmpty();
    }

    @Test
    void findPageReturnsRowsOrderedByIdAndCountsAll() {
        adapter.save(pokemon(3, "venusaur"));
        adapter.save(pokemon(1, "bulbasaur"));
        adapter.save(pokemon(2, "ivysaur"));

        assertThat(adapter.findPage(0, 2))
                .extracting(LocalPokemon::id)
                .containsExactly(1, 2);
        assertThat(adapter.count()).isEqualTo(3);
    }

    @Test
    void deleteByIdRemovesTheRow() {
        adapter.save(pokemon(1, "bulbasaur"));

        adapter.deleteById(1);

        assertThat(adapter.findById(1)).isEmpty();
    }

    private static LocalPokemon pokemon(int id, String name) {
        return new LocalPokemon(id, name, null, null, null, null, List.of());
    }
}