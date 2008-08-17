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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.DefaultConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.interfaces.MessageResource;
import org.apache.ftpserver.message.MessageResourceImpl;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parses the FtpServer "server" element into a Spring bean graph
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class ServerBeanDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    /**
     * {@inheritDoc}
     */
    protected Class<? extends FtpServer> getBeanClass(final Element element) {
        return FtpServer.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParse(final Element element,
            final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        List<Element> childs = SpringUtil.getChildElements(element);
        for (Element childElm : childs) {
            String childName = childElm.getLocalName();

            if ("listeners".equals(childName)) {
                Map listeners = parseListeners(childElm, parserContext, builder);

                if (listeners.size() > 0) {
                    builder.addPropertyValue("listeners", listeners);
                }
            } else if ("ftplets".equals(childName)) {
                Map ftplets = parseFtplets(childElm, parserContext, builder);
                builder.addPropertyValue("ftplets", ftplets);
            } else if ("file-user-manager".equals(childName)
                    || "db-user-manager".equals(childName)) {
                Object userManager = parserContext.getDelegate()
                        .parseCustomElement(childElm,
                                builder.getBeanDefinition());
                builder.addPropertyValue("userManager", userManager);
            } else if ("user-manager".equals(childName)) {
                builder.addPropertyValue("userManager", SpringUtil
                        .parseSpringChildElement(childElm, parserContext,
                                builder));
            } else if ("native-filesystem".equals(childName)) {
                Object fileSystem = parserContext.getDelegate()
                        .parseCustomElement(childElm,
                                builder.getBeanDefinition());
                builder.addPropertyValue("fileSystem", fileSystem);
            } else if ("filesystem".equals(childName)) {
                builder.addPropertyValue("fileSystem", SpringUtil
                        .parseSpringChildElement(childElm, parserContext,
                                builder));
            } else if ("commands".equals(childName)) {
                Object commandFactory = parserContext.getDelegate()
                        .parseCustomElement(childElm,
                                builder.getBeanDefinition());
                builder.addPropertyValue("commandFactory", commandFactory);
            } else if ("messages".equals(childName)) {
                MessageResource mr = parseMessageResource(childElm,
                        parserContext, builder);
                builder.addPropertyValue("messageResource", mr);

            } else {
                throw new FtpServerConfigurationException(
                        "Unknown configuration name: " + childName);
            }
        }

        // Configure login limits
        DefaultConnectionConfig connectionConfig = new DefaultConnectionConfig();
        if (StringUtils.hasText(element.getAttribute("max-logins"))) {
            connectionConfig.setMaxLogins(SpringUtil.parseInt(element,
                    "max-logins"));
        }
        if (StringUtils.hasText(element.getAttribute("max-anon-logins"))) {
            connectionConfig.setMaxAnonymousLogins(SpringUtil.parseInt(element,
                    "max-anon-logins"));
        }
        if (StringUtils.hasText(element.getAttribute("anon-enabled"))) {
            connectionConfig.setAnonymousLoginEnabled(SpringUtil.parseBoolean(
                    element, "anon-enabled", true));
        }
        if (StringUtils.hasText(element.getAttribute("max-login-failures"))) {
            connectionConfig.setMaxLoginFailures(SpringUtil.parseInt(element,
                    "max-login-failures"));
        }
        if (StringUtils.hasText(element.getAttribute("login-failure-delay"))) {
            connectionConfig.setLoginFailureDelay(SpringUtil.parseInt(element,
                    "login-failure-delay"));
        }

        builder.addPropertyValue("connectionConfig", connectionConfig);

    }

    /**
     * Parse the "messages" element
     */
    private MessageResource parseMessageResource(final Element childElm,
            final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {

        MessageResourceImpl mr = new MessageResourceImpl();

        if (StringUtils.hasText(childElm.getAttribute("languages"))) {
            String langString = childElm.getAttribute("languages");

            String[] languages = langString.split("[\\s,]+");

            mr.setLanguages(languages);
        }

        if (StringUtils.hasText(childElm.getAttribute("directory"))) {
            mr.setCustomMessageDirectory(new File(childElm
                    .getAttribute("directory")));

        }

        return mr;
    }

    /**
     * Parse the "ftplets" element
     */
    private Map parseFtplets(final Element childElm,
            final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        ManagedMap ftplets = new ManagedMap();

        List<Element> childs = SpringUtil.getChildElements(childElm);

        for (Element ftpletElm : childs) {
            ftplets
                    .put(ftpletElm.getAttribute("name"), SpringUtil
                            .parseSpringChildElement(ftpletElm, parserContext,
                                    builder));
        }

        return ftplets;
    }

    /**
     * Parse listeners elements
     */
    @SuppressWarnings("unchecked")
    private Map parseListeners(final Element listenersElm,
            final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        ManagedMap listeners = new ManagedMap();

        List<Element> childs = SpringUtil.getChildElements(listenersElm);

        for (Element listenerElm : childs) {
            Object listener = null;
            String ln = listenerElm.getLocalName();
            if ("nio-listener".equals(ln)) {
                listener = parserContext.getDelegate().parseCustomElement(
                        listenerElm, builder.getBeanDefinition());
            } else if ("listener".equals(ln)) {
                listener = SpringUtil.parseSpringChildElement(listenerElm,
                        parserContext, builder);
            } else {
                throw new FtpServerConfigurationException(
                        "Unknown listener element " + ln);
            }

            String name = listenerElm.getAttribute("name");

            listeners.put(name, listener);
        }

        return listeners;
    }
}
