package com.RotaDurak.RotaDurak;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class RotaDurakApplication {

	public static void main(String[] args) {
		SpringApplication.run(RotaDurakApplication.class, args);
	}

	@Bean
	ApplicationRunner printKafka(Environment env) {
		return args -> {
			System.out.println(">>> spring.kafka.bootstrap-servers = "
					+ env.getProperty("spring.kafka.bootstrap-servers"));
			System.out.println(">>> spring.kafka.consumer.bootstrap-servers = "
					+ env.getProperty("spring.kafka.consumer.bootstrap-servers"));
			System.out.println(">>> spring.kafka.properties.bootstrap.servers = "
					+ env.getProperty("spring.kafka.properties.bootstrap.servers"));
		};
	}

}
