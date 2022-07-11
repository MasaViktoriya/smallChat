package ru.masaviktoria.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import ru.masaviktoria.server.ServerCommandConstants;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    @FXML
    private TextFlow textFlow;
    @FXML
    private TextField messageField, loginField;
    @FXML
    private HBox messagePanel, authPanel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ListView<String> clientList;

    private final Network network;
    private File history;
    private String login;

    public ChatController() {
        this.network = new Network(this);
    }

    public void setAuthenticated(boolean authenticated) {
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        messagePanel.setVisible(authenticated);
        messagePanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAuthenticated(false);
    }

    public void displayMessage(String text) {
        Platform.runLater(() -> {
            addTextNode(text);
            logMessages(text);
        });
    }

    public void addTextNode(String textForTextFlow) {
        Text textNode = new Text(textForTextFlow);
        if (textForTextFlow.startsWith("PM ")) {
            textNode.setFill(Color.DARKGREEN);
            textFlow.getChildren().add(textNode);
        } else {
            textFlow.getChildren().add(textNode);
        }
    }

    private void logMessages(String text) {
        try (OutputStreamWriter historyWriter = new OutputStreamWriter(new FileOutputStream(history, true), StandardCharsets.UTF_8)) {
            historyWriter.write(text + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayClient(String nickName) {
        Platform.runLater(() -> clientList.getItems().add(nickName));
    }

    public void removeClient(String nickName) {
        Platform.runLater(() -> clientList.getItems().remove(nickName));
    }

    public boolean isNicknameInClientList(String oldNickName) {
        return clientList.getItems().contains(oldNickName);
    }

    public void sendAuth() {
        boolean authenticated = network.sendAuth(loginField.getText(), passwordField.getText());
        if (authenticated) {
            this.login = loginField.getText();
            history = new File("history_" + login + ".txt");
            loginField.clear();
            passwordField.clear();
            setAuthenticated(true);
            displayMessageHistory();
        }
    }

    private void displayMessageHistory() {
        ArrayList<String> historyList = new ArrayList<>();
        if (new File("history_" + login + ".txt").isFile()) {
            try (BufferedReader historyReader = new BufferedReader(new InputStreamReader(new FileInputStream(history), StandardCharsets.UTF_8))) {
                while (historyReader.ready()) {
                    historyList.add(historyReader.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] historyArray = new String[historyList.size()];
            historyArray = historyList.toArray(historyArray);
            if (historyArray.length > 200) {  // число 200 появилось, поскольку используется метод readLine, он считывает переносы строк как отдельные строки
                String[]
                        displayArray = Arrays.copyOfRange(historyArray, historyArray.length - 200, historyArray.length);
                for (String text : displayArray) {
                    addTextNode(text + "\n");
                }
            } else {
                for (String text : historyArray) {
                    addTextNode(text + "\n");
                }
            }
        }
    }

    public void sendMessage() {
        String selectedUser = clientList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            network.sendMessage(messageField.getText());
            messageField.clear();
        } else {
/*             clientList.addEventFilter(MouseEvent.MOUSE_CLICKED, event ->{
                if(clientList.getSelectionModel().getSelectedIndices().get(0) <= -1){
                clientList.getSelectionModel().clearSelection();
                }
            });*/
            network.sendMessage(ServerCommandConstants.PERSONALMESSAGE + selectedUser + " " + messageField.getText());
            messageField.clear();
            clientList.getSelectionModel().clearSelection();
        }
    }

    public void close() {
        network.closeConnection();
    }

    public String getLogin() {
        return login;
    }
}
