package be.uantwerpen.fti.nodeone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

@Getter
@Setter
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "replication")
public class ReplicationConfig {
    private String local;
    private String replica;
    private String log;
    private String storage;
}


