package notnekodev.barium.database;

import notnekodev.barium.Barium;

import java.io.File;
import java.sql.*;
import java.util.concurrent.*;

public class Database {
    private final String database_path;

    private Connection connection;
    private ExecutorService executor;

    public Database(String database_path) {
        this.database_path = database_path;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void connect() throws SQLException {
        File file = new File(database_path);
        file.getParentFile().mkdirs();

        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        connection.setAutoCommit(true);

        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "barium-sqlite");
            t.setDaemon(true);
            return t;
        });

        Barium.getInstance().logger.info("Connected to SQLite database {}", file.getAbsolutePath());
    }

    public void close() {
        if (executor != null) {
            executor.shutdown();

            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    Barium.getInstance().logger.error("Failed to shutdown database executor service in time!");
                }
            } catch (InterruptedException e) {
                Barium.getInstance().logger.warn("Failed to shutdown executor service during database closing: {}", e.getMessage());
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Barium.getInstance().logger.warn("Failed to close SQLite database: {}", e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public CompletableFuture<Void> execute(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);

                statement.execute();
            } catch (SQLException e) {
                Barium.getInstance().logger.warn("Failed to execute SQL command: {} ({})", e.getMessage(), e.getSQLState());
                throw new CompletionException(e);
            }
        }, executor);
    }

    public <T> CompletableFuture<T> query(String sql, ResultMapper<T> mapper, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapper.map(resultSet);
                }
            } catch (SQLException e) {
                Barium.getInstance().logger.warn("Failed to query SQL: {} ({})", e.getMessage(), e.getSQLState());
                throw new CompletionException(e);
            }
        }, executor);
    }

    public CompletableFuture<Integer> update(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() ->  {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);

                return statement.executeUpdate();
            } catch (SQLException e) {
                Barium.getInstance().logger.warn("Failed to update SQL call: {} ({})", e.getMessage(), e.getSQLState());
                throw new CompletionException(e);
            }
        }, executor);
    }

    public CompletableFuture<Void> batch(String sql, Iterable<Object[]> batch) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Object[] params : batch) {
                    bind(statement, params);

                    statement.addBatch();
                }
            } catch (SQLException e) {
                Barium.getInstance().logger.warn("Failed to batch SQL calls: {} ({})", e.getMessage(), e.getSQLState());
                throw new CompletionException(e);
            }
        }, executor);
    }

    public CompletableFuture<Void> transaction(Transaction transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.setAutoCommit(false);
                transaction.execute(connection);
                connection.commit();
            } catch (Exception e) {
                Barium.getInstance().logger.warn("Failed to complete transaction: {}", e.getMessage());
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    Barium.getInstance().logger.warn("Failed to rollback during error in transaction: {} ({})", e.getMessage(), ex.getSQLState());
                }

                throw new CompletionException(e);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    Barium.getInstance().logger.warn("Failed to reactivate auto commiting after transaction: {} ({})", e.getMessage(), e.getSQLState());
                }
            }
        }, executor);
    }

    private void bind(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}
