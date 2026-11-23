package org.lazberry.xmaslegacy;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
public abstract class DatabaseProcessor {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseProcessor(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;

		createTable();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

	public boolean getBooleanSafe(@NotNull ResultSet rs, @NotNull String column, boolean def) {
		try {
			return rs.getBoolean(column);
		} catch (SQLException e) {
			return def;
		}
	}

	public int getIntSafe(@NotNull ResultSet rs, @NotNull String column, int def) {
		try {
			return rs.getInt(column);
		} catch (SQLException e) {
			return def;
		}
	}

	public long getLongSafe(@NotNull ResultSet rs, @NotNull String column, long def) {
		try {
			return rs.getLong(column);
		} catch (SQLException e) {
			return def;
		}
	}

	public @Nullable String getStringSafe(@NotNull ResultSet rs, @NotNull String column, @Nullable String def) {
		try {
			String val = rs.getString(column);
			return val != null ? val : def;
		} catch (SQLException e) {
			return def;
		}
	}

    protected abstract void createTable();
}
