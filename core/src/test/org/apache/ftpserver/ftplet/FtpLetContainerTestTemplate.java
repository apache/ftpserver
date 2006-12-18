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

package org.apache.ftpserver.ftplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.ftpserver.FtpRequestImpl;
import org.apache.ftpserver.FtpWriter;


public abstract class FtpLetContainerTestTemplate extends TestCase {

    private FtpletContainer container = createFtpletContainer();
    private final List calls = new ArrayList();
    
    protected void setUp() throws Exception {
        //MockFtplet.callback = new MockFtpletCallback();
        //MockFtplet.callback.returnValue = FtpletEnum.RET_DEFAULT;
     } 
    
    protected abstract FtpletContainer createFtpletContainer();
    
    public void testAddAndGetFtplet() {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();
        
        assertNull(container.getFtplet("ftplet1"));
        assertNull(container.getFtplet("ftplet2"));
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        assertSame(ftplet1, container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));
    }
    
    public void testAddFtpletWithDuplicateName() {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();
        
        assertNull(container.getFtplet("ftplet1"));
        
        container.addFtplet("ftplet1", ftplet1);
        
        try {
            container.addFtplet("ftplet1", ftplet2);
            fail("IllegalArgumentException must be thrown");
        } catch(IllegalArgumentException e) {
            // ok
        }
        
        assertSame(ftplet1, container.getFtplet("ftplet1"));
    }

    public void testRemoveFtplet() {
        MockFtplet ftplet1 = new MockFtplet();
        MockFtplet ftplet2 = new MockFtplet();
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        assertSame(ftplet1, container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));
        
        assertSame(ftplet1, container.removeFtplet("ftplet1"));
        
        assertNull(container.getFtplet("ftplet1"));
        assertSame(ftplet2, container.getFtplet("ftplet2"));
        
        assertNull(container.removeFtplet("ftplet1"));
    }
    
    public void testOnConnect() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onConnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onConnect(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onConnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onConnect(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onConnect(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnDisconnect() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onDisconnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDisconnect(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onDisconnect(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDisconnect(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onDisconnect(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnLogin() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onLogin(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onLogin(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onLogin(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onLogin(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onLogin(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnDeleteStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onDeleteStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDeleteStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onDeleteStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDeleteStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onDeleteStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnDeleteEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onDeleteEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDeleteEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onDeleteEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDeleteEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onDeleteEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnUploadStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onUploadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onUploadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onUploadStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnUploadEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onUploadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onUploadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onUploadEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnDownloadStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onDownloadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDownloadStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onDownloadStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDownloadStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onDownloadStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnDownloadEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onDownloadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onDownloadEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onDownloadEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onDownloadEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onDownloadEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnRmdirStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onRmdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRmdirStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onRmdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRmdirStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onRmdirStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnRmdirEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onRmdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRmdirEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onRmdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRmdirEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onRmdirEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnMkdirStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onMkdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onMkdirStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onMkdirStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onMkdirStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onMkdirStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnMkdirEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onMkdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onMkdirEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onMkdirEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onMkdirEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onMkdirEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnAppendStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onAppendStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onAppendStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onAppendStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onAppendStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onAppendStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnAppendEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onAppendEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onAppendEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onAppendEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onAppendEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onAppendEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnUploadUniqueStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onUploadUniqueStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadUniqueStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onUploadUniqueStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadUniqueStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onUploadUniqueStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }

    public void testOnUploadUniqueEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onUploadUniqueEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onUploadUniqueEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onUploadUniqueEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onUploadUniqueEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onUploadUniqueEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnRenameStart() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onRenameStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRenameStart(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onRenameStart(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRenameStart(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onRenameStart(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnRenameEnd() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onRenameEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onRenameEnd(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onRenameEnd(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onRenameEnd(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onRenameEnd(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
    
    public void testOnSite() throws FtpException, IOException {
        MockFtplet ftplet1 = new MockFtplet() {
            public FtpletEnum onSite(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet1");
                return super.onSite(request, response);
            }
        };
        MockFtplet ftplet2 = new MockFtplet() {
            public FtpletEnum onSite(FtpRequest request, FtpResponse response) throws FtpException, IOException {
                calls.add("ftplet2");
                return super.onSite(request, response);
            }
        };
        
        container.addFtplet("ftplet1", ftplet1);
        container.addFtplet("ftplet2", ftplet2);
        
        container.onSite(new FtpRequestImpl(), new FtpWriter());
        
        assertEquals(2, calls.size());
        assertEquals("ftplet1", calls.get(0));
        assertEquals("ftplet2", calls.get(1));
    }
}
