package be.uantwerpen.fti.namingserver;

import be.uantwerpen.fti.namingserver.service.MulticastListener;
import be.uantwerpen.fti.namingserver.service.TcpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {
    private final MulticastListener multicastListener;

    @Override
    public void run(ApplicationArguments args) {
        multicastListener.listenForMulticast();
    }
}
