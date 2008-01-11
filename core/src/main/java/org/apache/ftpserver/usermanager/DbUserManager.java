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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is another database based user manager class. It has been
 * tested in MySQL and Oracle 8i database. The schema file is 
 * </code>res/ftp-db.sql</code>
 *
 * All the user attributes are replaced during run-time. So we can use
 * your database schema. Then you need to modify the SQLs in the configuration
 * file.
 */
public class DbUserManager extends AbstractUserManager {
    
    private final Logger LOG = LoggerFactory.getLogger(DbUserManager.class);
    
    private String insertUserStmt;
    private String updateUserStmt;
    private String deleteUserStmt;
    private String selectUserStmt;
    private String selectAllStmt;
    private String isAdminStmt;
    private String authenticateStmt;

    private DataSource dataSource;
    private Connection cachedConnection;
    
    /**
     * Retrive the data source used by the user manager
     * @return The current data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Set the data source to be used by the user manager
     * @param dataSource The data source to use
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Get the SQL INSERT statement used to add a new user.
     * @return The SQL statement
     */
    public String getSqlUserInsert() {
        return insertUserStmt;
    }
    
    /**
     * Set the SQL INSERT statement used to add a new user. All the dynamic values will be replaced during runtime.
     * @param sql The SQL statement
     */
    public void setSqlUserInsert(String sql) {
        insertUserStmt = sql;
    }
    
    /**
     * Get the SQL DELETE statement used to delete an existing user.
     * @return The SQL statement
     */
    public String getSqlUserDelete() {
        return insertUserStmt;
    }
    
    /**
     * Set the SQL DELETE statement used to delete an existing user. All the dynamic values will be replaced during runtime.
     * @param sql The SQL statement
     */
    public void setSqlUserDelete(String sql) {
        deleteUserStmt = sql;
    }
    
    /**
     * Get the SQL UPDATE statement used to update an existing user.
     * @return The SQL statement
     */
    public String getSqlUserUpdate() {
        return updateUserStmt;
    }
    
    /**
     * Set the SQL UPDATE statement used to update an existing user. All the dynamic values will be replaced during runtime.
     * @param sql The SQL statement
     */
    public void setSqlUserUpdate(String sql) {
        updateUserStmt = sql;
    }
    
    /**
     * Get the SQL SELECT statement used to select an existing user.
     * @return The SQL statement
     */
    public String getSqlUserSelect() {
        return selectUserStmt;
    }
    
    /**
     * Set the SQL SELECT statement used to select an existing user. All the dynamic values will be replaced during runtime.
     * @param sql The SQL statement
     */
    public void setSqlUserSelect(String sql) {
        selectUserStmt = sql;
    }
    
    /**
     * Get the SQL SELECT statement used to select all user ids.
     * @return The SQL statement
     */
    public String getSqlUserSelectAll() {
        return selectAllStmt;
    }
    
    /**
     * Set the SQL SELECT statement used to select all user ids. All the dynamic values will be replaced during runtime.
     * @param sql The SQL statement
     */
    public void setSqlUserSelectAll(String sql) {
        selectAllStmt = sql;
    }
    
    /**
     * Get the SQL SELECT statement used to authenticate user.
     * @return The SQL statement
     */
    public String getSqlUserAuthenticate() {
        return selectAllStmt;
    }
    
    /**
     * Set the SQL SELECT statement used to authenticate user. All the dynamic values will be replaced during runtime.
     * @param sql The SQL statement
     */
    public void setSqlUserAuthenticate(String sql) {
        authenticateStmt = sql;
    }
    
    /**
     * Get the SQL SELECT statement used to find whether an user is admin or not.
     * @return The SQL statement
     */
    public String getSqlUserAdmin() {
        return selectAllStmt;
    }
    
    /**
     * Set the SQL SELECT statement used to find whether an user is admin or not. All the dynamic values will be replaced during runtime.
     * @param sql The SQL statement
     */
    public void setSqlUserAdmin(String sql) {
        isAdminStmt = sql;
    }
    
