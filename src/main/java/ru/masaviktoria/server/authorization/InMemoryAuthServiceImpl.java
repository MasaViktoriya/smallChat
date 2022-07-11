package ru.masaviktoria.server.authorization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.masaviktoria.SQLConnection;

import java.sql.*;

public class InMemoryAuthServiceImpl  implements  AuthService {
    private static final Logger LOGGER = LogManager.getLogger(InMemoryAuthServiceImpl.class);

    public InMemoryAuthServiceImpl() {
    }

    @Override
    public void start() {
        LOGGER.debug("Сервис аутентификации инициализирован");

    }

    @Override
    public synchronized String getNicknameByLoginAndPassword(String login, String password) {
        try {
            SQLConnection.connect();
            try (ResultSet user = SQLConnection.statement.executeQuery(String.format("SELECT * FROM userList WHERE login = '%s'", login))) {
                while (user.next()) {
                    if (user.getString("password").equals(password)) {
                        return user.getString("nickname");
                    }
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLConnection.disconnect();
        }
        return null;
    }

    @Override
    public void end() {
        LOGGER.debug("Сервис аутентификации отключен");

    }
}
