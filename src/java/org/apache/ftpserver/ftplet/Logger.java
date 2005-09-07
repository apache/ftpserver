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
package org.apache.ftpserver.ftplet;

/**
 * Basic logger interface.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface Logger extends Component {

    /**
     * Write debug mesaage.
     */
    void debug(String msg);    
    
    /**
     * Write debug throwable.
     */
    void debug(String msg, Throwable th);

    /**
     * Write information mesaage.
     */
    void info(String msg);
    
    /**
     * Write information throwable.
     */
    void info(String msg, Throwable th);
    
    /**
     * Write warning mesaage.
     */
    void warn(String msg);
    
    /**
     * Write warning throwable.
     */
    void warn(String msg, Throwable th);
    
    /**
     * Write errpor mesaage.
     */
    void error(String msg);
    
    /**
     * Write warning throwable.
     */
    void error(String msg, Throwable th);
}
