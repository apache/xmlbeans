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

import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.lang.ref.PhantomReference;

import java.lang.reflect.Method;

import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.common.ResolverUtil;

import org.apache.xmlbeans.impl.newstore2.Saaj.SaajCallback;

import org.apache.xmlbeans.impl.newstore2.DomImpl.Dom;
import org.apache.xmlbeans.impl.newstore2.DomImpl.TextNode;
import org.apache.xmlbeans.impl.newstore2.DomImpl.CdataNode;
import org.apache.xmlbeans.impl.newstore2.DomImpl.SaajTextNode;
import org.apache.xmlbeans.impl.newstore2.DomImpl.SaajCdataNode;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlRuntimeException;

import javax.xml.namespace.QName;

final class Locale implements DOMImplementation, SaajCallback
{
    public static final String _xsi         = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String _schema      = "http://www.w3.org/2001/XMLSchema";
    public static final String _openFragUri = "http://www.openuri.org/fragment";
    public static final String _xml1998Uri  = "http://www.w3.org/XML/1998/namespace";
    public static final String _xmlnsUri    = "http://www.w3.org/2000/xmlns/";
    
    Locale ( )
    {
        _noSync = true;
        _tempFrames = new Cur [ _numTempFramesLeft = 8 ];
        _charUtil = CharUtil.getThreadLocalCharUtil();
    }

    public long version ( )
    {
        return _versionAll;
    }

    public QName makeQName ( String uri, String localPart )
    {
        assert localPart != null && localPart.length() > 0;
        // TODO - make sure name is a well formed name?

        return new QName( uri, localPart );
    }

    public QName makeQName ( String uri, String local, String prefix )
    {
        return new QName( uri, local, prefix == null ? "" : prefix );
    }

    QName makeQualifiedQName ( String uri, String qname )
    {
        if (qname == null)
            qname = "";

        int i = qname.indexOf( ':' );

        return i < 0
            ? new QName( uri, qname )
            : new QName( uri, qname.substring( i + 1 ), qname.substring( 0, i ) );
    }

    Cur permCur ( )
    {
        return getCur( Cur.PERM );
    }
    
    Cur weakCur ( Object o )
    {
        assert o != null && !(o instanceof Ref);
        
        Cur c = getCur( Cur.WEAK );
        
        assert c._obj == null;

        c._obj = new Ref( c, o );
        
        return c;
    }

    final ReferenceQueue refQueue ( )
    {
        if (_refQueue == null)
            _refQueue = new ReferenceQueue();

        return _refQueue;
    }

    final static class Ref extends PhantomReference
    {
        Ref ( Cur c, Object obj )
        {
            super( obj, c._locale.refQueue() );

            _cur = c;
        }

        final Cur _cur;
    }

    Cur tempCur ( )
    {
        Cur c = getCur( Cur.TEMP );

        if (c._tempFrame < 0)
        {
            assert _numTempFramesLeft < _tempFrames.length;
                
            int frame = _tempFrames.length - _numTempFramesLeft - 1;

            assert frame >= 0 && frame < _tempFrames.length;

            c._nextTemp = _tempFrames[ frame ];
            _tempFrames[ frame ] = c;
        
            c._tempFrame = frame;
        }
        
        return c;
    }

    private Cur getCur ( int curKind )
    {
        assert curKind == Cur.TEMP || curKind == Cur.PERM || curKind == Cur.WEAK;
        assert _curPool == null || _curPoolCount > 0;
        
        Cur c = _curPool;
        
        if (c == null)
        {
            c = new Cur( this );
            c._state = Cur.POOLED;
            c._tempFrame = -1;
            c._pos = -1;
        }
        else
        {
            _curPool = c.listRemove( _curPool );
            _curPoolCount--;
        }

        assert c._prev == null && c._next == null;
        assert c._xobj == null;
        assert c._pos == -1;
        assert c._obj == null;
                
        c._curKind = curKind;

        c._state = Cur.UNEMBEDDED;
        _unembedded = c.listInsert( _unembedded );

        return c;
    }

    TextNode createTextNode ( )
    {
        return _saaj == null ? new TextNode( this ) : new SaajTextNode( this );
    }

    CdataNode createCdataNode ( )
    {
        return _saaj == null ? new CdataNode( this ) : new SaajCdataNode( this );
    }

