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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.ftpserver.util.StringUtils;

/**
 * This is another database based user manager class. I have
 * tested it using MySQL and Oracle database. The sql file is <code>ftp-db.sql</code>
 *
 * @phoenix:block
 * @phoenix:service name="org.apache.ftpserver.usermanager.UserManagerInterface"
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class DbUserManager extends AbstractUserManager {

    private Connection mDbConnection       = null;

    private String mInsUserStmt = null;
    private String mDelUserStmt = null;
    private String mSelUserStmt = null;
    private String mGetAllStmt  = null;
    private String mUpdUserStmt = null;
    
    private String mUrl      = null;
    private String mUser     = null;
    private String mPassword = null;


    /**
     * Instantiate user manager - default constructor.
     *
     * @param cfg Ftp config object.
     */
    public DbUserManager() throws Exception {
    }


    /**
     * Set configuration - open database connection
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        String className = conf.getChild("driver").getValue();
        mUrl          = conf.getChild("url").getValue();
        mUser         = conf.getChild("user").getValue();
        mPassword     = conf.getChild("password").getValue();
        mInsUserStmt  = conf.getChild("sql-insert").getValue();
        mDelUserStmt  = conf.getChild("sql-delete").getValue();
        mSelUserStmt  = conf.getChild("sql-select").getValue();
        mGetAllStmt   = conf.getChild("sql-all").getValue();
        mUpdUserStmt  = conf.getChild("sql-update").getValue();

        try {
            Class.forName(className);

            openDbConnection();
            getLogger().info("Database user manager opened.");
        }
        catch(Exception ex) {
            throw new ConfigurationException("DbUserManager.configure()", ex);
        }
    }

    /**
     * Open connection to database.
     */
    private void openDbConnection() throws SQLException {
        mDbConnection = DriverManager.getConnection(mUrl, mUser, mPassword);
        mDbConnection.setAutoCommit(true);
        getLogger().info("Connection opened.");
    }

    /**
     * Close connection to database.
     */
    private void closeDbConnection() {
        if (mDbConnection != null) {
            try {mDbConnection.close(); } catch(SQLException ex) {}
            mDbConnection = null;
        }

        getLogger().info("Connection closed.");
    }

    /**
     * Prepare connection to database.
     */
    private void prepareDbConnection() throws SQLException {
        boolean closed = false;
        try {
            if ( (null == mDbConnection) || mDbConnection.isClosed() ) {
                closed = true;
            }
        }
        catch ( final SQLException se ) {
            closed = true;
        }

        if ( closed ) {
            closeDbConnection();
            openDbConnection();
        }
    }

    /**
     * Delete user. Delete the row from the table.
     */
    public synchronized void delete(String name) throws SQLException {
        HashMap map = new HashMap();
        map.put(User.ATTR_LOGIN, name);
        String sql = StringUtils.replaceString(mDelUserStmt, map);
        
        prepareDbConnection();
        Statement stmt = mDbConnection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }


    /**
     * Save user. If new insert a new row, else update the existing row.
     */
    public synchronized void save(User user) throws SQLException {
        
        // null value check
        if(user.getName() == null) {
            throw new NullPointerException("User name is null.");
        } 
        
        prepareDbConnection();   
        
        HashMap map = new HashMap();
        map.put(User.ATTR_LOGIN, user.getName());
        map.put(User.ATTR_PASSWORD, getPassword(user));
        map.put(User.ATTR_HOME, user.getVirtualDirectory().getRootDirectory());
        map.put(User.ATTR_ENABLE, String.valueOf(user.getEnabled()));
        map.put(User.ATTR_WRITE_PERM, String.valueOf(user.getVirtualDirectory().getWritePermission()));
        map.put(User.ATTR_MAX_IDLE_TIME, new Long(user.getMaxIdleTime()));
        map.put(User.ATTR_MAX_UPLOAD_RATE, new Integer(user.getMaxUploadRate()));
        map.put(User.ATTR_MAX_DOWNLOAD_RATE, new Integer(user.getMaxDownloadRate())); 
        
        String sql = null;      
        if( !doesExist(user.getName()) ) {
            sql = StringUtils.replaceString(mInsUserStmt, map);
        }
        else {
            sql = StringUtils.replaceString(mUpdUserStmt, map);
        }
        
        Statement stmt = mDbConnection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }


    /**
     * Get the user object. Fetch the row from the table.
     */
    public synchronized User getUserByName(String name) {
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            User thisUser = null;
            HashMap map = new HashMap();
            map.put(User.ATTR_LOGIN, name);
            String sql = StringUtils.replaceString(mSelUserStmt, map);

            prepareDbConnection();
            stmt = mDbConnection.createStatement();
            rs = stmt.executeQuery(sql);
            
            if(rs.next()) {
                thisUser = new User();
                thisUser.setName(rs.getString(1));
                thisUser.getVirtualDirectory().setRootDirectory(new File(rs.getString(3)));
                thisUser.setEnabled(rs.getString(4).equals(Boolean.TRUE.toString()));
                thisUser.getVirtualDirectory().setWritePermission(rs.getString(5).equals(Boolean.TRUE.toString()));
                thisUser.setMaxIdleTime(rs.getInt(6));
                thisUser.setMaxUploadRate(rs.getInt(7));
                thisUser.setMaxDownloadRate(rs.getInt(8));
            }
            return thisUser;
        }
        catch(Exception ex) {
            getLogger().error("DbUserManager.getUserByName()", ex);
        }
        finally {
            if(rs != null) {
                try { rs.close(); } catch(Exception ex) {}
            }
            if(stmt != null) {
                try { stmt.close(); } catch(Exception ex) {}
            }
        }
        
        return null;
    }


    /**
     * User existance check
     */
    public synchronized boolean doesExist(String name) {

        boolean bValid = false;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            HashMap map = new HashMap();
            map.put(User.ATTR_LOGIN, name);
            String sql = StringUtils.replaceString(mSelUserStmt, map);
            
            prepareDbConnection();
            stmt = mDbConnection.createStatement();
            rs = stmt.executeQuery(sql);
            bValid = rs.next();
            
        }
        catch(Exception ex) {
            bValid = false;
            getLogger().error("DbUserManager.doesExist()", ex);
        }
        finally {
            if(rs != null) {
                try { rs.close(); } catch(Exception ex) {}
            }
            if(stmt != null) {
                try { stmt.close(); } catch(Exception ex) {}
            }
        }
        
        return bValid;
    }


    /**
     * Get all user names from the database.
     */
    public synchronized List getAllUserNames() {
        
        ArrayList names = new ArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = mGetAllStmt;
            
            prepareDbConnection();
            stmt = mDbConnection.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                names.add(rs.getString(1));
            }
        }
        catch(Exception ex) {
            getLogger().error("DbUserManager.getAllUserNames()", ex);
        }
        finally {
            if(rs != null) {
                try { rs.close(); } catch(Exception ex) {}
            }
            if(stmt != null) {
                try { stmt.close(); } catch(Exception ex) {}
            }
        }
        
        return names;
    }


    /**
     * Get user password.
     * <pre>
     * If the password value is not null
     *    password = new password
     * else
     *   if user does exist
     *     password = old password
     *   else
     *     password = ""
     * </pre>
     */
    private synchronized String getPassword(User user) throws SQLException {
        if (user.getPassword() != null) {
            return user.getPassword();
        }
        
        String password = "";
        HashMap map = new HashMap();
        map.put(User.ATTR_LOGIN, user.getName());
        String sql = StringUtils.replaceString(mSelUserStmt, map);
        
        prepareDbConnection();
        Statement stmt = mDbConnection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            password = rs.getString(2);
        }
        rs.close();
        stmt.close();
        
        if (password == null) {
            password = "";
        }
        return password;
    }

    /**
     * User authentication
     */
    public synchronized boolean authenticate(String user, String password) {
        
        String existPassword = null;
        
        try {
            HashMap map = new HashMap();
            map.put(User.ATTR_LOGIN, user);
            String sql = StringUtils.replaceString(mSelUserStmt, map);
            
            prepareDbConnection();
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                existPassword = rs.getString(2);
            }
            rs.close();
            stmt.close();
        }
        catch(Exception ex) {
            getLogger().error("DbUserManager.authenticate()", ex);
            return false;
        }
        
        if (existPassword == null) {
            existPassword = "";
        }
        
        return existPassword.equals(password);
    }


    /**
     * Close this user manager. Close the database statements and connection.
     */
    public synchronized void dispose() {
        closeDbConnection();
    }
}
