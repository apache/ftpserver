package org.apache.ftpserver;

import java.io.File;

import org.apache.ftpserver.User;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface FileMonitor {

    void fileUploaded(User user, File fl);
    void fileDownloaded(User user, File fl);
    void fileDeleted(User user, File fl);

}
