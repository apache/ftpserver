package org.apache.ftpserver.interfaces;

import org.apache.ftpserver.FtpUser;

import java.io.File;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface FtpConnectionMonitor {
    void timerError(Exception ex);
    void removingIdleUser(FtpUser user);
    void newConnectionFrom(final FtpUser newUser);
    void cannotFindHome(File userHome, FtpUser user);
    void cannotCreateHome(File userHome, FtpUser user);
    void creatingHome(File userHome, FtpUser user);
    void userHomeNotADir(File userHome, FtpUser user);
    void anonConnection(FtpUser thisUser);
    void userLogin(final FtpUser thisUser);
    void authFailed(String user);
    void creatingUser(String adminName);
}

