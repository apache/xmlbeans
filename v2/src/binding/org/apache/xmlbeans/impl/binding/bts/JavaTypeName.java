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
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.internal.elements.PrimitiveClassImpl;

import java.lang.reflect.Array;

/**
 * Represents a Java class name, and provides some utility methods
 * for parsing out bits of the name.
 *
 * Examples:
 *
 * "int[]"
 * "java.lang.String"
 * "com.myco.MyClass$MyInnerClass$InnerInnerClass[][]"
 *
 * The classname is preceded by an "x=" if it is an XmlObject
 * subclass, i.e., if it's an XmlBeans-style class, for example:
 *
 * "x=org.apache.xmlbeans.XmlInt"
 */
public final class JavaTypeName
{

    // ========================================================================
    // Constants

    private static String XMLOBJECT_CLASSNAME = XmlObject.class.getName();

    // ========================================================================
    // Variables

    private final String className;
    private final String arrayString;
    private final boolean isXmlObject;

    // ========================================================================
    // Factories

    /**
     * Returns a JavaTypeName object for a fully-qualified class name.
     * The class-name should be dot-separated for packages and
     * dollar-separated for inner classes.
     *
     * The names "int", "byte" etc are considered special.
     * Arrays are dealt with as [][][] at the end.
     *
     * This is a static function to permit pooling in the future.
     */
    public static JavaTypeName forString(String className)
    {
        return new JavaTypeName(className);
    }


    /**
     * Builds a JavaTypeName for the array containing items with
     * the given JavaTypeName.
     */
    public static JavaTypeName forArray(JavaTypeName itemType, int depth)
    {
        // efficiency later
        String arrayBrackets = "";
        while (depth-- > 0)
            arrayBrackets += "[]";
        return forString(itemType.toString() + arrayBrackets);
    }

