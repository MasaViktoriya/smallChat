package ru.masaviktoria.client;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import ru.masaviktoria.CommonConstants;
import ru.masaviktoria.SQLConnection;
import ru.masaviktoria.server.ServerCommandConstants;


public class Network {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private final ChatController controller;


    public Network(ChatController chatController) {
        this.controller = chatController;
    }

    private void startReadServerMessages() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String messageFromServer = inputStream.readUTF();
                        System.out.println(messageFromServer);
                        if (messageFromServer.startsWith(ServerCommandConstants.ENTER)) {
                            String[] client = messageFromServer.split(" ");
                            controller.displayClient(client[1]);
                            controller.displayMessage("Пользователь " + client[1] + " зашел в чат\n");
                        } else if (messageFromServer.startsWith(ServerCommandConstants.EXIT)){
                            String[] client = messageFromServer.split(" ");
                            controller.removeClient(client[1]);
                            controller.displayMessage("Пользователь " + client[1] + " покинул чат\n");
                        } else if (messageFromServer.startsWith(ServerCommandConstants.CLIENTS)) {
                            String[] client = messageFromServer.split("\n");
                            for (int i = 1; i < client.length; i++){
                                controller.displayClient(client[i]);
                            }
                        }  else if (messageFromServer.startsWith(ServerCommandConstants.CHANGENICKNAME)){
                            String [] changeNick = messageFromServer.split(" ");
                            String oldNickName = changeNick[2];
                            String newNickName = changeNick[1];
                            String login = changeNick[3];
                            try{
                                SQLConnection.connect();
                                changeNickName(login, newNickName);
                                if (controller.isNicknameInClientList(oldNickName)){
                                    controller.removeClient(oldNickName);
                                    controller.displayClient(newNickName);
                                }
                                controller.displayMessage("Пользователь "  + oldNickName + " сменил ник на " + newNickName + "\n");
                            }catch(SQLException e){
                                e.printStackTrace();
                            } finally {
                                SQLConnection.disconnect();
                            }
                        }  else {
                            controller.displayMessage(messageFromServer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void initializeNetwork() throws IOException {
        socket = new Socket(CommonConstants.SERVER_ADDRESS, CommonConstants.SERVER_PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public boolean sendAuth (String login, String password) {
        try{
            if(socket == null || socket.isClosed()) {
                initializeNetwork();
            }
            outputStream.writeUTF(ServerCommandConstants.AUTHENTICATION +" " + login + " " + password);
            boolean authenticated = inputStream.readBoolean();
            if (authenticated) {
                startReadServerMessages();
            }
            return authenticated;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void changeNickName(String login, String newNickName) throws SQLException{
        SQLConnection.statement.executeUpdate(String.format("UPDATE userList SET nickname = '%s' WHERE login = '%s'", newNickName, login));
    }

    public void closeConnection() {
        try {
            outputStream.writeUTF(ServerCommandConstants.EXIT);
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
