package net.unikit.database.interfaces;

/**
 * An interface that lets you store your database connection values (username, password, database name, etc.).
 * @author Andreas Berks
 * @since 1.2.1
 */
public interface DatabaseConfiguration {
    /**
     * Getter for the authentication user.
     * @return The authentication user
     */
    String getUsername();

    /**
     * Getter for the JDBC password for the connection.
     * @return The JDBC password for the connection
     */
    String getPassword();

    /**
     * Getter for the name of the database host.
     * @return The name of the database host
     */
    String getHostname();

    /**
     * Getter for the port of the database.
     * @return The port of the database
     */
    int getPort();

    /**
     * Getter for the name of the database schema.
     * @return The name of the database schema
     */
    String getSchema();

    /**
     * Setter for the name of the database schema.
     * @param schema The name of the database schema
     */
    void setSchema(String schema);

    /**
     * Getter for the SQL dialect of the underlying database.
     * @return The SQL dialect of the underlying database
     */
    String getDialect();

    /**
     * Getter for the database driver, giving the driver's class name.
     * @return The database driver, giving the driver's class name.
     */
    String getDriverClass();
}
