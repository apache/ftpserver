package org.apache.ftpserver;

import java.io.IOException;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface CommandHandlerMonitor {
    void unknownResponseException(String message, Throwable th);

    void ipBlockException(String message, IOException ex);

    void addUserException(String message, UserManagerException ex);

    void setAttrException(String message, UserManagerException ex);

    void deleteUserException(String message, UserManagerException ex);
}
