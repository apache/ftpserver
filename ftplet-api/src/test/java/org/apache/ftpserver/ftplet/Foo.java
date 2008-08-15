package org.apache.ftpserver.ftplet;

import java.io.IOException;

import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpletEnum;

public class Foo extends DefaultFtplet{

    @Override
    public FtpletEnum onMkdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        session.write(new DefaultFtpReply(550, "Error!"));
        return FtpletEnum.RET_SKIP;
    }

    @Override
    public FtpletEnum onMkdirStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        if(session.isSecure() && session.getDataConnection().isSecure()) {
            // all is good
        }
        return null;
    }
    
    

}
