package org.apache.xmlbeans.impl.newstore2;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;

import java.io.PrintStream;

import org.apache.xmlbeans.impl.newstore2.DomImpl.Dom;

public final class Public2
{
    public static DOMImplementation getDomImplementation ( )
    {
        return new Locale();
    }
    
    public static void dump ( PrintStream o, Dom d )
    {
        d.dump( o );
    }

    public static void dump ( Dom d )
    {
        dump( System.out, d );
    }

    public static void dump ( Node n )
    {
        dump( System.out, n );
    }

    public static void dump ( PrintStream o, Node n )
    {
        dump( o, (Dom) n );
    }
}