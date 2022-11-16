package ru.masaviktoria.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.masaviktoria.CommonConstants;
import ru.masaviktoria.server.authorization.AuthService;
import ru.masaviktoria.server.authorization.InMemoryAuthServiceImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger("Server");
    private List<ClientHandler> connectedUsers;
    private final ExecutorService executorService;
    private final InMemoryAuthServiceImpl authService;

    public Server(InMemoryAuthServiceImpl authService, ExecutorService executorService) {
        this.authService = authService;
        this.executorService = executorService;
    }

    public void start(){
        try (ServerSocket server = new ServerSocket(CommonConstants.SERVER_PORT)) {
            this.authService.start();
            LOGGER.debug("Server started");
            connectedUsers = new ArrayList<>();
            while (true) {
                LOGGER.debug("Сервер ожидает подключения");
                Socket socket = server.accept();
                LOGGER.debug("Клиент подключился");
                new ClientHandler(executorService, this, socket);
            }
        }
        catch (IOException exception){
            exception.printStackTrace();
            LOGGER.debug("Ошибка в работе сервера");
        }
        finally {
            if (this.authService != null) {
                this.authService.end();
            }
        }
    }


    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNickNameBusy(String nickName) {
        for (ClientHandler handler: connectedUsers){
            if (handler.getNickname().equals(nickName)){
                return  true;
            }
        }
        return false;
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler handler: connectedUsers){
            handler.sendMessage(message);
        }
    }

    public synchronized void addConnectedUser(ClientHandler handler) {
        connectedUsers.add(handler);
    }

    public synchronized void disconnectUser(ClientHandler handler) {
        connectedUsers.remove(handler);
    }

    public void sendPersonalMessage(String senderNickName, String recipientNickName, String personalMessage) {
        for (ClientHandler handler: connectedUsers){
            if (handler.getNickname().equals(recipientNickName) || handler.getNickname().equals(senderNickName)) {
                handler.sendMessage("PM "+ senderNickName + " to "+ recipientNickName + ": " + personalMessage);
            }
        }
    }

    public String getClients(){
        StringBuilder builder = new StringBuilder("/clients\n");
        for (ClientHandler user: connectedUsers){
            builder.append(user.getNickname()).append("\n");
        }
        return builder.toString();
    }
}
