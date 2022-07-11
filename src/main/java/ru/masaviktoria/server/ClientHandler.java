package ru.masaviktoria.server;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.masaviktoria.SQLConnection;

public class ClientHandler {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private final Server server;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private String nickName;
    private String login;

    public ClientHandler (ExecutorService executorService, Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());

            executorService.execute(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        catch (IOException exception) {
            exception.printStackTrace();
            throw  new RuntimeException("Проблемы при создании обработчика");
        }
    }

    public void authentication () throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if(message.startsWith(ServerCommandConstants.AUTHENTICATION)) {
                String[] authInfo = message.split("\\s");
                String nickName = server.getAuthService().getNicknameByLoginAndPassword(authInfo[1],  authInfo[2]);
                String login = authInfo[1];
                if (nickName != null ) {
                    if (!server.isNickNameBusy(nickName)) {
                        sendAuthenticationMessage(true);
                        this.nickName = nickName;
                        this.login = login;
                        server.broadcastMessage(ServerCommandConstants.ENTER + " " + nickName);
                        sendMessage(server.getClients());
                        server.addConnectedUser(this);
                        return;
                    } else {
                        sendAuthenticationMessage(false);
                    }
                } else {
                    sendAuthenticationMessage(false);
                }
            }
        }
    }

    private void sendAuthenticationMessage(boolean authenticated) throws IOException {
        outputStream.writeBoolean(authenticated);
    }

    private void readMessages () throws IOException {
        while (true) {
            String messageInChat = inputStream.readUTF();
            System.out.println("от " + nickName + ": " + messageInChat);
            if (messageInChat.equals(ServerCommandConstants.EXIT)){
                closeConnection();
                return;
            } else if (messageInChat.startsWith(ServerCommandConstants.PERSONALMESSAGE)) {
                sendPersonalMessage(messageInChat);
            } else if (messageInChat.startsWith(ServerCommandConstants.CHANGENICKNAME)){
                server.broadcastMessage(messageInChat + " " + nickName + " " + login);
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                try {
                    SQLConnection.connect();
                    try (ResultSet newNickName = SQLConnection.statement.executeQuery(String.format("SELECT nickname FROM userlist WHERE login = '%s'", login))) {
                        while (newNickName.next()) {
                            this.nickName = newNickName.getString("nickname");
                        }
                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    SQLConnection.disconnect();
                }

            } else {
                server.broadcastMessage(nickName + ": " + messageInChat + "\n");
            }
        }
    }

    public void sendMessage (String message) {
        try {
            outputStream.writeUTF(message);
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
    }

    public void sendPersonalMessage (String messageInChat) {
        String [] personalMessageInfo = messageInChat.split(" ", 3);
        String recipientNickName = personalMessageInfo[1];
        String personalMessage = personalMessageInfo[2];
        String senderNickName = this.getNickname();
        server.sendPersonalMessage(senderNickName, recipientNickName, personalMessage + "\n");
    }

    private void closeConnection () {
        server.disconnectUser(this);
        server.broadcastMessage(ServerCommandConstants.EXIT + " " + nickName);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return login;
    }

    public String getNickname() {
        return nickName;
    }
}
