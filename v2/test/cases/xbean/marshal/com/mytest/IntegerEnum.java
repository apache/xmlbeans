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

public class IntegerEnum
    implements java.io.Serializable
{

    private java.math.BigInteger __value;

    protected IntegerEnum(java.math.BigInteger value)
    {
        __value = value;
    }

    public static final java.math.BigInteger _value1 = new java.math.BigInteger("333333333333333333333333");
    public static final com.mytest.IntegerEnum value1 = new com.mytest.IntegerEnum(_value1);

    public static final java.math.BigInteger _value2 = new java.math.BigInteger("44444444444444444444444");
    public static final com.mytest.IntegerEnum value2 = new com.mytest.IntegerEnum(_value2);

    public static final java.math.BigInteger _value3 = new java.math.BigInteger("55555555555555555555");
    public static final com.mytest.IntegerEnum value3 = new com.mytest.IntegerEnum(_value3);


    private static final java.util.Map _valueMap = _buildValueMap();


    // Gets the value for a enumerated value
    public java.math.BigInteger getValue()
    {
        return __value;
    }


    // Gets enumeration with a specific value
    // throws java.lang.IllegalArgumentException if
    // any invalid value is specified
    public static com.mytest.IntegerEnum fromValue(java.math.BigInteger value)
    {
        Object obj = _valueMap.get(value);
        if (obj == null) {
            java.lang.String msg = invalidValueMsg("" + value);
            msg = msg + (" valmap=" + _valueMap);
            throw new java.lang.IllegalArgumentException(msg);
        }
        return (com.mytest.IntegerEnum)obj;

    }

    private static java.lang.String invalidValueMsg(java.lang.String value)
    {
        java.lang.String msg = "invalid enumeration value: " + value;
        return msg;
    }


    // Gets enumeration from a String
    // throws java.lang.IllegalArgumentException if
    // any invalid value is specified
    public static com.mytest.IntegerEnum fromString(java.lang.String value)
    {
        java.math.BigInteger __tmp = XsTypeConverter.lexInteger(value);
        return fromValue(__tmp);
    }


    // Returns String representation of the enumerated value
    public java.lang.String toString()
    {
        return java.lang.String.valueOf(__value);
    }

    public boolean equals(java.lang.Object obj)
    {
        if (obj instanceof com.mytest.IntegerEnum) {
            java.math.BigInteger tmp_val = ((com.mytest.IntegerEnum)obj).getValue();
            return tmp_val.equals(__value);

        }
        return false;
    }


    public int hashCode()
    {
        int __hash__result__ = 17;
        __hash__result__ = 37 * __hash__result__ + (__value == null ? 0 : __value.hashCode());

        return __hash__result__;
    }

    private java.lang.Object readResolve()
        throws java.io.ObjectStreamException
    {
        return fromValue(__value);
    }


    private static java.util.Map _buildValueMap()
    {
        java.util.Map __valmap = new java.util.HashMap();

        __valmap.put(_value1, value1);
        __valmap.put(_value2, value2);
        __valmap.put(_value3, value3);


        return __valmap;
    }


}


