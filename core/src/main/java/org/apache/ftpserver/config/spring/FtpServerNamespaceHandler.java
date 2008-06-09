package org.apache.ftpserver.config.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registration point for FtpServer bean defintion parsers
 */
public class FtpServerNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * The FtpServer Spring config namespace
     */
    public static final String FTPSERVER_NS = "http://mina.apache.org/ftpserver/spring/v1";

   
    /**
     * Register the necessary element names with the appropriate
     * bean definition parser
     */
    public FtpServerNamespaceHandler() {
        registerBeanDefinitionParser("server", new ServerBeanDefinitionParser());        
        registerBeanDefinitionParser("nio-listener", new ListenerBeanDefinitionParser());        
        registerBeanDefinitionParser("file-user-manager", new UserManagerBeanDefinitionParser());        
        registerBeanDefinitionParser("db-user-manager", new UserManagerBeanDefinitionParser());        
        registerBeanDefinitionParser("native-filesystem", new FileSystemBeanDefinitionParser());        
        registerBeanDefinitionParser("commands", new CommandFactoryBeanDefinitionParser());
        
    }

    /**
     * {@inheritDoc}
     */
    public void init() {
        // do nothing
    }

}
