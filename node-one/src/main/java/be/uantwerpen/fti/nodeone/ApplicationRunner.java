package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.MulticastListener;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import be.uantwerpen.fti.nodeone.service.TcpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Component
@Slf4j
@AllArgsConstructor
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {
    private final MulticastListener multicastListener;
    private final NetworkService networkService;
    private final TcpService tcpService;

    @Override
    public void run(ApplicationArguments args) {
        networkService.registerNode();
        multicastListener.listenForMulticast();
        tcpService.listenUnicastResponse();
    }
}
