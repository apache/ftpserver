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

package org.apache.ftpserver.core;

import org.apache.ftpserver.CommandHandlerMonitor;
import org.apache.ftpserver.UserManagerException;
import org.apache.ftpserver.ip.IpRestrictorInterface;
import org.apache.ftpserver.usermanager.User;
import org.apache.ftpserver.usermanager.UserManagerInterface;
import org.apache.ftpserver.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Handle ftp site command.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class SiteCommandHandler {

    // as SimpleDateFormat is not thread-safe we have to use ThreadLocal
    private final static ThreadLocal DATE_FMT = new ThreadLocal() {
        protected Object initialValue() {
            return new SimpleDateFormat("MM/dd HH:mm:ss");
        }
    };

    protected final static Class[] INPUT_SIG = new Class[]{String[].class, FtpRequest.class};

    private AbstractFtpConfig mConfig;
    private UserImpl mUser;
    private CommandHandlerMonitor commandHandlerMonitor;


    /**
     * Constructor - set the configuration object
     */
    public SiteCommandHandler(AbstractFtpConfig cfg, UserImpl user) {
        mConfig = cfg;
        mUser = user;
    }


    /**
     * Handle site.
     */
    public String getResponse(FtpRequest request) {
        String argArray[] = parseArg(request.getArgument());

        String response = "";
        if (hasPermission(argArray)) {
            if ((argArray != null) && (argArray.length != 0)) {
                try {
                    String metName = "do" + argArray[0].toUpperCase();
                    Method actionMet = getClass().getDeclaredMethod(metName, INPUT_SIG);
                    response = (String) actionMet.invoke(this, new Object[]{argArray, request});
                } catch (UnsupportedOperationException exc) {
                    response = mConfig.getStatus().processNewLine("", 202);
                } catch (Throwable th) {
                    commandHandlerMonitor.unknownResponseException("SiteCommandHandler.getResponse()", th);
                    response = mConfig.getStatus().getResponse(530, request, mUser, null);
                }
            } else {
                response = mConfig.getStatus().getResponse(200, request, mUser, null);
            }
        } else {
            response = mConfig.getStatus().getResponse(530, request, mUser, null);
        }

        return response;
    }


    /**
     * Parse all the tokens.
     */
    private String[] parseArg(String arg) {
        if (arg == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(arg, " ");
        String[] args = new String[st.countTokens()];
        for (int i = 0; i < args.length; i++) {
            args[i] = st.nextToken();
        }

        return args;
    }


    /**
     * Has permission
     */
    private boolean hasPermission(String args[]) {
        UserManagerInterface userManager = mConfig.getUserManager();
        String adminName = userManager.getAdminName();
        if (args == null || mUser.getName().equals(adminName)) {
            return true;
        }
        return ((args.length > 0) && "HELP".equalsIgnoreCase(args[0]));
    }


    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////  All site command handlers ////////////////////////
    /**
     * Add banned IP
     */
    public String doADDIP(String[] args, FtpRequest cmd) {
        if (args.length != 2) {
            return mConfig.getStatus().getResponse(501, cmd, mUser, null);
        }

        String response = "";
        try {
            IpRestrictorInterface ipRestrictor = mConfig.getIpRestrictor();
            ipRestrictor.addEntry(args[1]);
            ipRestrictor.save();
            response = mConfig.getStatus().getResponse(200, cmd, mUser, null);
        } catch (IOException ex) {
            commandHandlerMonitor.ipBlockException("SiteCommandHandler.doADDIP()", ex);
            response = mConfig.getStatus().getResponse(451, cmd, mUser, null);
        }
        return response;
    }


    /**
     * Add user
     */
    public String doADDUSER(String[] args, FtpRequest cmd) {
        if (args.length != 2) {
            return mConfig.getStatus().getResponse(501, cmd, mUser, null);
        }

        String response = "";
        try {
            String userName = args[1];
            UserManagerInterface userManager = mConfig.getUserManager();
            if (!userManager.doesExist(userName)) {
                User user = new User();
                user.setName(userName);
                user.setPassword("");
                user.setEnabled(false);
                user.getVirtualDirectory().setWritePermission(false);
                user.setMaxUploadRate(0);
                user.setMaxDownloadRate(0);
                user.getVirtualDirectory().setRootDirectory(mConfig.getDefaultRoot());
                user.setMaxIdleTime(mConfig.getDefaultIdleTime());
                mConfig.getUserManager().save(user);
            }
            response = mConfig.getStatus().getResponse(200, cmd, mUser, null);
        } catch (UserManagerException ex) {
            commandHandlerMonitor.addUserException("SiteCommandHandler.doADDUSER()", ex);
            response = mConfig.getStatus().getResponse(451, cmd, mUser, null);
        }
        return response;
    }


    /**
     * Add banned IP
     */
    public String doDELIP(String[] args, FtpRequest cmd) {
        if (args.length != 2) {
            return mConfig.getStatus().getResponse(501, cmd, mUser, null);
        }

        String response = "";
        try {
            IpRestrictorInterface ipRestrictor = mConfig.getIpRestrictor();
            ipRestrictor.removeEntry(args[1]);
            ipRestrictor.save();
            response = mConfig.getStatus().getResponse(200, cmd, mUser, null);
        } catch (IOException ex) {
            commandHandlerMonitor.ipBlockException("SiteCommandHandler.doDELIP()", ex);
            response = mConfig.getStatus().getResponse(451, cmd, mUser, null);
        }
        return response;
    }


    /**
     * Delete user from repository.
     */
    public String doDELUSER(String[] args, FtpRequest cmd) {
        if (args.length != 2) {
            return mConfig.getStatus().getResponse(501, cmd, mUser, null);
        }

        String response = "";
        try {
            mConfig.getUserManager().delete(args[1]);
            response = mConfig.getStatus().getResponse(200, cmd, mUser, null);
        } catch (UserManagerException ex) {
            commandHandlerMonitor.deleteUserException("SiteCommandHandler.doDELUSER()", ex);
            response = mConfig.getStatus().getResponse(451, cmd, mUser, null);
        }
        return response;
    }

    /**
     * Describe user.
     */
    public String doDESCUSER(String[] args, FtpRequest cmd) {
        if (args.length != 2) {
            return mConfig.getStatus().getResponse(501, cmd, mUser, null);
        }

        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        User user = mConfig.getUserManager().getUserByName(args[1]);
        if (user != null) {
            sb.append(User.ATTR_LOGIN).append(" : ").append(user.getName()).append('\n');
            sb.append(User.ATTR_PASSWORD).append(" : ").append("******").append('\n');
            sb.append(User.ATTR_HOME).append(" : ").append(user.getVirtualDirectory().getRootDirectory()).append('\n');
            sb.append(User.ATTR_WRITE_PERM).append(" : ").append(user.getVirtualDirectory().getWritePermission()).append('\n');
            sb.append(User.ATTR_ENABLE).append(" : ").append(user.getEnabled()).append('\n');
            sb.append(User.ATTR_MAX_IDLE_TIME).append(" : ").append(user.getMaxIdleTime()).append('\n');
            sb.append(User.ATTR_MAX_UPLOAD_RATE).append(" : ").append(user.getMaxUploadRate()).append('\n');
            sb.append(User.ATTR_MAX_DOWNLOAD_RATE).append(" : ").append(user.getMaxDownloadRate()).append('\n');
        }
        sb.append('\n');
        return mConfig.getStatus().processNewLine(sb.toString(), 200);
    }


    /**
     * Display site help.
     */
    public String doHELP(String[] args, FtpRequest cmd) {
        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        sb.append("ADDIP <IP> : add banned IP entry").append('\n');
        sb.append("ADDUSER <userName> : add user").append('\n');
        sb.append("DELIP <IP> : delete banned IP entry").append('\n');
        sb.append("DELUSER <userName> : delete user").append('\n');
        sb.append("DESCUSER <userName> : describe user").append('\n');
        sb.append("HELP : display this message").append('\n');
        sb.append("KICK <userName> : close the connection").append('\n');
        sb.append("LISTIP : display all banned IPs").append('\n');
        sb.append("LISTUSER : display all user names").append('\n');
        sb.append("SETATTR <userName> <attrName> <attrValue> : set user attributes").append('\n');
        sb.append("STAT : show statistics").append('\n');
        sb.append("WHO : display all connected users").append('\n');
        sb.append('\n');
        return mConfig.getStatus().processNewLine(sb.toString(), 200);
    }

    /**
     * Disconnect ftp connections
     */
    public String doKICK(String[] args, FtpRequest cmd) {
        if (args.length != 2) {
            return mConfig.getStatus().getResponse(501, cmd, mUser, null);
        }
        String userName = args[1];
        List allUsers = mConfig.getConnectionService().getAllUsers();
        for (Iterator userIt = allUsers.iterator(); userIt.hasNext();) {
            UserImpl user = (UserImpl) userIt.next();
            if (userName.equals(user.getName())) {
                mConfig.getConnectionService().closeConnection(user.getSessionId());
            }
        }
        return mConfig.getStatus().getResponse(200, cmd, mUser, null);
    }


    /**
     * List all banned IPs.
     */
    public String doLISTIP(String[] args, FtpRequest cmd) {
        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        for (Iterator ipIt = mConfig.getIpRestrictor().getAllEntries().iterator(); ipIt.hasNext();) {
            sb.append(ipIt.next()).append('\n');
        }
        sb.append('\n');
        return mConfig.getStatus().processNewLine(sb.toString(), 200);
    }


    /**
     * List all the users.
     */
    public String doLISTUSER(String[] args, FtpRequest cmd) {
        Collection userNames = mConfig.getUserManager().getAllUserNames();
        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        for (Iterator userIt = userNames.iterator(); userIt.hasNext();) {
            sb.append(userIt.next()).append('\n');
        }
        sb.append('\n');
        return mConfig.getStatus().processNewLine(sb.toString(), 200);
    }


    /**
     * Delete user from repository.
     */
    public String doSETATTR(String[] args, FtpRequest cmd) {
        if (args.length != 4) {
            return mConfig.getStatus().getResponse(501, cmd, mUser, null);
        }

        boolean bSuccess = true;
        try {
            User user = mConfig.getUserManager().getUserByName(args[1]);
            if (user != null) {

                if (User.ATTR_PASSWORD.equals(args[2])) {
                    user.setPassword(args[3]);
                } else if (User.ATTR_HOME.equals(args[2])) {
                    user.getVirtualDirectory().setRootDirectory(args[3]);
                } else if (User.ATTR_WRITE_PERM.equals(args[2])) {
                    user.getVirtualDirectory().setWritePermission("true".equals(args[3]));
                } else if (User.ATTR_ENABLE.equals(args[2])) {
                    user.setEnabled("true".equals(args[3]));
                } else if (User.ATTR_MAX_IDLE_TIME.equals(args[2])) {
                    user.setMaxIdleTime(Integer.parseInt(args[3]));
                } else if (User.ATTR_MAX_UPLOAD_RATE.equals(args[2])) {
                    user.setMaxUploadRate(Integer.parseInt(args[3]));
                } else if (User.ATTR_MAX_DOWNLOAD_RATE.equals(args[2])) {
                    user.setMaxDownloadRate(Integer.parseInt(args[3]));
                } else {
                    bSuccess = false;
                }

                if (bSuccess) {
                    mConfig.getUserManager().save(user);
                }
            } else {
                bSuccess = false;
            }
        } catch (UserManagerException ex) {
            commandHandlerMonitor.setAttrException("SiteCommandHandler.doSETATTR()", ex);
            bSuccess = false;
        }

        String response = "";
        if (bSuccess) {
            response = mConfig.getStatus().getResponse(200, cmd, mUser, null);
        } else {
            response = mConfig.getStatus().getResponse(451, cmd, mUser, null);
        }

        return response;
    }


    /**
     * Delete user from repository.
     */
    public String doSTAT(String[] args, FtpRequest cmd) {
        FtpStatistics stat = mConfig.getStatistics();
        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        sb.append("Start Time               : ").append(((SimpleDateFormat) DATE_FMT.get()).format(stat.getStartTime())).append('\n');
        sb.append("Upload Number            : ").append(stat.getFileUploadNbr()).append('\n');
        sb.append("Download Number          : ").append(stat.getFileDownloadNbr()).append('\n');
        sb.append("Delete Number            : ").append(stat.getFileDeleteNbr()).append('\n');
        sb.append("Uploade Bytes            : ").append(stat.getFileUploadSize()).append('\n');
        sb.append("Downloaded Bytes         : ").append(stat.getFileDownloadSize()).append('\n');
        sb.append("Current Logins           : ").append(stat.getLoginNbr()).append('\n');
        sb.append("Total Logins             : ").append(stat.getTotalLoginNbr()).append('\n');
        sb.append("Current Anonymous Logins : ").append(stat.getAnonLoginNbr()).append('\n');
        sb.append("Total Anonymous Logins   : ").append(stat.getTotalAnonLoginNbr()).append('\n');
        sb.append("Current Connections      : ").append(stat.getConnectionNbr()).append('\n');
        sb.append("Total Connections        : ").append(stat.getTotalConnectionNbr()).append('\n');
        sb.append('\n');
        return mConfig.getStatus().processNewLine(sb.toString(), 200);
    }


    /**
     * Display all connected users.
     */
    public String doWHO(String[] args, FtpRequest cmd) {
        StringBuffer sb = new StringBuffer();
        List allUsers = mConfig.getConnectionService().getAllUsers();

        sb.append('\n');
        for (Iterator userIt = allUsers.iterator(); userIt.hasNext();) {
            UserImpl user = (UserImpl) userIt.next();
            if (!user.hasLoggedIn()) {
                continue;
            }

            SimpleDateFormat fmt = (SimpleDateFormat) DATE_FMT.get();
            sb.append(StringUtils.pad(user.getName(), ' ', true, 16));
            sb.append(StringUtils.pad(user.getClientAddress().getHostAddress(), ' ', true, 16));
            sb.append(StringUtils.pad(fmt.format(new Date(user.getLoginTime())), ' ', true, 16));
            sb.append(StringUtils.pad(fmt.format(new Date(user.getLastAccessTime())), ' ', true, 16));
            sb.append('\n');
        }
        sb.append('\n');
        return mConfig.getStatus().processNewLine(sb.toString(), 200);
    }

}

