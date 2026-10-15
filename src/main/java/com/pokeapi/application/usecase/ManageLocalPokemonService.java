package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.DeleteLocalPokemonUseCase;
import com.pokeapi.application.port.in.UpdateLocalPokemonCommand;
import com.pokeapi.application.port.in.UpdateLocalPokemonUseCase;
import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.exception.ResourceNotFoundException;
import com.pokeapi.domain.model.LocalPokemon;

import java.util.List;
import java.util.regex.Pattern;

public class ManageLocalPokemonService implements UpdateLocalPokemonUseCase, DeleteLocalPokemonUseCase {

    private static final int MAX_TEXT_LENGTH = 100;
    private static final int MAX_TAGS = 20;
    private static final int MAX_TAG_LENGTH = 20;
    private static final Pattern TAG_PATTERN = Pattern.compile("[a-z0-9-]+");

    private final LocalPokemonRepository repository;

    public ManageLocalPokemonService(LocalPokemonRepository repository) {
        this.repository = repository;
    }

    @Override
    public LocalPokemon update(int id, UpdateLocalPokemonCommand command) {
        List<String> tags = command.tags() == null ? List.of() : command.tags();
        validate(command.localizedName(), command.region(), tags);

        LocalPokemon stored = find(id);
        LocalPokemon updated = new LocalPokemon(
                stored.id(), stored.name(), stored.imageUrl(), stored.description(),
                command.localizedName(), command.region(), tags);
        return repository.save(updated);
    }

    @Override
    public void delete(int id) {
        find(id);
        repository.deleteById(id);
    }

    private LocalPokemon find(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pokemon " + id + " not found"));
    }

    private static void validate(String localizedName, String region, List<String> tags) {
        validateText("localizedName", localizedName);
        validateText("region", region);

        if (tags.size() > MAX_TAGS) {
            throw new InvalidDataException("tags must contain at most " + MAX_TAGS + " items");
        }
        for (String tag : tags) {
            if (tag == null || tag.length() > MAX_TAG_LENGTH || !TAG_PATTERN.matcher(tag).matches()) {
                throw new InvalidDataException("tags may only contain lowercase letters, digits and hyphens"
                        + " (max " + MAX_TAG_LENGTH + " characters each)");
            }
        }
    }

    private static void validateText(String field, String value) {
        if (value == null) {
            return; // null clears the field
        }
        if (value.isBlank()) {
            throw new InvalidDataException(field + " must not be blank");
        }
        if (value.length() > MAX_TEXT_LENGTH) {
            throw new InvalidDataException(field + " must be at most " + MAX_TEXT_LENGTH + " characters");
        }
    }
}