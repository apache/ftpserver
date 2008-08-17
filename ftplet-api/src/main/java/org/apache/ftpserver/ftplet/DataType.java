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
 * Type safe enum for describing the data type
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class DataType {

    /**
     * Binary data type
     */
    public static final DataType BINARY = new DataType("I");

    /**
     * ASCII data type
     */
    public static final DataType ASCII = new DataType("A");

    /**
     * Parses the argument value from the TYPE command into the type safe class
     * 
     * @param argument
     *            The argument value from the TYPE command. Not case sensitive
     * @return The appropriate data type
     * @throws IllegalArgumentException
     *             If the data type is unknown
     */
    public static DataType parseArgument(char argument) {
        switch (argument) {
        case 'A':
        case 'a':
            return ASCII;
        case 'I':
        case 'i':
            return BINARY;
        default:
            throw new IllegalArgumentException("Unknown data type: " + argument);
        }
    }

    private String type;

    /**
     * Private constructor.
     */
    private DataType(String type) {
        this.type = type;
    }

    /**
     * Return the data type string.
     */
    public String toString() {
        return type;
    }
}
