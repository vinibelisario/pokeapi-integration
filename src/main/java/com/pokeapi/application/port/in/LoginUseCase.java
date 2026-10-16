package com.pokeapi.application.port.in;

public interface LoginUseCase {

    String login(UserCredentials credentials);
}