package net.unikit.database.common.implementations;

import net.unikit.database.common.interfaces.DatabaseConfiguration;

import java.io.*;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Andreas on 27.10.2015.
 */
public final class DatabaseConfigurationUtils {
    private DatabaseConfigurationUtils() {
    }

    public static DatabaseConfiguration createDatabaseConfiguration(String username, String password, String hostname, int port, String schema, String dialect, String driverClass) throws NullPointerException {
        return DatabaseConfigurationImpl.create(username, password, hostname, port, schema, dialect, driverClass);
    }

    public static DatabaseConfiguration createDatabaseConfiguration(Properties properties) throws NullPointerException, IllegalArgumentException {
        return DatabaseConfigurationImpl.create(properties);
    }

    public static DatabaseConfiguration createDatabaseConfigurationFromProperties(InputStream inputStream) throws NullPointerException, IOException, IllegalArgumentException {
        return DatabaseConfigurationImpl.createFromProperties(inputStream);
    }

    public static DatabaseConfiguration createDatabaseConfigurationFromProperties(String filename) throws NullPointerException, IOException, IllegalArgumentException {
        return DatabaseConfigurationImpl.createFromProperties(filename);
    }

    public static DatabaseConfiguration createDatabaseConfigurationFromXML(InputStream inputStream) throws NullPointerException, IOException, IllegalArgumentException {
        return DatabaseConfigurationImpl.createFromXML(inputStream);
    }

    public static DatabaseConfiguration createDatabaseConfigurationFromXML(String filename) throws NullPointerException, IOException, IllegalArgumentException {
        return DatabaseConfigurationImpl.createFromXML(filename);
    }

    public static void storeDefaultDatabaseConfigurationToProperties(String filename, String comment) throws NullPointerException, IOException {
        checkNotNull(filename, "value of 'filename' is null!");
        checkNotNull(comment, "value of 'comment' is null!");

        String username = "root";
        String password = "root123";
        String hostname = "localhost";
        int port = 3306;
        String schema = "test";
        String dialect = "org.hibernate.dialect.MySQLDialect";
        String driverClass = "com.mysql.jdbc.Driver";

        Properties properties = new Properties();
        properties.setProperty("USERNAME", username);
        properties.setProperty("PASSWORD", password);
        properties.setProperty("HOSTNAME", hostname);
        properties.setProperty("PORT", String.valueOf(port));
        properties.setProperty("SCHEMA", schema);
        properties.setProperty("DIALECT", dialect);
        properties.setProperty("DRIVER_CLASS", driverClass);

        OutputStream outputStream = new FileOutputStream(filename);;
        properties.store(outputStream, comment);
        outputStream.close();
    }

    public static void storeDefaultDatabaseConfigurationToXML(String filename, String comment) throws NullPointerException, IOException {
        checkNotNull(filename, "value of 'filename' is null!");
        checkNotNull(comment, "value of 'comment' is null!");

        String username = "root";
        String password = "root123";
        String hostname = "localhost";
        int port = 3306;
        String schema = "test";
        String dialect = "org.hibernate.dialect.MySQLDialect";
        String driverClass = "com.mysql.jdbc.Driver";

        Properties properties = new Properties();
        properties.setProperty("USERNAME", username);
        properties.setProperty("PASSWORD", password);
        properties.setProperty("HOSTNAME", hostname);
        properties.setProperty("PORT", String.valueOf(port));
        properties.setProperty("SCHEMA", schema);
        properties.setProperty("DIALECT", dialect);
        properties.setProperty("DRIVER_CLASS", driverClass);

        OutputStream outputStream = new FileOutputStream(filename);;
        properties.storeToXML(outputStream, comment);
        outputStream.close();
    }
}
