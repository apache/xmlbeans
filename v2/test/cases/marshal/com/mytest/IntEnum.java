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

package com.mytest;


import org.apache.xmlbeans.impl.util.XsTypeConverter;


public class IntEnum
    implements java.io.Serializable
{

    private int __value;

    protected IntEnum(int value)
    {
        __value = value;
    }

    public static final int _value1 = 55;
    public static final com.mytest.IntEnum value1 = new com.mytest.IntEnum(_value1);

    public static final int _value2 = 33;
    public static final com.mytest.IntEnum value2 = new com.mytest.IntEnum(_value2);

    public static final int _value3 = 44;
    public static final com.mytest.IntEnum value3 = new com.mytest.IntEnum(_value3);


    // Gets the value for a enumerated value
    public int getValue()
    {
        return __value;
    }


    // Gets enumeration with a specific value
    // throws java.lang.IllegalArgumentException if
    // any invalid value is specified
    public static com.mytest.IntEnum fromValue(int value)
    {
        switch (value) {
            case _value1:
                return value1;
            case _value2:
                return value2;
            case _value3:
                return value3;
            default:
                throw new java.lang.IllegalArgumentException(invalidValueMsg("" + value));
        }

    }

    private static java.lang.String invalidValueMsg(java.lang.String value)
    {
        java.lang.String msg = "invalid enumeration value: " + value;
        return msg;
    }


    // Gets enumeration from a String
    // throws java.lang.IllegalArgumentException if
    // any invalid value is specified
    public static com.mytest.IntEnum fromString(java.lang.String value)
    {
        int __tmp = XsTypeConverter.lexInt(value);
        return fromValue(__tmp);
    }


    // Returns String representation of the enumerated value
    public java.lang.String toString()
    {
        return java.lang.String.valueOf(__value);
    }

    public boolean equals(java.lang.Object obj)
    {
        if (obj instanceof com.mytest.IntEnum) {
            int tmp_val = ((com.mytest.IntEnum)obj).getValue();
            return (tmp_val == __value);

        }
        return false;
    }


    public int hashCode()
    {
        int __hash__result__ = 17;
        __hash__result__ = 37 * __hash__result__ + __value;

        return __hash__result__;
    }

    private java.lang.Object readResolve()
        throws java.io.ObjectStreamException
    {
        return fromValue(__value);
    }


}


