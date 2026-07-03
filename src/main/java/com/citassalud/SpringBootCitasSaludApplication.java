package com.citassalud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBootCitasSaludApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCitasSaludApplication.class, args);
	}

}
