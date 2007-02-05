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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.command.AbstractCommand;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.interfaces.Command;
import org.apache.ftpserver.interfaces.CommandFactory;


/**
 * Command factory to return appropriate command implementation
 * depending on the FTP request command string.
 */
public 
class DefaultCommandFactory implements CommandFactory, Component {

    private LogFactory logFactory;
    private Log log;
    private HashMap commandMap = new HashMap();  
    
    
    /**
     * Set the log factory.
     */
    public void setLogFactory(LogFactory factory) {
        this.logFactory = factory;
        log = factory.getInstance(getClass());
    }
    
    /**
     * Configure the command factory - populate the command map.
     */
    public void configure(Configuration conf) throws FtpException {
        
        // first populate the default command list
        commandMap.put("ABOR", new org.apache.ftpserver.command.ABOR());
        commandMap.put("ACCT", new org.apache.ftpserver.command.ACCT());
        commandMap.put("APPE", new org.apache.ftpserver.command.APPE());
        commandMap.put("AUTH", new org.apache.ftpserver.command.AUTH());
        commandMap.put("CDUP", new org.apache.ftpserver.command.CDUP());
        commandMap.put("CWD",  new org.apache.ftpserver.command.CWD());
        commandMap.put("DELE", new org.apache.ftpserver.command.DELE());
        commandMap.put("EPRT", new org.apache.ftpserver.command.EPRT());
        commandMap.put("EPSV", new org.apache.ftpserver.command.EPSV());
        commandMap.put("FEAT", new org.apache.ftpserver.command.FEAT());
        commandMap.put("HELP", new org.apache.ftpserver.command.HELP());
        commandMap.put("LANG", new org.apache.ftpserver.command.LANG());
        commandMap.put("LIST", new org.apache.ftpserver.command.LIST());
        commandMap.put("MD5", new org.apache.ftpserver.command.MD5());
        commandMap.put("MMD5", new org.apache.ftpserver.command.MD5());
        commandMap.put("MDTM", new org.apache.ftpserver.command.MDTM());
        commandMap.put("MLST", new org.apache.ftpserver.command.MLST());
        commandMap.put("MKD",  new org.apache.ftpserver.command.MKD());
        commandMap.put("MLSD", new org.apache.ftpserver.command.MLSD());
        commandMap.put("MODE", new org.apache.ftpserver.command.MODE());
        commandMap.put("NLST", new org.apache.ftpserver.command.NLST());
        commandMap.put("NOOP", new org.apache.ftpserver.command.NOOP());
        commandMap.put("OPTS", new org.apache.ftpserver.command.OPTS());
        commandMap.put("PASS", new org.apache.ftpserver.command.PASS());
        commandMap.put("PASV", new org.apache.ftpserver.command.PASV());
        commandMap.put("PBSZ", new org.apache.ftpserver.command.PBSZ());
        commandMap.put("PORT", new org.apache.ftpserver.command.PORT());
        commandMap.put("PROT", new org.apache.ftpserver.command.PROT());
        commandMap.put("PWD",  new org.apache.ftpserver.command.PWD());
        commandMap.put("QUIT", new org.apache.ftpserver.command.QUIT());
        commandMap.put("REIN", new org.apache.ftpserver.command.REIN());
        commandMap.put("REST", new org.apache.ftpserver.command.REST());
        commandMap.put("RETR", new org.apache.ftpserver.command.RETR());
        commandMap.put("RMD",  new org.apache.ftpserver.command.RMD());
        commandMap.put("RNFR", new org.apache.ftpserver.command.RNFR());
        commandMap.put("RNTO", new org.apache.ftpserver.command.RNTO());
        commandMap.put("SITE", new org.apache.ftpserver.command.SITE());
        commandMap.put("SIZE", new org.apache.ftpserver.command.SIZE());
        commandMap.put("STAT", new org.apache.ftpserver.command.STAT());
        commandMap.put("STOR", new org.apache.ftpserver.command.STOR());
        commandMap.put("STOU", new org.apache.ftpserver.command.STOU());
        commandMap.put("STRU", new org.apache.ftpserver.command.STRU());
        commandMap.put("SYST", new org.apache.ftpserver.command.SYST());
        commandMap.put("TYPE", new org.apache.ftpserver.command.TYPE());
        commandMap.put("USER", new org.apache.ftpserver.command.USER());
        
        // now populate the configured commands
        Configuration sconf = conf.subset("command");
        if(sconf == null || sconf.isEmpty()) {
            return;
        }
        
        Iterator cmds = sconf.getKeys();
        if(cmds == null) {
            return;
        }
        
        while(cmds.hasNext()) {
            String cmdName = (String)cmds.next();
            String cmdClass = sconf.getString(cmdName, null);
            if(cmdClass == null || cmdClass.equals("")) {
                throw new FtpException("Command not found :: " + cmdName);
            }
            try {
                Class clazz = Class.forName(cmdClass);
                Command cmd = (Command)clazz.newInstance();
                commandMap.put(cmdName, cmd);
            }
            catch(Exception ex) {
                log.error("DefaultCommandFactory.configure()", ex);
                throw new FtpException("DefaultCommandFactory.configure()", ex);
            }
        }
        
        Collection commandEntries = commandMap.values();
        
        for (Iterator iter = commandEntries.iterator(); iter.hasNext();) {
            Command command = (Command) iter.next();
            
            if(command instanceof AbstractCommand) {
                AbstractCommand abstractCommand = (AbstractCommand) command;
                abstractCommand.setLogFactory(logFactory);
            }
        }
    }
    
    /**
     * Get command. Returns null if not found.
     */
    public Command getCommand(String cmdName) {
        if(cmdName == null || cmdName.equals("")) {
            return null;
        }
        return (Command)commandMap.get(cmdName);
    }
    
    /**
     * Cose the command factory - does nothing.
     */
    public void dispose() {
        commandMap.clear();
    }
}
