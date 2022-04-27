package be.uantwerpen.fti.nodeone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "network")
public class NetworkConfig {
    private String ipAddress;
    private String hostName;
    private int multicastPort;
    private String multicastGroupIp;
    private int socketPort;

}
