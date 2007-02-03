package org.apache.ftpserver;

import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;

public class ComponentBean extends Bean {

    private Configuration config;
    private Component component;
    private LogFactory logFactory;
    private Class clazz;
    
    public ComponentBean(Configuration config, Class clazz, LogFactory logFactory) {
        this.clazz = clazz;
        this.logFactory = logFactory;
        this.config = config;
    }
    
    public Object initBean() throws Exception {
        component = (Component) clazz.newInstance();
        
        component.setLogFactory(logFactory);
        component.configure(config);
        return component;
    }
    
    public Object getBean() {
        return component;
    }

    public void destroyBean() {
        component.dispose();
        component = null;
    }
    
}
