/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SimpleValue;

/**
 * A class to hold and compare a list of XmlObjects for use by keys
 * keyrefs during validation.
 */
public class XmlObjectList
{
    private final XmlObject[] _objects;

    /**
     * Construct a new empty object list of the given fixed size.
     */
    public XmlObjectList(int objectCount) {
        _objects = new XmlObject[objectCount];
    }

    /**
     * Set an object by index unless a value has been previously
     * set at that location.
     * 
     * @return true if the value was set, false if the value has
     * already been set
     */
    public boolean set(XmlObject o, int index)
    {
        if (_objects[index] != null)
            return false;

        _objects[index] = o;
        return true;
    }

    /**
     * Tests that all values have been set. Needed for keys.
     */
    public boolean filled() {
        for (int i = 0 ; i < _objects.length ; i++)
            if (_objects[i] == null) return false;

        return true;
    }
    
    /**
     * Tests that all values have been set. Needed for keys.
     */
    public int unfilled()
    {
        for (int i = 0 ; i < _objects.length ; i++)
            if (_objects[i] == null) return i;

        return -1;
    }

    public boolean equals(Object o) {
        if (!( o instanceof XmlObjectList))
            return false;

        XmlObjectList other = (XmlObjectList)o;

        if (other._objects.length != this._objects.length)
            return false;

        for (int i = 0 ; i < _objects.length ; i++) {
            // Ignore missing values
            if (_objects[i] == null || other._objects[i] == null)
                return false;

            if (! _objects[i].valueEquals(other._objects[i]))
                return false;
        }

        return true;
    }

    public int hashCode()
    {
        int h = 0;

        for (int i = 0 ; i < _objects.length ; i++)
            if (_objects[i] != null)
                h = 31 * h + _objects[i].valueHashCode();

        return h;
    }
    
    private static String prettytrim(String s)
    {
        int end;
        for (end = s.length(); end > 0; end -= 1)
        {
            if (!XMLChar.isSpace(s.charAt(end - 1)))
                break;
        }
        int start;
        for (start = 0; start < end; start += 1)
        {
            if (!XMLChar.isSpace(s.charAt(start)))
                break;
        }
        return s.substring(start, end);
    }

    public String toString() {
        StringBuffer b = new StringBuffer();

        for (int i = 0 ; i < _objects.length ; i++)
        {
            if (i != 0) b.append(" ");
            b.append(prettytrim(((SimpleValue)_objects[i]).stringValue()));
        }

        return b.toString();
    }
}


