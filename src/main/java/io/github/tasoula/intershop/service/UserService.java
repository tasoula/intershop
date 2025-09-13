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
    private int cookieMaxAge;

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Mono<UUID> createUser() {
        return repository.save(new User()).map(User::getId);
    }

    @Scheduled(cron = "0 0 * * * *") // Каждый час
   // @Scheduled(fixedDelay = 30000) Каждые 30 секунд (для тестирования)
    @Transactional
    public Mono<Void> deleteExpiredUsers() {
        Timestamp expirationTime = new Timestamp( System.currentTimeMillis() - (cookieMaxAge * 1000L));
        return repository.deleteByCreatedAtBefore(expirationTime);//.subscribe();
    }

}

