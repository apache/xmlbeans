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

package org.apache.xmlbeans.impl.newstore2;

import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import org.apache.xmlbeans.XmlCursor;

import java.io.PrintStream;

import org.apache.xmlbeans.impl.newstore2.DomImpl.Dom;

import org.apache.xmlbeans.impl.newstore2.Saver.TextSaver;

public final class Public2
{
    private static Locale newLocale ( Saaj saaj )
    {
        Locale l = new Locale();

        if (saaj != null)
        {
            l._saaj = saaj;
            saaj.setCallback( l );
        }

        return l;
    }

    public static void setSync ( Document doc, boolean sync )
    {
        assert doc instanceof Dom;

        Locale l = ((Dom) doc).locale();

        l._noSync = ! sync;
    }

    public static DOMImplementation getDomImplementation ( )
    {
        return newLocale( null );
    }
    
    public static DOMImplementation getDomImplementation ( Saaj saaj )
    {
        return newLocale( saaj );
    }
    
    public static Document parse ( String s )
    {
        Locale l = newLocale( null );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }

        return (Document) d;
    }
    
    public static Document parse ( String s, Saaj saaj )
    {
        Locale l = newLocale( saaj );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }

        return (Document) d;
    }
    
    public static Document parse ( InputStream is, Saaj saaj )
    {
        Locale l = newLocale( saaj );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = l.load( is ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = l.load( is ); } finally { l.exit(); } }

        return (Document) d;
    }
    
    public static XMLStreamReader getStream ( Node n )
    {
        assert n instanceof Dom;

        Dom d = (Dom) n;
        
        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { return DomImpl.getXmlStreamReader( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return DomImpl.getXmlStreamReader( d ); } finally { l.exit(); } }
    }

    public static String save ( Node n )
    {
        assert n instanceof Dom;

        Dom d = (Dom) n;
        
        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { return saveImpl( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return saveImpl( d ); } finally { l.exit(); } }
    }
    
    public static String saveImpl ( Dom d )
    {
        Cur c = d.tempCur();
        
        String s = new TextSaver( c, null, null ).saveToString();

        c.release();

        return s;
    }
    
    public static XmlCursor getCursor ( Node n )
    {
        assert n instanceof Dom;

        Dom d = (Dom) n;

        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { return DomImpl.getXmlCursor( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return DomImpl.getXmlCursor( d ); } finally { l.exit(); } }
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