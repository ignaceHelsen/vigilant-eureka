package be.uantwerpen.fti.nodeone;

import be.uantwerpen.fti.nodeone.service.MulticastListener;
import be.uantwerpen.fti.nodeone.service.NetworkService;
import be.uantwerpen.fti.nodeone.service.ReplicationService;
import be.uantwerpen.fti.nodeone.service.TcpListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@RequiredArgsConstructor
public class ExampleServletContextListener
        implements ServletContextListener {

    private final NetworkService networkService;
    private final TcpListener tcpService;
    private final MulticastListener multicastListener;
    private final ReplicationService replicationService;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        networkService.nodeShutDown();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        tcpService.listenUnicastResponse();
        multicastListener.listenForMulticast();

        tcpService.listenForUpdateNext();
        tcpService.listenForUpdatePrevious();

        networkService.registerNode();

        replicationService.initializeReplication();
        replicationService.precheck(); // check if all needed directories for replication are present.
        replicationService.lookForFilesAtNeighbouringNodes();
    }
}
