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
    public static class MemoryBackend implements Backend
    {
        public Locale newLocale ( )
        {
            return new Mcur.Mlocale();
        }
    }
    
    public static Backend memoryBackend ( )
    {
        return new MemoryBackend();
    }
    
    public static Locale getLocale ( Backend be )
    {
        return be.newLocale();
    }
    
    public static DOMImplementation getImplementation ( Saaj saaj, Backend be )
    {
        Locale l = getLocale( be );

        if (saaj != null)
        {
            l._saaj = saaj;
            saaj.setCallback( l );
        }

        return (DOMImplementation) l;
    }
    
    public static DOMImplementation getImplementation ( Backend be )
    {
        return (DOMImplementation) getLocale( be );
    }

    public static Document parse ( String s, Backend be )
    {
        Locale l = getLocale( be );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }

        return (Document) d;
    }

    public static Document parse ( String s, Backend be, Saaj saaj )
    {
        Locale l = getLocale( be );

        if (saaj != null)
        {
            l._saaj = saaj;
            saaj.setCallback( l );
        }

        Dom d;

        if (l.noSync())         { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = l.load( s ); } finally { l.exit(); } }

        return (Document) d;
    }

    public static Document parse ( InputStream is, Backend be, Saaj saaj )
    {
        Locale l = getLocale( be );

        if (saaj != null)
        {
            l._saaj = saaj;
            saaj.setCallback( l );
        }

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

    public static XmlCursor getCursor ( Node n )
    {
        assert n instanceof Dom;

        Dom d = (Dom) n;

        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { return DomImpl.getXmlCursor( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return DomImpl.getXmlCursor( d ); } finally { l.exit(); } }
    }
}