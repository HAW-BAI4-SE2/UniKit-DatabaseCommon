package net.unikit.database.implementations;

import net.unikit.database.interfaces.DatabaseConfiguration;
import net.unikit.database.interfaces.AbstractDatabaseManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractDatabaseManagerImpl implements AbstractDatabaseManager {
    private final DatabaseConfiguration databaseConfiguration;
    private SessionFactory sessionFactory;

    private void validate() throws NullPointerException {
        checkNotNull(databaseConfiguration, "value of 'databaseConfiguration' is null!");
        checkArgument(databaseConfiguration.getDialect().equals("org.hibernate.dialect.MySQLDialect"),
                "no support for dialect '" + databaseConfiguration.getDialect() + "'!");
        checkArgument(databaseConfiguration.getDriverClass().equals("com.mysql.jdbc.Driver"),
                "no support for driver class '" + databaseConfiguration.getDriverClass() + "'!");
    }

    private void init() {
        String connectionUrl = "jdbc:mysql://" + databaseConfiguration.getHostname() + ":" +
                databaseConfiguration.getPort() + "/" + databaseConfiguration.getSchema();

        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.dialect", databaseConfiguration.getDialect());
        configuration.setProperty("hibernate.connection.driver_class", databaseConfiguration.getDriverClass());
        configuration.setProperty("hibernate.connection.url", connectionUrl);
        configuration.setProperty("hibernate.connection.username", databaseConfiguration.getUsername());
        configuration.setProperty("hibernate.connection.password", databaseConfiguration.getPassword());
        configuration.setProperty("hibernate.enable_lazy_load_no_trans", "true");

        registerAnnotatedClasses(configuration);

        sessionFactory = configuration.buildSessionFactory();
    }

    private AbstractDatabaseManagerImpl(DatabaseConfiguration databaseConfiguration) throws NullPointerException {
        this.databaseConfiguration = databaseConfiguration;
        this.sessionFactory = null;
        validate();
        init();
    }

    protected abstract void registerAnnotatedClasses(Configuration configuration);
}
