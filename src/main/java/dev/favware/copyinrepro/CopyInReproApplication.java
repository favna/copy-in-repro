package dev.favware.copyinrepro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CopyInReproApplication {
	public static void main(String[] args) {
		SpringApplication.run(CopyInReproApplication.class, args);
	}
}
