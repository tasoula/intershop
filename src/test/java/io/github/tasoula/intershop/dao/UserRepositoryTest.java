package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends SpringBootPostgreSQLBase{

   @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        user1 = new User();
        user1.setCreatedAt(Timestamp.from(Instant.now()));
        user2 = new User();
        user2.setCreatedAt(Timestamp.from(Instant.now()));
    }

    @Test
    void saveAndFindUsers() {
        var users = userRepository.saveAll(List.of(user1, user2))
                .thenMany(userRepository.findAll())
                .toIterable();

        assertThat(users)
                .withFailMessage("Пользователи не найдены")
                .isNotEmpty()
                .withFailMessage("пользователей не 2")
                .hasSize(2);
    }

    @Test
    void deleteByCreatedAtBefore_ShouldDeleteUsers() {
        Timestamp beforeTimestamp = Timestamp.from(Instant.now().plusSeconds(1));

        userRepository.saveAll(List.of(user1, user2))
                .thenMany(userRepository.deleteByCreatedAtBefore(beforeTimestamp))
                .blockLast();

        // Assert
        List<User> users = userRepository.findAll().collectList().block();
        assertThat(users).isEmpty();  // Assuming the test setup creates users.  If not, adjust.
    }

    @Test
    void deleteByCreatedAtBefore_ShouldNotDeleteUsers_WhenNoMatch() {
        Timestamp futureTimestamp = Timestamp.from(Instant.now().minusSeconds(1000));

        userRepository.saveAll(List.of(user1, user2))
                .thenMany(userRepository.deleteByCreatedAtBefore(futureTimestamp))
                .blockLast();

        // Assert
        List<User> users = userRepository.findAll().collectList().block();
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(2);
    }
}