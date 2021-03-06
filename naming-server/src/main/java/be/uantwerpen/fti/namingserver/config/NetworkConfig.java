package be.uantwerpen.fti.namingserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "network")
public class NetworkConfig {
    private String hostName;
    private int multicastPort;
    private String multicastGroupIp;
    private int socketPort;
}
