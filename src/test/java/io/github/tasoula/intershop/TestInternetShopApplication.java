package io.github.tasoula.intershop;

import org.springframework.boot.SpringApplication;

public class TestInternetShopApplication {

	public static void main(String[] args) {
		SpringApplication.from(InternetShopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
