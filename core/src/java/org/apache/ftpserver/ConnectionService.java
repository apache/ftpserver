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
package org.apache.ftpserver;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.ftpserver.util.Message;
import org.apache.ftpserver.ConnectionObserver;
import org.apache.ftpserver.SpyConnectionInterface;
import org.apache.ftpserver.ConnectionMonitor;
import org.apache.ftpserver.usermanager.UserManagerInterface;

/**
 * Ftp connection service class. It tracks all ftp connections.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class ConnectionService {

    private ConnectionObserver mObserver;
    private AbstractFtpConfig mConfig;
    private ConnectionMonitor connectionMonitor;
    private Timer mTimer;
    private Vector mConList;


    /**
     * Constructor. Start scheduler job.
     */
    public ConnectionService(AbstractFtpConfig cfg, ConnectionMonitor connectionMonitor) throws UserManagerException {
        mConfig = cfg;
        this.connectionMonitor = connectionMonitor;
        mConList = new Vector();

        // default users creation
        if (mConfig.mbCreateUsers)
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
   private void createDefaultUsers() throws UserManagerException {
        UserManagerInterface userManager = mConfig.getUserManager();

        // create admin user
        String adminName = userManager.getAdminName();
        if(!userManager.doesExist(adminName)) {
            connectionMonitor.creatingUser(adminName);
            org.apache.ftpserver.usermanager.User adminUser = new org.apache.ftpserver.usermanager.User();
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
        if (mConfig.isAnonymousLoginAllowed()
                && !userManager.doesExist(User.ANONYMOUS)) {
            connectionMonitor.creatingUser(User.ANONYMOUS);
            org.apache.ftpserver.usermanager.User anonUser = new org.apache.ftpserver.usermanager.User();
            anonUser.setName(User.ANONYMOUS);
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
    public void setObserver(ConnectionObserver obsr ) {
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
    public ConnectionObserver getObserver() {
        return mObserver;
    }

    /**
     * User login method. If successfull, populates the user object.
     */
    public boolean login(final UserImpl thisUser) {

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
            connectionMonitor.authFailed(user);
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
        connectionMonitor.userLogin(thisUser);

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
            final UserImpl thisUser = con.getUser();
            if (thisUser.hasLoggedIn()) {
                mConfig.getStatistics().setLogout(thisUser.getIsAnonymous());
            }

            // close socket
            con.stop();

            // send message
            Message msg = new Message() {
                public void execute() {
                    ConnectionObserver observer = mObserver;
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
            UserImpl user = (UserImpl)userIt.next();
            closeConnection(user.getSessionId());
        }
    }

    /**
     * Populate user properties
     */
    private boolean populateProperties(UserImpl thisUser, String user) {

        // get the existing user
        UserManagerInterface userManager = mConfig.getUserManager();
        org.apache.ftpserver.usermanager.User existUser = userManager.getUserByName(user);
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
    private boolean checkConnection(UserImpl thisUser) {
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
            connectionMonitor.anonConnection(thisUser);
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
    private boolean createHome(UserImpl user) {

        File userHome = new File( user.getVirtualDirectory().getRootDirectory() );
        if( userHome.exists() ) {
            if( !userHome.isDirectory() ) {
                connectionMonitor.userHomeNotADir(userHome, user);
                return false;
            }
        }
        else {
            if( mConfig.isCreateHome() ) {
                connectionMonitor.creatingHome(userHome, user);
                if( !userHome.mkdirs() ) {
                    connectionMonitor.cannotCreateHome(userHome, user);
                    return false;
                }
            }
            else {
                connectionMonitor.cannotFindHome(userHome, user);
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

        final UserImpl newUser = newCon.getUser();

        mConList.add(newCon);
        newUser.setMaxIdleTime(mConfig.getDefaultIdleTime());
        newUser.getVirtualDirectory().setRootDirectory(mConfig.getDefaultRoot());
        newCon.setObserver(mObserver);
        connectionMonitor.newConnectionFrom(newUser);

        // notify observer about a new connection
        final ConnectionObserver observer = mObserver;
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
                    UserImpl user = con.getUser();
                    if (!user.isActive(currTime)) {
                        inactiveUserList.add(user);
                    }
                }
            }
        }

        // remove inactive users
        for( Iterator userIt=inactiveUserList.iterator(); userIt.hasNext(); ) {
            UserImpl user = (UserImpl)userIt.next();
            connectionMonitor.removingIdleUser(user);
            closeConnection(user.getSessionId());
        }

        // reload user data
        UserManagerInterface userManager = mConfig.getUserManager();
        try {
            userManager.reload();
        }
        catch(Exception ex) {
            connectionMonitor.timerError(ex);
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
