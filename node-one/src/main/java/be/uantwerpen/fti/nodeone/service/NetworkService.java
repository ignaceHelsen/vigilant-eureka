package be.uantwerpen.fti.nodeone.service;

import be.uantwerpen.fti.nodeone.config.NetworkConfig;
import be.uantwerpen.fti.nodeone.domain.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkService implements
        ApplicationListener<ContextRefreshedEvent> {

    private final NetworkConfig networkConfig;
    private final RestService restService;

    public void registerNode() {
        RegisterRequest registerRequest = new RegisterRequest(networkConfig.getIpAddress(), networkConfig.getHostName());
        boolean success = restService.registerNode(registerRequest);

        if (success) {
            log.info("Registering node was successful");

        }
        else {
            log.warn("Registering node failed");
            // Handle failure
        }

    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registerNode();
    }
}
