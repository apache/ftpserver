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


/**
 * This class encapsulates the return values of the ftplet methods. 
 * 
 * RET_DEFAULT < RET_NO_FTPLET < RET_SKIP < RET_DISCONNECT
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
final class FtpletEnum {


     /**
      * This return value indicates that the next ftplet method will 
      * be called. If no other ftplet is available, the ftpserver will
      * process the request.
      */
     public static final FtpletEnum RET_DEFAULT = new FtpletEnum(0);


     /**
      * This return value indicates that the other ftplet methods will
      * not be called but the ftpserver will continue processing this 
      * request.
      */
     public static final FtpletEnum RET_NO_FTPLET = new FtpletEnum(1); 
      
      
     /**
      * It indicates that the ftpserver will skip everything. No further 
      * processing (both ftplet and server) will be done for this request.
      */ 
     public static final FtpletEnum RET_SKIP = new FtpletEnum(2);
     
     
     /**
      * It indicates that the server will skip and disconnect the client.
      * No other request from the same client will be served.
      */
     public static final FtpletEnum RET_DISCONNECT = new FtpletEnum(3);


     private int type;


     /**
      * Private constructor - set the type
      */
     private FtpletEnum(int type) {
         this.type = type;
     }

     /**
      * Equality check
      */
     public boolean equals(Object obj) {
         if(obj instanceof FtpletEnum) {
             return type == ((FtpletEnum)obj).type;
         }
         return false;
     }

     /**
      * String representation
      */
     public String toString() {
         return String.valueOf(type);
     }

}
