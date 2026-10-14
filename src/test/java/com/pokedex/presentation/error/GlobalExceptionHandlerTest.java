package com.pokedex.presentation.error;

import com.pokedex.application.exception.ExternalServiceException;
import com.pokedex.domain.exception.InvalidDataException;
import com.pokedex.domain.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test (no Spring context): a fake controller throws each exception
 * and we check the HTTP status and the JSON body produced by the handler.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFound_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Pokemon 999 not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidData_returns400() throws Exception {
        mockMvc.perform(get("/test/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid data"));
    }

    @Test
    void externalServiceFailure_returns502() throws Exception {
        mockMvc.perform(get("/test/external"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("External service error"));
    }

    @Test
    void unexpectedError_returns500_withoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }

    @Test
    void invalidBody_returns400_withFieldErrors() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    @RestController
    static class FakeController {

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Pokemon 999 not found");
        }

        @GetMapping("/test/invalid")
        void invalid() {
            throw new InvalidDataException("Region must not be blank");
        }

        @GetMapping("/test/external")
        void external() {
            throw new ExternalServiceException("PokeAPI unavailable", new RuntimeException("timeout"));
        }

        @GetMapping("/test/boom")
        void boom() {
            throw new IllegalStateException("secret internal detail");
        }

        @PostMapping("/test/body")
        void body(@Valid @RequestBody SampleRequest request) {
        }
    }

    record SampleRequest(@NotBlank String name) {
    }
}
