/* ====================================================================
 * Copyright 2002 - 2004
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
 *
 *
 * $Id$
 */
package org.apache.ftpserver.util;

import java.io.File;
import java.io.FilenameFilter;
import org.apache.ftpserver.util.RegularExpr;

/**
 * This is regular expression filename filter.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class FileRegularFilter implements FilenameFilter {

    private RegularExpr mRegularExpr = null;

    /**
     * Constructor.
     * @param pattern regular expression
     */
    public FileRegularFilter(String pattern) {
        if ((pattern == null) || pattern.equals("") || pattern.equals("*")) {
            mRegularExpr = null;
        }
        else {
            mRegularExpr = new RegularExpr(pattern);
        }
    }

    /**
     * Tests if a specified file should be included in a file list.
     * @param dir - the directory in which the file was found
     * @param name - the name of the file.
     */
    public boolean accept(File dir, String name) {
        if (mRegularExpr == null) {
            return true;
        }
        return mRegularExpr.isMatch(name);
    }
}
