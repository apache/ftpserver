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

import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.Configuration;

public abstract class Bean {

    @SuppressWarnings("unchecked")
    public static Bean createBean(Configuration config, String defaultClass) throws Exception {
        String className = config.getString("class", defaultClass);

        Class<?> clazz = Class.forName(className);

        boolean isComponent = Component.class.isAssignableFrom(clazz);

        if (isComponent) {
            return new ComponentBean(config, (Class<Component>) clazz);
        } else {
            return new PojoBean(config, (Class<Object>) clazz);
        }
    }

    public abstract Object initBean() throws Exception;

    public abstract void destroyBean();

    public abstract Object getBean();

}
