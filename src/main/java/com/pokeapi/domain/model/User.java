package com.pokeapi.domain.model;

public record User(Long id, String username, String passwordHash) {
}