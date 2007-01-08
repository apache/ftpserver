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

package org.apache.ftpserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Provides support for parsing a passive ports string as well as
 * keeping track of reserved passive ports.
 */
public class PassivePorts {

    private static final int MAX_PORT = 65535;
    private int[] passivePorts;
    private boolean[] reservedPorts;
    
    /**
     * Parse a string containing passive ports
     * @param portsString A string of passive ports, can contain a single
     *   port (as an integer), multiple ports seperated by commas (e.g. 123,124,125) 
     *   or ranges of ports, including open ended ranges (e.g. 123-125, 30000-, -1023).
     *   Combinations for single ports and ranges is also supported.
     * @return An instance of {@link PassivePorts} based on the parsed string
     * @throws IllegalArgumentException If any of of the ports in the string is
     *   invalid (e.g. not an integer or too large for a port number) 
     */
    public static PassivePorts parse(String portsString) {
        List passivePortsList = new ArrayList();
        
        boolean inRange = false;
        Integer lastPort = new Integer(1);
        StringTokenizer st = new StringTokenizer(portsString, ",;-", true);
        while(st.hasMoreTokens()) {
            String token = st.nextToken().trim();

            if(",".equals(token) || ";".equals(token)) {
                if(inRange) {
                    fillRange(passivePortsList, lastPort, new Integer(MAX_PORT));
                }
                
                // reset state
                lastPort = new Integer(1);
                inRange = false;
            } else if("-".equals(token)) {
                inRange = true;
            } else if(token.length() == 0) {
                // ignore whitespace
            } else {
                Integer port = Integer.valueOf(token);
                
                verifyPort(port.intValue());
                
                if(inRange) {
                    // add all numbers from last int
                    fillRange(passivePortsList, lastPort, port);
                    
                    inRange = false;
                }
                
                addPort(passivePortsList, port);
                
                lastPort = port;
            }
        }
        
        if(inRange) {
            fillRange(passivePortsList, lastPort, new Integer(MAX_PORT));
        }
        
        int[] passivePorts = new int[passivePortsList.size()];
        
        Iterator iter = passivePortsList.iterator();
        
        int counter = 0;
        while (iter.hasNext()) {
            Integer port = (Integer) iter.next();
            passivePorts[counter] = port.intValue();
            counter++;
        }
        
        return new PassivePorts(passivePorts);
    }

    /**
     * Fill a range of ports
     */
    private static void fillRange(List passivePortsList, Integer beginPort, Integer endPort) {
        for(int i = beginPort.intValue(); i<=endPort.intValue(); i++ ) {
            Integer rangePort = new Integer(i);
            addPort(passivePortsList, rangePort);
        }
    }

    /**
     * Add a single port if not already in list
     */
    private static void addPort(List passivePortsList, Integer rangePort) {
        if(!passivePortsList.contains(rangePort)) {
            passivePortsList.add(rangePort);
        }
    }
    
    /**
     * Verify that the port is within the range of allowed ports
     */
    private static void verifyPort(int port) {
        if(port < 0) {
            throw new IllegalArgumentException("Port can not be negative: " + port);
        } else if(port > MAX_PORT) {
            throw new IllegalArgumentException("Port too large: " + port);
        }
    }
    
    private PassivePorts(int[] passivePorts) {
        this.passivePorts = passivePorts;
        
        reservedPorts = new boolean[passivePorts.length];
    }
    
    public int reserveNextPort() {
        // search for a free port            
        for(int i=0; i<passivePorts.length; i++) {
            if(!reservedPorts[i]) {
                if(passivePorts[i] != 0) {
                    reservedPorts[i] = true;
                }
                return passivePorts[i];
            }
        }
        
        return -1;
    }
    
    public void releasePort(int port) {
        for(int i=0; i<passivePorts.length; i++) {
            if(passivePorts[i] == port) {
                reservedPorts[i] = false;
                break;
            }
        }
    }

}