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

package org.apache.xmlbeans;

/**
 * An exception that is thrown if there is corruption or a version mismatch
 * in a compiled schema type system.
 */ 
public class SchemaTypeLoaderException extends XmlRuntimeException
{
    private int _code;
    
    /** Constructs an exception with the given message, filename, extension, and code */
    public SchemaTypeLoaderException(String message, String name, String handle, int code)
    {
        super(message + " (" + name + "." + handle + ") - code " + code);
        _code = code;
    }

    /** Constructs an exception with the given message, filename, extension, code, and cause */
    public SchemaTypeLoaderException(String message, String name, String handle, int code, Exception cause)
    {
        super(message + " (" + name + "." + handle + ") - code " + code);
        _code = code;
        initCause(cause);
    }

    /** Returns the reason for the failure, given by one of the numeric constants in this class */
    public int getCode()
    {
        return _code;
    }

    /* See {@link #getCode}. */
    public static final int NO_RESOURCE = 0;
    /* See {@link #getCode}. */
    public static final int WRONG_MAGIC_COOKIE = 1;
    /* See {@link #getCode}. */
    public static final int WRONG_MAJOR_VERSION = 2;
    /* See {@link #getCode}. */
    public static final int WRONG_MINOR_VERSION = 3;
    /* See {@link #getCode}. */
    public static final int WRONG_FILE_TYPE = 4;
    /* See {@link #getCode}. */
    public static final int UNRECOGNIZED_INDEX_ENTRY = 5;
    /* See {@link #getCode}. */
    public static final int WRONG_PROPERTY_TYPE = 6;
    /* See {@link #getCode}. */
    public static final int MALFORMED_CONTENT_MODEL = 7;
    /* See {@link #getCode}. */
    public static final int WRONG_SIMPLE_VARIETY = 8;
    /* See {@link #getCode}. */
    public static final int IO_EXCEPTION = 9;
    /* See {@link #getCode}. */
    public static final int INT_TOO_LARGE = 10;
    /* See {@link #getCode}. */
    public static final int BAD_PARTICLE_TYPE = 11;
    /* See {@link #getCode}. */
    public static final int NOT_WRITEABLE = 12;
    /* See {@link #getCode}. */
    public static final int BAD_HANDLE = 13;
    /* See {@link #getCode}. */
    public static final int NESTED_EXCEPTION = 14;
}

