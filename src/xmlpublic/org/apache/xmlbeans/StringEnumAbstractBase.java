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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * The base class for code-generated string enumeration value classes.
 * <p>
 * Subclasses are intended to be final types with a finite set of
 * singleton instances.  Each instance has a string value, which
 * it returns via {@link #toString}, and an int value for the purpose
 * of switching in case statements, returned via {@link #intValue}.
 * <p>
 * Each subclass manages an instance of {@link StringEnumAbstractBase.Table},
 * which holds all the singleton instances for the subclass. A Table
 * can return a singleton instance given a String or an integer code.
 */ 
public class StringEnumAbstractBase implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String _string;
    private int _int;

    /**
     * Singleton instances should only be created by subclasses.
     */ 
    protected StringEnumAbstractBase(String s, int i)
        { _string = s; _int = i; }

    /** Returns the underlying string value */
    public final String toString()
        { return _string; }
    /** Returns an int code that can be used for switch statements */
    public final int intValue()
        { return _int; }
    /** Returns the hash code of the underlying string */
    public final int hashCode()
        { return _string.hashCode(); }

    /**
     * Used to manage singleton instances of enumerations.
     * Each subclass of StringEnumAbstractBase has an instance
     * of a table to hold the singleton instances.
     */ 
    public static final class Table
    {
        private Map _map;
        private List _list;
        public Table(StringEnumAbstractBase[] array)
        {
            _map = new HashMap(array.length);
            _list = new ArrayList(array.length + 1);
            for (int i = 0; i < array.length; i++)
            {
                _map.put(array[i].toString(), array[i]);
                int j = array[i].intValue();
                while (_list.size() <= j)
                    _list.add(null);
                _list.set(j, array[i]);
            }
        }
        
        /** Returns the singleton for a {@link String}, or null if none. */
        public StringEnumAbstractBase forString(String s)
        {
            return (StringEnumAbstractBase)_map.get(s);
        }
        /** Returns the singleton for an int code, or null if none. */
        public StringEnumAbstractBase forInt(int i)
        {
            if (i < 0 || i > _list.size())
                return null;
            return (StringEnumAbstractBase)_list.get(i);
        }
        /** Returns the last valid int code (the first is 1; zero is not used). */
        public int lastInt()
        {
            return _list.size() - 1;
        }
    }
}
