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

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.test.TestUtil;
import org.apache.ftpserver.util.IoUtils;
import org.hsqldb.jdbc.jdbcDataSource;

public class DbUserManagerTest extends UserManagerTestTemplate {

    private static final File INIT_SQL_SCRIPT = new File(TestUtil.getBaseDir(), "src/test/dbusermanagertest-hsql.sql");
    
    private jdbcDataSource ds;
    private Connection conn;
    
    private void createDatabase() throws Exception {
        conn = ds.getConnection();
        conn.setAutoCommit(true);
        
        String ddl = IoUtils.readFully(new FileReader(INIT_SQL_SCRIPT));
        
        Statement stm = conn.createStatement();
        stm.execute(ddl);
    }
    
    protected UserManager createUserManager() throws FtpException {
        DbUserManager manager = new DbUserManager();
        
        manager.setDataSource(ds);
        manager.setSqlUserInsert("INSERT INTO FTP_USER (uid, userpassword, homedirectory, enableflag, writepermission, idletime, uploadrate, downloadrate, maxloginnumber, maxloginperip) VALUES ('{uid}', '{userpassword}', '{homedirectory}', '{enableflag}', '{writepermission}', {idletime}, {uploadrate}, {downloadrate}, {maxloginnumber}, {maxloginperip})");
        manager.setSqlUserUpdate("UPDATE FTP_USER SET userpassword='{userpassword}',homedirectory='{homedirectory}',enableflag='{enableflag}',writepermission='{writepermission}',idletime={idletime},uploadrate={uploadrate},downloadrate={downloadrate},maxloginnumber={maxloginnumber}, maxloginperip={maxloginperip} WHERE uid='{uid}'");
        manager.setSqlUserDelete("DELETE FROM FTP_USER WHERE uid = '{uid}'");
        manager.setSqlUserSelect("SELECT * FROM FTP_USER WHERE uid = '{uid}'");
        manager.setSqlUserSelectAll("SELECT uid FROM FTP_USER ORDER BY uid");
        manager.setSqlUserAuthenticate("SELECT uid FROM FTP_USER WHERE uid='{uid}' AND userpassword='{userpassword}'");
        manager.setSqlUserAdmin("SELECT uid FROM FTP_USER WHERE uid='{uid}' AND uid='admin'");
        
        return manager;
        
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        ds = new jdbcDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:ftpd");
        ds.setUser("sa");
        ds.setPassword("");
        
        
        createDatabase();
        
        super.setUp();
    }


    protected void tearDown() throws Exception {
        Statement stm = conn.createStatement();
        stm.execute("SHUTDOWN");
        
        super.tearDown();
    }
    
}
