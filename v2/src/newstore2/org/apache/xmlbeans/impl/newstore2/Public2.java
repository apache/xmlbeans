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
import java.io.OutputStream;
import java.io.IOException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;

import java.io.PrintStream;

import org.apache.xmlbeans.impl.newstore2.DomImpl.Dom;

import org.apache.xmlbeans.impl.newstore2.Saver.TextSaver;

import org.apache.xmlbeans.impl.values.TypeStore;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.apache.xmlbeans.impl.values.TypeStoreVisitor;
import org.apache.xmlbeans.impl.values.TypeStoreUserFactory;

import org.apache.xmlbeans.SchemaType;

import org.apache.xmlbeans.impl.values.NamespaceManager;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaField;

import org.apache.xmlbeans.QNameSet;

public final class Public2
{
    public static final String SAAJ_IMPL = "SAAJ_IMPL";
    
    private static Locale newLocale ( Saaj saaj )
    {
        XmlOptions options = null;
        
        if (saaj != null)
        {
            options = new XmlOptions();
            options.put( SAAJ_IMPL, saaj );
        }
        
        return Locale.getLocale( null, options );
    }

    public static void setSync ( Document doc, boolean sync )
    {
        assert doc instanceof Dom;

        Locale l = ((Dom) doc).locale();

        l._noSync = ! sync;
    }

    public static String compilePath ( String path, XmlOptions options )
    {
        return Path.compilePath( path, options );
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
    
    public static Document parse ( String s, XmlOptions options )
    {
        Locale l = newLocale( null );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = l.load( s, options ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = l.load( s, options ); } finally { l.exit(); } }

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
    
    public static Document parse ( InputStream is, XmlOptions options )
    {
        Locale l = newLocale( null );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = l.load( is, options ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = l.load( is, options ); } finally { l.exit(); } }

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
    
    public static Node getNode ( XMLStreamReader s )
    {
        return Jsr173.nodeFromStream( s );
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
        return save( n, null );
    }
    
    public static void save ( Node n, OutputStream os, XmlOptions options ) throws IOException
    {
        XmlCursor c = getCursor( n );

        c.save( os, options );

        c.dispose();
    }
    
    public static String save ( Node n, XmlOptions options )
    {
        assert n instanceof Dom;

        Dom d = (Dom) n;
        
        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { return saveImpl( d, options ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return saveImpl( d, options ); } finally { l.exit(); } }
    }
    
    private static String saveImpl ( Dom d, XmlOptions options )
    {
        Cur c = d.tempCur();
        
        String s = new TextSaver( c, options, null ).saveToString();

        c.release();

        return s;
    }
    
    public static String save ( XmlCursor c )
    {
        return save( c, null );
    }
    
    public static String save ( XmlCursor xc, XmlOptions options )
    {
        Cursor cursor = (Cursor) xc;
        
        Locale l = cursor.locale();

        if (l.noSync())         { l.enter(); try { return saveImpl( cursor, options ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return saveImpl( cursor, options ); } finally { l.exit(); } }
    }
    
    private static String saveImpl ( Cursor cursor, XmlOptions options )
    {
        Cur c = cursor.tempCur();

        String s = new TextSaver( c, options, null ).saveToString();
        
        c.release();

        return s;
    }
    
    public static XmlCursor newStore ( )
    {
        return newStore( null );
    }
    
    public static XmlCursor newStore ( Saaj saaj )
    {
        Locale l = newLocale( saaj );

        Cur c = l.permCur();

        c.createRoot();

        Cursor cursor = new Cursor( c );

        c.release();

        return cursor;
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

    public static void dump ( PrintStream o, Node n )
    {
        dump( o, (Dom) n );
    }

    public static void dump ( PrintStream o, XmlCursor c )
    {
        ((Cursor) c).dump( o );
    }

    public static void dump ( Dom  d )      { dump( System.out, d ); }
    public static void dump ( Node n )      { dump( System.out, n ); }
    public static void dump ( XmlCursor c ) { dump( System.out, c ); }

    public static void test ( Node n ) throws Exception
    {
        Dom d = (Dom) n;

        Locale l = d.locale();
        
//        Locale l = Locale.getLocale( XmlBeans.getContextTypeLoader(), null );
        
        l.enter();

        try
        {
            doTest( l, d );
        }
        finally
        {
            l.exit();
        }
    }
        
    public static void doTest ( Locale l, Dom d )
    {
        DomImpl.impl_saajCallback_setSaajData( d, new Object() );
    }
}
 