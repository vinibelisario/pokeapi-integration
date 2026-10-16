package com.pokeapi.infrastructure.persistence;

import com.pokeapi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepositoryAdapter adapter;

    @Test
    void saveAssignsAnIdAndFindsTheUserByUsername() {
        User saved = adapter.save(new User(null, "alice", "hashed"));

        assertThat(saved.id()).isNotNull();
        assertThat(adapter.findByUsername("alice")).hasValueSatisfying(user -> {
            assertThat(user.id()).isEqualTo(saved.id());
            assertThat(user.passwordHash()).isEqualTo("hashed");
        });
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        assertThat(adapter.findByUsername("nobody")).isEmpty();
    }
}