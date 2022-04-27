package be.uantwerpen.fti.nodeone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NodeOneApplication {
	public static void main(String[] args) {
		SpringApplication.run(NodeOneApplication.class, args);
	}
}
