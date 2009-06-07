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

package org.apache.ftpserver.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Provides support for parsing a passive ports string as well as keeping track
 * of reserved passive ports.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class PassivePorts {

    private static final int MAX_PORT = 65535;

    private int[] passivePorts;

    private boolean[] reservedPorts;

    private String passivePortsString;

    private boolean checkIfBound;
    
    /**
     * Parse a string containing passive ports
     * 
     * @param portsString
     *            A string of passive ports, can contain a single port (as an
     *            integer), multiple ports seperated by commas (e.g.
     *            123,124,125) or ranges of ports, including open ended ranges
     *            (e.g. 123-125, 30000-, -1023). Combinations for single ports
     *            and ranges is also supported.
     * @return An instance of {@link PassivePorts} based on the parsed string
     * @throws IllegalArgumentException
     *             If any of of the ports in the string is invalid (e.g. not an
     *             integer or too large for a port number)
     */
    private static int[] parse(final String portsString) {
        List<Integer> passivePortsList = new ArrayList<Integer>();

        boolean inRange = false;
        Integer lastPort = 1;
        StringTokenizer st = new StringTokenizer(portsString, ",;-", true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            if (",".equals(token) || ";".equals(token)) {
                if (inRange) {
                    fillRange(passivePortsList, lastPort, MAX_PORT);
                }

                // reset state
                lastPort = 1;
                inRange = false;
            } else if ("-".equals(token)) {
                inRange = true;
            } else if (token.length() == 0) {
                // ignore whitespace
            } else {
                Integer port = Integer.valueOf(token);

                verifyPort(port.intValue());

                if (inRange) {
                    // add all numbers from last int
                    fillRange(passivePortsList, lastPort, port);

                    inRange = false;
                }

                addPort(passivePortsList, port);

                lastPort = port;
            }
        }

        if (inRange) {
            fillRange(passivePortsList, lastPort, MAX_PORT);
        }

        int[] passivePorts = new int[passivePortsList.size()];

        Iterator<Integer> iter = passivePortsList.iterator();

        int counter = 0;
        while (iter.hasNext()) {
            Integer port = iter.next();
            passivePorts[counter] = port.intValue();
            counter++;
        }

        return passivePorts;
    }

    /**
     * Fill a range of ports
     */
    private static void fillRange(final List<Integer> passivePortsList,
            final Integer beginPort, final Integer endPort) {
        for (int i = beginPort.intValue(); i <= endPort.intValue(); i++) {
            addPort(passivePortsList, i);
        }
    }

    /**
     * Add a single port if not already in list
     */
    private static void addPort(final List<Integer> passivePortsList,
            final Integer rangePort) {
        if (!passivePortsList.contains(rangePort)) {
            passivePortsList.add(rangePort);
        }
    }

    /**
     * Verify that the port is within the range of allowed ports
     */
    private static void verifyPort(final int port) {
        if (port < 0) {
            throw new IllegalArgumentException("Port can not be negative: "
                    + port);
        } else if (port > MAX_PORT) {
            throw new IllegalArgumentException("Port too large: " + port);
        }
    }

    public PassivePorts(final String passivePorts, boolean checkIfBound) {
        this(parse(passivePorts), checkIfBound);

        this.passivePortsString = passivePorts;
    }

    public PassivePorts(final int[] passivePorts, boolean checkIfBound) {
        if (passivePorts != null) {
            this.passivePorts = passivePorts.clone();
        } else {
            this.passivePorts = null;
        }

        reservedPorts = new boolean[passivePorts.length];
        this.checkIfBound = checkIfBound;
    }

    private boolean checkPortUnbound(int port) {
        // is this check disabled?
        if(!checkIfBound) {
            return true;
        }
        
        // if using 0 port, it will always be available
        if(port == 0) {
            return true;
        }
        
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // port probably in used, check next
            return false;
        } finally {
            if(ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    // could not close, check next
                    return false;
                }
            }
        }
    }
    
    public int reserveNextPort() {
        // search for a free port
        for (int i = 0; i < passivePorts.length; i++) {
            if (!reservedPorts[i] && checkPortUnbound(passivePorts[i])) {
                if (passivePorts[i] != 0) {
                    reservedPorts[i] = true;
                }
                return passivePorts[i];
            }
        }

        return -1;
    }

    public void releasePort(final int port) {
        for (int i = 0; i < passivePorts.length; i++) {
            if (passivePorts[i] == port) {
                reservedPorts[i] = false;
                break;
            }
        }
    }

    @Override
    public String toString() {
        if (passivePortsString != null) {
            return passivePortsString;
        } else {
            StringBuffer sb = new StringBuffer();

            for (int port : passivePorts) {
                sb.append(port);
                sb.append(",");
            }
            // remove the last ,
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }

}