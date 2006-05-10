/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.command;

import java.io.IOException;

/**
 * {@link PASS} test case
 * 
 * @author <a href="mailto:vlsergey@gmail.com">Sergey Vladimirov</a>
 */
public class PASSTest extends AbstractCommandTest {

    public void testAnonymousOk() throws IOException {
        wait("220 ", 1000);
        sendCommand("USER anonymous");
        wait("331 ", 1000);
        sendCommand("PASS anonymous@testuri.net");
        wait("230 ", 1000);
    }

    public void testNoSuchUser() throws IOException {
        wait("220 ", 1000);
        sendCommand("USER nosuchuser");
        wait("331 ", 1000000);
        sendCommand("PASS anonymous@testuri.net");
        wait("530 ", 1000);
    }

}
