package com.pokeapi.infrastructure.seed;

import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.application.port.out.PasswordHasher;
import com.pokeapi.application.port.out.UserRepository;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private LocalPokemonRepository localPokemonRepository;

    private DemoDataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DemoDataSeeder(userRepository, passwordHasher, localPokemonRepository, "demo12345");
    }

    @Test
    void seedCreatesDemoUserAndPokemonWhenMissing() {
        when(userRepository.findByUsername("demo")).thenReturn(Optional.empty());
        when(passwordHasher.hash("demo12345")).thenReturn("hashed");

        seeder.seed();

        verify(userRepository).save(argThat(user ->
                user.username().equals("demo") && user.passwordHash().equals("hashed")));
        verify(localPokemonRepository, times(3)).save(any(LocalPokemon.class));
    }

    @Test
    void seedDoesNothingWhenDemoUserAlreadyExists() {
        when(userRepository.findByUsername("demo"))
                .thenReturn(Optional.of(new User(1L, "demo", "hashed")));

        seeder.seed();

        verify(userRepository, never()).save(any(User.class));
        verify(localPokemonRepository, never()).save(any(LocalPokemon.class));
    }

    @Test
    void seedDoesNotOverwritePokemonThatAlreadyExist() {
        when(userRepository.findByUsername("demo")).thenReturn(Optional.empty());
        when(passwordHasher.hash("demo12345")).thenReturn("hashed");
        when(localPokemonRepository.findById(1))
                .thenReturn(Optional.of(new LocalPokemon(1, "bulbasaur", "img", "desc",
                        "Bulbizarre", "Kanto", java.util.List.of("starter"))));

        seeder.seed();

        verify(localPokemonRepository, never()).save(argThat(pokemon -> pokemon.id() == 1));
        verify(localPokemonRepository, times(2)).save(any(LocalPokemon.class));
    }
}