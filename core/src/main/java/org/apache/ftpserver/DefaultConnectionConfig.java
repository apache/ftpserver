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

package org.apache.ftpserver;

/**
 * 
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 *
 */
public class DefaultConnectionConfig implements ConnectionConfig {

    private int maxLogins = 10;

    private boolean anonymousLoginEnabled = true;

    private int maxAnonymousLogins = 10;

    private int maxLoginFailures = 3;

    private int loginFailureDelay = 500;

    public int getLoginFailureDelay() {
        return loginFailureDelay;
    }

    public int getMaxAnonymousLogins() {
        return maxAnonymousLogins;
    }

    public int getMaxLoginFailures() {
        return maxLoginFailures;
    }

    public int getMaxLogins() {
        return maxLogins;
    }

    public boolean isAnonymousLoginEnabled() {
        return anonymousLoginEnabled;
    }

    public void setMaxLogins(final int maxLogins) {
        this.maxLogins = maxLogins;
    }

    public void setAnonymousLoginEnabled(final boolean anonymousLoginEnabled) {
        this.anonymousLoginEnabled = anonymousLoginEnabled;
    }

    public void setMaxAnonymousLogins(final int maxAnonymousLogins) {
        this.maxAnonymousLogins = maxAnonymousLogins;
    }

    public void setMaxLoginFailures(final int maxLoginFailures) {
        this.maxLoginFailures = maxLoginFailures;
    }

    public void setLoginFailureDelay(final int loginFailureDelay) {
        this.loginFailureDelay = loginFailureDelay;
    }

}
