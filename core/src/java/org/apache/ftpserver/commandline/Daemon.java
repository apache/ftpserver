package org.apache.ftpserver.commandline;

import java.io.FileInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ConfigurableFtpServerContext;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.config.PropertiesConfiguration;
import org.apache.ftpserver.config.XmlConfigurationHandler;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.EmptyConfiguration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.util.IoUtils;

/**
 * Invokes FtpServer as a daemon, running in the background. 
 * Used for example for the Windows service.
 */
public class Daemon {

    private static Log log = LogFactory.getLog(Daemon.class);
    
    private static FtpServer server;
    private static Object lock = new Object();
    
    public static void main(String[] args) throws Exception {
        try{
            if(server == null) {
                // get configuration
                Configuration config = getConfiguration(args);
                if(config == null) {
                    log.error("No configuration provided");
                    throw new FtpException("No configuration provided");
                }
    
                // create root configuration object
                FtpServerContext serverContext = new ConfigurableFtpServerContext(config);
    
                // start the server
                server = new FtpServer(serverContext);  
            }
            
            String command = "start";
            
            if(args != null && args.length > 0) {
                command = args[0];
            }
            
            
            if(command.equals("start")) {
                log.info("Starting FTP server daemon");
                server.start();
                
                synchronized (lock) {
                    lock.wait();
                }
            } else if(command.equals("stop")) {
                synchronized (lock) {
                    lock.notify();
                }
                log.info("Stopping FTP server daemon");
                server.stop();
            }
        } catch(Throwable t) {
            log.error("Daemon error", t);
        }
    }

    /**
     * Get the configuration object.
     */
    private static Configuration getConfiguration(String[] args) throws Exception {
    
        Configuration config = null;
        FileInputStream in = null;
        try {
            if(args == null || args.length < 2) {
                log.info("Using default configuration....");
                config = EmptyConfiguration.INSTANCE;
            }
            else if( (args.length == 2) && args[1].equals("-default") ) {
                log.info("Using default configuration....");
                config = EmptyConfiguration.INSTANCE;
            }
            else if( (args.length == 3) && args[1].equals("-xml") ) {
                log.info("Using xml configuration file " + args[2] + "...");
                in = new FileInputStream(args[2]);
                XmlConfigurationHandler xmlHandler = new XmlConfigurationHandler(in);
                config = xmlHandler.parse();
            }
            else if( (args.length == 3) && args[1].equals("-prop") ) {
                log.info("Using properties configuration file " + args[2] + "...");
                in = new FileInputStream(args[2]);
                config = new PropertiesConfiguration(in);
            } else {
                throw new FtpException("Invalid configuration option");
            }
        }
        finally {
            IoUtils.close(in);
        }
        
        return config;
    }
}
