package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import lombok.RequiredArgsConstructor;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@RequiredArgsConstructor
public class ExampleServletContextListener
        implements ServletContextListener {

    private final NetworkService networkService;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        networkService.nodeShutDown();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Triggers when context initializes


    }
}
