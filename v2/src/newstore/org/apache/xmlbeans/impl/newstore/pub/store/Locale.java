package org.apache.xmlbeans.impl.newstore.pub.store;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.lang.ref.PhantomReference;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

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

import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.namespace.QName;

import org.w3c.dom.DOMImplementation;

import org.apache.xmlbeans.impl.newstore.SaajImpl;

import org.apache.xmlbeans.impl.newstore.Saaj;
import org.apache.xmlbeans.impl.newstore.Saaj.SaajCallback;

import org.apache.xmlbeans.impl.newstore.pub.store.Dom.TextNode;
import org.apache.xmlbeans.impl.newstore.pub.store.Dom.CdataNode;

import org.apache.xmlbeans.impl.newstore.DomImpl;
import org.apache.xmlbeans.impl.newstore.DomImpl.SaajTextNode;
import org.apache.xmlbeans.impl.newstore.DomImpl.SaajCdataNode;

public abstract class Locale implements DOMImplementation, SaajCallback
{
    public Locale ( )
    {
        _noSync = true;
        _tempFrames = new Cur [ 4 ];
    }

    protected abstract Cur         newCur         ( );
    protected abstract LoadContext newLoadContext ( );

    public static abstract class LoadContext
    {
        protected abstract void startElement ( QName name                             );
        protected abstract void endElement   (                                        );
        protected abstract void xmlns        ( String prefix, String uri              );
        protected abstract void attr         ( String local, String uri, String value );
        protected abstract void comment      ( char[] buff, int off, int cch          );
        protected abstract void procInst     ( String target, String value            );
        protected abstract void text         ( char[] buff, int off, int cch          );
        protected abstract Cur  finish       (                                        );
    }

    public QName makeQName ( String uri, String localPart )
    {
        assert localPart != null && localPart.length() > 0;
        // TODO - make sure name is a well formed name?

        return new QName( uri, localPart );
    }

    public QName makeQName ( String uri, String local, String prefix )
    {
        return new QName( uri, local, prefix );
    }

    public QName makeQualifiedQName ( String uri, String qname )
    {
        assert qname != null && qname.length() > 0;

        int i = qname.indexOf( ':' );

        return i < 0
            ? new QName( uri, qname )
            : new QName( uri, qname.substring( i + 1 ), qname.substring( 0, i ) );
    }

    public final boolean noSync ( )
    {
        return _noSync;
    }

