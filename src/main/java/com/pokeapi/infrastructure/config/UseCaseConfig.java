package com.pokeapi.infrastructure.config;

import com.pokeapi.application.port.in.GetPokemonDetailUseCase;
import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.application.usecase.GetPokemonDetailService;
import com.pokeapi.application.usecase.ListPokemonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ListPokemonUseCase listPokemonUseCase(PokemonCatalogGateway gateway) {
        return new ListPokemonService(gateway);
    }

    @Bean
    public GetPokemonDetailUseCase getPokemonDetailUseCase(PokemonDetailGateway gateway) {
        return new GetPokemonDetailService(gateway);
    }
}