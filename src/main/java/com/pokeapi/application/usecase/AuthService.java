package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.LoginUseCase;
import com.pokeapi.application.port.in.RegisterUserUseCase;
import com.pokeapi.application.port.in.UserCredentials;
import com.pokeapi.application.port.out.PasswordHasher;
import com.pokeapi.application.port.out.TokenGateway;
import com.pokeapi.application.port.out.UserRepository;
import com.pokeapi.domain.exception.ConflictException;
import com.pokeapi.domain.exception.InvalidCredentialsException;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.model.User;

import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService implements RegisterUserUseCase, LoginUseCase {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]{3,30}");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72; // BCrypt ignores anything after 72 bytes

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final TokenGateway tokenGateway;

    public AuthService(UserRepository repository, PasswordHasher passwordHasher, TokenGateway tokenGateway) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.tokenGateway = tokenGateway;
    }

    @Override
    public User register(UserCredentials credentials) {
        validate(credentials);

        if (repository.findByUsername(credentials.username()).isPresent()) {
            throw new ConflictException("username " + credentials.username() + " already exists");
        }

        String hash = passwordHasher.hash(credentials.password());
        return repository.save(new User(null, credentials.username(), hash));
    }

    @Override
    public String login(UserCredentials credentials) {
        Optional<User> user = repository.findByUsername(credentials.username());

        // Same message for "no such user" and "wrong password", so usernames are not revealed.
        if (user.isEmpty() || !passwordHasher.matches(credentials.password(), user.get().passwordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return tokenGateway.generate(user.get().username());
    }

    private static void validate(UserCredentials credentials) {
        if (credentials.username() == null || !USERNAME_PATTERN.matcher(credentials.username()).matches()) {
            throw new InvalidDataException("username must be 3 to 30 characters: letters, digits or underscore");
        }
        if (credentials.password() == null
                || credentials.password().length() < MIN_PASSWORD_LENGTH
                || credentials.password().length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidDataException("password must be between 8 and 72 characters");
        }
    }
}