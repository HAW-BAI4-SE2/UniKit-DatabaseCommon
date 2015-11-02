package net.unikit.database.common.implementations;

import net.unikit.database.common.interfaces.DatabaseConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Andreas on 27.10.2015.
 */
final class DatabaseConfigurationImpl implements DatabaseConfiguration {
    private final String username;
    private final String password;
    private final String hostname;
    private final int port;
    private final String schema;
    private final String dialect;
    private final String driverClass;

    private void validate() throws NullPointerException {
        checkNotNull(username, "value of 'username' is null!");
        checkNotNull(password, "value of 'password' is null!");
        checkNotNull(hostname, "value of 'hostname' is null!");
        checkNotNull(schema, "value of 'schema' is null!");
        checkNotNull(dialect, "value of 'dialect' is null!");
        checkNotNull(driverClass, "value of 'driverClass' is null!");
    }

    private DatabaseConfigurationImpl(String username, String password, String hostname, int port, String schema, String dialect, String driverClass) throws NullPointerException {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
        this.schema = schema;
        this.dialect = dialect;
        this.driverClass = driverClass;
        validate();
    }

    public static DatabaseConfiguration create(String username, String password, String hostname, int port, String schema, String dialect, String driverClass) throws NullPointerException {
        return new DatabaseConfigurationImpl(username, password, hostname, port, schema, dialect, driverClass);
    }

    public static DatabaseConfiguration create(Properties properties) throws NullPointerException, IllegalArgumentException {
        checkNotNull(properties, "value of 'properties' is null!");

        checkArgument(properties.containsKey("USERNAME"), "key 'USERNAME' is missing!");
        checkArgument(properties.containsKey("PASSWORD"), "key 'PASSWORD' is missing!");
        checkArgument(properties.containsKey("HOSTNAME"), "key 'HOSTNAME' is missing!");
        checkArgument(properties.containsKey("PORT"), "key 'PORT' is missing!");
        checkArgument(properties.containsKey("SCHEMA"), "key 'SCHEMA' is missing!");
        checkArgument(properties.containsKey("DIALECT"), "key 'DIALECT' is missing!");
        checkArgument(properties.containsKey("DRIVER_CLASS"), "key 'DRIVER_CLASS' is missing!");

        String username = properties.getProperty("USERNAME");
        String password = properties.getProperty("PASSWORD");
        String hostname = properties.getProperty("HOSTNAME");
        int port = Integer.parseInt(properties.getProperty("PORT"));
        String schema = properties.getProperty("SCHEMA");
        String dialect = properties.getProperty("DIALECT");
        String driverClass = properties.getProperty("DRIVER_CLASS");

        return create(username, password, hostname, port, schema, dialect, driverClass);
    }

    public static DatabaseConfiguration createFromProperties(InputStream inputStream) throws NullPointerException, IOException, IllegalArgumentException {
        checkNotNull(inputStream, "value of 'inputStream' is null!");

        Properties properties = new Properties();
        properties.load(inputStream);

        return create(properties);
    }

    public static DatabaseConfiguration createFromProperties(String filename) throws NullPointerException, IOException, IllegalArgumentException {
        checkNotNull(filename, "value of 'filename' is null!");

        InputStream inputStream = new FileInputStream(filename);
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        return create(properties);
    }

    public static DatabaseConfiguration createFromXML(InputStream inputStream) throws NullPointerException, IOException, IllegalArgumentException {
        checkNotNull(inputStream, "value of 'inputStream' is null!");

        Properties properties = new Properties();
        properties.loadFromXML(inputStream);

        return create(properties);
    }

    public static DatabaseConfiguration createFromXML(String filename) throws NullPointerException, IOException, IllegalArgumentException {
        checkNotNull(filename, "value of 'filename' is null!");

        InputStream inputStream = new FileInputStream(filename);
        Properties properties = new Properties();
        properties.loadFromXML(inputStream);
        inputStream.close();

        return create(properties);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getDialect() {
        return dialect;
    }

    @Override
    public String getDriverClass() {
        return driverClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseConfigurationImpl)) return false;

        DatabaseConfigurationImpl that = (DatabaseConfigurationImpl) o;

        if (getPort() != that.getPort()) return false;
        if (!getUsername().equals(that.getUsername())) return false;
        if (!getPassword().equals(that.getPassword())) return false;
        if (!getHostname().equals(that.getHostname())) return false;
        if (!getSchema().equals(that.getSchema())) return false;
        if (!getDialect().equals(that.getDialect())) return false;
        return getDriverClass().equals(that.getDriverClass());

    }

    @Override
    public int hashCode() {
        int result = getUsername().hashCode();
        result = 31 * result + getPassword().hashCode();
        result = 31 * result + getHostname().hashCode();
        result = 31 * result + getPort();
        result = 31 * result + getSchema().hashCode();
        result = 31 * result + getDialect().hashCode();
        result = 31 * result + getDriverClass().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DatabaseConfiguration{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", schema='" + schema + '\'' +
                ", dialect='" + dialect + '\'' +
                ", driverClass='" + driverClass + '\'' +
                '}';
    }
}
