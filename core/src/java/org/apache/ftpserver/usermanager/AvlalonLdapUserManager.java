package org.apache.ftpserver.usermanager;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.ftpserver.core.AvalonUserManagerMonitor;
import org.apache.ftpserver.core.AvalonUserManagerMonitor;

import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;

/**
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.usermanager.UserManagerInterface"
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvlalonLdapUserManager extends LdapUserManager implements Configurable, LogEnabled {

    private Logger logger;

    public void enableLogging(Logger logger) {
        this.logger = logger;
        userManagerMonitor = new AvalonUserManagerMonitor(logger);
    }

    /**
     * Instantiate <code>UserManager</code> implementation.
     * Open LDAP connection.
     */
    public void configure(Configuration conf) throws ConfigurationException {

        // get ldap parameters
        String url      = conf.getChild("url").getValue();
        String admin    = conf.getChild("admin").getValue();
        String password = conf.getChild("password").getValue();
        String auth     = conf.getChild("authentication").getValue();

        mstRoot     = conf.getChild("root").getValue();
        mstDnPrefix = conf.getChild("prefix").getValue();
        mstDnSuffix = conf.getChild("suffix").getValue();

        try {
            mAdminEnv = new Properties();
            mAdminEnv.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            mAdminEnv.setProperty(javax.naming.Context.PROVIDER_URL, url);
            mAdminEnv.setProperty(javax.naming.Context.SECURITY_AUTHENTICATION, auth);
            mAdminEnv.setProperty(javax.naming.Context.SECURITY_PRINCIPAL, admin);
            mAdminEnv.setProperty(javax.naming.Context.SECURITY_CREDENTIALS, password);
            mAdminContext = new InitialDirContext(mAdminEnv);


            // create objectClass attribute
            mObjClassAttr = new BasicAttribute(OBJ_CLASS, false);
            mObjClassAttr.add("ftpUsers");
            mObjClassAttr.add("inetOrgPerson");
            mObjClassAttr.add("organizationalPerson");
            mObjClassAttr.add("person");
            mObjClassAttr.add("top");

            logger.info("LDAP user manager opened.");
        }
        catch(NamingException ex) {
            throw new ConfigurationException("LdapUserManager.configure()", ex);
        }
        Configuration adminConf = conf.getChild("ftp-admin-name", false);
        mstAdminName = "admin";
        if(adminConf != null) {
            mstAdminName = adminConf.getValue(mstAdminName);
        }

    }


}
