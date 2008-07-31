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

package org.apache.ftpserver.config.spring;

import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.DbUserManager;
import org.apache.ftpserver.usermanager.PropertiesUserManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parses the FtpServer "file-user-manager" or "db-user-manager" elements into a Spring
 * bean graph
 */
public class UserManagerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    
    @Override
    protected Class<? extends UserManager> getBeanClass(final Element element) {
        if(element.getLocalName().equals("file-user-manager")) {
            return PropertiesUserManager.class;
        } else {
            return DbUserManager.class;
        }
    }

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        if(getBeanClass(element) == PropertiesUserManager.class) {
            builder.addPropertyValue("propFile", element.getAttribute("file"));
            if(StringUtils.hasText(element.getAttribute("encrypt-passwords")) &&
                    element.getAttribute("encrypt-passwords").equals("true")) {
                builder.addPropertyValue("encryptPasswords", true);
            }
            builder.setInitMethodName("configure");
        } else {
            Element dsElm = SpringUtil.getChildElement(element, 
                    FtpServerNamespaceHandler.FTPSERVER_NS, "data-source");
            
            // schema ensure we get the right type of element
            Element springElm = SpringUtil.getChildElement(dsElm, null, null);
            Object o;
            if("bean".equals(springElm.getLocalName())) {
                o = parserContext.getDelegate().parseBeanDefinitionElement(springElm, builder.getBeanDefinition());
            } else {
                // ref
                o = parserContext.getDelegate().parsePropertySubElement(springElm, builder.getBeanDefinition());

            }
            builder.addPropertyValue("dataSource", o);
            
            builder.addPropertyValue("sqlUserInsert",       getSql(element, "insert-user"));
            builder.addPropertyValue("sqlUserUpdate",       getSql(element, "update-user"));
            builder.addPropertyValue("sqlUserDelete",       getSql(element, "delete-user"));
            builder.addPropertyValue("sqlUserSelect",       getSql(element, "select-user"));
            builder.addPropertyValue("sqlUserSelectAll",    getSql(element, "select-all-users"));
            builder.addPropertyValue("sqlUserAdmin",        getSql(element, "is-admin"));
            builder.addPropertyValue("sqlUserAuthenticate", getSql(element, "authenticate"));
            
            builder.setInitMethodName("configure");
        }
    }
    
    private String getSql(final Element element, final String elmName) {
        return SpringUtil.getChildElementText(element, FtpServerNamespaceHandler.FTPSERVER_NS, elmName);    
    }
}
