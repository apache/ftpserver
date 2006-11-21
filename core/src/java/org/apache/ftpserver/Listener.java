package org.apache.ftpserver;

public interface Listener {
    void start() throws Exception;

    void stop();

    boolean isStopped();

    void suspend();

    void resume();

    boolean isSuspended();
}