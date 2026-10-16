package com.pokeapi.application.port.out;

import com.pokeapi.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    User save(User user);
}