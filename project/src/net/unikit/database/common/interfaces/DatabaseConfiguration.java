package net.unikit.database.common.interfaces;

/**
 * Created by Andreas on 27.10.2015.
 */
public interface DatabaseConfiguration {
    String getUsername();

    String getPassword();

    String getHostname();

    int getPort();

    String getSchema();

    String getDialect();

    String getDriverClass();
}
