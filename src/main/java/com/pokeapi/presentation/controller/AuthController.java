package com.pokeapi.presentation.controller;

import com.pokeapi.application.port.in.LoginUseCase;
import com.pokeapi.application.port.in.RegisterUserUseCase;
import com.pokeapi.application.port.in.UserCredentials;
import com.pokeapi.domain.model.User;
import com.pokeapi.presentation.dto.CredentialsRequest;
import com.pokeapi.presentation.dto.TokenResponse;
import com.pokeapi.presentation.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody CredentialsRequest request) {
        User user = registerUserUseCase.register(toCredentials(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(user.id(), user.username()));
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody CredentialsRequest request) {
        return new TokenResponse(loginUseCase.login(toCredentials(request)));
    }

    private static UserCredentials toCredentials(CredentialsRequest request) {
        return new UserCredentials(request.username(), request.password());
    }
}