package be.uantwerpen.fti.nodeone.config;

import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.component.HashCalculator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;


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
    private int updateNextSocketPort;
    private int updatePreviousSocketPort;
    private int replicationSocketPort;

    @Bean
    public NodeStructure nodeStructure(HashCalculator hashCalculator) {
        return new NodeStructure(0, hashCalculator.calculateHash(hostName), 0);
    }
}