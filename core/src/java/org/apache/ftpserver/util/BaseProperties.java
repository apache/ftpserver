/* ====================================================================
 * Copyright 2002 - 2004
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
 *
 *
 * $Id$
 */
package org.apache.ftpserver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

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
    public BaseProperties() {
    }

    /**
     * Load existing property.
     */
    public BaseProperties(Properties prop) {
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
    public BaseProperties(InputStream is) throws IOException {
        load(is);
    }

    /**
     * Get all property key names.
     */
    public List getAllKeys() {
        Vector keys = new Vector();
        for (Enumeration en = propertyNames(); en.hasMoreElements();) {
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

    public boolean getBoolean(String str, boolean bol) {
        try {
            return getBoolean(str);
        } catch (PropertiesException ex) {
            return bol;
        }
    }


    /**
     * Get integer value.
     */
    public int getInteger(String str) throws PropertiesException {
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

    public int getInteger(String str, int intVal) {
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

    public long getLong(String str, long val) {
        try {
            return getLong(str);
        } catch (PropertiesException ex) {
            return val;
        }
    }


    /**
     * Get double value.
     */
    public double getDouble(String str) throws PropertiesException {
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

    public double getDouble(String str, double doubleVal) {
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
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }

        try {
            return InetAddress.getByName(str);
        } catch (UnknownHostException ex) {
            throw new PropertiesException("Host " + str + " not found");
        }
    }

    public InetAddress getInetAddress(String str, InetAddress addr) {
        try {
            return getInetAddress(str);
        } catch (PropertiesException ex) {
            return addr;
        }
    }

    /**
     * Get <code>File</code> object.
     */
    public File getFile(String str) throws PropertiesException {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }
        return new File(str);
    }

    public File getFile(String str, File fl) {
        try {
            return getFile(str);
        } catch (PropertiesException ex) {
            return fl;
        }

    }


    /**
     * Get <code>Class</code> object
     */
    public Class getClass(String str) throws PropertiesException {
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

    public Class getClass(String str, Class cls) {
        try {
            return getClass(str);
        } catch (PropertiesException ex) {
            return cls;
        }
    }


    /**
     * Get <code>TimeZone</code>
     */
    public TimeZone getTimeZone(String str) throws PropertiesException {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }
        return TimeZone.getTimeZone(str);
    }

    public TimeZone getTimeZone(String str, TimeZone tz) {
        try {
            return getTimeZone(str);
        } catch (PropertiesException ex) {
            return tz;
        }
    }


    /**
     * Get <code>DateFormat</code> object.
     */
    public SimpleDateFormat getDateFormat(String str) throws PropertiesException {
        str = getProperty(str);
        if (str == null) {
            throw new PropertiesException(str + " : not found");
        }
        return new SimpleDateFormat(str);
    }

    public SimpleDateFormat getDateFormat(String str, SimpleDateFormat fmt) {
        try {
            return getDateFormat(str);
        } catch (PropertiesException ex) {
            return fmt;
        }
    }


    /**
     * Get <code>Date</code> object.
     */
    public Date getDate(String str, DateFormat fmt) throws PropertiesException {
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

    public Date getDate(String str, DateFormat fmt, Date dt) {
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
    public void setProperty(String key, boolean val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set integer value.
     */
    public void setProperty(String key, int val) {
        setProperty(key, String.valueOf(val));
    }


    /**
     * Set double value.
     */
    public void setProperty(String key, double val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set float value.
     */
    public void setProperty(String key, float val) {
        setProperty(key, String.valueOf(val));
    }

    /**
     * Set long value.
     */
    public void setProperty(String key, long val) {
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
    public void setProperty(String key, File val) {
        setProperty(key, val.getAbsolutePath());
    }

    /**
     * Set <code>DateFormat</code> object.
     */
    public void setProperty(String key, SimpleDateFormat val) {
        setProperty(key, val.toPattern());
    }

    /**
     * Set <code>TimeZone</code> object.
     */
    public void setProperty(String key, TimeZone val) {
        setProperty(key, val.getID());
    }

    /**
     * Set <code>Date</code> object.
     */
    public void setProperty(String key, Date val, DateFormat fmt) {
        setProperty(key, fmt.format(val));
    }

    /**
     * Set <code>Class</code> object.
     */
    public void setProperty(String key, Class val) {
        setProperty(key, val.getName());
    }

}
