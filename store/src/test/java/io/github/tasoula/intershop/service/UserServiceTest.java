package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dao.UserRepository;
import io.github.tasoula.intershop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/*
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Устанавливаем значение cookieMaxAge через ReflectionTestUtils, так как внедрение через @Value в тестах может быть сложным
        ReflectionTestUtils.setField(userService, "cookieMaxAge", 3600); // Пример: 3600 секунд (1 час)
    }

    @Test
    void createUser_shouldCreateAndSaveUser() {
        // Arrange
        User newUser = new User();
        newUser.setId(UUID.randomUUID()); // Присваиваем ID, чтобы избежать NullPointerException
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(newUser));

        // Act
        UUID userId = userService.createUser().block();

        // Assert
        assertNotNull(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteExpiredUsers_shouldDeleteUsersCreatedBeforeExpirationTime() {
        // Arrange
        long cookieMaxAgeSeconds = 3600; // Значение должно совпадать с установленным в @BeforeEach
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = currentTimeMillis - (cookieMaxAgeSeconds * 1000L);
        Timestamp expirationTimestamp = new Timestamp(expirationTimeMillis);

        // Act
        userService.deleteExpiredUsers();

        // Assert
        verify(userRepository, times(1)).deleteByCreatedAtBefore(expirationTimestamp);
    }


}

 */