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
package org.apache.ftpserver;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.ftpserver.util.Message;
import org.apache.ftpserver.interfaces.FtpConnectionObserver;
import org.apache.ftpserver.interfaces.SpyConnectionInterface;
import org.apache.ftpserver.usermanager.User;
import org.apache.ftpserver.usermanager.UserManagerInterface;

/**
 * Ftp connection service class. It tracks all ftp connections.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class ConnectionService {

    private FtpConnectionObserver mObserver;
    private FtpConfig mConfig;
    private Timer mTimer;
    private Vector mConList;


    /**
     * Constructor. Start scheduler job.
     */
    public ConnectionService(FtpConfig cfg) throws Exception {
        mConfig = cfg;
        mConList = new Vector();

        // default users creation
        createDefaultUsers();

        // set timer to remove inactive users and load data
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                timerTask();
            }
        };
        mTimer.schedule(timerTask, 0, mConfig.getSchedulerInterval()*1000);
    }

   /**
    * Create default users (admin/anonymous) if necessary
    */
   private void createDefaultUsers() throws Exception {
        UserManagerInterface userManager = mConfig.getUserManager();

        // create admin user
        String adminName = userManager.getAdminName();
        if(!userManager.doesExist(adminName)) {
            mConfig.getLogger().info("Creating user " + adminName);
            User adminUser = new User();
            adminUser.setName(adminName);
            adminUser.setPassword(adminName);
            adminUser.setEnabled(true);
            adminUser.getVirtualDirectory().setWritePermission(true);
            adminUser.setMaxUploadRate(0);
            adminUser.setMaxDownloadRate(0);
            adminUser.getVirtualDirectory().setRootDirectory(mConfig.getDefaultRoot());
            adminUser.setMaxIdleTime(mConfig.getDefaultIdleTime());
            userManager.save(adminUser);
        }

        // create anonymous user
        if(!userManager.doesExist(FtpUser.ANONYMOUS)) {
            mConfig.getLogger().info("Creating user " + FtpUser.ANONYMOUS);
            User anonUser = new User();
            anonUser.setName(FtpUser.ANONYMOUS);
            anonUser.setPassword("");
            anonUser.setEnabled(true);
            anonUser.getVirtualDirectory().setWritePermission(false);
            anonUser.setMaxUploadRate(4800);
            anonUser.setMaxDownloadRate(4800);
            anonUser.getVirtualDirectory().setRootDirectory(mConfig.getDefaultRoot());
            anonUser.setMaxIdleTime(mConfig.getDefaultIdleTime());
            userManager.save(anonUser);
        }
    }

    /**
     * It returns a list of all the currently connected users.
     */
    public List getAllUsers() {
        List userList = new ArrayList();
        synchronized(mConList) {
            for(Iterator conIt=mConList.iterator(); conIt.hasNext(); ) {
                BaseFtpConnection conObj = (BaseFtpConnection)conIt.next();
                if (conObj != null) {
                    userList.add(conObj.getUser());
                }
            }
        }
        return userList;
    }

    /**
     * Set user manager observer.
     */
    public void setObserver(FtpConnectionObserver obsr ) {
        mObserver = obsr;
        synchronized(mConList) {
            for(Iterator conIt=mConList.iterator(); conIt.hasNext(); ) {
                BaseFtpConnection conObj = (BaseFtpConnection)conIt.next();
                if (conObj != null) {
                    conObj.setObserver(mObserver);
                }
            }
        }
    }

    /**
     * Get the observer.
     */
    public FtpConnectionObserver getObserver() {
        return mObserver;
    }

    /**
     * User login method. If successfull, populates the user object.
     */
    public boolean login(final FtpUser thisUser) {

        // already logged in
        if(thisUser.hasLoggedIn()) {
            return true;
        }

        // get name and password
        String user = thisUser.getName();
        String password = thisUser.getPassword();
        if( (user == null) || (password == null) ) {
            return false;
        }

        // authenticate user
        UserManagerInterface userManager = mConfig.getUserManager();
        boolean bAnonymous = thisUser.getIsAnonymous();
        if ( !(bAnonymous || userManager.authenticate(user, password)) ) {
            mConfig.getLogger().warn("Authentication failed - " + user);
            return false;
        }

        // populate user properties
        if (!populateProperties(thisUser, user)){
            return false;
        }

        // user enable check
        if(!thisUser.getEnabled()) {
            return false;
        }

        // connection limit check
        if (!checkConnection(thisUser)){
            return false;
        }

        thisUser.login();
        thisUser.setPassword(null);

        // create user home if necessary
        if( !createHome(thisUser) ) {
            return false;
        }
        mConfig.getLogger().info("User login - " + thisUser.getClientAddress().getHostAddress() + " - " + thisUser.getName());

        // update global statistics
        mConfig.getStatistics().setLogin(thisUser.getIsAnonymous());
        return true;
    }

    /**
     * Close ftp connection for this session id.
     */
    public void closeConnection(final String sessId) {
        BaseFtpConnection con = null;
        synchronized(mConList) {
            con = getConnection(sessId);
            if (con != null) {
                mConList.remove(con);
            }
        }

        // close connection
        if (con != null) {

            // logout notification
            final FtpUser thisUser = con.getUser();
            if (thisUser.hasLoggedIn()) {
                mConfig.getStatistics().setLogout(thisUser.getIsAnonymous());
            }

            // close socket
            con.stop();

            // send message
            Message msg = new Message() {
                public void execute() {
                    FtpConnectionObserver observer = mObserver;
                    if(observer != null) {
                        observer.removeConnection(thisUser);
                    }
                }
            };
            mConfig.getMessageQueue().add(msg);
            mConfig.getStatistics().setCloseConnection();
        }
    }


    /**
     * Close all - close all the connections.
     */
    public void closeAllConnections() {
        List allUsers = getAllUsers();
        for( Iterator userIt = allUsers.iterator(); userIt.hasNext(); ) {
            FtpUser user = (FtpUser)userIt.next();
            closeConnection(user.getSessionId());
        }
    }

    /**
     * Populate user properties
     */
    private boolean populateProperties(FtpUser thisUser, String user) {

        // get the existing user
        UserManagerInterface userManager = mConfig.getUserManager();
        User existUser = userManager.getUserByName(user);
        if(existUser == null) {
            return false;
        }

        // map properties
        thisUser.getVirtualDirectory().setRootDirectory(new File(existUser.getVirtualDirectory().getRootDirectory()));
        thisUser.setEnabled(existUser.getEnabled());
        thisUser.getVirtualDirectory().setWritePermission(existUser.getVirtualDirectory().getWritePermission());
        thisUser.setMaxIdleTime(existUser.getMaxIdleTime());
        thisUser.setMaxUploadRate(existUser.getMaxUploadRate());
        thisUser.setMaxDownloadRate(existUser.getMaxDownloadRate());
        return true;
    }

    /**
     * Connection limit check.
     */
    private boolean checkConnection(FtpUser thisUser) {
        int maxLogins = mConfig.getMaxConnections();
        int maxAnonLogins = mConfig.getMaxAnonymousLogins();
        int anonNbr = mConfig.getStatistics().getAnonLoginNbr();
        int totalNbr = mConfig.getStatistics().getLoginNbr();

        // final check
        if(thisUser.getIsAnonymous()) {
            if(!mConfig.isAnonymousLoginAllowed()) {
               return false;
            }
            if( (anonNbr>=maxAnonLogins) || (totalNbr>=maxLogins) ) {
               return false;
            }
            mConfig.getLogger().info("Anonymous connection - " + thisUser.getClientAddress().getHostAddress() + " - " + thisUser.getPassword());
        }
        else {
            if(totalNbr>=maxLogins) {
                return false;
            }
        }
        return true;
    }


    /**
     * Create user home directory if necessary
     */
    private boolean createHome(FtpUser user) {

        File userHome = new File( user.getVirtualDirectory().getRootDirectory() );
        if( userHome.exists() ) {
            if( !userHome.isDirectory() ) {
                mConfig.getLogger().warn("User home (" + userHome.getAbsolutePath() + ") for user " + user.getName() + " is not a directory.");
                return false;
            }
        }
        else {
            if( mConfig.isCreateHome() ) {
                mConfig.getLogger().info("Creating home (" + userHome.getAbsolutePath() + ") for user " + user.getName());
                if( !userHome.mkdirs() ) {
                    mConfig.getLogger().warn("Cannot create home (" + userHome.getAbsolutePath() + ") for user " + user.getName());
                    return false;
                }
            }
            else {
                mConfig.getLogger().warn("Cannot find home (" + userHome.getAbsolutePath() + ") for user " + user.getName());
                return false;
            }
        }

        return true;
    }


    /**
     * New connection has been established - not yet logged-in.
     */
    public void newConnection(final BaseFtpConnection newCon) {

        // null user - ignore
        if (newCon == null) {
            return;
        }

        final FtpUser newUser = newCon.getUser();

        mConList.add(newCon);
        newUser.setMaxIdleTime(mConfig.getDefaultIdleTime());
        newUser.getVirtualDirectory().setRootDirectory(mConfig.getDefaultRoot());
        newCon.setObserver(mObserver);
        mConfig.getLogger().info("New connection from " + newUser.getClientAddress().getHostAddress());

        // notify observer about a new connection
        final FtpConnectionObserver observer = mObserver;
        if (observer != null) {
            Message msg = new Message() {
                public void execute() {
                    observer.newConnection(newUser);
                }
            };
            mConfig.getMessageQueue().add(msg);
        }

        // update global statistics
        mConfig.getStatistics().setOpenConnection();
    }


    /**
     * Set connection spy object
     */
    public void setSpyObject(String sessId, SpyConnectionInterface spy) {
        BaseFtpConnection con = getConnection(sessId);
        if (con != null) {
            con.setSpyObject(spy);
        }
    }

    /**
     * Get connection object
     */
    public BaseFtpConnection getConnection(String sessId) {
        BaseFtpConnection con = null;
        synchronized(mConList) {
            for(Iterator conIt=mConList.iterator(); conIt.hasNext(); ) {
                BaseFtpConnection conObj = (BaseFtpConnection)conIt.next();
                if (conObj != null) {
                    if ( conObj.getUser().getSessionId().equals(sessId) ) {
                        con = conObj;
                        break;
                    }
                }
            }
        }
        return con;
    }

    /**
     * Reset all spy objects
     */
    public void resetAllSpyObjects() {
        synchronized(mConList) {
            for(Iterator conIt=mConList.iterator(); conIt.hasNext(); ) {
                BaseFtpConnection conObj = (BaseFtpConnection)conIt.next();
                if (conObj != null) {
                    conObj.setSpyObject(null);
                }
            }
        }
    }

    /**
     * Timer thread will call this method periodically to
     * close inactice connections and load user information.
     */
    public void timerTask() {

        // get inactive user list
        ArrayList inactiveUserList = new ArrayList();
        long currTime = System.currentTimeMillis();
        synchronized(mConList) {
            for( Iterator conIt=mConList.iterator(); conIt.hasNext(); ) {
                BaseFtpConnection con = (BaseFtpConnection)conIt.next();
                if (con != null) {
                    FtpUser user = con.getUser();
                    if (!user.isActive(currTime)) {
                        inactiveUserList.add(user);
                    }
                }
            }
        }

        // remove inactive users
        for( Iterator userIt=inactiveUserList.iterator(); userIt.hasNext(); ) {
            FtpUser user = (FtpUser)userIt.next();
            mConfig.getLogger().info("Removing idle user " + user);
            closeConnection(user.getSessionId());
        }

        // reload user data
        UserManagerInterface userManager = mConfig.getUserManager();
        try {
            userManager.reload();
        }
        catch(Exception ex) {
            mConfig.getLogger().error("ConnectionService.timerTask()", ex);
        }
    }

    /**
     * Dispose connection service. If logs out all the connected
     * users and stops the cleaner thread.
     */
    public void dispose() {

        // close all connections
        if (mConList != null) {
            closeAllConnections();
            mConList = null;
        }

        // stop timer
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

}
