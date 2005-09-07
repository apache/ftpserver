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
package org.apache.ftpserver.logger;

import java.util.Date;

/**
 * Redirect all the log messages to console (System.out).
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class ConsoleLogger extends AbstractLogger {

    /**
     * Print log message.
     */
    public void write(int level, String msg) {
        String logStr = "[" + getDateFormat().format(new Date()) + "] " +
                        "(" + LOG_LABELS[level] + ") " + 
                        msg;
        System.out.println(logStr);
    }
    
    /**
     * Print log message and exception.
     */
    public void write(int level, String msg, Throwable th) {
        write(level, msg);
        th.printStackTrace();
    } 
}
