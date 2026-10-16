package com.pokeapi.infrastructure.seed;

import com.pokeapi.application.port.out.LocalPokemonRepository;
import com.pokeapi.application.port.out.PasswordHasher;
import com.pokeapi.application.port.out.UserRepository;
import com.pokeapi.domain.model.LocalPokemon;
import com.pokeapi.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "pokeapi.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private static final String DEMO_USERNAME = "demo";

    private static final List<LocalPokemon> DEMO_POKEMON = List.of(
            new LocalPokemon(1, "bulbasaur",
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
                    "A strange seed was planted on its back at birth.",
                    "Bulbizarre", "Kanto", List.of("starter", "grass")),
            new LocalPokemon(4, "charmander",
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png",
                    "It shows its feelings through the flame on its tail.",
                    "Salameche", "Kanto", List.of("starter", "fire")),
            new LocalPokemon(25, "pikachu",
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
                    "When several gather, their electricity can build storms.",
                    "Pikachu", "Kanto", List.of("electric", "mascot")));

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final LocalPokemonRepository localPokemonRepository;
    private final String demoPassword;

    public DemoDataSeeder(UserRepository userRepository,
                          PasswordHasher passwordHasher,
                          LocalPokemonRepository localPokemonRepository,
                          @Value("${pokeapi.seed.demo-password}") String demoPassword) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.localPokemonRepository = localPokemonRepository;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed();
    }

    public void seed() {
        // Runs only on the first start. Later starts see the demo user and do nothing.
        if (userRepository.findByUsername(DEMO_USERNAME).isPresent()) {
            return;
        }

        userRepository.save(new User(null, DEMO_USERNAME, passwordHasher.hash(demoPassword)));

        for (LocalPokemon pokemon : DEMO_POKEMON) {
            if (localPokemonRepository.findById(pokemon.id()).isEmpty()) {
                localPokemonRepository.save(pokemon);
            }
        }
    }
}