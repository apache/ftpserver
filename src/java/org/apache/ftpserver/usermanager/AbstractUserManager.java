/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * $Id$
 */
package org.apache.ftpserver.usermanager;

import org.apache.avalon.phoenix.BlockContext;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * Abstract user manager class.
 *
 * @author Paul Hammant <a href="mailto:Paul_Hammant@yahoo.com">Paul_Hammant@yahoo.com</a>
 */
public
abstract class AbstractUserManager extends AbstractLogEnabled
                                   implements UserManagerInterface,
                                              Contextualizable,
                                              Configurable,
                                              Initializable,
                                              Disposable {

    protected Configuration mConfig;
    protected BlockContext mBlockContext;
    protected String mstAdminName;

    /**
     * Set context object - first step.
     */
    public void contextualize(Context context) throws ContextException {
        mBlockContext = (BlockContext) context;
    }

    /**
     * Get block context.
     */
    public BlockContext getContext() {
        return mBlockContext;
    }

    /**
     * Configure user manager - third step.
     */
    public void configure(Configuration config) throws ConfigurationException {
        mConfig = config;

        Configuration adminConf = mConfig.getChild("ftp-admin-name", false);
        mstAdminName = "admin";
        if(adminConf != null) {
            mstAdminName = adminConf.getValue(mstAdminName);
        }
    }

    /**
     * Get config object.
     */
    public Configuration getConfig() {
        return mConfig;
    }


    /**
     * Initialize - fourth step.
     */
    public void initialize() throws Exception {
    }


    /**
     * Reload user data - dummy implementation.
     */
    public void reload() throws Exception {
    }

    /**
     * Get admin name
     */
    public String getAdminName() {
        return mstAdminName;
    }

    /**
     * Close user manager - dummy implementation.
     */
    public void dispose() {
        getLogger().info("Closing user manager...");
    }

}
