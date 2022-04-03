package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkService implements ApplicationListener<ContextRefreshedEvent> {
    private final NetworkConfig networkConfig;
    private final RestService restService;

    public void registerNode() {
        RegisterRequest registerRequest = new RegisterRequest(networkConfig.getIpAddress(), networkConfig.getHostName());
        restService.registerNode(registerRequest);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registerNode();
    }
}
