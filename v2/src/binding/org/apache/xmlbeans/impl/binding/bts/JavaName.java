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
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/package org.apache.xmlbeans.impl.binding.bts;

/**
 * Represents a Java class name, and provides some utility methods
 * for parsing out bits of the name.
 * 
 * Examples:
 * 
 * "int[]"
 * "java.lang.String"
 * "com.myco.MyClass$MyInnerClass$InnerInnerClass[][]"
 */
public final class JavaName
{
    private final String className;
    private final String arrayString;

    /**
     * Returns a JavaName object for a fully-qualified class name.
     * The class-name should be dot-separated for packages and
     * dollar-separated for inner classes.
     * 
     * The names "int", "byte" etc are considered special.
     * Arrays are dealt with as [][][] at the end.
     * 
     * This is a static function to permit pooling in the future.
     */
    public static JavaName forString(String className)
    {
        return new JavaName(className);
    }
    
    /**
     * Do not use this constructor; use forClassName instead.
     */ 
    private JavaName(String className)
    {
        if (className == null)
            throw new IllegalArgumentException();
        
        int arrayDepth = 0;
        for (int i = className.length() - 2; i >= 0; i -= 2)
        {
            if (className.charAt(i) != '[' || className.charAt(i + 1) != ']')
                break;
            arrayDepth += 1;
        }
        this.className = className.substring(0, className.length() - 2 * arrayDepth);
        this.arrayString = className.substring(className.length() - 2 * arrayDepth);
    }
    
    /**
     * Returns the fully-qualified class name.
     */ 
    public String toString()
    {
        return className + arrayString;
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
    public JavaName getArrayItemType(int depth)
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
     * True if this is an inner class name.
     */ 
    public boolean isInnerClass()
    {
        return (className.lastIndexOf('$') >= 0);
    }
    
    /**
     * Returns the JavaName of the containing class, or null if this
     * is not an inner class name.
     */ 
    public JavaName getContainingClass()
    {
        int index = className.lastIndexOf('$');
        if (index < 0)
            return null;
        return JavaName.forString(className.substring(0, index));
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

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof JavaName)) return false;

        final JavaName javaName = (JavaName) o;

        if (!className.equals(javaName.className)) return false;
        if (!arrayString.equals(javaName.arrayString)) return false;

        return true;
    }

    public int hashCode()
    {
        return className.hashCode() + arrayString.length();
    }
}
