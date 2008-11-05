/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ftpserver.message;

import java.io.File;
import java.util.List;

import org.apache.ftpserver.message.impl.DefaultMessageResource;

/**
 * Factory for creating message resource implementation
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 693604 $, $Date: 2008-09-09 22:55:19 +0200 (Tue, 09 Sep 2008) $
 */
public class MessageResourceFactory {

    private List<String> languages;

    private File customMessageDirectory;

    public MessageResource createMessageResource() {
        return new DefaultMessageResource(languages, customMessageDirectory);
    }
    
    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public File getCustomMessageDirectory() {
        return customMessageDirectory;
    }

    public void setCustomMessageDirectory(File customMessageDirectory) {
        this.customMessageDirectory = customMessageDirectory;
    }
}
