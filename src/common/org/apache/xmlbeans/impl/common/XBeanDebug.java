/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
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
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.common;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class XBeanDebug
{
    public static final int TRACE_SCHEMA_LOADING = 0x0001;
    public static final String traceProp = "org.apache.xmlbeans.impl.debug";
    public static final String defaultProp = ""; // "TRACE_SCHEMA_LOADING";

    private static int _enabled = initializeBitsFromProperty();
    private static int _indent = 0;
    private static String _indentspace = "                                                                                ";

    private static int initializeBitsFromProperty()
    {
        int bits = 0;
        String prop = System.getProperty(traceProp, defaultProp);
        if (prop.indexOf("TRACE_SCHEMA_LOADING") >= 0)
            bits |= TRACE_SCHEMA_LOADING;
        return bits;
    }
    public static void enable(int bits)
    {
        _enabled = _enabled | bits;
    }

    public static void disable(int bits)
    {
        _enabled = _enabled & ~bits;
    }

    public static void trace(int bits, String message, int indent)
    {
        if (test(bits))
        {
            synchronized (XBeanDebug.class)
            {
                if (indent < 0)
                    _indent += indent;

                String spaces = _indent < 0 ? "" : _indent > _indentspace.length() ? _indentspace : _indentspace.substring(0, _indent);
                String logmessage = Thread.currentThread().getName() + ": " + spaces + message + "\n";
                System.err.print(logmessage);

                if (indent > 0)
                    _indent += indent;
            }
        }
    }

    public static boolean test(int bits)
    {
        return (_enabled & bits) != 0;
    }
    
    static PrintStream _err;
    
    public static String log(String message)
    {
        log(message, null);
        return message;
    }
    
    public static String logStackTrace(String message)
    {
        log(message, new Throwable());
        return message;
    }
    
    private synchronized static String log(String message, Throwable stackTrace)
    {
        if (_err == null)
        {
            try
            {
                File diagnosticFile = File.createTempFile("xmlbeandebug", ".log");
                _err = new PrintStream(new FileOutputStream(diagnosticFile));
                System.err.println("Diagnostic XML Bean debug log file created: " + diagnosticFile);
            }
            catch (IOException e)
            {
                _err = System.err;
            }
        }
        _err.println(message);
        if (stackTrace != null)
        {
            stackTrace.printStackTrace(_err);
        }
        return message;
    }
    
    public static Throwable logException(Throwable t)
    {
        log(t.getMessage(), t);
        return t;
    }
}
