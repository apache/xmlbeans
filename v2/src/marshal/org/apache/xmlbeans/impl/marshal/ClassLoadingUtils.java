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

package org.apache.xmlbeans.impl.marshal;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class ClassLoadingUtils
{
    private static final Map PRIMITIVES = new HashMap();

    static
    {
        PRIMITIVES.put(boolean.class.getName(), boolean.class);
        PRIMITIVES.put(char.class.getName(), char.class);
        PRIMITIVES.put(byte.class.getName(), byte.class);
        PRIMITIVES.put(short.class.getName(), short.class);
        PRIMITIVES.put(int.class.getName(), int.class);
        PRIMITIVES.put(long.class.getName(), long.class);
        PRIMITIVES.put(float.class.getName(), float.class);
        PRIMITIVES.put(double.class.getName(), double.class);
    }


    public static Class loadClass(String className,
                                  ClassLoader backup_classloader)
        throws ClassNotFoundException
    {
        Class prim_class = loadPrimitiveClass(className);
        if (prim_class != null) return prim_class;

        Class array_class = loadArrayClass(className, backup_classloader);
        if (array_class != null)
            return array_class;
        else
            return loadNonArrayClass(className, backup_classloader);
    }

    private static Class loadPrimitiveClass(String className)
    {
        return (Class)PRIMITIVES.get(className);
    }

    private static Class loadNonArrayClass(String className,
                                           ClassLoader backup_classloader)
        throws ClassNotFoundException
    {
        Class prim_class = loadPrimitiveClass(className);

        if (prim_class != null) {
            return prim_class;
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (cl == null) {
            cl = backup_classloader;
        }

        try {
            return cl.loadClass(className);
        }
        catch (ClassNotFoundException e) {
            return Class.forName(className);
        }
    }

    private static Class loadArrayClass(String className,
                                        ClassLoader backup_classloader)
        throws ClassNotFoundException
    {
        StringBuffer component = new StringBuffer(className.length());
        int rank = getArrayComponentNameFromDecl(component, className);

        if (rank < 1) return null; //not an array

        int[] ranks = new int[rank];

        Arrays.fill(ranks, 1);

        Class component_class = loadNonArrayClass(component.toString(),
                                                  backup_classloader);
        Object instance = Array.newInstance(component_class, ranks);
        return instance.getClass();
    }

    //TODO: make sure we don't have a another version of this method somewhere
    //compname holds return value
    //return int is number of dimensions of array
    public static int getArrayComponentNameFromDecl(StringBuffer compname,
                                                    String aname)
    {
        compname.setLength(0);

        final int first_bracket = aname.indexOf('[');

        if (first_bracket <= 0) {
            compname.append(aname);
            return 0;
        }

        final String base = aname.substring(0, first_bracket).trim();
        compname.append(base);

        int rank = 0;
        for (int idx = aname.indexOf(']'); idx >= 0;
             idx = aname.indexOf(']', idx + 1)) {
            rank++;
        }

        assert compname.length() > 0;
        assert rank > 0;
        return rank;
    }

    static Object newInstance(Class javaClass)
    {
        try {
            return javaClass.newInstance();
        }
        catch (InstantiationException e) {
            //TODO: real error handling
            throw (RuntimeException)(new RuntimeException(e.getMessage()).initCause(e));
        }
        catch (IllegalAccessException e) {
            //TODO: real error handling
            throw (RuntimeException)(new RuntimeException(e.getMessage()).initCause(e));
        }
    }


}
