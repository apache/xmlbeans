/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding;

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

        return true;
    }

    public int hashCode()
    {
        return className.hashCode();
    }
}
