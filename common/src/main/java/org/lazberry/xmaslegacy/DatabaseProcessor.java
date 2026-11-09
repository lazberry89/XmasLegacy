package org.lazberry.xmaslegacy;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
public abstract class DatabaseProcessor {
    private final String url;
    private final String user;
    private final String password;

    protected DatabaseProcessor(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public abstract void createTable();
}
