package be.uantwerpen.fti.nodeone.config;

import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "network")
public class NetworkConfig {
    private String ipAddress;
    private String hostName;

    @Bean
    public NodeStructure nodeStructure() {
        return new NodeStructure(0,0);
    }
}