    /**
     * Builds a JavaTypeName for the given JClass.
     */
    public static JavaTypeName forJClass(JClass jClass)
    {
        if (jClass.isArrayType()) {
            return forArray(forJClass(jClass.getArrayComponentType()),
                            jClass.getArrayDimensions());
        }
        JClass[] interfaces = jClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getQualifiedName().equals(XMLOBJECT_CLASSNAME))
                return forString("x=" + jClass.getQualifiedName());
        }

        return forString(jClass.getQualifiedName());
    }




    // ========================================================================
    // Constructors

    /**
     * Do not use this constructor; use forClassName instead.
     */
    private JavaTypeName(String className)
    {
        if (className == null)
            throw new IllegalArgumentException();

        if (className.startsWith("x=")) {
            this.isXmlObject = true;
            className = className.substring(2);
        } else {
            this.isXmlObject = false;
        }

        int arrayDepth = 0;
        for (int i = className.length() - 2; i >= 0; i -= 2) {
            if (className.charAt(i) != '[' || className.charAt(i + 1) != ']')
                break;
            arrayDepth += 1;
        }
        this.className = className.substring(0, className.length() - 2 * arrayDepth);
        this.arrayString = className.substring(className.length() - 2 * arrayDepth);
    }

    private JavaTypeName(String nonArrayClassName,
                         String arrayString,
                         boolean isXmlObject)
    {
        assert !nonArrayClassName.startsWith("[");

        this.className = nonArrayClassName;
        this.arrayString = arrayString;
        this.isXmlObject = isXmlObject;
    }


    // ========================================================================
    // Public methods

    /**
     * True for classnames that are XmlObjects.
     */
    public boolean isXmlObject()
    {
        return isXmlObject;
    }

    /**
     * Returns the array depth, 0 for non-arrays, 1 for [], 2 for [][], etc.
     */
    public int getArrayDepth()
    {
        return arrayString.length() / 2;
    }

    /**
     * Returns the array item type (peeling off "n" array indexes)
     */
    public JavaTypeName getArrayItemType(int depth)
    {
        if (arrayString.length() < depth * 2)
            return null;
        return forString(className + arrayString.substring(0, arrayString.length() - 2 * depth));
    }

    /**
     * Returns the dot-separated package name.
     */
    public String getPackage()
    {
        int index = className.lastIndexOf('.');
        if (index <= 0)
            return "";
        return className.substring(0, index);
    }

    /**
     * Returns the array string, peeling off the first depth levels
     */
    public String getArrayString(int depth)
    {
        if (arrayString.length() < depth * 2)
            return null;
        return arrayString.substring(2 * depth, arrayString.length());
    }

    /**
     * True if this is an inner class name.
     */
    public boolean isInnerClass()
    {
        return (className.lastIndexOf('$') >= 0);
    }

    /**
     * Returns the JavaTypeName of the containing class, or null if this
     * is not an inner class name.
     */
    public JavaTypeName getContainingClass()
    {
        int index = className.lastIndexOf('$');
        if (index < 0)
            return null;
        return JavaTypeName.forString(className.substring(0, index));
    }

    /**
     * Returns the short class name (i.e., no dots or dollars).
     */
    public String getShortClassName()
    {
        int index = className.lastIndexOf('$');
        int index2 = className.lastIndexOf('.');
        if (index2 > index)
            index = index2;
        if (index < 0)
            return className;
        return className.substring(index + 1);
    }


    /**
     * Is this JavaTypeName a name for the given class
     *
     * @param c
     * @return
     */
    public boolean isNameForClass(Class c) {
        //TODO: optimize this method, or better yet avoid using it
        JavaTypeName cname = forClassName(c.getName());
        return this.equals(cname);
    }

    // ========================================================================
    // Object implementation

    /**
     * Returns the fully-qualified class name.
     */
    public String toString()
    {
        if (isXmlObject)
            return "x=" + className + arrayString;
        return className + arrayString;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof JavaTypeName)) return false;

        final JavaTypeName javaName = (JavaTypeName)o;

        if (isXmlObject != javaName.isXmlObject) return false;
        if (!className.equals(javaName.className)) return false;
        if (!arrayString.equals(javaName.arrayString)) return false;

        return true;
    }

    public int hashCode()
    {
        return className.hashCode() + arrayString.length() + (isXmlObject ? 1 : 0);
    }


    // ========================================================================
    // Deprecated methods

    /**
     * Loads the class represented by this JavaTypeName in the given
     * ClassLoader.  This is really horrible - the impedance mismatch
     * in the naming here is very really painful.  Need to do something better.
     */
    /**
     * @deprecated
     */
    public Class loadClassIn(ClassLoader loader) throws ClassNotFoundException
    {
        int d = getArrayDepth();
        if (d == 0) {
            String s = toString();
            Class out = PrimitiveClassImpl.getPrimitiveClass(s);
            if (out != null) return out;
            return loader.loadClass(s);
        } else {
            Class clazz = PrimitiveClassImpl.getPrimitiveClass(className);
            if (clazz == null) clazz = loader.loadClass(className);
            int[] dimensions = new int[d];
            return Array.newInstance(clazz, dimensions).getClass();

            /* THIS IS COMMENTED OUT BECAUSE IT IS BROKEN ON THE CURRENT 1.5 BETA
               SEE SUN BUG 4983838.  IT'S NOT CLEAR WHETHER OR NOT THEY'RE GOING
               TO FIX IT.  MAYBE THE WORKAROUND IS BETTER ANYWAY, THOUGH :(
            StringWriter buff = new StringWriter();
            for(int i=0; i<d; i++) buff.write("[");
            String s = toString();
            s = s.substring(0,s.indexOf("["));
            String fd = PrimitiveJClass.getFieldDescriptor(s);
            if (fd != null) {
            buff.write(fd);
            } else {
            buff.write("L");
            buff.write(s);
            buff.write(";");
            }
            return loader.loadClass(buff.toString());
            */
        }
    }


    /**
     * @deprecated

    public static JavaTypeName forJClass(org.apache.xmlbeans.impl.jam_old.JClass jClass)
    {
        if (jClass.isArray()) {
            return forArray(forJClass(jClass.getArrayComponentType()), jClass.getArrayDimensions());
        }

        org.apache.xmlbeans.impl.jam_old.JClass[] interfaces = jClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getQualifiedName().equals(XMLOBJECT_CLASSNAME))
                return forString("x=" + jClass.getQualifiedName());
        }

        return forString(jClass.getQualifiedName());
    }
     */

    /**
     * create a JavaTypeName for a String in the form return by
     * java.lang.Class.getName()
     *
     * @param class_name
     * @return
     */
    public static JavaTypeName forClassName(final String class_name)
    {
        if ('[' != class_name.charAt(0)) {
            return new JavaTypeName(class_name, "", false);
        }

        final int first_bracket = class_name.indexOf('[');
        final int last_bracket = class_name.lastIndexOf('[');
        final int semi = class_name.indexOf(';', last_bracket);

        final String compname;

        if (semi == -1) {
            char array_type = class_name.charAt(1 + last_bracket);
            compname = getPrimitiveArrayBase(array_type);
        } else {
            compname = class_name.substring(last_bracket + 2, semi);
        }
        final int dims = (1 + last_bracket - first_bracket);

        assert compname.length() > 0 ;

        StringBuffer array_str = new StringBuffer(2 * dims);
        for (int i = 0; i < dims; i++) {
            array_str.append("[]");
        }

        final JavaTypeName jtn = new JavaTypeName(compname,
                                                  array_str.toString(),
                                                  false);
        return jtn;
    }

    private static String getPrimitiveArrayBase(char array_type)
    {
        switch (array_type) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'Z':
                return "boolean";
            default:
                throw new AssertionError("unknown array type: " + array_type);
        }
    }

}
