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
import java.io.IOException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.ftpserver.util.IoUtils;

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

    private static final String GET_ALL_USERS_SQL = "SELECT LOGIN_ID FROM FTP_USER";
    private static final String GET_USER_SQL      = "SELECT * FROM FTP_USER WHERE LOGIN_ID = ?";
    private static final String NEW_USER_SQL      = "INSERT INTO FTP_USER VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_USER_SQL   = "UPDATE FTP_USER SET PASSWORD = ?, HOME_DIR = ?, ENABLED = ?, WRITE_PERM = ?, IDLE_TIME = ?, UPLOAD_RATE = ?, DOWNLOAD_RATE = ? WHERE LOGIN_ID = ?";
    private static final String DELETE_USER_SQL   = "DELETE FROM FTP_USER WHERE LOGIN_ID = ?";

    private String m_dbUrl;
    private String m_dbUser;
    private String m_dbPassword;
    private Connection mDbConnection = null;

    private PreparedStatement mNewUserStmt = null;
    private PreparedStatement mDelUserStmt = null;
    private PreparedStatement mGetUserStmt = null;
    private PreparedStatement mGetAllStmt  = null;
    private PreparedStatement mUpdUserStmt = null;


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

        // open database connection
        String className = conf.getChild("driver").getValue();
        m_dbUrl = conf.getChild("url").getValue();
        m_dbUser = conf.getChild("user").getValue();
        m_dbPassword = conf.getChild("password").getValue();

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
    private void openDbConnection()
        throws SQLException
    {
        mDbConnection = DriverManager.getConnection(m_dbUrl, m_dbUser, m_dbPassword);
        mDbConnection.setAutoCommit(true);

        // prepare statements
        mGetAllStmt = mDbConnection.prepareStatement( GET_ALL_USERS_SQL );
        mGetUserStmt = mDbConnection.prepareStatement( GET_USER_SQL );
        mNewUserStmt = mDbConnection.prepareStatement( NEW_USER_SQL );
        mUpdUserStmt = mDbConnection.prepareStatement( UPDATE_USER_SQL );
        mDelUserStmt = mDbConnection.prepareStatement( DELETE_USER_SQL );

        getLogger().info("Connection opened.");
    }

    /**
     * Close connection to database.
     */
    private void closeDbConnection()
    {
        if (mNewUserStmt != null) {
            try {mNewUserStmt.close(); } catch(SQLException ex) {}
            mNewUserStmt = null;
        }

        if (mDelUserStmt != null) {
            try {mDelUserStmt.close(); } catch(SQLException ex) {}
            mDelUserStmt = null;
        }

        if (mGetUserStmt != null) {
            try {mGetUserStmt.close(); } catch(SQLException ex) {}
            mGetUserStmt = null;
        }

        if (mGetAllStmt != null) {
            try {mGetAllStmt.close(); } catch(SQLException ex) {}
            mGetAllStmt = null;
        }

        if (mUpdUserStmt != null) {
            try {mUpdUserStmt.close(); } catch(SQLException ex) {}
            mUpdUserStmt = null;
        }

        if (mDbConnection != null) {
            try {mDbConnection.close(); } catch(SQLException ex) {}
            mDbConnection = null;
        }

        getLogger().info("Connection closed.");
    }

    /**
     * Prepare connection to database.
     */
    private void prepareDbConnection()
        throws SQLException
    {
        boolean closed = false;
        try
        {
            //FIXME: better connection check.
            if ( null == mDbConnection || mDbConnection.isClosed() )
            {
                closed = true;
            }
        }
        catch ( final SQLException se )
        {
            closed = true;
        }

        if ( closed )
        {
            closeDbConnection();
            openDbConnection();
        }
    }

    /**
     * Delete user. Delete the row from the table.
     */
    public synchronized void delete(String name) throws SQLException {
        prepareDbConnection();

        mDelUserStmt.setString(1, name);
        mDelUserStmt.executeUpdate();
    }


    /**
     * Save user. If new insert a new row, else update the existing row.
     */
    public synchronized void save(User user) throws SQLException {
        prepareDbConnection();

        // null value check
        if(user.getName() == null) {
            throw new NullPointerException("User name is null.");
        }

        if( !doesExist(user.getName()) ) {
            mNewUserStmt.setString(1, user.getName());
            mNewUserStmt.setString(2, getPassword(user));
            mNewUserStmt.setString(3, user.getVirtualDirectory().getRootDirectory());
            mNewUserStmt.setString(4, String.valueOf(user.getEnabled()));
            mNewUserStmt.setString(5, String.valueOf(user.getVirtualDirectory().getWritePermission()));
            mNewUserStmt.setInt(6, user.getMaxIdleTime());
            mNewUserStmt.setInt(7, user.getMaxUploadRate());
            mNewUserStmt.setInt(8, user.getMaxDownloadRate());
            mNewUserStmt.executeUpdate();
        }
        else {
            mUpdUserStmt.setString(1, getPassword(user));
            mUpdUserStmt.setString(2, user.getVirtualDirectory().getRootDirectory());
            mUpdUserStmt.setString(3, String.valueOf(user.getEnabled()));
            mUpdUserStmt.setString(4, String.valueOf(user.getVirtualDirectory().getWritePermission()));
            mUpdUserStmt.setInt(5, user.getMaxIdleTime());
            mUpdUserStmt.setInt(6, user.getMaxUploadRate());
            mUpdUserStmt.setInt(7, user.getMaxDownloadRate());
            mUpdUserStmt.setString(8, user.getName());
            mUpdUserStmt.executeUpdate();
        }
    }


    /**
     * Get the user object. Fetch the row from the table.
     */
    public synchronized User getUserByName(String name) {
        try {
            prepareDbConnection();

            User thisUser = null;
            mGetUserStmt.setString(1, name);
            ResultSet rs = mGetUserStmt.executeQuery();
            if(rs.next()) {
                thisUser = new User();
                thisUser.setName(rs.getString("LOGIN_ID"));
                thisUser.getVirtualDirectory().setRootDirectory(new File(rs.getString("HOME_DIR")));
                thisUser.setEnabled(rs.getString("ENABLED").equals(Boolean.TRUE.toString()));
                thisUser.getVirtualDirectory().setWritePermission(rs.getString("WRITE_PERM").equals(Boolean.TRUE.toString()));
                thisUser.setMaxIdleTime(rs.getInt("IDLE_TIME"));
                thisUser.setMaxUploadRate(rs.getInt("UPLOAD_RATE"));
                thisUser.setMaxDownloadRate(rs.getInt("DOWNLOAD_RATE"));
            }
            rs.close();
            return thisUser;
        }
        catch(SQLException ex) {
            getLogger().error("DbUserManager.getUserByName()", ex);
        }
        return null;
    }


    /**
     * User existance check
     */
    public synchronized boolean doesExist(String name) {
        boolean bValid = false;
        try {
            prepareDbConnection();

            mGetUserStmt.setString(1, name);
            ResultSet rs = mGetUserStmt.executeQuery();
            bValid = rs.next();
            rs.close();
        }
        catch(SQLException ex) {
            bValid = false;
            getLogger().error("DbUserManager.doesExist()", ex);
        }
        return bValid;
    }


    /**
     * Get all user names from the database.
     */
    public synchronized List getAllUserNames() {
        ArrayList names = new ArrayList();
        try {
            prepareDbConnection();

            ResultSet rs = mGetAllStmt.executeQuery();
            while(rs.next()) {
                names.add(rs.getString("LOGIN_ID"));
            }
            rs.close();
        }
        catch(SQLException ex) {
            getLogger().error("DbUserManager.getAllUserNames()", ex);
        }
        Collections.sort(names);
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
        prepareDbConnection();

        if (user.getPassword() != null) {
            return user.getPassword();
        }

        String password = "";
        mGetUserStmt.setString(1, user.getName());
        ResultSet rs = mGetUserStmt.executeQuery();
        if (rs.next()) {
            password = rs.getString("PASSWORD");
        }
        rs.close();
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
            prepareDbConnection();

            mGetUserStmt.setString(1, user);
            ResultSet rs = mGetUserStmt.executeQuery();
            if (rs.next()) {
                existPassword = rs.getString("PASSWORD");
            }
            rs.close();
        }
        catch(SQLException ex) {
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
