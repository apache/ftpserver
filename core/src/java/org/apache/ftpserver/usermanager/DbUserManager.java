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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.util.StringUtils;

/**
 * This is another database based user manager class. It has been
 * tested in MySQL and Oracle 8i database. The schema file is 
 * </code>res/ftp-db.sql</code>
 *
 * All the user attributes are replaced during run-time. So we can use
 * your database schema. Then you need to modify the SQLs in the configuration
 * file.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class DbUserManager extends AbstractUserManager {
    
    private Log log;
    
    private Connection connection;
    
    private String insertUserStmt;
    private String updateUserStmt;
    private String deleteUserStmt;
    private String selectUserStmt;
    private String selectAllStmt;
    private String isAdminStmt;
    private String authenticateStmt;
    
    private String jdbcUrl;
    private String dbUser;
    private String dbPassword;
    
    private String adminName;
    
    
    /**
     * Set the log factory.
     */
    public void setLogFactory(LogFactory factory) {
        log = factory.getInstance(getClass());
    }
    
    /**
     * Configure user manager.
     */
    public void configure(Configuration config) throws FtpException {
        
        try {
            String className = config.getString("jdbc-driver");
            Class.forName(className);
            
            jdbcUrl          = config.getString("jdbc-url");
            dbUser           = config.getString("jdbc-user", null);
            dbPassword       = config.getString("jdbc-password", null);
            
            insertUserStmt   = config.getString("sql-user-insert");
            deleteUserStmt   = config.getString("sql-user-delete");
            updateUserStmt   = config.getString("sql-user-update");
            selectUserStmt   = config.getString("sql-user-select");
            selectAllStmt    = config.getString("sql-user-select-all");
            authenticateStmt = config.getString("sql-user-authenticate");
            isAdminStmt      = config.getString("sql-user-admin");
            
            openConnection();
            
            adminName = config.getString("admin", "admin");
            log.info("Database connection opened.");
        }
        catch(Exception ex) {
            log.fatal("DbUserManager.configure()", ex);
            throw new FtpException("DbUserManager.configure()", ex);
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
        
        // check input
        if(login == null) {
            return false;
        }
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            
            // create the sql query
            HashMap map = new HashMap();
            map.put( ATTR_LOGIN, escapeString(login) );
            String sql = StringUtils.replaceString(isAdminStmt, map);
            log.info(sql);
            
            // execute query
            prepareConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            return rs.next();
        }
        catch(SQLException ex) {
            log.error("DbUserManager.isAdmin()", ex);
            throw new FtpException("DbUserManager.isAdmin()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.isAdmin()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.isAdmin()", ex);
                }
            }
        }
    }
    
    /**
     * Open connection to database.
     */
    private void openConnection() throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        connection.setAutoCommit(true);
    }
    
    /**
     * Close connection to database.
     */
    private void closeConnection() {
        if (connection != null) {        
            try {
                connection.close(); 
            } 
            catch(SQLException ex) {
                log.error("DbUserManager.closeConnection()", ex);
            }
            connection = null;
        }
        
        log.info("Database connection closed.");
    }
    
    /**
     * Prepare connection to database.
     */
    private void prepareConnection() throws SQLException {
        boolean isClosed = false;    
        try {
            if( (connection == null) || connection.isClosed() ) {
                isClosed = true;
            }
        }
        catch(SQLException ex) {
            log.error("DbUserManager.prepareConnection()", ex);
            isClosed = true;
        }
        
        if (isClosed) {
            closeConnection();
            openConnection();
        }
    }
    
    /**
     * Delete user. Delete the row from the table.
     */
    public synchronized void delete(String name) throws FtpException {
        
        // create sql query
        HashMap map = new HashMap();
        map.put( ATTR_LOGIN, escapeString(name) );
        String sql = StringUtils.replaceString(deleteUserStmt, map);
        log.info(sql);
        
        // execute query
        Statement stmt = null;
        try {
            prepareConnection();
            stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        catch(SQLException ex) {
            log.error("DbUserManager.delete()", ex);
            throw new FtpException("DbUserManager.delete()", ex);
        }
        finally {
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.delete()", ex);
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
            HashMap map = new HashMap();
            map.put( ATTR_LOGIN, escapeString(user.getName()) );
            map.put( ATTR_PASSWORD, escapeString(getPassword(user)) );
            map.put( ATTR_HOME, escapeString(user.getHomeDirectory()) );
            map.put( ATTR_ENABLE, String.valueOf(user.getEnabled()) );
            map.put( ATTR_WRITE_PERM, String.valueOf(user.getWritePermission()) );
            map.put( ATTR_MAX_IDLE_TIME, new Integer(user.getMaxIdleTime()) );
            map.put( ATTR_MAX_UPLOAD_RATE, new Integer(user.getMaxUploadRate()) );
            map.put( ATTR_MAX_DOWNLOAD_RATE, new Integer(user.getMaxDownloadRate()) ); 
            map.put( ATTR_MAX_LOGIN_NUMBER, new Integer(user.getMaxLoginNumber()));
            map.put( ATTR_MAX_LOGIN_PER_IP, new Integer(user.getMaxLoginPerIP()));
            
            String sql = null;      
            if( !doesExist(user.getName()) ) {
                sql = StringUtils.replaceString(insertUserStmt, map);
            }
            else {
                sql = StringUtils.replaceString(updateUserStmt, map);
            }
            log.info(sql);
            
            // execute query
            prepareConnection();
            stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        }
        catch(SQLException ex) {
            log.error("DbUserManager.save()", ex);
            throw new FtpException("DbUserManager.save()", ex);
        }
        finally {
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUsermanager.error()", ex);
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
            HashMap map = new HashMap();
            map.put( ATTR_LOGIN, escapeString(name) );
            String sql = StringUtils.replaceString(selectUserStmt, map);
            log.info(sql);
            
            // execute query
            prepareConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            
            // populate user object
            BaseUser thisUser = null;
            String trueStr = Boolean.TRUE.toString();
            if(rs.next()) {
                thisUser = new BaseUser();
                thisUser.setName(rs.getString(ATTR_LOGIN));
                thisUser.setHomeDirectory(rs.getString(ATTR_HOME));
                thisUser.setEnabled(trueStr.equalsIgnoreCase(rs.getString(ATTR_ENABLE)));
                thisUser.setWritePermission(trueStr.equalsIgnoreCase(rs.getString(ATTR_WRITE_PERM)));
                thisUser.setMaxLoginNumber(rs.getInt(ATTR_MAX_LOGIN_NUMBER));
                thisUser.setMaxLoginPerIP(rs.getInt(ATTR_MAX_LOGIN_PER_IP));
                thisUser.setMaxIdleTime(rs.getInt(ATTR_MAX_IDLE_TIME));
                thisUser.setMaxUploadRate(rs.getInt(ATTR_MAX_UPLOAD_RATE));
                thisUser.setMaxDownloadRate(rs.getInt(ATTR_MAX_DOWNLOAD_RATE));
            }
            return thisUser;
        }
        catch(SQLException ex) {
            log.error("DbUserManager.getUserByName()", ex);
            throw new FtpException("DbUserManager.getUserByName()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.getUserByName()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.getUserByName()", ex);
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
            HashMap map = new HashMap();
            map.put( ATTR_LOGIN, escapeString(name) );
            String sql = StringUtils.replaceString(selectUserStmt, map);
            log.info(sql);
            
            // execute query
            prepareConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            return rs.next();
        }
        catch(SQLException ex) {
            log.error("DbUserManager.doesExist()", ex);
            throw new FtpException("DbUserManager.doesExist()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.doesExist()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.doesExist()", ex);
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
            log.info(sql);
            
            // execute query
            prepareConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            
            // populate list
            ArrayList names = new ArrayList();
            while(rs.next()) {
                names.add(rs.getString(ATTR_LOGIN));
            }
            return (String[]) names.toArray(new String[0]);
        }
        catch(SQLException ex) {
            log.error("DbUserManager.getAllUserNames()", ex);
            throw new FtpException("DbUserManager.getAllUserNames()", ex);
        }
        finally {
            if(rs != null) {
                try { 
                    rs.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.getAllUserNames()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.getAllUserNames()", ex);
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
        HashMap map = new HashMap();
        map.put( ATTR_LOGIN, escapeString(user.getName()) );
        String sql = StringUtils.replaceString(selectUserStmt, map);
        log.info(sql);
        
        // execute query
        Statement stmt = null;
        ResultSet rs = null;
        try {
            prepareConnection();
            stmt = connection.createStatement();
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
                    log.error("DbUserManager.getPassword()", ex);
                }
            }
            if(stmt != null) {
                try { 
                    stmt.close(); 
                } 
                catch(Exception ex) {
                    log.error("DbUserManager.getPassword()", ex);
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
                HashMap map = new HashMap();
                map.put( ATTR_LOGIN, escapeString(user) );
                map.put( ATTR_PASSWORD, escapeString(password) );
                String sql = StringUtils.replaceString(authenticateStmt, map);
                log.info(sql);
                
                // execute query
                prepareConnection();
                stmt = connection.createStatement();
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
                log.error("DbUserManager.authenticate()", ex);
                throw new AuthenticationFailedException("Authentication failed", ex);
            }
            finally {
                if(rs != null) {
                    try { 
                        rs.close(); 
                    } 
                    catch(Exception ex) {
                        log.error("DbUserManager.authenticate()", ex);
                    }
                }
                if(stmt != null) {
                    try { 
                        stmt.close(); 
                    } 
                    catch(Exception ex) {
                        log.error("DbUserManager.authenticate()", ex);
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