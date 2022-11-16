package ru.masaviktoria.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.masaviktoria.server.authorization.InMemoryAuthServiceImpl;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerStarter {
    private static final Logger LOGGER = LogManager.getLogger("ServerStarter");
    public static void main(String[] args) {
        InMemoryAuthServiceImpl authService = new InMemoryAuthServiceImpl();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Server server = new Server(authService, executorService);
        server.start();
        LOGGER.info("Server online");
    }
}
