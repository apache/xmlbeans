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

package org.apache.xmlbeans.impl.newstore.pub;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import org.apache.xmlbeans.impl.newstore.pub.store.Backend;
import org.apache.xmlbeans.impl.newstore.pub.store.Locale;
import org.apache.xmlbeans.impl.newstore.pub.store.Cur;

import org.apache.xmlbeans.XmlCursor;

import org.apache.xmlbeans.impl.newstore.Mcur;
import org.apache.xmlbeans.impl.newstore.DomImpl;
import org.apache.xmlbeans.impl.newstore.Saaj;
import org.apache.xmlbeans.impl.newstore.pub.store.Backend;
import org.apache.xmlbeans.impl.newstore.pub.store.Dom;

import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;

public class Public
{
//    public static class MemoryBackend implements Backend
//    {
//        public Locale newLocale ( )
//        {
//            return new Mcur.Mlocale();
//        }
//    }
//    
//    public static Backend memoryBackend ( )
//    {
//        return new MemoryBackend();
//    }
//    
//    public static Locale getLocale ( Backend be )
//    {
//        return be.newLocale();
//    }
//    
//    public static DOMImplementation getImplementation ( Saaj saaj, Backend be )
//    {
//        Locale l = getLocale( be );
//
//        if (saaj != null)
//        {
//            l._saaj = saaj;
//            saaj.setCallback( l );
//        }
//
//        return (DOMImplementation) l;
//    }
//    
//    public static DOMImplementation getImplementation ( Backend be )
//    {
//        return (DOMImplementation) getLocale( be );
//    }
//
//    public static Document parse ( String s, Backend be )
//    {
//        Locale l = getLocale( be );
//
//        Dom d;
//
//        if (l.noSync())         { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
//        else synchronized ( l ) { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
//
//        return (Document) d;
//    }
//
//    public static Document parse ( String s, Backend be, Saaj saaj )
//    {
//        Locale l = getLocale( be );
//
//        if (saaj != null)
//        {
//            l._saaj = saaj;
//            saaj.setCallback( l );
//        }
//
//        Dom d;
//
//        if (l.noSync())         { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
//        else synchronized ( l ) { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
//
//        return (Document) d;
//    }
//
//    public static Document parse ( InputStream is, Backend be, Saaj saaj )
//    {
//        Locale l = getLocale( be );
//
//        if (saaj != null)
//        {
//            l._saaj = saaj;
//            saaj.setCallback( l );
//        }
//
//        Dom d;
//
//        if (l.noSync())         { l.enter(); try { d = l.load( is ); } finally { l.exit(); } }
//        else synchronized ( l ) { l.enter(); try { d = l.load( is ); } finally { l.exit(); } }
//
//        return (Document) d;
//    }
//
//    public static XMLStreamReader getStream ( Node n )
//    {
//        assert n instanceof Dom;
//
//        Dom d = (Dom) n;
//        
//        Locale l = d.locale();
//
//        if (l.noSync())         { l.enter(); try { return DomImpl.getXmlStreamReader( d ); } finally { l.exit(); } }
//        else synchronized ( l ) { l.enter(); try { return DomImpl.getXmlStreamReader( d ); } finally { l.exit(); } }
//    }
//
//    public static XmlCursor getCursor ( Node n )
//    {
//        assert n instanceof Dom;
//
//        Dom d = (Dom) n;
//
//        Locale l = d.locale();
//
//        if (l.noSync())         { l.enter(); try { return DomImpl.getXmlCursor( d ); } finally { l.exit(); } }
//        else synchronized ( l ) { l.enter(); try { return DomImpl.getXmlCursor( d ); } finally { l.exit(); } }
//    }
}