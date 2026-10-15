package com.pokeapi.infrastructure.config;

import com.pokeapi.application.port.in.GetPokemonDetailUseCase;
import com.pokeapi.application.port.in.ListPokemonUseCase;
import com.pokeapi.application.port.in.QueryLocalPokemonUseCase;
import com.pokeapi.application.port.in.SyncPokemonUseCase;
import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.application.port.out.PokemonCatalogGateway;
import com.pokeapi.application.port.out.PokemonDetailGateway;
import com.pokeapi.application.usecase.*;
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

    @Bean
    public SyncPokemonUseCase syncPokemonUseCase(PokemonDetailGateway gateway,
                                                 LocalPokemonRepository repository) {
        return new SyncPokemonService(gateway, repository);
    }

    @Bean
    public QueryLocalPokemonUseCase queryLocalPokemonUseCase(LocalPokemonRepository repository) {
        return new QueryLocalPokemonService(repository);
    }

    @Bean
    public ManageLocalPokemonService manageLocalPokemonService(LocalPokemonRepository repository) {
        return new ManageLocalPokemonService(repository);
    }
}