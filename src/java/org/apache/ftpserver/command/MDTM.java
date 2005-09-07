// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.command;

import org.apache.ftpserver.Command;
import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.RequestHandler;
import org.apache.ftpserver.ftplet.FileObject;
import org.apache.ftpserver.ftplet.FtpException;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * <code>MDTM &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 * 
 * Returns the date and time of when a file was modified.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class MDTM implements Command {

    /**
     * Execute command
     */
    public void execute(RequestHandler handler,
                        FtpRequestImpl request, 
                        FtpWriter out) throws IOException, FtpException {
        
        // reset state
        request.resetState();
        
        // argument check
        String fileName = request.getArgument();
        if(fileName == null) {
            out.send(501, "MDTM", null);
            return;  
        }
        
        // get file object
        FileObject file = null;
        try {
            file = request.getFileSystemView().getFileObject(fileName);
        }
        catch(Exception ex) {
        }
        if(file == null) {
            out.send(550, "MDTM", fileName);
            return;
        }
        
        // now print date
        fileName = file.getFullName();
        if(file.doesExist()) {
            String dateStr = getTimeString( file.getLastModified() );
            out.send(213, "MDTM", dateStr);
        }
        else {
            out.send(550, "MDTM", fileName);
        }
    } 

    /**
     * Get time string.
     */
    private String getTimeString(long time) {
        StringBuffer sb = new StringBuffer(18);
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(time);
        
        // year
        sb.append(cal.get(Calendar.YEAR));
        
        // month
        int month = cal.get(Calendar.MONTH) + 1;
        if(month < 10) {
            sb.append('0');
        }
        sb.append(month);
        
        // date
        int date = cal.get(Calendar.DATE);
        if(date < 10) {
            sb.append('0');
        }
        sb.append(date);
        
        // hour
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour < 10) {
            sb.append('0');
        }
        sb.append(hour);
        
        // minute
        int min = cal.get(Calendar.MINUTE);
        if(min < 10) {
            sb.append('0');
        }
        sb.append(min);
        
        // second
        int sec = cal.get(Calendar.SECOND);
        if(sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        
        // millisecond
        int milli = cal.get(Calendar.MILLISECOND);
        if(milli < 100) {
            sb.append('0');
        }
        if(milli < 10) {
            sb.append('0');
        }
        sb.append(milli);
        return sb.toString();
    }
}
