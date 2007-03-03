package org.apache.ftpserver.perftest;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.log4j.Logger;

public class FtpClientLogger implements ProtocolCommandListener {

    private final Logger log = Logger.getLogger(FtpClientLogger.class);
    
    private String clientId;
    
    public FtpClientLogger(String clientId) {
        this.clientId = clientId;
    }
    
    public void protocolCommandSent(ProtocolCommandEvent event) {
        log.debug(clientId + " > " + event.getMessage().trim());
        
    }

    public void protocolReplyReceived(ProtocolCommandEvent event) {
        log.debug(clientId + " < " + event.getMessage().trim());
    }

}
