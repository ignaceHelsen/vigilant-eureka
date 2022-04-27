package be.uantwerpen.fti.nodeone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
public class NodeOneApplication {
	public static void main(String[] args) {
		SpringApplication.run(NodeOneApplication.class, args);
	}
}
