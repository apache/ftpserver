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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.DefaultDataConnectionConfiguration;
import org.apache.ftpserver.DefaultDataConnectionConfiguration.Active;
import org.apache.ftpserver.DefaultDataConnectionConfiguration.Passive;
import org.apache.ftpserver.interfaces.DataConnectionConfiguration;
import org.apache.ftpserver.listener.nio.NioListener;
import org.apache.ftpserver.ssl.DefaultSslConfiguration;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.mina.filter.firewall.Subnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parses the FtpServer "nio-listener" element into a Spring
 * bean graph
 */
public class ListenerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private final Logger LOG = LoggerFactory.getLogger(ListenerBeanDefinitionParser.class);

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<NioListener> getBeanClass(Element element) {
        return NioListener.class;
    }

    /**
     * Parse CIDR notations into MINA {@link Subnet}s. 
     * TODO: move to Mina
     */
    private Subnet parseSubnet(String subnet) {
        if(subnet == null) {
            throw new NullPointerException("subnet can not be null");
        }
        
        String[] tokens = subnet.split("/");

        String ipString;
        String maskString;
        if(tokens.length == 2) {
            ipString = tokens[0];
            maskString = tokens[1];
        } else if(tokens.length == 1) {
            ipString = tokens[0];
            maskString = "32";
        } else {
            throw new IllegalArgumentException("Illegal subnet format: " + subnet);
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(ipString);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Illegal IP address in subnet: " + subnet);
        }
        
        int mask = Integer.parseInt(maskString);
        if(mask < 0 || mask > 32) {
            throw new IllegalArgumentException("Mask must be in the range 0-32");
        }
        
        return new Subnet(address, mask);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        if(StringUtils.hasText(element.getAttribute("port"))) {
            builder.addPropertyValue("port", Integer.parseInt(element.getAttribute("port")));
        }
        
        SslConfiguration ssl = parseSsl(element);
        if(ssl != null) {
            builder.addPropertyValue("sslConfiguration", ssl);
        }
        
        Element dataConElm = SpringUtil.getChildElement(element, FtpServerNamespaceHandler.FTPSERVER_NS, "data-connection");
        DataConnectionConfiguration dc = parseDataConnection(dataConElm, ssl);
        builder.addPropertyValue("dataConnectionConfiguration", dc);
        
        Element blacklistElm = SpringUtil.getChildElement(element, FtpServerNamespaceHandler.FTPSERVER_NS, "blacklist");
        if(blacklistElm != null && StringUtils.hasText(blacklistElm.getTextContent())) {
            String[] blocks = blacklistElm.getTextContent().split("[\\s,]+");
            List<Subnet> subnets = new ArrayList<Subnet>();
            
            for(String block : blocks) {
                subnets.add(parseSubnet(block));
            }
            
            builder.addPropertyValue("blockedSubnets", subnets);
        }
        
        if(StringUtils.hasText(element.getAttribute("idle-timeout"))) {
            builder.addPropertyValue("idleTimeout", SpringUtil.parseInt(element, "idle-timeout", 300));
        }
        if(StringUtils.hasText(element.getAttribute("port"))) {
            builder.addPropertyValue("port", SpringUtil.parseInt(element, "port", 21));
        }
        InetAddress localAddress = SpringUtil.parseInetAddress(element, "local-address");
        if(localAddress != null) {
            builder.addPropertyValue("localAddress", localAddress);
        }
        builder.addPropertyValue("implicitSsl", SpringUtil.parseBoolean(element, "implicit-ssl", false));
    }
    
    
    private SslConfiguration parseSsl(Element parent) {
        Element sslElm = SpringUtil.getChildElement(parent, FtpServerNamespaceHandler.FTPSERVER_NS, "ssl");
        
        if(sslElm != null) {
            DefaultSslConfiguration ssl = new DefaultSslConfiguration();
        
            Element keyStoreElm = SpringUtil.getChildElement(sslElm, FtpServerNamespaceHandler.FTPSERVER_NS, "keystore");
            if(keyStoreElm != null) {
                ssl.setKeystoreFile(SpringUtil.parseFile(keyStoreElm, "file"));
                ssl.setKeystorePassword(SpringUtil.parseString(keyStoreElm, "password"));
                
                String type = SpringUtil.parseString(keyStoreElm, "type");
                if(type != null) {
                    ssl.setKeystoreType(type);
                }

                String keyAlias = SpringUtil.parseString(keyStoreElm, "key-alias");
                if(keyAlias != null) {
                    ssl.setKeyAlias(keyAlias);
                }
                
                String keyPassword = SpringUtil.parseString(keyStoreElm, "key-password");
                if(keyPassword != null) {
                    ssl.setKeyPassword(keyPassword);
                }
                
                String algorithm = SpringUtil.parseString(keyStoreElm, "algorithm");
                if(algorithm != null) {
                    ssl.setKeystoreAlgorithm(algorithm);
                }
            }
            
            Element trustStoreElm = SpringUtil.getChildElement(sslElm, FtpServerNamespaceHandler.FTPSERVER_NS, "truststore");
            if(trustStoreElm != null) {
                ssl.setTruststoreFile(SpringUtil.parseFile(trustStoreElm, "file"));
                ssl.setTruststorePassword(SpringUtil.parseString(trustStoreElm, "password"));
                
                String type = SpringUtil.parseString(trustStoreElm, "type");
                if(type != null) {
                    ssl.setTruststoreType(type);
                }
                
                String algorithm = SpringUtil.parseString(trustStoreElm, "algorithm");
                if(algorithm != null) {
                    ssl.setTruststoreAlgorithm(algorithm);
                }
            }
            
            String clientAuthStr = SpringUtil.parseString(sslElm, "client-authentication");
            if(clientAuthStr != null) { 
                ssl.setClientAuthentication(clientAuthStr);
            } 
            
            String enabledCiphersuites = SpringUtil.parseString(sslElm, "enabled-ciphersuites");
            if(enabledCiphersuites != null) { 
                ssl.setEnabledCipherSuites(enabledCiphersuites.split(" "));
            }  

            String protocol = SpringUtil.parseString(sslElm, "protocol");
            if(protocol != null) { 
                ssl.setSslProtocol(protocol);
            }  

            
            return ssl;
        } else {
            return null;
        }
        
    
    }
    
    private DataConnectionConfiguration parseDataConnection(Element element, SslConfiguration listenerSslConfiguration) {
        DefaultDataConnectionConfiguration dc = new DefaultDataConnectionConfiguration();
        
        if(element != null) {
            // data con config element available
            SslConfiguration ssl = parseSsl(element);
            if(ssl != null) {
                LOG.debug("SSL configuration found for the data connection");
                dc.setSslConfiguration(ssl);
            } else {
                // go look for the parent element SSL config
                // find the listener element
                if(listenerSslConfiguration != null) {
                    LOG.debug("SSL configuration found for the listener, falling back for that for the data connection");
                    dc.setSslConfiguration(listenerSslConfiguration);
                }
            }
            
            Element activeElm = SpringUtil.getChildElement(element, FtpServerNamespaceHandler.FTPSERVER_NS, "active");
            if(activeElm != null) {
                Active active = new Active();
                active.setEnable(SpringUtil.parseBoolean(activeElm, "enabled", true));
                active.setIpCheck(SpringUtil.parseBoolean(activeElm, "ip-check", false));
                active.setLocalPort(SpringUtil.parseInt(activeElm, "local-port", 0));
                
                InetAddress localAddress = SpringUtil.parseInetAddress(activeElm, "local-address");
                if(localAddress != null) {
                    active.setLocalAddress(localAddress);
                }
                
                dc.setActive(active);
            }
            
            Element passiveElm = SpringUtil.getChildElement(element, FtpServerNamespaceHandler.FTPSERVER_NS, "passive");
            if(passiveElm != null) {
                Passive passive = new Passive();
                
                InetAddress address = SpringUtil.parseInetAddress(passiveElm, "address");
                if(address != null) {
                    passive.setAddress(address);
                }
                
                InetAddress externalAddress = SpringUtil.parseInetAddress(passiveElm, "external-address");
                if(externalAddress != null) {
                    passive.setExternalAddress(externalAddress);
                }
                
                String ports = SpringUtil.parseString(passiveElm, "ports");
                if(ports != null) {
                    passive.setPorts(ports);
                }
                dc.setPassive(passive);
            }
        } else {
            // no data conn config element, do we still have SSL config from the parent?
            if(listenerSslConfiguration != null) {
                LOG.debug("SSL configuration found for the listener, falling back for that for the data connection");
                dc.setSslConfiguration(listenerSslConfiguration);
            }

        }

        return dc;
    }


}
