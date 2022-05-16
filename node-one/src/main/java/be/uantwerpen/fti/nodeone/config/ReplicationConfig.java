package be.uantwerpen.fti.nodeone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Getter
@Setter
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "replication")
public class ReplicationConfig {
    private String local;
    private String replica;
}