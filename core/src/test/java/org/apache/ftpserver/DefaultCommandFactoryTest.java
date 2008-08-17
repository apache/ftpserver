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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.ftpserver.command.NOOP;
import org.apache.ftpserver.command.STOR;
import org.apache.ftpserver.interfaces.Command;

/**
 * 
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 *
 */
public class DefaultCommandFactoryTest extends TestCase {

    public void testReturnFromDefaultUpper() {
        DefaultCommandFactory factory = new DefaultCommandFactory();
        Command command = factory.getCommand("STOR");

        assertNotNull(command);
        assertTrue(command instanceof STOR);
    }

    public void testReturnFromDefaultLower() {
        DefaultCommandFactory factory = new DefaultCommandFactory();
        Command command = factory.getCommand("stor");

        assertNotNull(command);
        assertTrue(command instanceof STOR);
    }

    public void testReturnFromDefaultUnknown() {
        DefaultCommandFactory factory = new DefaultCommandFactory();
        Command command = factory.getCommand("dummy");

        assertNull(command);
    }

    public void testOverride() {
        DefaultCommandFactory factory = new DefaultCommandFactory();
        Map<String, Command> commands = new HashMap<String, Command>();
        commands.put("stor", new NOOP());
        factory.setCommandMap(commands);

        Command command = factory.getCommand("Stor");

        assertTrue(command instanceof NOOP);
    }

    public void testAppend() {
        DefaultCommandFactory factory = new DefaultCommandFactory();
        Map<String, Command> commands = new HashMap<String, Command>();
        commands.put("foo", new NOOP());
        factory.setCommandMap(commands);

        assertTrue(factory.getCommand("FOO") instanceof NOOP);
        assertTrue(factory.getCommand("stor") instanceof STOR);
    }

    public void testAppendWithoutDefault() {
        DefaultCommandFactory factory = new DefaultCommandFactory();
        factory.setUseDefaultCommands(false);
        Map<String, Command> commands = new HashMap<String, Command>();
        commands.put("foo", new NOOP());
        factory.setCommandMap(commands);

        assertTrue(factory.getCommand("FOO") instanceof NOOP);
        assertNull(factory.getCommand("stor"));
    }
}
