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

import org.apache.xmlbeans.impl.marshal.util.ArrayUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

public class YourClass
{
    private MyClass myClass;
    private float myFloat;
    private float attrib;
    private boolean someBool;
//    private List bools;// = newBoolList();
//    private List strs;// = newStringList();
    private long[] longArray;// = {RND.nextLong(), RND.nextLong()};

    private boolean[] booleanArray;// = {true, false, true};
    private String[] stringArray = {"ONE:"+RND.nextInt(), "TWO:"+RND.nextInt()};
    private MyClass[] myClassArray;//{new MyClass(), new MyClass()};

    private QName qn = new QName("URI" + RND.nextInt(), "LNAME"+RND.nextInt());
    private QName qn2 = new QName("URI" + RND.nextInt(), "LNAME"+RND.nextInt());

    //hack alert
    static final Random RND = new Random();

    private List newStringList()
    {
        ArrayList l = new ArrayList();
        l.add("one:" + RND.nextInt());
        l.add("two:" + RND.nextInt());
        l.add(null);
        l.add("three:" + RND.nextInt());
        return l;
    }

    private List newBoolList()
    {
        ArrayList l = new ArrayList();
        l.add(Boolean.TRUE);
        l.add(Boolean.FALSE);
//        l.add(null);
//        l.add(Boolean.TRUE);
//        l.add(Boolean.FALSE);
        return l;
    }

    public float getMyFloat()
    {
        return myFloat;
    }

    public void setMyFloat(float myFloat)
    {
        this.myFloat = myFloat;
    }

    public MyClass getMyClass()
    {
        return myClass;
    }

    public void setMyClass(MyClass myClass)
    {
        this.myClass = myClass;
    }

    public boolean isSomeBool()
    {
        return someBool;
    }

    public void setSomeBool(boolean someBool)
    {
        this.someBool = someBool;
    }
//
//    public List getBools()
//    {
//        return bools;
//    }
//
//    public void setBools(List bools)
//    {
//        this.bools = bools;
//    }


    /**
     *  @xsdgen:attribute.name Attrib
     */
    public float getAttrib()
    {
        return attrib;
    }

    public void setAttrib(float attrib)
    {
        this.attrib = attrib;
    }

//    public List getStrs()
//    {
//        return strs;
//    }
//
//    public void setStrs(List strs)
//    {
//        this.strs = strs;
//    }

    public long[] getLongArray()
    {
        return longArray;
    }

    public void setLongArray(long[] longArray)
    {
        this.longArray = longArray;
    }


    public String[] getStringArray()
    {
        return stringArray;
    }

    public void setStringArray(String[] stringArray)
    {
        this.stringArray = stringArray;
    }

    public MyClass[] getMyClassArray()
    {
        return myClassArray;
    }

    public void setMyClassArray(MyClass[] myClassArray)
    {
        this.myClassArray = myClassArray;
    }

    public boolean[] getBooleanArray()
    {
        return booleanArray;
    }

    public void setBooleanArray(boolean[] booleanArray)
    {
        this.booleanArray = booleanArray;
    }


    public QName getQn()
    {
        return qn;
    }

    public void setQn(QName qn)
    {
        this.qn = qn;
    }

    public QName getQn2()
    {
        return qn2;
    }

    public void setQn2(QName qn2)
    {
        this.qn2 = qn2;
    }


    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof YourClass)) return false;

        final YourClass yourClass = (YourClass)o;

        if (attrib != yourClass.attrib) return false;
        if (myFloat != yourClass.myFloat) return false;
        if (someBool != yourClass.someBool) return false;
        if (!Arrays.equals(booleanArray, yourClass.booleanArray)) return false;
//        if (bools != null ? !bools.equals(yourClass.bools) : yourClass.bools != null) return false;
        if (!Arrays.equals(longArray, yourClass.longArray)) return false;
        if (myClass != null ? !myClass.equals(yourClass.myClass) : yourClass.myClass != null) return false;
        if (!Arrays.equals(myClassArray, yourClass.myClassArray)) return false;
        if (!Arrays.equals(stringArray, yourClass.stringArray)) return false;
//        if (strs != null ? !strs.equals(yourClass.strs) : yourClass.strs != null) return false;

        if (qn != null ? !qn.equals(yourClass.qn) : yourClass.qn != null) return false;
        if (qn2 != null ? !qn2.equals(yourClass.qn2) : yourClass.qn2 != null) return false;


        return true;
    }

    public int hashCode()
    {
        int result;
        result = (myClass != null ? myClass.hashCode() : 0);
        result = 29 * result + Float.floatToIntBits(myFloat);
        result = 29 * result + Float.floatToIntBits(attrib);
        result = 29 * result + (someBool ? 1 : 0);
//        result = 29 * result + (bools != null ? bools.hashCode() : 0);
//        result = 29 * result + (strs != null ? strs.hashCode() : 0);
        return result;
    }




    public String toString()
    {
        return "com.mytest.YourClass{" +
            "myClass=" + myClass +
            ", myFloat=" + myFloat +
            ", attrib=" + attrib +
            ", someBool=" + someBool +
            ", qn=" + qn +
            ", qn2=" + qn2 +
//            ", bools=" + (bools == null ? null : "size:" + bools.size() + bools) +
//            ", strs=" + (strs == null ? null : "size:" + strs.size() + strs) +
            ", longArray=" + ArrayUtils.arrayToString(longArray) +
            ", booleanArray=" + ArrayUtils.arrayToString(booleanArray) +
            ", stringArray=" + ArrayUtils.arrayToString(stringArray) +
            ", myClassArray=" + ArrayUtils.arrayToString(myClassArray) +
            "}";
    }



}
