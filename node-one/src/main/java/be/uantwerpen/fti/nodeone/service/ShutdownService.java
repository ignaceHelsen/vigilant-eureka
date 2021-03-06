package be.uantwerpen.fti.nodeone.service;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class ShutdownService {
    private final ApplicationContext appContext;

    public void scheduleShutdown() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.schedule(this::initiateShutdown, 5, TimeUnit.SECONDS);
    }

    /*
     * Invoke with `0` to indicate no error or different code to indicate
     * abnormal exit. es: shutdownManager.initiateShutdown(0);
     **/
    public void initiateShutdown(){
        SpringApplication.exit(appContext, () -> 0);
    }
}
