package io.github.tasoula.intershop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InternetShopApplication {	public static void main(String[] args) {
		SpringApplication.run(InternetShopApplication.class, args);
	}
}
