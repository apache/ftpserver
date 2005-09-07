// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.usermanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Logger;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;

/** 
 * Ldap based user manager class where the object class is ftpusers. This has
 * been tested with OpenLDAP. The BaseUser object will be serialized in LDAP. 
 * Here the assumption is that the java object schema is available (RFC 2713). 
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class LdapUserManager implements UserManager {
    
    // LDAP attributes
    private final static String CN         = "cn";
    private final static String CLASS_NAME = "javaClassName";
    private final static String OBJ_CLASS  = "objectClass";
    
    private final static String[] CN_ATTRS = {
        CN
    };
    
    private String m_adminName;
    private DirContext m_adminContext;
    private String m_userBaseDn;
    private Attribute m_objClassAttr;
    private Logger m_logger;
    
    
    /**
     * Set logger.
     */
    public void setLogger(Logger logger) {
        m_logger = logger;
    }
    
    /**
     * Instantiate LDAP based <code>UserManager</code> implementation.
     */
    public void configure(Configuration config) throws FtpException { 
        
        try {
            
            // get admin name 
            m_adminName = config.getString("admin", "admin");
            
            // get ldap parameters
            String url      = config.getString("ldap-url");
            String admin    = config.getString("ldap-admin-dn");
            String password = config.getString("ldap-admin-password");
            String auth     = config.getString("ldap-authentication", "simple");

            m_userBaseDn    = config.getString("ldap-user-base-dn");
            
            // create connection
            Properties adminEnv = new Properties();
            adminEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            adminEnv.setProperty(Context.PROVIDER_URL, url);
            adminEnv.setProperty(Context.SECURITY_AUTHENTICATION, auth);             
            adminEnv.setProperty(Context.SECURITY_PRINCIPAL, admin);             
            adminEnv.setProperty(Context.SECURITY_CREDENTIALS, password);                     
            m_adminContext = new InitialDirContext(adminEnv);
            
            // create objectClass attribute
            m_objClassAttr = new BasicAttribute(OBJ_CLASS, false);
            m_objClassAttr.add("javaObject");
            m_objClassAttr.add("top");
            
            m_logger.info("LDAP user manager opened.");
        }
        catch(FtpException ex) {
            throw ex;
        }
        catch(Exception ex) {
            m_logger.error("LdapUserManager.configure()", ex);
            throw new FtpException("LdapUserManager.configure()", ex);
        }
    }
    
    /**
     * Get the admin name.
     */
    public String getAdminName() {
        return m_adminName;
    }
    
    /**
     * Get all user names.
     */
    public synchronized Collection getAllUserNames() throws FtpException {
        
        try {
            // search ldap
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(m_objClassAttr);
            matchAttrs.put( new BasicAttribute(CLASS_NAME, BaseUser.class.getName()) );
            NamingEnumeration answers = m_adminContext.search(m_userBaseDn, matchAttrs, CN_ATTRS);
            m_logger.info("Getting all users under " + m_userBaseDn);
            
            // populate list
            ArrayList allUsers = new ArrayList();
            while (answers.hasMore()) {
                SearchResult sr = (SearchResult)answers.next();
                String cn = sr.getAttributes().get(CN).get().toString();
                allUsers.add(cn);
            }
            Collections.sort(allUsers);
            return allUsers;
        }
        catch(NamingException ex) {
            m_logger.error("LdapUserManager.getAllUserNames()", ex);
            throw new FtpException("LdapUserManager.getAllUserNames()", ex);
        }
    } 
    
    /**
     * Get user object.
     */
    public synchronized User getUserByName(String name) throws FtpException {
        
        User user = null;
        try {
            String dn = getDN(name);
            m_logger.info("Getting user object for " + dn);
            user = (User)m_adminContext.lookup(dn);
        }
        catch(NamingException ex) {
            user = null;
        }
        return user;
    }
    
    /**
     * User authentication.
     */
    public boolean authenticate(String login, String password) throws FtpException {
        boolean success = false;
        User user = getUserByName(login);
        if(user != null) {
            success = (password != null) &&
                      (password.equals(user.getPassword()));
        }
        return success;
    }
    
    /**
     * Save user.
     */
    public synchronized void save(User user) throws FtpException {
        try {
            String name = user.getName();
            String dn = getDN(name);
            BaseUser newUser = new BaseUser(user);
            
            // if password is not available, 
            // do not change the existing password
            User existUser = getUserByName(name);
            if( (existUser != null) && (newUser.getPassword() == null) ) {
                newUser.setPassword(existUser.getPassword());
            }

            // set attributes
            Attributes attrs = new BasicAttributes(true);
            attrs.put(new BasicAttribute(CN, name));
            attrs.put(new BasicAttribute(CLASS_NAME, BaseUser.class.getName()));
            
            // bind object
            m_logger.info("Rebinding user " + dn);
            m_adminContext.rebind(dn, newUser, attrs);
        }
        catch(NamingException ex) {
            m_logger.error("LdapUserManager.save()", ex);
            throw new FtpException("LdapUserManager.save()", ex);
        }
    }
    
    /**
     * User existance check.
     */
    public synchronized boolean doesExist(String name) throws FtpException {
        return getUserByName(name) != null;
    }
    
    /**
     * Delete user.
     */
    public synchronized void delete(String userName) throws FtpException {
        try {
            String dn = getDN(userName);
            m_logger.info("Unbinding " + dn);
            m_adminContext.unbind(dn);
        }
        catch(NamingException ex) {
            m_logger.error("LdapUserManager.delete()", ex);
            throw new FtpException("LdapUserManager.delete()", ex);
        }
    }
    
    /**
     * Close user manager.
     */
    public synchronized void dispose() {
        if (m_adminContext != null) {
            try {
                m_adminContext.close();
            }
            catch(NamingException ex) {
            }
            m_adminContext = null;
        }
    }
    
    /**
     * Get the distinguished name (DN) for this user name.
     */
    private String getDN(String userName) throws NamingException {
        
        StringBuffer valBuf = new StringBuffer(userName);
        for (int i=0; i<valBuf.length(); i++) {
            char ch = valBuf.charAt(i);
            if (ch == '\\' || 
                ch == ','  || 
                ch == '+'  ||
                ch == '\"' || 
                ch == '<'  || 
                ch == '>'  || 
                ch == ';'  ) {
                valBuf.insert(i, '\\'); 
                i++;
            }
        }
        return CN + '=' + valBuf.toString() + ',' + m_userBaseDn;
    }
}    
