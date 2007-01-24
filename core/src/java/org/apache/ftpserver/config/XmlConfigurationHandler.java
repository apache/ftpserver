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

package org.apache.ftpserver.config;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAXHandler helps to build XmlConfiguration out of sax events.
 */
public 
class XmlConfigurationHandler extends DefaultHandler {

    private ArrayList elements = new ArrayList();
    private XmlConfiguration root = null;
    private InputSource source = null;
    private StringBuffer elemVal = new StringBuffer(128);
    
    
    /**
     * Constructor.
     * @param is xml input stream
     */
    public XmlConfigurationHandler(InputStream is) {
        source = new InputSource(is);
    }
    
    /**
     * Get parser instance
     */
    protected XMLReader getParser() throws Exception {                                           
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        return parser.getXMLReader();
    }
    
    /**
     * Parse the input to create xml configuration
     */
    public XmlConfiguration parse() throws Exception {                               
        if(root != null) {
            return root;
        }
        
        XMLReader xmlreader = getParser();
        xmlreader.setContentHandler(this);
        xmlreader.setErrorHandler(this);
        xmlreader.parse(source); 
        
        return root;
    }

    /**
     * Update last element value.
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        elemVal.append(ch, start, length);
    }

    /**
     * Remove the last element from stack.
     */
    public void endElement(String uri,
                           String lname,
                           String qname) throws SAXException {
        
        int location = elements.size() - 1;
        XmlConfiguration lastElem = (XmlConfiguration)elements.remove(location);

        if(lastElem.getChildCount() == 0) {
            String trimmedElemVal = elemVal.toString().trim();
            
            if(trimmedElemVal.equals("")) {
                lastElem.setValue(null);
            }
            else {
                lastElem.setValue(trimmedElemVal);
            }
        }
        else {
            lastElem.setValue(null);
        }
        
        if(location == 0) {
            root = lastElem;
        }
    }

    /**
     * New element. Set element attributes and push into stack.
     */
    public void startElement(String uri,
                             String lname,
                             String qname,
                             Attributes attrs ) throws SAXException {
        
        XmlConfiguration element = new XmlConfiguration(qname); 
        int lastIdx = elements.size() - 1;
        elemVal.setLength(0);

        if(lastIdx > -1) {
            XmlConfiguration parent = (XmlConfiguration)elements.get(lastIdx);
            parent.setValue(null);
            parent.addChild(element);
        }
        elements.add(element);
    }

    /**
     * Handle parsing error.
     */
    public void error(SAXParseException exception) throws SAXParseException {
        throw exception;
    }
        
    /**
     * Handle parsing error.
     */
    public void fatalError(SAXParseException exception) throws SAXParseException {
        throw exception;
    }
    
    /**
     * Handle parsing warning.
     */
    public void warning(SAXParseException exception) throws SAXParseException {
        throw exception;
    }
}


