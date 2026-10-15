package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.presentation.dto.PokemonPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final ListPokemonUseCase listPokemonUseCase;

    public PokemonController(ListPokemonUseCase listPokemonUseCase) {
        this.listPokemonUseCase = listPokemonUseCase;
    }

    @GetMapping
    public PokemonPageResponse listPokemon(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return PokemonPageResponse.from(listPokemonUseCase.listPokemon(page, size));
    }
}