    boolean entered ( )
    {
        return _tempFrames.length - _numTempFramesLeft > 0;
    }

    void enter ( )
    {
        assert _numTempFramesLeft >= 0;
        
        if (--_numTempFramesLeft <= 0)
        {
            Cur[] newTempFrames = new Cur [ (_numTempFramesLeft = _tempFrames.length) * 2 ];
            System.arraycopy( _tempFrames, 0, newTempFrames, 0, _tempFrames.length );
            _tempFrames = newTempFrames;
        }
        
        if (++_entryCount > 1000)
        {
            _entryCount = 0;

            if (_refQueue != null)
            {
                for ( ; ; )
                {
                    Ref ref = (Ref) _refQueue.poll();

                    if (ref == null)
                        break;

                    ref._cur.release();
                }
            }
        }
    }
    
    void exit ( )
    {
        assert _numTempFramesLeft >= 0;

        int frame = _tempFrames.length - ++_numTempFramesLeft;

        Cur c = _tempFrames [ frame ];

        _tempFrames [ frame ] = null;
        
        while ( c != null )
        {
            assert c._tempFrame == frame;

            Cur next = c._nextTemp;

            c._nextTemp = null;
            c._tempFrame = -1;

            c.release();

            c = next;
        }
    }
    
    //
    //
    //

    boolean noSync ( )
    {
        return _noSync;
    }
    
    public static boolean beginsWithXml ( String name )
    {
        if (name.length() < 3)
            return false;

        char ch;

        if (((ch = name.charAt( 0 )) == 'x' || ch == 'X') &&
                ((ch = name.charAt( 1 )) == 'm' || ch == 'M') &&
                ((ch = name.charAt( 2 )) == 'l' || ch == 'L'))
        {
            return true;
        }

        return false;
    }

    //
    // Loading/parsing
    //

    static abstract class LoadContext
    {
        protected abstract void startElement ( QName name                             );
        protected abstract void endElement   (                                        );
        
        protected abstract void attr         ( String local, String uri, String prefix,
                                               String value );
        
        protected abstract void comment      ( char[] buff, int off, int cch          );
        protected abstract void procInst     ( String target, String value            );
        protected abstract void text         ( char[] buff, int off, int cch          );
        protected abstract Cur  finish       (                                        );
        
    }

    private static ThreadLocal tl_saxLoaders =
        new ThreadLocal ( ) { protected Object initialValue ( ) { return newSaxLoader(); } };

    private static SaxLoader getSaxLoader ( )
    {
        return (SaxLoader) tl_saxLoaders.get();
    }

    private static SaxLoader newSaxLoader ( )
    {
        SaxLoader sl = null;
        
        try
        {
            sl = PiccoloSaxLoader.newInstance();

            if (sl == null)
                sl = DefaultSaxLoader.newInstance();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Can't find an XML parser", e );
        }

        if (sl == null)
            throw new RuntimeException( "Can't find an XML parser" );
        
        return sl;
    }

    private static class DefaultSaxLoader extends SaxLoader
    {
        private DefaultSaxLoader ( XMLReader xr )
        {
            super( xr, null );
        }
        
        static SaxLoader newInstance ( ) throws Exception
        {
            return
                new DefaultSaxLoader(
                    SAXParserFactory.newInstance().newSAXParser().getXMLReader() );
        }
    }
    
    private static class PiccoloSaxLoader extends SaxLoader
    {
        // TODO - Need to look at root.java to bring this loader up to
        // date with all needed features

        private PiccoloSaxLoader (
            XMLReader xr, Locator startLocator, Method m_getEncoding, Method m_getVersion )
        {
            super( xr, startLocator );

            _m_getEncoding = m_getEncoding;
            _m_getVersion = m_getVersion;
        }

        static SaxLoader newInstance ( ) throws Exception
        {
            Class pc = null;
            
            try
            {
                pc = Class.forName( "com.bluecast.xml.Piccolo" );
            }
            catch ( ClassNotFoundException e )
            {
                return null;
            }
                
            XMLReader xr = (XMLReader) pc.newInstance();

            Method m_getEncoding     = pc.getMethod( "getEncoding", null );
            Method m_getVersion      = pc.getMethod( "getVersion", null );
            Method m_getStartLocator = pc.getMethod( "getStartLocator", null );

            Locator startLocator =
                (Locator) m_getStartLocator.invoke( xr, null );

            return new PiccoloSaxLoader( xr, startLocator, m_getEncoding, m_getVersion );
        }
        
