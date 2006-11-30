package org.apache.ftpserver;

import org.apache.ftpserver.interfaces.Connection;

/**
 * Interface for the component responsible for waiting for incoming
 * socket requests and kicking off {@link Connection}s 
 *
 */
public interface Listener {
    
    /**
     * Start the listener, will initiate the listener waiting
     * on the socket.
     * The method should not return until the listener has
     * started accepting socket requests.
     * 
     * @throws Exception On error during start up
     */
    void start() throws Exception;

    /**
     * Stop the listener, it should no longer except socket requests.
     * The method should not return until the listener has stopped
     * accepting socket requests.
     */
    void stop();

    /**
     * Checks if the listener is currently started.
     * 
     * @return True if the listener is started
     */
    boolean isStopped();

    /**
     * Temporarily stops the listener from accepting socket requests.
     * Resume the listener by using the {@link #resume()} method.
     * The method should not return until the listener has stopped 
     * accepting socket requests.
     */
    void suspend();

    /**
     * Resumes a suspended listener. 
     * The method should not return until the listener has
     * started accepting socket requests.
     */
    void resume();

    /**
     * Checks if the listener is currently suspended
     * @return
     */
    boolean isSuspended();
}