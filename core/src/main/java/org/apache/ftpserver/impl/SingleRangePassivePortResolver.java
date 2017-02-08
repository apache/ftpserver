package org.apache.ftpserver.impl;

public class SingleRangePassivePortResolver implements PassivePortResolver {

    private PassivePorts passivePorts;

    public SingleRangePassivePortResolver(String range) {
        passivePorts = new PassivePorts(range,true);
    }

    public PassivePorts getPassivePorts() {
        return passivePorts;
    }

    public void setPassivePorts(PassivePorts passivePorts) {
        this.passivePorts = passivePorts;
    }

    public synchronized int requestPassivePort(FtpIoSession session) {
        return passivePorts.reserveNextPort();
    }

    public synchronized void releasePassivePort(FtpIoSession session, int port) {
        passivePorts.releasePort(port);
    }

    @Override
    public String toString() {
        return passivePorts.toString();
    }

    public static SingleRangePassivePortResolver buildFromRange(String range) {
        return new SingleRangePassivePortResolver(range);
    }

}
