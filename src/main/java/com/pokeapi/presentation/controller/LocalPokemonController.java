package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.QueryLocalPokemonUseCase;
import com.pokeapi.application.port.in.SyncPokemonUseCase;
import com.pokeapi.presentation.dto.LocalPokemonPageResponse;
import com.pokeapi.presentation.dto.LocalPokemonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/local/pokemon")
public class LocalPokemonController {

    private final SyncPokemonUseCase syncPokemonUseCase;
    private final QueryLocalPokemonUseCase queryLocalPokemonUseCase;

    public LocalPokemonController(SyncPokemonUseCase syncPokemonUseCase,
                                  QueryLocalPokemonUseCase queryLocalPokemonUseCase) {
        this.syncPokemonUseCase = syncPokemonUseCase;
        this.queryLocalPokemonUseCase = queryLocalPokemonUseCase;
    }

    @PostMapping("/sync/{idOrName}")
    public LocalPokemonResponse syncPokemon(@PathVariable("idOrName") String idOrName) {
        return LocalPokemonResponse.from(syncPokemonUseCase.syncPokemon(idOrName));
    }

    @GetMapping
    public LocalPokemonPageResponse listLocalPokemon(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return LocalPokemonPageResponse.from(queryLocalPokemonUseCase.list(page, size));
    }

    @GetMapping("/{id}")
    public LocalPokemonResponse getLocalPokemon(@PathVariable("id") int id) {
        return LocalPokemonResponse.from(queryLocalPokemonUseCase.getById(id));
    }
}