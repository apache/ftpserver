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
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class XmlConfigurationHandler extends DefaultHandler {

    private ArrayList m_elements = new ArrayList();
    private XmlConfiguration m_root = null;
    private InputSource m_source = null;
    private StringBuffer m_elemVal = new StringBuffer(128);
    
    
    /**
     * Constructor.
     * @param is xml input stream
     */
    public XmlConfigurationHandler(InputStream is) {
        m_source = new InputSource(is);
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
        if(m_root != null) {
            return m_root;
        }
        
        XMLReader xmlreader = getParser();
        xmlreader.setContentHandler(this);
        xmlreader.setErrorHandler(this);
        xmlreader.parse(m_source); 
        
        return m_root;
    }

    /**
     * Update last element value.
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        m_elemVal.append(ch, start, length);
    }

    /**
     * Remove the last element from stack.
     */
    public void endElement(String uri,
                           String lname,
                           String qname) throws SAXException {
        
        int location = m_elements.size() - 1;
        XmlConfiguration lastElem = (XmlConfiguration)m_elements.remove(location);

        if(lastElem.getChildCount() == 0) {
            String elemVal = m_elemVal.toString().trim();
            
            if(elemVal.equals("")) {
                lastElem.setValue(null);
            }
            else {
                lastElem.setValue(elemVal);
            }
        }
        else {
            lastElem.setValue(null);
        }
        
        if(location == 0) {
            m_root = lastElem;
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
        int lastIdx = m_elements.size() - 1;
        m_elemVal.setLength(0);

        if(lastIdx > -1) {
            XmlConfiguration parent = (XmlConfiguration)m_elements.get(lastIdx);
            parent.setValue(null);
            parent.addChild(element);
        }
        m_elements.add(element);
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


