package org.apache.ftpserver.interfaces;

import java.io.File;

import org.apache.ftpserver.FtpUser;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface FtpConnectionMonitor {
    void timerError(Exception ex);
    void removingIdleUser(FtpUser user);
    void newConnectionFrom(FtpUser newUser);
    void cannotFindHome(File userHome, FtpUser user);
    void cannotCreateHome(File userHome, FtpUser user);
    void creatingHome(File userHome, FtpUser user);
    void userHomeNotADir(File userHome, FtpUser user);
    void anonConnection(FtpUser thisUser);
    void userLogin(FtpUser thisUser);
    void authFailed(String user);
    void creatingUser(String adminName);
}

