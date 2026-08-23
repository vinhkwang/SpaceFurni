package com.spacefurni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpaceFurniApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpaceFurniApplication.class, args);
	}

}
