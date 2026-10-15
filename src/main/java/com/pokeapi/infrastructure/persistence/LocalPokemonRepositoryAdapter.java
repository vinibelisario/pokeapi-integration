package com.pokeapi.infrastructure.persistence;

import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.domain.model.LocalPokemon;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class LocalPokemonRepositoryAdapter implements LocalPokemonRepository {

    private final SpringDataLocalPokemonRepository jpaRepository;

    public LocalPokemonRepositoryAdapter(SpringDataLocalPokemonRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LocalPokemon> findById(int id) {
        return jpaRepository.findById(id).map(LocalPokemonRepositoryAdapter::toDomain);
    }

    @Override
    public LocalPokemon save(LocalPokemon pokemon) {
        return toDomain(jpaRepository.save(toEntity(pokemon)));
    }

    @Override
    public List<LocalPokemon> findPage(int page, int size) {
        return jpaRepository.findAll(PageRequest.of(page, size, Sort.by("id")))
                .map(LocalPokemonRepositoryAdapter::toDomain)
                .getContent();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    private static LocalPokemonEntity toEntity(LocalPokemon pokemon) {
        String tags = pokemon.tags().isEmpty() ? null : String.join(",", pokemon.tags());
        return new LocalPokemonEntity(
                pokemon.id(),
                pokemon.name(),
                pokemon.imageUrl(),
                pokemon.description(),
                pokemon.localizedName(),
                pokemon.region(),
                tags);
    }

    private static LocalPokemon toDomain(LocalPokemonEntity entity) {
        return new LocalPokemon(
                entity.getId(),
                entity.getName(),
                entity.getImageUrl(),
                entity.getDescription(),
                entity.getLocalizedName(),
                entity.getRegion(),
                parseTags(entity.getInternalTags()));
    }

    private static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }
}