    public final void enter ( )
    {
        _numTempFrames++;

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

    public final void exit ( )
    {
        assert _numTempFrames > 0;

        _numTempFrames--;

        if (_numTempFrames < _tempFrames.length)
        {

            Cur c = _tempFrames[ _numTempFrames ];
            _tempFrames[ _numTempFrames ] = null;

            while ( c != null )
            {
                assert c._tempCurFrame == _numTempFrames;

                Cur next = c._nextTemp;

                c._nextTemp = null;
                c._tempCurFrame = -1;

                c.release();

                c = next;
            }
        }
    }

    public final long version ( )
    {
        return _versionAll;
    }

    public final Cur permCur ( )
    {
        return getCur( null, Cur.PERM );
    }

    public final Cur tempCur ( )
    {
        return addTempCur( getCur( null, Cur.TEMP ) );
    }

    public final Cur weakCur ( Object o )
    {
        assert o != null && !(o instanceof Ref);
        return getCur( o, Cur.WEAK );
    }

    final static class Ref extends PhantomReference
    {
        Ref ( Cur c, Object obj )
        {
            super( obj, c._locale().refQueue() );

            _cur = c;
        }

        final Cur _cur;
    }

    final ReferenceQueue refQueue ( )
    {
        if (_refQueue == null)
            _refQueue = new ReferenceQueue();

        return _refQueue;
    }

    private final Cur getCur ( Object obj, int curKind )
    {
        if (_pool == null)
        {
            Cur c = newCur();
            _pool = c.listInsert( _pool, Cur.POOLED );

            assert _poolCount == 0;
            _poolCount++;
        }

        Cur c = _pool;

        _pool = c.listRemove( _pool );

        _poolCount--;
        assert _poolCount >= 0;

        _unembedded = c.listInsert( _unembedded, Cur.UNEMBEDDED );

        assert c._obj == null;

        if (obj != null)
            c._obj = new Ref( c, obj );

        c._curKind = curKind;

        return c;
    }

    private final Cur addTempCur ( Cur c )
    {
        int frame = _numTempFrames - 1;

//        assert c != null && frame >= 0;
//        assert c._tempCurFrame == -1 || c._tempCurFrame == frame;

        if (c._tempCurFrame < 0)
        {
            if (_numTempFrames >= _tempFrames.length)
            {
                Cur[] newTempFrames = new Cur [ _tempFrames.length * 2 ];
                System.arraycopy( _tempFrames, 0, newTempFrames, 0, _tempFrames.length );
                _tempFrames = newTempFrames;
            }

            c._nextTemp = _tempFrames[ frame ];
            c._tempCurFrame = frame;
            _tempFrames[ frame ] = c;
        }

        return c;
    }

    public TextNode createTextNode ( )
    {
        return _saaj == null ? new TextNode( this ) : new SaajTextNode( this );
    }

    public CdataNode createCdataNode ( )
    {
        return _saaj == null ? new CdataNode( this ) : new SaajCdataNode( this );
    }

    //
    // Loading/parsing
    //

    private static class SaxLoader
            implements ContentHandler, LexicalHandler, ErrorHandler, EntityResolver
    {
        SaxLoader ( )
        {
            try
            {
                _xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

                _xr.setFeature( "http://xml.org/sax/features/namespace-prefixes", true );
                _xr.setFeature( "http://xml.org/sax/features/namespaces", true );
                _xr.setFeature( "http://xml.org/sax/features/validation", false );


                _xr.setProperty( "http://xml.org/sax/properties/lexical-handler", this );
                _xr.setContentHandler( this );
                _xr.setErrorHandler( this );
                _xr.setEntityResolver( this );
            }
            catch ( Throwable e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        public Cur load ( Locale l, InputSource is )
        {
            _locale = l;
            _context = l.newLoadContext();

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
            _context.startElement( _locale.makeQualifiedQName( uri, qName ) );

            for ( int i = 0, len = atts.getLength() ; i < len ; i++ )
            {
                String aqn = atts.getQName( i );

                if (aqn.equals( "xmlns" ))
                    _context.xmlns( "", atts.getValue( i ) );
                else if (aqn.startsWith( "xmlns:" ))
                    _context.xmlns( aqn.substring( 6 ), atts.getValue( i ) );
                else
                {
                    String attrLocal = atts.getLocalName( i );

                    if (attrLocal.length() == 0)
                        attrLocal = aqn;

                    _context.attr( attrLocal, atts.getURI( i ), atts.getValue( i ) );
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
    }

    private Dom load ( InputSource is )
    {
        return new SaxLoader().load( this, is ).getDom();
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
        return DomImpl._domImplementation_createDocumentType( this, qname, publicId, systemId );
    }

    public boolean hasFeature ( String feature, String version )
    {
        return DomImpl._domImplementation_hasFeature( this, feature, version );
    }

    //
    // SaajCallback methods
    //

    public void setSaajData ( Node n, Object o )
    {
        assert n instanceof Dom;

        SaajImpl.saajCallback_setSaajData( (Dom) n, o );
    }

    public Object getSaajData ( Node n )
    {
        assert n instanceof Dom;

        return SaajImpl.saajCallback_getSaajData( (Dom) n );
    }

    public Element createSoapElement ( QName name, QName parentName )
    {
        assert _ownerDoc != null;

        return SaajImpl.saajCallback_createSoapElement( _ownerDoc, name, parentName );
    }

    public Element importSoapElement ( Document doc, Element elem, boolean deep, QName parentName )
    {
        assert doc instanceof Dom;

        return SaajImpl.saajCallback_importSoapElement( (Dom) doc, elem, deep, parentName );
    }

    //
    //
    //
    
    private ReferenceQueue _refQueue;

    Cur _pool;
    int _poolCount;

    public long _versionAll;
    public long _versionSansText;

    public Cur _unembedded;

    private boolean _noSync;

    private int   _entryCount;
    private Cur[] _tempFrames;
    private int   _numTempFrames;

    public Dom _ownerDoc;
    public static Saaj _saaj;
}
