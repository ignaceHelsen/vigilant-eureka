package be.uantwerpen.fti.nodeone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "naming.server")
public class NamingServerConfig {
    private String address;
    private String port;
}
