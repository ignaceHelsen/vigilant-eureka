package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class NodeOneServletContextListener
        implements ServletContextListener {

    private final NetworkService networkService;
    private final TcpListener tcpService;
    private final MulticastListener multicastListener;
    private final ReplicationService replicationService;
    private final FileService fileService;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("Node is shutting down");
        replicationService.shutdown();
        networkService.nodeShutDown();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        tcpService.listenUnicastResponse();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        executor.execute(multicastListener::listenForMulticast);

        tcpService.listenForUpdateNext();
        tcpService.listenForUpdatePrevious();

        networkService.registerNode();

        replicationService.initializeReplication();
        fileService.precheck(); // check if all needed directories for replication are present.

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::scheduleLookForFiles, 30, TimeUnit.SECONDS);
    }

    public void scheduleLookForFiles() {
        replicationService.lookForFilesAtNeighbouringNodes();
    }
}

