package be.uantwerpen.fti.nodeone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class NodeOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(NodeOneApplication.class, args);
	}
}
