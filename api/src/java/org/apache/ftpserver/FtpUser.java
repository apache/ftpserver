/*
 * Created on Aug 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.ftpserver;

import java.io.OutputStream;
import java.net.InetAddress;

/**
 * @author Vladimirov
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface FtpUser {
    public static final String ANONYMOUS = "anonymous";

    /**
     * Get the user data type.
     */
    public abstract char getType();

    /**
     * Set the data type. Supported types are A (ascii) and I (binary).
     * 
     * @return true if success
     */
    public abstract boolean setType(char type);

    /**
     * Get the file structure.
     */
    public abstract char getStructure();

    /**
     * Set the file structure. Supported structure type is F (file).
     * 
     * @return true if success
     */
    public abstract boolean setStructure(char stru);

    /**
     * Get the transfer mode.
     */
    public abstract char getMode();

    /**
     * Set the transfer type. Supported transfer type is S (stream).
     * 
     * @return true if success
     */
    public abstract boolean setMode(char md);

    /**
     * Get output stream. Returns <code>ftpserver.util.AsciiOutputStream</code>
     * if the transfer type is ASCII.
     */
    public abstract OutputStream getOutputStream(OutputStream os);

    /**
     * Is an anonymous user?
     */
    public abstract boolean getIsAnonymous();

    /**
     * Get the user name.
     */
    public String getName();

    /**
     * Get the user password.
     */
    public String getPassword();

    /**
     * Get client address
     */
    public InetAddress getClientAddress();
}