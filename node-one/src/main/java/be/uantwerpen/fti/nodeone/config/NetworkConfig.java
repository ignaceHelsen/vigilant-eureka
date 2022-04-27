package be.uantwerpen.fti.nodeone.config;

import be.uantwerpen.fti.nodeone.ExampleServletContextListener;
import be.uantwerpen.fti.nodeone.domain.NodeStructure;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import be.uantwerpen.fti.nodeone.service.TcpService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContextListener;

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

    @Bean
    ServletListenerRegistrationBean<ServletContextListener> servletListener(NetworkService networkService) {
        ServletListenerRegistrationBean<ServletContextListener> srb
                = new ServletListenerRegistrationBean<>();
        srb.setListener(new ExampleServletContextListener(networkService));
        return srb;
    }
}
