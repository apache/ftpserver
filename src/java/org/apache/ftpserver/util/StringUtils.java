/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1997-2003 The Apache Software Foundation. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software
 *    itself, if and wherever such third-party acknowledgments
 *    normally appear.
 *
 * 4. The names "Incubator", "FtpServer", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * $Id$
 */
package org.apache.ftpserver.util;

import java.util.Map;

/**
 * String utility methods.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */

public
class StringUtils {

    private static final char SEPARATOR = '\n';

    /**
     * This is a string replacement method.
     */
    public static String replaceString(String source, String oldStr, String newStr) {
        StringBuffer sb = new StringBuffer(source.length());
        int sind = 0;
        int cind = 0;
        while ((cind=source.indexOf(oldStr, sind)) != -1) {
            sb.append(source.substring(sind, cind));
            sb.append(newStr);
            sind = cind + oldStr.length();
        }
        sb.append(source.substring(sind));
        return sb.toString();
    }

    /**
     * Replace string
     */ 
    public static String replaceString(String source, Object[] args) {
        int startIndex = 0;
        int openIndex = source.indexOf('{', startIndex);
        if (openIndex == -1) {
            return source;
        }
        
        int closeIndex = source.indexOf('}', startIndex);
        if( (closeIndex == -1) || (openIndex > closeIndex) ) {
            return source;
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append(source.substring(startIndex, openIndex));
        while(true) {
            String intStr = source.substring(openIndex+1, closeIndex);
            int index = Integer.parseInt(intStr);
            sb.append(args[index]);
            
            startIndex = closeIndex + 1;
            openIndex = source.indexOf('{', startIndex);
            if (openIndex == -1) {
                sb.append(source.substring(startIndex));
                break;
            }
            
            closeIndex = source.indexOf('}', startIndex);
            if( (closeIndex == -1) || (openIndex > closeIndex) ) {
               sb.append(source.substring(startIndex));
               break;
            }
            sb.append(source.substring(startIndex, openIndex));
        }
        return sb.toString();
    }
    

    /**
     * Replace string
     */ 
    public static String replaceString(String source, Map args) {
        int startIndex = 0;
        int openIndex = source.indexOf('{', startIndex);
        if (openIndex == -1) {
            return source;
        }
        
        int closeIndex = source.indexOf('}', startIndex);
        if( (closeIndex == -1) || (openIndex > closeIndex) ) {
            return source;
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append(source.substring(startIndex, openIndex));
        while(true) {
            String key = source.substring(openIndex+1, closeIndex);
            Object val = args.get(key);
            if(val != null) {
                sb.append(val);
            }
            
            startIndex = closeIndex + 1;
            openIndex = source.indexOf('{', startIndex);
            if (openIndex == -1) {
                sb.append(source.substring(startIndex));
                break;
            }
            
            closeIndex = source.indexOf('}', startIndex);
            if( (closeIndex == -1) || (openIndex > closeIndex) ) {
               sb.append(source.substring(startIndex));
               break;
            }
            sb.append(source.substring(startIndex, openIndex));
        }
        return sb.toString();
    }
    
    
    /**
     * This method is used to insert HTML block dynamically
     *
     * @param source the HTML code to be processes
     * @param bReplaceNl if true '\n' will be replaced by <br>
     * @param bReplaceTag if true '<' will be replaced by &lt; and
     *                          '>' will be replaced by &gt;
     * @param bReplaceQuote if true '\"' will be replaced by &quot;
     */
    public static String formatHtml(String source,
                                    boolean bReplaceNl,
                                    boolean bReplaceTag,
                                    boolean bReplaceQuote) {

        StringBuffer sb = new StringBuffer();
        int len = source.length();
        for (int i=0; i<len; i++) {
            char c = source.charAt(i);
            switch (c) {
            case '\"':
                if (bReplaceQuote)sb.append("&quot;");
                else sb.append(c);
                break;

            case '<':
                if (bReplaceTag) sb.append("&lt;");
                else sb.append(c);
                break;

            case '>':
                if (bReplaceTag) sb.append("&gt;");
                else sb.append(c);
                break;

            case '\n':
                if (bReplaceNl) {
                    if (bReplaceTag) sb.append("&lt;br&gt;");
                    else sb.append("<br>");
                } else {
                    sb.append(c);
                }
                break;

            case '\r':
                break;

            case '&':
                sb.append("&amp;");
                break;

            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Pad string object
     */
    public static String pad(String src,
                             char padChar,
                             boolean rightPad,
                             int totalLength) {

        int srcLength = src.length();
        if (srcLength >= totalLength) {
            return src;
        }

        int padLength = totalLength - srcLength;
        StringBuffer sb = new StringBuffer(padLength);
        for(int i=0; i<padLength; ++i) {
            sb.append(padChar);
        }

        if (rightPad) {
            return src + sb.toString();
        }
        else {
            return sb.toString() + src;
        }
    }

    /**
     * Get hex string from byte array
     */
    public static String toHexString(byte[] res) {
        StringBuffer sb = new StringBuffer(res.length << 1);
        for(int i=0; i<res.length; i++) {
            String digit = Integer.toHexString(0xFF & res[i]);
            if (digit.length() == 1) {
                digit = '0' + digit;
            }
            sb.append(digit);
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Get byte array from hex string
     */
    public static byte[] toByteArray(String hexString) {
        int arrLength = hexString.length() >> 1;
        byte buff[] = new byte[arrLength];
        for(int i=0; i<arrLength; i++) {
            int index = i << 1;
            String digit = hexString.substring(index, index+2);
            buff[i] = (byte)Integer.parseInt(digit, 16);
        }
        return buff;
    }

}
