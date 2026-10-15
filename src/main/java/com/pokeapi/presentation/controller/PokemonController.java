package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.GetPokemonDetailUseCase;
import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.presentation.dto.PokemonDetailResponse;
import com.pokeapi.presentation.dto.PokemonPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final ListPokemonUseCase listPokemonUseCase;
    private final GetPokemonDetailUseCase getPokemonDetailUseCase;

    public PokemonController(ListPokemonUseCase listPokemonUseCase,
                             GetPokemonDetailUseCase getPokemonDetailUseCase) {
        this.listPokemonUseCase = listPokemonUseCase;
        this.getPokemonDetailUseCase = getPokemonDetailUseCase;
    }

    @GetMapping
    public PokemonPageResponse listPokemon(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return PokemonPageResponse.from(listPokemonUseCase.listPokemon(page, size));
    }

    @GetMapping("/{idOrName}")
    public PokemonDetailResponse getPokemonDetail(@PathVariable("idOrName") String idOrName) {
        return PokemonDetailResponse.from(getPokemonDetailUseCase.getPokemonDetail(idOrName));
    }
}