/* ====================================================================
 * Copyright 2002 - 2004
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
 *
 *
 * $Id$
 */
package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.UserManagerException;
import org.apache.ftpserver.interfaces.FtpUserManagerMonitor;
import org.apache.ftpserver.util.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is another database based user manager class. I have
 * tested it using MySQL and Oracle database. The sql file is <code>ftp-db.sql</code>
 *
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class DbUserManager extends AbstractUserManager {

    private Connection mDbConnection       = null;

    protected String mInsUserStmt = null;
    protected String mDelUserStmt = null;
    protected String mSelUserStmt = null;
    protected String mGetAllStmt  = null;
    protected String mUpdUserStmt = null;

    protected String mUrl      = null;
    protected String mUser     = null;
    protected String mPassword = null;

    private FtpUserManagerMonitor ftpUserManagerMonitor;

    /**
     * Open connection to database.
     */
    protected void openDbConnection() throws SQLException {
        mDbConnection = DriverManager.getConnection(mUrl, mUser, mPassword);
        mDbConnection.setAutoCommit(true);
    }

    /**
     * Close connection to database.
     */
    private void closeDbConnection() {
        if (mDbConnection != null) {
            try {mDbConnection.close(); } catch(SQLException ex) {}
            mDbConnection = null;
        }
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
    public synchronized void delete(String name) throws UserManagerException {
        try {
            HashMap map = new HashMap();
            map.put(User.ATTR_LOGIN, name);
            String sql = StringUtils.replaceString(mDelUserStmt, map);

            prepareDbConnection();
            Statement stmt = mDbConnection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            throw new UserManagerException(e);
        }
    }


    /**
     * Save user. If new insert a new row, else update the existing row.
     */
    public synchronized void save(User user) throws UserManagerException {

        try {
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
        } catch (SQLException e) {
            throw new UserManagerException(e);
        }
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
            ftpUserManagerMonitor.generalError("DbUserManager.getUserByName()", ex);
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
            ftpUserManagerMonitor.generalError("DbUserManager.doesExist()", ex);
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
            ftpUserManagerMonitor.generalError("DbUserManager.getAllUserNames()", ex);
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
            ftpUserManagerMonitor.generalError("DbUserManager.authenticate()", ex);
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
