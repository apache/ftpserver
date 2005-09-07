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
package org.apache.ftpserver.interfaces;

import org.apache.ftpserver.ftplet.FileObject;

/**
 * This is the file related activity observer.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
interface FileObserver {

    /**
     * User file upload notification.
     */
    void notifyUpload(IConnection connection, FileObject file, long size);
    
    /**
     * User file download notification.
     */
    void notifyDownload(IConnection connection, FileObject file, long size);
    
    /**
     * User file delete notification.
     */
    void notifyDelete(IConnection connection, FileObject file);
    
    /**
     * User make directory notification.
     */
    void notifyMkdir(IConnection connection, FileObject file);
    
    /**
     * User remove directory notification.
     */
    void notifyRmdir(IConnection connection, FileObject file);
    
}
