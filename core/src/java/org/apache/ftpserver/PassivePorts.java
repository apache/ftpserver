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

import java.util.StringTokenizer;


/**
 * Provides support for parsing a passive ports string as well as
 * keeping track of reserved passive ports.
 */
public class PassivePorts {

    private int[][] passivePorts;
    
    /**
     * Parse a string containing passive ports
     * @param portsString A string of passive ports, can contain a single
     *   port (as an integer) or multiple ports seperated by commas
     * @return An instance of {@link PassivePorts} based on the parsed string
     * @throws NumberFormatException If any of of the ports in the string is
     *   invalid (e.g. not an integer) 
     */
    public static PassivePorts parse(String portsString) {
        int[][] passivePorts;
        
        StringTokenizer st = new StringTokenizer(portsString, " ,;\t\n\r\f");
        passivePorts = new int[st.countTokens()][2];
        for(int i=0; i<passivePorts.length; i++) {
            passivePorts[i][0] = Integer.parseInt(st.nextToken());
            passivePorts[i][1] = 0;
        }
        
        return new PassivePorts(passivePorts);
    }
    
    private PassivePorts(int[][] passivePorts) {
        this.passivePorts = passivePorts;
    }
    
    public int reserveNextPort() {
        // search for a free port            
        for(int i=0; i<passivePorts.length; i++) {
            if(passivePorts[i][1] == 0) {
                if(passivePorts[i][0] != 0) {
                    passivePorts[i][1] = 1;
                }
                return passivePorts[i][0];
            }
        }
        
        return -1;
    }
    
    public void releasePort(int port) {
        for(int i=0; i<passivePorts.length; i++) {
            if(passivePorts[i][0] == port) {
                passivePorts[i][1] = 0;
                break;
            }
        }
    }

}