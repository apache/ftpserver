package org.apache.ftpserver.usermanager;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.ftpserver.util.BaseProperties;

import java.io.File;
import java.io.IOException;

/**
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.usermanager.UserManagerInterface"
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvalonPropertiesUserManager extends PropertiesUserManager implements Configurable, Contextualizable, LogEnabled {
    private Logger logger;

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set application context
     */
    public void contextualize(Context context) throws ContextException {
        try {
            File appDir = (File)context.get("app.home");
            if(!appDir.exists()) {
                appDir.mkdirs();
            }
            mUserDataFile = new File(appDir, USER_PROP);

            mUserDataFile.createNewFile();
            mUserData = new BaseProperties(mUserDataFile);
            mlLastModified = mUserDataFile.lastModified();
            logger.info("Loaded user data file - " + mUserDataFile);
        }
        catch(IOException ex) {
            logger.error(ex.getMessage(), ex);
            throw new ContextException(ex.getMessage());
        }
    }

    /**
     * Set configuration
     */
    public void configure(Configuration conf) throws ConfigurationException {
        mbEncrypt = conf.getChild("encrypt").getValueAsBoolean(false);
        Configuration adminConf = conf.getChild("ftp-admin-name", false);
        mstAdminName = "admin";
        if(adminConf != null) {
            mstAdminName = adminConf.getValue(mstAdminName);
        }

    }
}
