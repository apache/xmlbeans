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
    private static int getArrayComponentNameFromDecl(StringBuffer compname,
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
