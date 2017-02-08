package org.apache.ftpserver.impl;

public interface PassivePortResolver {

    /**
     * Get passive data port. Data port number zero (0) means that any available
     * port will be used.
     */
    int requestPassivePort(FtpIoSession session);

    void releasePassivePort(final FtpIoSession session, final int port);

    String toString();

}
