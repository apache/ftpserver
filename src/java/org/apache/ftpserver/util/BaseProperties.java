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
package org.apache.ftpserver.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 * This class encapsulates <code>java.util.Properties</code> to
 * add java primitives and some other java classes.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class BaseProperties extends Properties {


    /**
     * Default constructor.
     */
    public BaseProperties()  {
    }

    /**
     * Load existing property.
     */
    public BaseProperties(Properties prop)  {
        super(prop);
    }

    /**
     * Load properties from file
     */
    public BaseProperties(File fl) throws IOException {
        FileInputStream fis = new FileInputStream(fl);
        load(fis);
        fis.close();
    }

    /**
     * Load properties from <code>InputStream</code>
     */
    public BaseProperties(InputStream is) throws IOException  {
        load(is);
    }

    /**
     * Get all property key names.
     */
    public List getAllKeys() {
        Vector keys = new Vector();
        for(Enumeration en = propertyNames(); en.hasMoreElements(); ) {
            keys.add(en.nextElement().toString());
        }
        return keys;
    }


    //////////////////////////////////////////
    ////////  Properties Get Methods  ////////
    //////////////////////////////////////////
    /**
     * Get boolean value.
     */
    public boolean getBoolean(String str) throws PropertiesException {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }

        return str.toLowerCase().equals("true");
    }

    public boolean getBoolean(String str, boolean bol)  {
        try {
            return getBoolean(str);
        } catch (PropertiesException ex) {
            return bol;
        }
    }


    /**
     * Get integer value.
     */
    public int getInteger(String str) throws PropertiesException  {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new PropertiesException(ex);
        }
    }

    public int getInteger(String str, int intVal)  {
        try {
            return getInteger(str);
        } catch (PropertiesException ex) {
            return intVal;
        }
    }


    /**
     * Get long value.
     */
    public long getLong(String str) throws PropertiesException {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            throw new PropertiesException(ex);
        }
    }

    public long getLong(String str, long val)  {
        try {
            return getLong(str);
        } catch (PropertiesException ex) {
            return val;
        }
    }


    /**
     * Get double value.
     */
    public double getDouble(String str) throws PropertiesException  {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            throw new PropertiesException(ex);
        }
    }

    public double getDouble(String str, double doubleVal)  {
        try {
            return getDouble(str);
        } catch (PropertiesException ex) {
            return doubleVal;
        }
    }

    /**
     * Get <code>InetAddress</code>.
     */
    public InetAddress getInetAddress(String str) throws PropertiesException {
        str = getProperty(str);
        if(str == null) {
            throw new PropertiesException(str + " : not found");
        }

        try {
            return InetAddress.getByName(str);
        }
        catch(UnknownHostException ex) {
            throw new PropertiesException("Host " + str + " not found");
        }
    }

    public InetAddress getInetAddress(String str, InetAddress addr) {
        try {
            return getInetAddress(str);
        }
        catch(PropertiesException ex) {
            return addr;
        }
    }

    /**
     * Get <code>File</code> object.
     */
    public File getFile(String str) throws PropertiesException  {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }
        return new File(str);
    }

    public File getFile(String str, File fl)  {
        try {
            return getFile(str);
        } catch (PropertiesException ex) {
            return fl;
        }

    }


    /**
     * Get <code>Class</code> object
     */
    public Class getClass(String str) throws PropertiesException  {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }

        try {
            return Class.forName(str);
        } catch (ClassNotFoundException ex) {
            throw new PropertiesException(ex);
        }
    }

    public Class getClass(String str, Class cls)  {
        try {
            return getClass(str);
        } catch (PropertiesException ex) {
            return cls;
        }
    }


    /**
     * Get <code>TimeZone</code>
     */
    public TimeZone getTimeZone(String str) throws PropertiesException  {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }
        return TimeZone.getTimeZone(str);
    }

    public TimeZone getTimeZone(String str, TimeZone tz)  {
        try {
            return getTimeZone(str);
        } catch (PropertiesException ex) {
            return tz;
        }
    }


    /**
     * Get <code>DateFormat</code> object.
     */
    public SimpleDateFormat getDateFormat(String str) throws PropertiesException  {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str +  " : not found");
        }
        return new SimpleDateFormat(str);
    }

    public SimpleDateFormat getDateFormat(String str, SimpleDateFormat fmt)  {
        try {
            return getDateFormat(str);
        } catch (PropertiesException ex) {
            return fmt;
        }
    }


    /**
     * Get <code>Date</code> object.
     */
    public Date getDate(String str, DateFormat fmt) throws PropertiesException  {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }

        try {
            return fmt.parse(str);
        } catch (ParseException ex) {
            throw new PropertiesException(ex);
        }
    }

    public Date getDate(String str, DateFormat fmt, Date dt)  {
        try {
            return getDate(str, fmt);
        } catch (PropertiesException ex) {
            return dt;
        }
    }


    //////////////////////////////////////////
    ////////  Properties Set Methods  ////////
    //////////////////////////////////////////
    /**
     * Set boolean value.
     */
    public void setProperty(String key, boolean val)  {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set integer value.
     */
    public void setProperty(String key, int val)  {
        setProperty(key, String.valueOf(val));
    }


    /**
     * Set double value.
     */
    public void setProperty(String key, double val)  {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set float value.
     */
    public void setProperty(String key, float val)  {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set long value.
     */
    public void setProperty(String key, long val)  {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set <code>InetAddress</code>.
     */
    public void setInetAddress(String key, InetAddress val) {
        setProperty(key, val.getHostAddress());
    }

    /**
     * Set <code>File</code> object.
     */
    public void setProperty(String key, File val)  {
        setProperty(key, val.getAbsolutePath());
    }

    /**
     * Set <code>DateFormat</code> object.
     */
    public void setProperty(String key, SimpleDateFormat val)  {
        setProperty(key, val.toPattern());
    }

    /**
     * Set <code>TimeZone</code> object.
     */
    public void setProperty(String key, TimeZone val)  {
        setProperty(key, val.getID());
    }

    /**
     * Set <code>Date</code> object.
     */
    public void setProperty(String key, Date val, DateFormat fmt)  {
        setProperty(key, fmt.format(val));
    }

    /**
     * Set <code>Class</code> object.
     */
    public void setProperty(String key, Class val)  {
        setProperty(key, val.getName());
    }

}
