package com.pokeapi.infrastructure.persistence;

import com.pokeapi.application.port.out.UserRepository;
import com.pokeapi.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springData;

    public UserRepositoryAdapter(SpringDataUserRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springData.findByUsername(username).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = springData.save(new UserEntity(user.username(), user.passwordHash()));
        return toDomain(saved);
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getPasswordHash());
    }
}