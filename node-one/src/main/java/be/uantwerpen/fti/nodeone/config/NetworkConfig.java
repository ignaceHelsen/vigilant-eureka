package be.uantwerpen.fti.nodeone.config;

import be.uantwerpen.fti.nodeone.ExampleServletContextListener;
import be.uantwerpen.fti.nodeone.service.MulticastListener;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import be.uantwerpen.fti.nodeone.service.TcpListener;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.servlet.ServletContextListener;


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
