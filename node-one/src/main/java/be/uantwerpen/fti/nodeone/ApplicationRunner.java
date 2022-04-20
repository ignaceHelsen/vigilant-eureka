package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.MulticastListener;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {
    private final MulticastListener multicastListener;
    private final NetworkService networkService;

    @Override
    public void run(ApplicationArguments args) {
        networkService.registerNode();
        multicastListener.listenForMulticast();
    }
}