    /**
     * Configure user manager.
     */
    public void configure() {
        
        if(dataSource == null) {
            throw new FtpServerConfigurationException("Required data source not provided");
        }
        if(insertUserStmt == null) {
            throw new FtpServerConfigurationException("Required insert user SQL statement not provided");
        }
        if(updateUserStmt == null) {
            throw new FtpServerConfigurationException("Required update user SQL statement not provided");
        }
        if(deleteUserStmt == null) {
            throw new FtpServerConfigurationException("Required delete user SQL statement not provided");
        }
        if(selectUserStmt == null) {
            throw new FtpServerConfigurationException("Required select user SQL statement not provided");
        }
        if(selectAllStmt == null) {
            throw new FtpServerConfigurationException("Required select all users SQL statement not provided");
        }
        if(isAdminStmt == null) {
            throw new FtpServerConfigurationException("Required is admin user SQL statement not provided");
        }
        if(authenticateStmt == null) {
            throw new FtpServerConfigurationException("Required authenticate user SQL statement not provided");
        }        
        
        try {
            // test the connection
            createConnection();
            
            LOG.info("Database connection opened.");
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.configure()", ex);
            throw new FtpServerConfigurationException("DbUserManager.configure()", ex);
        }
    }
    
    /**
     * @return true if user with this login is administrator
     */
    public boolean isAdmin(String login) throws FtpException {
        
        // check input
        if(login == null) {
            return false;
        }
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            
            // create the sql query
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put( ATTR_LOGIN, escapeString(login) );
            String sql = StringUtils.replaceString(isAdminStmt, map);
            LOG.info(sql);
            
            // execute query
            stmt = createConnection().createStatement();
            rs = stmt.executeQuery(sql);
            return rs.next();
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.isAdmin()", ex);
            throw new FtpException("DbUserManager.isAdmin()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.isAdmin()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.isAdmin()", ex);
                }
            }
        }
    }
    
    /**
     * Open connection to database.
     */
    private synchronized Connection createConnection() throws SQLException {
        boolean isClosed = false;    
        try {
            if( (cachedConnection == null) || cachedConnection.isClosed() ) {
                isClosed = true;
            }
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.prepareConnection()", ex);
            isClosed = true;
        }
        
        if (isClosed) {
            closeConnection();

            cachedConnection = dataSource.getConnection();
            cachedConnection.setAutoCommit(true);
        }
        
        return cachedConnection;
    }
    
    /**
     * Close connection to database.
     */
    private void closeConnection() {
        if (cachedConnection != null) {        
            try {
                cachedConnection.close(); 
            } 
            catch(SQLException ex) {
                LOG.error("DbUserManager.closeConnection()", ex);
            }
            cachedConnection = null;
        }
        
        LOG.info("Database connection closed.");
    }
    
    /**
     * Delete user. Delete the row from the table.
     */
    public synchronized void delete(String name) throws FtpException {
        
        // create sql query
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put( ATTR_LOGIN, escapeString(name) );
        String sql = StringUtils.replaceString(deleteUserStmt, map);
        LOG.info(sql);
        
        // execute query
        Statement stmt = null;
        try {
            stmt = createConnection().createStatement();
            stmt.executeUpdate(sql);
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.delete()", ex);
            throw new FtpException("DbUserManager.delete()", ex);
        }
        finally {
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.delete()", ex);
                }
            }
        }
    }
    
    /**
     * Save user. If new insert a new row, else update the existing row.
     */
    public synchronized void save(User user) throws FtpException {
        
        // null value check
        if(user.getName() == null) {
            throw new NullPointerException("User name is null.");
        } 
        
        Statement stmt = null;
        try {
            
            // create sql query
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put( ATTR_LOGIN, escapeString(user.getName()) );
            map.put( ATTR_PASSWORD, escapeString(getPassword(user)) );
            
            String home = user.getHomeDirectory();
            if(home == null) {
                home = "/";
            }
            map.put( ATTR_HOME, escapeString(home) );
            map.put( ATTR_ENABLE, String.valueOf(user.getEnabled()) );
            
            map.put( ATTR_WRITE_PERM, String.valueOf(user.authorize(new WriteRequest()) != null) );
            map.put( ATTR_MAX_IDLE_TIME, new Integer(user.getMaxIdleTime()) );
            
            
            TransferRateRequest transferRateRequest = new TransferRateRequest();
            transferRateRequest = (TransferRateRequest) user.authorize(transferRateRequest);
            
            if(transferRateRequest != null) {
                map.put( ATTR_MAX_UPLOAD_RATE, new Integer(transferRateRequest.getMaxUploadRate()) );
                map.put( ATTR_MAX_DOWNLOAD_RATE, new Integer(transferRateRequest.getMaxDownloadRate()) ); 
            } else {
                map.put( ATTR_MAX_UPLOAD_RATE, new Integer(0));
                map.put( ATTR_MAX_DOWNLOAD_RATE, new Integer(0) ); 
            }

            // request that always will succeed
            ConcurrentLoginRequest concurrentLoginRequest = new ConcurrentLoginRequest(0, 0);
            concurrentLoginRequest = (ConcurrentLoginRequest) user.authorize(concurrentLoginRequest);
            
            if(concurrentLoginRequest != null) {
                map.put( ATTR_MAX_LOGIN_NUMBER, 
                        new Integer(concurrentLoginRequest.getMaxConcurrentLogins()));
                map.put( ATTR_MAX_LOGIN_PER_IP, 
                        new Integer(concurrentLoginRequest.getMaxConcurrentLoginsPerIP()));
            } else {
                map.put( ATTR_MAX_LOGIN_NUMBER, new Integer(0));
                map.put( ATTR_MAX_LOGIN_PER_IP, new Integer(0));
            }
            

            
            String sql = null;      
            if( !doesExist(user.getName()) ) {
                sql = StringUtils.replaceString(insertUserStmt, map);
            }
            else {
                sql = StringUtils.replaceString(updateUserStmt, map);
            }
            LOG.info(sql);
            
            // execute query
            stmt = createConnection().createStatement();
            stmt.executeUpdate(sql);
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.save()", ex);
            throw new FtpException("DbUserManager.save()", ex);
        }
        finally {
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUsermanager.error()", ex);
                }
            }
        }
    }
    
    /**
     * Get the user object. Fetch the row from the table.
     */
    public synchronized User getUserByName(String name) throws FtpException {
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            
            // create sql query
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put( ATTR_LOGIN, escapeString(name) );
            String sql = StringUtils.replaceString(selectUserStmt, map);
            LOG.info(sql);
            
            // execute query
            stmt = createConnection().createStatement();
            rs = stmt.executeQuery(sql);
            
            // populate user object
            BaseUser thisUser = null;
            String trueStr = Boolean.TRUE.toString();
            if(rs.next()) {
                thisUser = new BaseUser();
                thisUser.setName(rs.getString(ATTR_LOGIN));
                thisUser.setHomeDirectory(rs.getString(ATTR_HOME));
                thisUser.setEnabled(trueStr.equalsIgnoreCase(rs.getString(ATTR_ENABLE)));
                thisUser.setMaxIdleTime(rs.getInt(ATTR_MAX_IDLE_TIME));
                
                List<Authority> authorities = new ArrayList<Authority>();
                if(trueStr.equalsIgnoreCase(rs.getString(ATTR_WRITE_PERM))) {
                    authorities.add(new WritePermission());
                }
                
                authorities.add(new ConcurrentLoginPermission(rs.getInt(ATTR_MAX_LOGIN_NUMBER), rs.getInt(ATTR_MAX_LOGIN_PER_IP)));
                authorities.add(new TransferRatePermission(rs.getInt(ATTR_MAX_DOWNLOAD_RATE), rs.getInt(ATTR_MAX_UPLOAD_RATE)));
                
                thisUser.setAuthorities(authorities.toArray(new Authority[0]));
            }
            return thisUser;
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.getUserByName()", ex);
            throw new FtpException("DbUserManager.getUserByName()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.getUserByName()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.getUserByName()", ex);
                }
            }
        }
    }
    
    /**
     * User existance check.
     */
    public synchronized boolean doesExist(String name) throws FtpException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            
            // create the sql
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put( ATTR_LOGIN, escapeString(name) );
            String sql = StringUtils.replaceString(selectUserStmt, map);
            LOG.info(sql);
            
            // execute query
            stmt = createConnection().createStatement();
            rs = stmt.executeQuery(sql);
            return rs.next();
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.doesExist()", ex);
            throw new FtpException("DbUserManager.doesExist()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.doesExist()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.doesExist()", ex);
                }
            }
        }
    }
    
    /**
     * Get all user names from the database.
     */
    public synchronized String[] getAllUserNames() throws FtpException {
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            
            // create sql query
            String sql = selectAllStmt;
            LOG.info(sql);
            
            // execute query
            stmt = createConnection().createStatement();
            rs = stmt.executeQuery(sql);
            
            // populate list
            ArrayList<String> names = new ArrayList<String>();
            while(rs.next()) {
                names.add(rs.getString(ATTR_LOGIN));
            }
            return names.toArray(new String[0]);
        }
        catch(SQLException ex) {
            LOG.error("DbUserManager.getAllUserNames()", ex);
            throw new FtpException("DbUserManager.getAllUserNames()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.getAllUserNames()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.getAllUserNames()", ex);
                }
            }
        }
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
        
        String password = user.getPassword();
        if (password != null) {
            return password;
        }

        // create sql query
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put( ATTR_LOGIN, escapeString(user.getName()) );
        String sql = StringUtils.replaceString(selectUserStmt, map);
        LOG.info(sql);
        
        // execute query
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = createConnection().createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                password = rs.getString(ATTR_PASSWORD);
            }
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.getPassword()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    LOG.error("DbUserManager.getPassword()", ex);
                }
            }
        }
        
        if (password == null) {
            password = "";
        }
        return password;
    }
    
    /**
     * User authentication.
     */
    public synchronized User authenticate(Authentication authentication) throws AuthenticationFailedException {
        if(authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            
            String user = upauth.getUsername(); 
            String password = upauth.getPassword(); 
        
            if(user == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            
            if(password == null) {
                password = "";
            }
            
            Statement stmt = null;
            ResultSet rs = null;
            try {
                
                // create the sql query
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put( ATTR_LOGIN, escapeString(user) );
                map.put( ATTR_PASSWORD, escapeString(password) );
                String sql = StringUtils.replaceString(authenticateStmt, map);
                LOG.info(sql);
                
                // execute query
                stmt = createConnection().createStatement();
                rs = stmt.executeQuery(sql);
                if(rs.next()) {
                    try {
                        return getUserByName(user);
                    } catch(FtpException e) {
                        throw new AuthenticationFailedException("Authentication failed", e);
                    }
                } else {
                    throw new AuthenticationFailedException("Authentication failed");
                }
            } catch(SQLException ex) {
                LOG.error("DbUserManager.authenticate()", ex);
                throw new AuthenticationFailedException("Authentication failed", ex);
            }
            finally {
                if(rs != null) {
                    try { 
                        rs.close(); 
                    } 
                    catch(Exception ex) {
                        LOG.error("DbUserManager.authenticate()", ex);
                    }
                }
                if(stmt != null) {
                    try { 
                        stmt.close(); 
                    } 
                    catch(Exception ex) {
                        LOG.error("DbUserManager.authenticate()", ex);
                    }
                }
            }
        } else if(authentication instanceof AnonymousAuthentication) {
            try {
                if(doesExist("anonymous")) {
                    return getUserByName("anonymous");
                } else {
                    throw new AuthenticationFailedException("Authentication failed");
                }
            } catch(AuthenticationFailedException e) {
                throw e;
            } catch(FtpException e) {
                throw new AuthenticationFailedException("Authentication failed", e);
            }
        } else {
            throw new IllegalArgumentException("Authentication not supported by this user manager");
        }
    }
    
    /**
     * Close this user manager. Close the database statements and connection.
     */
    public synchronized void dispose() {
        closeConnection();
    }
    
    /**
     * Escape string to be embedded in SQL statement.
     */
    private String escapeString(String input) {
        if(input == null) {
            return input;
        }
        
        StringBuffer valBuf = new StringBuffer(input);
        for (int i=0; i<valBuf.length(); i++) {
            char ch = valBuf.charAt(i);
            if (ch == '\'' || 
                ch == '\\' || 
                ch == '$'  || 
                ch == '^'  || 
                ch == '['  || 
                ch == ']'  || 
                ch == '{'  || 
                ch == '}') {
                 
                valBuf.insert(i, '\\'); 
                i++;
             }
         }
         return valBuf.toString();
    }
}