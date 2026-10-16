package com.pokeapi.application.port.out;

import java.util.Optional;

public interface TokenGateway {

    String generate(String username);

    Optional<String> validate(String token);
}