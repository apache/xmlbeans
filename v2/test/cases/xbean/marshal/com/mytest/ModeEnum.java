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



public class ModeEnum
    implements java.io.Serializable
{

    private java.lang.String __value;

    protected ModeEnum(java.lang.String value)
    {
        __value = value;
    }

    public static final java.lang.String _Off = "Off";
    public static final com.mytest.ModeEnum Off = new com.mytest.ModeEnum(_Off);

    public static final java.lang.String _On = "On";
    public static final com.mytest.ModeEnum On = new com.mytest.ModeEnum(_On);


    private static final java.util.Map _valueMap = _buildValueMap();


    // Gets the value for a enumerated value
    public java.lang.String getValue()
    {
        return __value;
    }


    // Gets enumeration with a specific value
    // throws java.lang.IllegalArgumentException if
    // any invalid value is specified
    public static com.mytest.ModeEnum fromValue(java.lang.String value)
    {
        Object obj = _valueMap.get(value);
        if (obj == null) {
            java.lang.String msg = invalidValueMsg("" + value);
            msg = msg + (" valmap=" + _valueMap);
            throw new java.lang.IllegalArgumentException(msg);
        }
        return (com.mytest.ModeEnum)obj;

    }

    private static java.lang.String invalidValueMsg(java.lang.String value)
    {
        java.lang.String msg = "invalid enumeration value: " + value;
        return msg;
    }


    // Gets enumeration from a String
    // throws java.lang.IllegalArgumentException if
    // any invalid value is specified
    public static com.mytest.ModeEnum fromString(java.lang.String value)
    {
        return fromValue(value);
    }


    // Returns String representation of the enumerated value
    public java.lang.String toString()
    {
        return java.lang.String.valueOf(__value);
    }

    public java.lang.String toXml() {
        return toString();
    }

    public boolean equals(java.lang.Object obj)
    {
        if (obj instanceof com.mytest.ModeEnum) {
            java.lang.String tmp_val = ((com.mytest.ModeEnum)obj).getValue();
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

        __valmap.put(_Off, Off);
        __valmap.put(_On, On);


        return __valmap;
    }


}


