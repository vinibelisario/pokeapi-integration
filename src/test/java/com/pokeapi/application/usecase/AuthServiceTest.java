package com.pokeapi.application.usecase;

import com.pokeapi.application.port.in.UserCredentials;
import com.pokeapi.application.port.out.PasswordHasher;
import com.pokeapi.application.port.out.TokenGateway;
import com.pokeapi.application.port.out.UserRepository;
import com.pokeapi.domain.exception.ConflictException;
import com.pokeapi.domain.exception.InvalidCredentialsException;
import com.pokeapi.domain.exception.InvalidDataException;
import com.pokeapi.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenGateway tokenGateway;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(repository, passwordHasher, tokenGateway);
    }

    @Test
    void registerStoresTheUserWithAHashedPassword() {
        when(repository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordHasher.hash("secret123")).thenReturn("hashed");
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            return new User(1L, toSave.username(), toSave.passwordHash());
        });

        User result = service.register(new UserCredentials("alice", "secret123"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.passwordHash()).isEqualTo("hashed");
    }

    @Test
    void registerRejectsAnExistingUsername() {
        when(repository.findByUsername("alice"))
                .thenReturn(Optional.of(new User(1L, "alice", "hashed")));

        assertThatThrownBy(() -> service.register(new UserCredentials("alice", "secret123")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("alice");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void registerRejectsInvalidUsername() {
        assertThatThrownBy(() -> service.register(new UserCredentials("a b", "secret123")))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("username");

        verifyNoInteractions(repository, passwordHasher, tokenGateway);
    }

    @Test
    void registerRejectsShortPassword() {
        assertThatThrownBy(() -> service.register(new UserCredentials("alice", "short")))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("password");

        verifyNoInteractions(repository, passwordHasher, tokenGateway);
    }

    @Test
    void loginReturnsATokenForValidCredentials() {
        when(repository.findByUsername("alice"))
                .thenReturn(Optional.of(new User(1L, "alice", "hashed")));
        when(passwordHasher.matches("secret123", "hashed")).thenReturn(true);
        when(tokenGateway.generate("alice")).thenReturn("token-123");

        String token = service.login(new UserCredentials("alice", "secret123"));

        assertThat(token).isEqualTo("token-123");
    }

    @Test
    void loginRejectsAWrongPassword() {
        when(repository.findByUsername("alice"))
                .thenReturn(Optional.of(new User(1L, "alice", "hashed")));
        when(passwordHasher.matches("wrong-pass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new UserCredentials("alice", "wrong-pass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        verify(tokenGateway, never()).generate("alice");
    }

    @Test
    void loginRejectsAnUnknownUserWithTheSameMessage() {
        when(repository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new UserCredentials("nobody", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }
}