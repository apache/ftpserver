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

package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.UserManager;


/**
 * Abstract common base type for {@link UserManager} implementations
 */
public abstract 
class AbstractUserManager implements UserManager, Component {

    public static final String ATTR_LOGIN             = "uid";
    public static final String ATTR_PASSWORD          = "userpassword";
    public static final String ATTR_HOME              = "homedirectory";
    public static final String ATTR_WRITE_PERM        = "writepermission";
    public static final String ATTR_ENABLE            = "enableflag";
    public static final String ATTR_MAX_IDLE_TIME     = "idletime";
    public static final String ATTR_MAX_UPLOAD_RATE   = "uploadrate";
    public static final String ATTR_MAX_DOWNLOAD_RATE = "downloadrate";
    public static final String ATTR_MAX_LOGIN_NUMBER = "maxloginnumber";
    public static final String ATTR_MAX_LOGIN_PER_IP = "maxloginperip";

}

