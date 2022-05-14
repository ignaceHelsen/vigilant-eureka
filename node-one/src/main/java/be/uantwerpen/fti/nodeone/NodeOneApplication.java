package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.MulticastListener;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import be.uantwerpen.fti.nodeone.service.ReplicationService;
import be.uantwerpen.fti.nodeone.service.TcpListener;
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
                                                                                   MulticastListener multicastListener, ReplicationService replicationService) {
        ServletListenerRegistrationBean<ServletContextListener> srb = new ServletListenerRegistrationBean<>();
        srb.setListener(new NodeOneServletContextListener(networkService, tcpListener, multicastListener, replicationService));
        return srb;
    }

}
