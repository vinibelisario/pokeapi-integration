package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.DeleteLocalPokemonUseCase;
import com.pokeapi.application.port.in.QueryLocalPokemonUseCase;
import com.pokeapi.application.port.in.SyncPokemonUseCase;
import com.pokeapi.application.port.in.UpdateLocalPokemonCommand;
import com.pokeapi.application.port.in.UpdateLocalPokemonUseCase;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.presentation.dto.LocalPokemonPageResponse;
import com.pokeapi.presentation.dto.LocalPokemonResponse;
import com.pokeapi.presentation.dto.UpdateLocalPokemonRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/local/pokemon")
public class LocalPokemonController {

    private final SyncPokemonUseCase syncPokemonUseCase;
    private final QueryLocalPokemonUseCase queryLocalPokemonUseCase;
    private final UpdateLocalPokemonUseCase updateLocalPokemonUseCase;
    private final DeleteLocalPokemonUseCase deleteLocalPokemonUseCase;

    public LocalPokemonController(SyncPokemonUseCase syncPokemonUseCase,
                                  QueryLocalPokemonUseCase queryLocalPokemonUseCase,
                                  UpdateLocalPokemonUseCase updateLocalPokemonUseCase,
                                  DeleteLocalPokemonUseCase deleteLocalPokemonUseCase) {
        this.syncPokemonUseCase = syncPokemonUseCase;
        this.queryLocalPokemonUseCase = queryLocalPokemonUseCase;
        this.updateLocalPokemonUseCase = updateLocalPokemonUseCase;
        this.deleteLocalPokemonUseCase = deleteLocalPokemonUseCase;
    }

    @PostMapping("/sync/{idOrName}")
    public LocalPokemonResponse sync(@PathVariable String idOrName) {
        return LocalPokemonResponse.from(syncPokemonUseCase.syncPokemon(idOrName));
    }

    @GetMapping
    public LocalPokemonPageResponse list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return LocalPokemonPageResponse.from(queryLocalPokemonUseCase.list(page, size));
    }

    @GetMapping("/{id}")
    public LocalPokemonResponse getById(@PathVariable int id) {
        return LocalPokemonResponse.from(queryLocalPokemonUseCase.getById(id));
    }

    @PutMapping("/{id}")
    public LocalPokemonResponse update(@PathVariable int id, @RequestBody UpdateLocalPokemonRequest request) {
        LocalPokemon updated = updateLocalPokemonUseCase.update(id,
                new UpdateLocalPokemonCommand(request.localizedName(), request.region(), request.tags()));
        return LocalPokemonResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        deleteLocalPokemonUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}