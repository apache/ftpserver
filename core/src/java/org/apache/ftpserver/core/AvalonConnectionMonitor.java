package org.apache.ftpserver.core;

import org.apache.avalon.framework.logger.Logger;
import org.apache.ftpserver.ConnectionMonitor;
import org.apache.ftpserver.User;

import java.io.File;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public class AvalonConnectionMonitor implements ConnectionMonitor {

    Logger logger;

    public AvalonConnectionMonitor(Logger logger) {
        this.logger = logger;
    }

    public void timerError(Exception ex) {
        logger.error("ConnectionService.timerTask()", ex);
    }

    public void removingIdleUser(User user) {
        logger.info("Removing idle user " + user);
    }

    public void newConnectionFrom(final User newUser) {
        logger.info("New connection from " + newUser.getClientAddress().getHostAddress());
    }

    public void cannotFindHome(File userHome, User user) {
        logger.warn("Cannot find home (" + userHome.getAbsolutePath() + ") for user " + user.getName());
    }

    public void cannotCreateHome(File userHome, User user) {
        logger.warn("Cannot create home (" + userHome.getAbsolutePath() + ") for user " + user.getName());
    }

    public void creatingHome(File userHome, User user) {
        logger.info("Creating home (" + userHome.getAbsolutePath() + ") for user " + user.getName());
    }

    public void userHomeNotADir(File userHome, User user) {
        logger.warn("User home (" + userHome.getAbsolutePath() + ") for user " + user.getName() + " is not a directory.");
    }

    public void anonConnection(User thisUser) {
        logger.info("Anonymous connection - " + thisUser.getClientAddress().getHostAddress() + " - " + thisUser.getPassword());
    }

    public void userLogin(final User thisUser) {
        logger.info("User login - " + thisUser.getClientAddress().getHostAddress() + " - " + thisUser.getName());
    }

    public void authFailed(String user) {
        logger.warn("Authentication failed - " + user);
    }

    public void creatingUser(String adminName) {
        logger.info("Creating user " + adminName);
    }
}
