package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.servlet.ServletContextListener;

@EnableAsync
@SpringBootApplication
public class NodeOneApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeOneApplication.class, args);
    }

    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> servletListener(NetworkService networkService, TcpListener tcpListener,
                                                                                   MulticastListener multicastListener, ReplicationService replicationService, FileService fileService) {
        ServletListenerRegistrationBean<ServletContextListener> srb = new ServletListenerRegistrationBean<>();
        srb.setListener(new ExampleServletContextListener(networkService, tcpListener, multicastListener, replicationService, fileService));
        return srb;
    }
}
