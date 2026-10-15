package com.pokeapi.infrastructure.config;

import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.application.usecase.ListPokemonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ListPokemonUseCase listPokemonUseCase(PokemonCatalogGateway gateway) {
        return new ListPokemonService(gateway);
    }
}