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

import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.Logger;

/**
 * Null logger implementation.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class NullLogger implements Logger {

    /**
     * Set logger - does nothing
     */
    public void setLogger(Logger logger) {
    }
    
    /**
     * Does nothing
     */
    public void configure(Configuration config) {
    }
    
    /**
     * Does nothing
     */
    public void dispose() {
    }

    /**
     * Does nothing
     */
    public void debug(String msg) {
    }
    
    /**
     * Does nothing
     */
    public void debug(String msg, Throwable th) {
    }

    /**
     * Does nothing
     */
    public void info(String msg) {
    }

    /**
     * Does nothing
     */
    public void info(String msg, Throwable th) {
    }

    /**
     * Does nothing
     */
    public void warn(String msg) {
    }

    /**
     * Does nothing
     */
    public void warn(String msg, Throwable th) {
    }

    /**
     * Does nothing
     */
    public void error(String msg) {
    }
    
    /**
     * Does nothing
     */
    public void error(String msg, Throwable th) {
    }
}
