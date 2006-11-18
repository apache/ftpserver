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

package org.apache.ftpserver.interfaces;

import java.util.Date;

import org.apache.ftpserver.FtpStatisticsImpl;
import org.apache.ftpserver.ftplet.FtpRequest;

import junit.framework.TestCase;

public abstract class ServerFtpStatisticsTestTemplate extends TestCase {

    public static class MockConnection implements Connection {

        public void close() {
        }

        public FtpRequest getRequest() {
            return null;
        }

        public void setObserver(ConnectionObserver observer) {            
        }

        public void run() {            
        }
        
    }
    
    public void testConnectionCount() {
        ServerFtpStatistics stats = createStatistics();
        
        assertEquals(0, stats.getTotalConnectionNumber());
        assertEquals(0, stats.getCurrentConnectionNumber());

        stats.setOpenConnection(new MockConnection());
        assertEquals(1, stats.getTotalConnectionNumber());
        assertEquals(1, stats.getCurrentConnectionNumber());

        stats.setOpenConnection(new MockConnection());
        assertEquals(2, stats.getTotalConnectionNumber());
        assertEquals(2, stats.getCurrentConnectionNumber());
        
        stats.setCloseConnection(new MockConnection());
        assertEquals(2, stats.getTotalConnectionNumber());
        assertEquals(1, stats.getCurrentConnectionNumber());

        stats.setCloseConnection(new MockConnection());
        assertEquals(2, stats.getTotalConnectionNumber());
        assertEquals(0, stats.getCurrentConnectionNumber());

        // This should never occure
        stats.setCloseConnection(new MockConnection());
        assertEquals(2, stats.getTotalConnectionNumber());
        assertEquals(0, stats.getCurrentConnectionNumber());
    }
    
    public void testStartDateImmutable() {
        ServerFtpStatistics stats = createStatistics();
        Date date = stats.getStartTime();
        date.setYear(1);
        
        Date actual = stats.getStartTime();
        
        assertFalse(1 == actual.getYear());
        
    }

    protected abstract FtpStatisticsImpl createStatistics();

}