package com.pokeapi.application.port.in;

import com.pokeapi.domain.model.User;

public interface RegisterUserUseCase {

    User register(UserCredentials credentials);
}