package com.sinomed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;



@SpringBootApplication(scanBasePackages = "com.sinomed.*")
@EnableJpaRepositories(basePackages = {"com.sinomed.repository"})
public class SinomedApplication {

	public static void main(String[] args) {
		SpringApplication.run(SinomedApplication.class, args);
	}

}
