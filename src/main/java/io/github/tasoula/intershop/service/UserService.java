package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.UserRepository;
import io.github.tasoula.intershop.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class UserService {

    @Value("${cookie.max.age.seconds}")
    private int coockieMaxAge;

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
/*
    @Scheduled(cron = "0 0 * * * *") // Каждый час
    //@Scheduled(fixedDelay = 30000) // Каждые 30 секунд (для тестирования)
    @Transactional
    public void deleteExpiredUsers() {
        long expirationTime = System.currentTimeMillis() - (coockieMaxAge * 1000L);
        Timestamp expirationTimestamp = new Timestamp(expirationTime);

        // Удаляем пользователей, созданных раньше срока истечения куки
        repository.deleteByCreatedAtBefore(expirationTimestamp);

        System.out.println("Deleted expired users."); // Log для подтверждения работы
    }
     */

    public Mono<UUID> createUser() {
        return repository.save(new User()).map(User::getId);
    }

/*    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void deleteExpiredUsers() {
        Timestamp expirationTime = new Timestamp( System.currentTimeMillis() - (coockieMaxAge * 1000L));
        repository.deleteByCreatedAtBefore(expirationTime).subscribe(); // Обрабатываем Mono<Void>
     //   System.out.println("Deleted expired users.");
    }

 */
}

