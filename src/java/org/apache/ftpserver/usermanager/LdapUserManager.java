/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * $Id$
 */
package org.apache.ftpserver.usermanager;


import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import javax.naming.directory.ModificationItem;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.ftpserver.util.StringUtils;

/**
 * Ldap based user manager class. Tested using Netscape Directory Server 4.1.
 * The LDAP requires the password to be nonempty for simple authentication. So
 * instead of using empty string password (""), we will be using single space (" ").
 * <br>
 * The required LDAP attribute types:
 * <ul>
 *   <li>memberuid</li>
 *   <li>uid</li>
 *   <li>cn</li>
 *   <li>sn</li>
 *   <li>userpassword</li>
 *   <li>objectclass</li>
 *   <li>enableflag (created by ftp-db.ldif file)</li>
 *   <li>homedirectory</li>
 *   <li>writepermission (created by ftp-db.ldif file)</li>
 *   <li>idletime (created by ftp-db.ldif file)</li>
 *   <li>uploadrate (created by ftp-db.ldif file)</li>
 *   <li>downloadrate (created by ftp-db.ldif file)</li>
 * </ul>
 *
 * Some of the above mentioned attribute types are created by ftd-db.ldif schema file.
 * The schema file also creates an object class called ftpUsers derived from
 * inetOrgPerson and have all these attributes.<br>
 * Assumed LDAP objectclass hierarchy:<br>
 * <pre>
 *        top
 *         |
 *       person
 *         |
 * organizationalPerson
 *         |
 *    inetOrgPerson
 *         |
 *      ftpUsers
 * </pre>
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.usermanager.UserManagerInterface"
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class LdapUserManager extends AbstractUserManager {


    // LDAP attributes
    private final static String LOGIN      = "memberuid";
    private final static String CN         = "cn";
    private final static String SN         = "sn";
    private final static String OBJ_CLASS  = "objectclass";
    
    private final static String[] ALL_ATTRS = {
        User.ATTR_LOGIN,
        User.ATTR_ENABLE,
        User.ATTR_HOME,
        User.ATTR_WRITE_PERM,
        User.ATTR_MAX_IDLE_TIME,
        User.ATTR_MAX_UPLOAD_RATE,
        User.ATTR_MAX_DOWNLOAD_RATE
    };

    private final static String[] UID_ATTRS = {
        User.ATTR_LOGIN
    };


    // Currently we are using only one connection.
    // So all the methods are synchronized.
    private DirContext mAdminContext;
    private Properties mAdminEnv;
    private String mstRoot;
    private String mstDnPrefix;
    private String mstDnSuffix;
    private Attribute mObjClassAttr;


    /**
     * Default constructor
     */
    public LdapUserManager() {
    }


    /**
     * Instantiate <code>UserManager</code> implementation.
     * Open LDAP connection.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

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
            mAdminEnv.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            mAdminEnv.setProperty(Context.PROVIDER_URL, url);
            mAdminEnv.setProperty(Context.SECURITY_AUTHENTICATION, auth);
            mAdminEnv.setProperty(Context.SECURITY_PRINCIPAL, admin);
            mAdminEnv.setProperty(Context.SECURITY_CREDENTIALS, password);
            mAdminContext = new InitialDirContext(mAdminEnv);


            // create objectClass attribute
            mObjClassAttr = new BasicAttribute(OBJ_CLASS, false);
            mObjClassAttr.add("ftpUsers");
            mObjClassAttr.add("inetOrgPerson");
            mObjClassAttr.add("organizationalPerson");
            mObjClassAttr.add("person");
            mObjClassAttr.add("top");

            getLogger().info("LDAP user manager opened.");
        }
        catch(NamingException ex) {
            throw new ConfigurationException("LdapUserManager.configure()", ex);
        }
    }


    /**
     * Get all user names.
     */
    public synchronized List getAllUserNames() {
        ArrayList allUsers = new ArrayList();

        try {
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(mObjClassAttr);
            NamingEnumeration answers = mAdminContext.search(mstRoot, matchAttrs, UID_ATTRS);
            while (answers.hasMore()) {
                SearchResult sr = (SearchResult)answers.next();
                String uid = sr.getAttributes().get(User.ATTR_LOGIN).get().toString();
                allUsers.add(uid);
            }
        }
        catch(Exception ex) {
            getLogger().error("LdapUserManager.getAllUserNames()", ex);
        }

        Collections.sort(allUsers);
        return allUsers;
    }


    /**
     * Get user object.
     */
    public synchronized User getUserByName(String name) {
        User user = null;

        try {
            String dn = getDN(name);
            Attributes attrs = mAdminContext.getAttributes(dn, ALL_ATTRS);

            user = new User();
            user.setName(attrs.get(User.ATTR_LOGIN).get().toString());
            user.getVirtualDirectory().setRootDirectory(new File(attrs.get(User.ATTR_HOME).get().toString()));
            user.setEnabled(Boolean.TRUE.toString().equals(attrs.get(User.ATTR_ENABLE).get().toString()));
            user.getVirtualDirectory().setWritePermission(Boolean.TRUE.toString().equals(attrs.get(User.ATTR_WRITE_PERM).get().toString()));  
            user.setMaxIdleTime( Integer.parseInt(attrs.get(User.ATTR_MAX_IDLE_TIME).get().toString()) );
            user.setMaxUploadRate( Integer.parseInt(attrs.get(User.ATTR_MAX_UPLOAD_RATE).get().toString()) );
            user.setMaxDownloadRate( Integer.parseInt(attrs.get(User.ATTR_MAX_DOWNLOAD_RATE).get().toString()) );
        }
        catch(Exception ex) {
            getLogger().error("LdapUserManager.getUserByName()", ex);
            user = null;
        }

        return user;
    }


    /**
     * User authentication.
     */
    public boolean authenticate(String login, String password) {

        // empty password string is not allowed
        if (password == null) {
            password = " ";
        }
        if (password.equals("")) {
            password = " ";
        }

        try {
            if( doesExist(login) ) {
                Properties userProp = (Properties)mAdminEnv.clone();
                String dn = getDN(login);
                userProp.setProperty(Context.SECURITY_PRINCIPAL, dn);
                userProp.setProperty(Context.SECURITY_CREDENTIALS, password);

                DirContext userContext = new InitialDirContext(userProp);
                userContext.close();
                return true;
            }
        }
        catch(NamingException ex) {
        }
        return false;
    }


    /**
     * Save user
     */
    public synchronized void save(User user) throws NamingException {
        if (doesExist(user.getName())) {
            update(user);
        }
        else {
            add(user);
        }
    }


    /**
     * Add a new user
     */
    private synchronized void add(User user) throws NamingException {

        // empty password is not allowed
        if (user.getPassword() == null) {
            user.setPassword(" ");
        }
        if (user.getPassword().equals("")) {
            user.setPassword(" ");
        }

        String dn = getDN(user.getName());

        Attributes attrs = new BasicAttributes(true);
        attrs.put(new BasicAttribute(LOGIN, user.getName()));
        attrs.put(new BasicAttribute(User.ATTR_LOGIN, user.getName()));
        attrs.put(new BasicAttribute(CN, user.getName()));
        attrs.put(new BasicAttribute(SN, user.getName()));
        attrs.put(new BasicAttribute(User.ATTR_PASSWORD, user.getPassword()));

        attrs.put(mObjClassAttr);

        attrs.put(new BasicAttribute(User.ATTR_ENABLE,            String.valueOf(user.getEnabled())));
        attrs.put(new BasicAttribute(User.ATTR_HOME,              user.getVirtualDirectory().getRootDirectory()));
        attrs.put(new BasicAttribute(User.ATTR_WRITE_PERM,        String.valueOf(user.getVirtualDirectory().getWritePermission())));
        attrs.put(new BasicAttribute(User.ATTR_MAX_IDLE_TIME,     String.valueOf(user.getMaxIdleTime())));
        attrs.put(new BasicAttribute(User.ATTR_MAX_UPLOAD_RATE,   String.valueOf(user.getMaxUploadRate())));
        attrs.put(new BasicAttribute(User.ATTR_MAX_DOWNLOAD_RATE, String.valueOf(user.getMaxDownloadRate())));

        mAdminContext.bind(dn, null, attrs);
    }


    /**
     * Update an existing user
     */
    private synchronized void update(User user) throws NamingException {
        String dn = getDN(user.getName());
        ArrayList mods = new ArrayList();

        if (user.getPassword() != null) {
            if (user.getPassword().equals("")) {
                user.setPassword(" ");
            }
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(User.ATTR_PASSWORD, user.getPassword())));
        }
        mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(User.ATTR_ENABLE,            String.valueOf(user.getEnabled()))));
        mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(User.ATTR_HOME,              user.getVirtualDirectory().getRootDirectory())));
        mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(User.ATTR_WRITE_PERM,        String.valueOf(user.getVirtualDirectory().getWritePermission()))));
        mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(User.ATTR_MAX_IDLE_TIME,     String.valueOf(user.getMaxIdleTime()))));
        mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(User.ATTR_MAX_UPLOAD_RATE,   String.valueOf(user.getMaxUploadRate()))));
        mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(User.ATTR_MAX_DOWNLOAD_RATE, String.valueOf(user.getMaxDownloadRate()))));


        ModificationItem modArr[] = new ModificationItem[mods.size()];
        for(int i=0; i<modArr.length; i++) {
            modArr[i] = (ModificationItem)mods.get(i);
        }
        mAdminContext.modifyAttributes(dn, modArr);
    }


    /**
     * User existance check
     */
    public synchronized boolean doesExist(String name) {
        boolean bExist = false;
        try {
            String dn = getDN(name);
            mAdminContext.getAttributes(dn, UID_ATTRS);
            bExist = true;
        }
        catch(NamingException ex) {
        }
        return bExist;
    }


    /**
     * Delete user
     */
    public synchronized void delete(String userName) throws NamingException {
        String dn = getDN(userName);
        mAdminContext.unbind(dn);
    }


    /**
     * Close user manager
     */
    public synchronized void dispose() {
        if (mAdminContext != null) {
            try {
                mAdminContext.close();
            }
            catch(NamingException ex) {
            }
            mAdminContext = null;
        }
    }

    /**
     * Get the distinguished name (DN) for this user name
     */
    private String getDN(String userName) throws NamingException {

        //escape special characters
        userName = StringUtils.replaceString(userName, "\\", "\\\\");
        userName = StringUtils.replaceString(userName, ",", "\\,");
        userName = StringUtils.replaceString(userName, "+", "\\+");
        userName = StringUtils.replaceString(userName, "\"", "\\\"");
        userName = StringUtils.replaceString(userName, "<", "\\<");
        userName = StringUtils.replaceString(userName, ">", "\\>");
        userName = StringUtils.replaceString(userName, ";", "\\;");

        return mstDnPrefix + userName + mstDnSuffix;
    }

}
