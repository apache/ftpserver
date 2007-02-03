package org.apache.ftpserver;

import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;

public abstract class Bean {

    public static Bean createBean(Configuration config, String defaultClass, LogFactory logFactory) throws Exception {
        String className = config.getString("class", defaultClass);
        
        Class clazz = Class.forName(className);
        
        boolean isComponent = Component.class.isAssignableFrom(clazz);
        
        if(isComponent) {
            return new ComponentBean(config, clazz, logFactory);
        } else {
            return new PojoBean(config, clazz, logFactory);
        }
    }
    
    public abstract Object initBean() throws Exception;

    public abstract void destroyBean();
    
    public abstract Object getBean();
    
    
    
}
