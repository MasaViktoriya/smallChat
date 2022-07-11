package ru.masaviktoria.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStarter {
    private static final Logger LOGGER = LogManager.getLogger("ServerStarter");
    public static void main(String[] args) {
        LOGGER.info("Server online");
        new Server();
    }
}
