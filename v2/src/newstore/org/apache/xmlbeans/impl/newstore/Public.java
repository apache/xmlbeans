package org.apache.xmlbeans.impl.newstore;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import org.apache.xmlbeans.impl.newstore.xcur.Xcur;
import org.apache.xmlbeans.impl.newstore.xcur.Mcur;
import org.apache.xmlbeans.impl.newstore.xcur.Fcur;

import org.apache.xmlbeans.impl.newstore.xcur.Master;
import org.apache.xmlbeans.impl.newstore.DomImpl.Dom;

import org.apache.xmlbeans.impl.newstore.Saaj;

import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;

public class Public
{
    public static Master getMaster ( int type )
    {
        assert type == 0 || type == 1;
        return type == 0 ? Fcur.newMaster() : Mcur.newMaster();
    }
    
    public static DOMImplementation getImplementation ( Saaj saaj, int type )
    {
        Master m = getMaster( type );

        if (saaj != null)
        {
            m._saaj = saaj;
            saaj.setCallback( m );
        }

        return (DOMImplementation) m;
    }
    
    public static DOMImplementation getImplementation ( int type )
    {
        return (DOMImplementation) getMaster( type );
    }

    public static Document parse ( String s, int type )
    {
        Master m = getMaster( type );

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = m.load( s ); }
            else synchronized ( m ) { m.enter(); d = m.load( s ); }
        }
        finally
        {
            m.exit();
        }

        return (Document) d;
    }

    public static Document parse ( String s, int type, Saaj saaj )
    {
        Master m = getMaster( type );

        if (saaj != null)
        {
            m._saaj = saaj;
            saaj.setCallback( m );
        }

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = m.load( s ); }
            else synchronized ( m ) { m.enter(); d = m.load( s ); }
        }
        finally
        {
            m.exit();
        }

        return (Document) d;
    }

    public static Document parse ( InputStream is, int type, Saaj saaj )
    {
        Master m = getMaster( type );

        if (saaj != null)
        {
            m._saaj = saaj;
            saaj.setCallback( m );
        }

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = m.load( is ); }
            else synchronized ( m ) { m.enter(); d = m.load( is ); }
        }
        finally
        {
            m.exit();
        }

        return (Document) d;
    }

    public static XMLStreamReader getStream ( Node n )
    {
        assert n instanceof Dom;

        Dom d = (Dom) n;
        
        Master m = d.master();

        try
        {
            if (m.noSync())         { m.enter(); return DomImpl.getXmlStreamReader( d ); }
            else synchronized ( m ) { m.enter(); return DomImpl.getXmlStreamReader( d ); }
        }
        finally
        {
            m.exit();
        }
    }
}