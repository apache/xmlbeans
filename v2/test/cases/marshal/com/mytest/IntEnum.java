/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
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


