package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.TcpListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppStartupRunner implements ApplicationRunner {

    private final TcpListener tcpService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        tcpService.listenForUpdateNext();
        tcpService.listenForUpdatePrevious();
    }
}
