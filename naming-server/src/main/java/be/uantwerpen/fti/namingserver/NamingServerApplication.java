package be.uantwerpen.fti.namingserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NamingServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(NamingServerApplication.class, args);
	}
}