package org.apache.ftpserver.interfaces;

import java.io.File;

import org.apache.ftpserver.FtpUser;

/**
 * @author Paul Hammant
 * @version $Revision$
 */
public interface FtpFileMonitor {

    void fileUploaded(FtpUser user, File fl);
    void fileDownloaded(FtpUser user, File fl);
    void fileDeleted(FtpUser user, File fl);

}
