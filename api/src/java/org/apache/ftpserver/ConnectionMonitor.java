package org.apache.ftpserver;

import java.io.File;

import org.apache.ftpserver.User;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface ConnectionMonitor {
    void timerError(Exception ex);
    void removingIdleUser(User user);
    void newConnectionFrom(User newUser);
    void cannotFindHome(File userHome, User user);
    void cannotCreateHome(File userHome, User user);
    void creatingHome(File userHome, User user);
    void userHomeNotADir(File userHome, User user);
    void anonConnection(User thisUser);
    void userLogin(User thisUser);
    void authFailed(String user);
    void creatingUser(String adminName);
}

