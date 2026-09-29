package com.ecommerece_rag_service.config;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;
import javax.sql.DataSource;

final class SqliteVecDataSource implements DataSource {

	private final DataSource delegate;
	private final String extensionPath;

	SqliteVecDataSource(DataSource delegate, String extensionPath) {
		this.delegate = Objects.requireNonNull(delegate);
		if (extensionPath == null || extensionPath.isBlank()) {
			throw new IllegalStateException(
					"app.sqlite-vec.extension-path must point to the sqlite-vec native library when sqlite-vec is enabled.");
		}

		Path library = Path.of(extensionPath).toAbsolutePath().normalize();
		if (!Files.isRegularFile(library)) {
			throw new IllegalStateException("sqlite-vec native library does not exist: " + library);
		}
		this.extensionPath = library.toString();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return loadExtension(delegate.getConnection());
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return loadExtension(delegate.getConnection(username, password));
	}

	private Connection loadExtension(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT load_extension(?)")) {
			statement.setString(1, extensionPath);
			statement.execute();
			return connection;
		} catch (SQLException exception) {
			try {
				connection.close();
			} catch (SQLException closeException) {
				exception.addSuppressed(closeException);
			}
			throw exception;
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return delegate.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		delegate.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		delegate.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return delegate.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return delegate.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return iface.cast(this);
		}
		return delegate.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this) || delegate.isWrapperFor(iface);
	}
}
