package org.apache.ftpserver.usermanager;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 * @author Paul Hammant
 * @version $Revision$
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.usermanager.UserManagerInterface"
 */
public class AvalonDbUserManager extends DbUserManager implements Configurable {
    /**
     * Set configuration - open database connection
     */
    public void configure(Configuration conf) throws ConfigurationException {

        String className = conf.getChild("driver").getValue();
        mUrl = conf.getChild("url").getValue();
        mUser = conf.getChild("user").getValue();
        mPassword = conf.getChild("password").getValue();
        mInsUserStmt = conf.getChild("sql-insert").getValue();
        mDelUserStmt = conf.getChild("sql-delete").getValue();
        mSelUserStmt = conf.getChild("sql-select").getValue();
        mGetAllStmt = conf.getChild("sql-all").getValue();
        mUpdUserStmt = conf.getChild("sql-update").getValue();

        try {
            Class.forName(className);

            openDbConnection();
        } catch (Exception ex) {
            throw new ConfigurationException("DbUserManager.configure()", ex);
        }
        Configuration adminConf = conf.getChild("ftp-admin-name", false);
        mstAdminName = "admin";
        if (adminConf != null) {
            mstAdminName = adminConf.getValue(mstAdminName);
        }


    }


}