        private Method _m_getEncoding;
        private Method _m_getVersion;
    }
    
    private static abstract class SaxLoader
            implements ContentHandler, LexicalHandler, ErrorHandler, EntityResolver
    {
        SaxLoader ( XMLReader xr, Locator startLocator )
        {
            _xr = xr;
            _startLocator = startLocator;
            
            try
            {
                _xr.setFeature( "http://xml.org/sax/features/namespace-prefixes", true );
                _xr.setFeature( "http://xml.org/sax/features/namespaces", true );
                _xr.setFeature( "http://xml.org/sax/features/validation", false );
                _xr.setProperty( "http://xml.org/sax/properties/lexical-handler", this );
                _xr.setContentHandler( this );
                _xr.setErrorHandler( this );
                
                EntityResolver entRes = ResolverUtil.getGlobalEntityResolver();
                
                if (entRes == null)
                    entRes = this;
                
                xr.setEntityResolver( entRes );
            }
            catch ( Throwable e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        public Cur load ( Locale l, InputSource is )
        {
            _locale = l;
            _context = new Cur.CurLoadContext( _locale );

            try
            {
                _xr.parse( is );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }

            return _context.finish();
        }

        public void setDocumentLocator ( Locator locator )
        {
            // TODO - hook up locator ...
        }

        public void startDocument ( ) throws SAXException
        {
            // Do nothing ... start of document is implicit
        }

        public void endDocument ( ) throws SAXException
        {
            // Do nothing ... end of document is implicit
        }

        public void startElement ( String uri, String local, String qName, Attributes atts )
            throws SAXException
        {
            if (local.length() == 0)
                local = qName;
            
            // Out current parser (Piccolo) does not error when a
            // namespace is used and not defined.  Check for these here

            if (qName.indexOf( ':' ) >= 0 && uri.length() == 0)
            {
                XmlError err =
                    XmlError.forMessage(
                        "Use of undefined namespace prefix: " +
                            qName.substring( 0, qName.indexOf( ':' ) ));

                throw new XmlRuntimeException( err.toString(), null, err );
            }

            _context.startElement( _locale.makeQualifiedQName( uri, qName ) );

            for ( int i = 0, len = atts.getLength() ; i < len ; i++ )
            {
                String aqn = atts.getQName( i );

                if (aqn.equals( "xmlns" ))
                {
                    _context.attr( "xmlns", _xmlnsUri, null, atts.getValue( i ) );
                }
                else if (aqn.startsWith( "xmlns:" ))
                {
                    String prefix = aqn.substring( 6 );

                    if (prefix.length() == 0)
                    {
                        XmlError err =
                            XmlError.forMessage( "Prefix not specified", XmlError.SEVERITY_ERROR );

                        throw new XmlRuntimeException( err.toString(), null, err );
                    }

                    String attrUri = atts.getValue( i );
                    
                    if (attrUri.length() == 0)
                    {
                        XmlError err =
                            XmlError.forMessage(
                                "Prefix can't be mapped to no namespace: " + prefix,
                                XmlError.SEVERITY_ERROR );

                        throw new XmlRuntimeException( err.toString(), null, err );
                    }

                    _context.attr( prefix, _xmlnsUri, "xmlns", attrUri );
                }
                else
                {
                    int colon = aqn.indexOf( ':' );

                    if (colon < 0)
                        _context.attr( aqn, atts.getURI( i ), null, atts.getValue( i ) );
                    else
                    {
                        _context.attr(
                            aqn.substring( colon + 1 ), atts.getURI( i ), aqn.substring( 0, colon ),
                            atts.getValue( i ) );
                    }
                }
            }
        }

        public void endElement ( String namespaceURI, String localName, String qName )
            throws SAXException
        {
            _context.endElement();
        }

        public void characters ( char ch[], int start, int length ) throws SAXException
        {
            _context.text( ch, start, length );
        }

        public void ignorableWhitespace ( char ch[], int start, int length ) throws SAXException
        {
        }

        public void comment ( char ch[], int start, int length ) throws SAXException
        {
            _context.comment( ch, start, length );
        }

        public void processingInstruction ( String target, String data ) throws SAXException
        {
            _context.procInst( target, data );
        }

        public void startDTD ( String name, String publicId, String systemId ) throws SAXException
        {
        }

        public void endDTD ( ) throws SAXException
        {
        }

        public void startPrefixMapping ( String prefix, String uri ) throws SAXException
        {
            if (beginsWithXml( prefix ) && ! ( "xml".equals( prefix ) && _xml1998Uri.equals( uri )))
            {
                XmlError err =
                    XmlError.forMessage(
                        "Prefix can't begin with XML: " + prefix, XmlError.SEVERITY_ERROR );

                throw new XmlRuntimeException( err.toString(), null, err );
            }
        }

        public void endPrefixMapping ( String prefix ) throws SAXException
        {
        }

        public void skippedEntity ( String name ) throws SAXException
        {
//            throw new RuntimeException( "Not impl: skippedEntity" );
        }

        public void startCDATA ( ) throws SAXException
        {
        }

        public void endCDATA ( ) throws SAXException
        {
        }

        public void startEntity ( String name ) throws SAXException
        {
//            throw new RuntimeException( "Not impl: startEntity" );
        }

        public void endEntity ( String name ) throws SAXException
        {
//            throw new RuntimeException( "Not impl: endEntity" );
        }

        public void fatalError ( SAXParseException e ) throws SAXException
        {
            throw e;
        }

        public void error ( SAXParseException e ) throws SAXException
        {
            throw e;
        }

        public void warning ( SAXParseException e ) throws SAXException
        {
            throw e;
        }

        public InputSource resolveEntity ( String publicId, String systemId )
        {
            return new InputSource( new StringReader( "" ) );
        }

        private Locale      _locale;
        private XMLReader   _xr;
        private LoadContext _context;
        private Locator     _startLocator;
    }

    private Dom load ( InputSource is )
    {
        return getSaxLoader().load( this, is ).getDom();
    }

    public Dom load ( Reader r )
    {
        return load( new InputSource( r ) );
    }

    public Dom load ( String s )
    {
        return load( new InputSource( new StringReader( s ) ) );
    }

    public Dom load ( InputStream in )
    {
        return load( new InputSource( in ) );
    }

    //
    // DOMImplementation methods
    //

    public Document createDocument ( String uri, String qname, DocumentType doctype )
    {
        return DomImpl._domImplementation_createDocument( this, uri, qname, doctype );
    }

    public DocumentType createDocumentType ( String qname, String publicId, String systemId )
    {
        throw new RuntimeException( "Not implemented" );
//        return DomImpl._domImplementation_createDocumentType( this, qname, publicId, systemId );
    }

    public boolean hasFeature ( String feature, String version )
    {
        throw new RuntimeException( "Not implemented" );
//        return DomImpl._domImplementation_hasFeature( this, feature, version );
    }

    //
    // SaajCallback methods
    //

    public void setSaajData ( Node n, Object o )
    {
        assert n instanceof Dom;

        DomImpl.saajCallback_setSaajData( (Dom) n, o );
    }

    public Object getSaajData ( Node n )
    {
        assert n instanceof Dom;

        return DomImpl.saajCallback_getSaajData( (Dom) n );
    }

    public Element createSoapElement ( QName name, QName parentName )
    {
        assert _ownerDoc != null;

        return DomImpl.saajCallback_createSoapElement( _ownerDoc, name, parentName );
    }

    public Element importSoapElement ( Document doc, Element elem, boolean deep, QName parentName )
    {
        assert doc instanceof Dom;

        return DomImpl.saajCallback_importSoapElement( (Dom) doc, elem, deep, parentName );
    }

    //
    //
    //

    boolean _noSync;

    private ReferenceQueue _refQueue;
    private int            _entryCount;
   
    private int   _numTempFramesLeft;
    private Cur[] _tempFrames;

    Cur _curPool;
    int _curPoolCount;

    Cur _unembedded;
    
    long _versionAll;
    long _versionSansText;
    
    CharUtil _charUtil;
    
    static Saaj _saaj;
    
    Dom _ownerDoc;
}