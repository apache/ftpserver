/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ftpserver.usermanager;

import java.util.ArrayList;
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

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ldap based user manager class where the object class is ftpusers. This has
 * been tested with OpenLDAP. The BaseUser object will be serialized in LDAP.
 * Here the assumption is that the java object schema is available (RFC 2713).
 */
public
class LdapUserManager extends AbstractUserManager implements Component {
    
    private final Logger LOG = LoggerFactory.getLogger(LdapUserManager.class);
    
    // LDAP attributes
    private final static String CN         = "cn";
    private final static String CLASS_NAME = "javaClassName";
    private final static String OBJ_CLASS  = "objectClass";
    
    private final static String[] CN_ATTRS = {
        CN
    };
    
    private String adminName;
    private DirContext adminContext;
    private String userBaseDn;
    private Attribute objClassAttr;

    
    /**
     * Instantiate LDAP based <code>UserManager</code> implementation.
     */
    public void configure(Configuration config) throws FtpException { 
        
        try {
            
            // get admin name
            adminName = config.getString("admin", "admin");
            
            // get ldap parameters
            String url      = config.getString("ldap-url");
            String admin    = config.getString("ldap-admin-dn");
            String password = config.getString("ldap-admin-password");
            String auth     = config.getString("ldap-authentication", "simple");

            userBaseDn    = config.getString("ldap-user-base-dn");
            
            // create connection
            Properties adminEnv = new Properties();
            adminEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            adminEnv.setProperty(Context.PROVIDER_URL, url);
            adminEnv.setProperty(Context.SECURITY_AUTHENTICATION, auth);             
            adminEnv.setProperty(Context.SECURITY_PRINCIPAL, admin);             
            adminEnv.setProperty(Context.SECURITY_CREDENTIALS, password);                     
            adminContext = new InitialDirContext(adminEnv);
            
            // create objectClass attribute
            objClassAttr = new BasicAttribute(OBJ_CLASS, false);
            objClassAttr.add("javaObject");
            objClassAttr.add("top");
            
            LOG.info("LDAP user manager opened.");
        }
        catch(FtpException ex) {
            throw ex;
        }
        catch(Exception ex) {
            LOG.error("LdapUserManager.configure()", ex);
            throw new FtpException("LdapUserManager.configure()", ex);
        }
    }
    
    /**
     * Get the admin name.
     */
    public String getAdminName() {
        return adminName;
    }
    
    /**
     * @return true if user with this login is administrator
     */
    public boolean isAdmin(String login) throws FtpException {
        return adminName.equals(login);
    }
    
    /**
     * Get all user names.
     */
    public synchronized String[] getAllUserNames() throws FtpException {
        
        try {
            // search ldap
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(objClassAttr);
            matchAttrs.put( new BasicAttribute(CLASS_NAME, BaseUser.class.getName()) );
            NamingEnumeration<SearchResult> answers = adminContext.search(userBaseDn, matchAttrs, CN_ATTRS);
            LOG.info("Getting all users under " + userBaseDn);
            
            // populate list
            ArrayList<String> allUsers = new ArrayList<String>();
            while (answers.hasMore()) {
                SearchResult sr = (SearchResult)answers.next();
                String cn = sr.getAttributes().get(CN).get().toString();
                allUsers.add(cn);
            }
            Collections.sort(allUsers);
            return allUsers.toArray(new String[0]);
        }
        catch(NamingException ex) {
            LOG.error("LdapUserManager.getAllUserNames()", ex);
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
            LOG.info("Getting user object for " + dn);
            user = (User)adminContext.lookup(dn);
        }
        catch(NamingException ex) {
            LOG.debug("Failed to retrive user: " + name, ex);
            user = null;
        }
        return user;
    }
    
    /**
     * User authentication.
     */
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        if(authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            
            String login = upauth.getUsername(); 
            String password = upauth.getPassword(); 
            
            if(login == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            
            if(password == null) {
                password = "";
            }
            
            User user;
            try {
                user = getUserByName(login);
            } catch (FtpException e) {
                throw new AuthenticationFailedException("Authentication failed", e); 
            }
            
            if(user != null && password.equals(user.getPassword())) {
                    return user;
            } else {
                    throw new AuthenticationFailedException("Authentication failed"); 
            }
        } else if(authentication instanceof AnonymousAuthentication) {
            try {
                if(doesExist("anonymous")) {
                    return getUserByName("anonymous");
                } else {
                    throw new AuthenticationFailedException("Authentication failed");
                }
            } catch (FtpException e) {
                throw new AuthenticationFailedException("Authentication failed", e);
            }
        } else {
            throw new IllegalArgumentException("Authentication not supported by this user manager");
        }

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
            LOG.info("Rebinding user " + dn);
            adminContext.rebind(dn, newUser, attrs);
        }
        catch(NamingException ex) {
            LOG.error("LdapUserManager.save()", ex);
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
            LOG.info("Unbinding " + dn);
            adminContext.unbind(dn);
        }
        catch(NamingException ex) {
            LOG.error("LdapUserManager.delete()", ex);
            throw new FtpException("LdapUserManager.delete()", ex);
        }
    }
    
    /**
     * Close user manager.
     */
    public synchronized void dispose() {
        if (adminContext != null) {
            try {
                adminContext.close();
            }
            catch(NamingException ex) {
            }
            adminContext = null;
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
        return CN + '=' + valBuf.toString() + ',' + userBaseDn;
    }
}    
