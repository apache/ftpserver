package org.apache.ftpserver.config;

import java.util.Properties;

import org.apache.ftpserver.ftplet.Configuration;

public class PropertiesConfigurationTest extends ConfigurationTestTemplate {

    protected Configuration createConfiguration() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("config.socket-factory.address", "localhost");
        properties.setProperty("config.socket-factory.booltrue", "true");
        properties.setProperty("config.socket-factory.boolfalse", "false");
        properties.setProperty("config.socket-factory.port", "21");
        properties.setProperty("config.socket-factory.double", "1.234");
        properties.setProperty("config.socket-factory.ssl.ssl-protocol", "TLS");
        properties.setProperty("config.socket-factory.ssl.client-authentication", "false");
        properties.setProperty("config.empty", "");

        return new PropertiesConfiguration(properties);
    }
    

}
