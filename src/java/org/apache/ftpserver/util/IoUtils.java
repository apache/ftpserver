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

import java.io.*;
import java.util.Random;

/**
 * IO utility methods.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public
class IoUtils {

   /**
    * Random number generator to make unique file name
    */
   private static final Random RANDOM_GEN = new Random(System.currentTimeMillis());


   /**
    * Get a <code>BufferedInputStream</code>.
    */
   public static BufferedInputStream getBufferedInputStream(InputStream in) {
        BufferedInputStream bin = null;
        if(in instanceof java.io.BufferedInputStream) {
            bin = (BufferedInputStream)in;
        }
        else {
            bin = new BufferedInputStream(in);
        }
        return bin;
   }


   /**
    * Get a <code>BufferedOutputStream</code>.
    */
   public static BufferedOutputStream getBufferedOutputStream(OutputStream out) {
        BufferedOutputStream bout = null;
        if(out instanceof java.io.BufferedOutputStream) {
            bout = (BufferedOutputStream)out;
        }
        else {
            bout = new BufferedOutputStream(out);
        }
        return bout;
   }


   /**
    * Get <code>BufferedReader</code>.
    */
   public static BufferedReader getBufferedReader(Reader rd) {
        BufferedReader br = null;
        if(br instanceof java.io.BufferedReader) {
            br = (BufferedReader)rd;
        }
        else {
            br = new BufferedReader(rd);
        }
        return br;
   }


   /**
    * Get <code>BufferedWriter</code>.
    */
   public static BufferedWriter getBufferedWriter(Writer wr) {
        BufferedWriter bw = null;
        if(wr instanceof java.io.BufferedWriter) {
            bw = (BufferedWriter)wr;
        }
        else {
            bw = new BufferedWriter(wr);
        }
        return bw;
   }


   /**
    * Get unique file object.
    */
   public static File getUniqueFile(File oldFile) {
        File newFile = oldFile;
        while (true) {
            if (!newFile.exists()) {
                break;
            }
            newFile = new File(oldFile.getAbsolutePath() + '.' + Math.abs(RANDOM_GEN.nextLong()));
        }
        return newFile;
   }


   /**
    * No exception <code>InputStream</code> close method.
    */
   public static void close(InputStream is) {
       if(is != null) {
           try { is.close(); } catch(Exception ex) {}
       }
   }

   /**
    * No exception <code>OutputStream</code> close method.
    */
   public static void close(OutputStream os) {
       if(os != null) {
           try { os.close(); } catch(Exception ex) {}
       }
   }

   /**
    * No exception <code>java.io.Reader</code> close method.
    */
   public static void close(Reader rd) {
       if(rd != null) {
           try { rd.close(); } catch(Exception ex) {}
       }
   }


   /**
    * No exception <code>java.io.Writer</code> close method.
    */
   public static void close(Writer wr) {
       if(wr != null) {
           try { wr.close(); } catch(Exception ex) {}
       }
   }


    /**
     * Get exception stack trace.
     */
    public static String getStackTrace(Throwable ex) {
        String result = "";
        try  {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.close();
            sw.close();
            result = sw.toString();
        }
        catch(Exception e)  {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
     * @param bufferSize Size of internal buffer to use.
     */
    public static void copy(Reader input, Writer output, int bufferSize ) throws IOException {
        char buffer[] = new char[bufferSize];
        int n = 0;
        while( (n=input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
    }

    /**
     * Copy chars from a <code>InputStream</code> to a <code>OutputStream</code>.
     * @param bufferSize Size of internal buffer to use.
     */
    public static void copy(InputStream input, OutputStream output, int bufferSize ) throws IOException {
        byte buffer[] = new byte[bufferSize];
        int n = 0;
        while( (n=input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
    }


    /**
     * Read fully from reader
     */
    public static String readFully(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        copy(reader, writer, 1024);
        return writer.toString();
    }


    /**
     * Read fully from stream
     */
    public static String readFully(InputStream input) throws IOException {
        StringWriter writer = new StringWriter();
        InputStreamReader reader = new InputStreamReader(input);
        copy(reader, writer, 1024);
        return writer.toString();
    }

}
