package org.apache.xmlbeans.impl.newstore.xcur;

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

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.newstore.DomImpl;
import org.apache.xmlbeans.impl.newstore.DomImpl.Dom;

import org.apache.xmlbeans.impl.newstore.SaajImpl;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.lang.ref.PhantomReference;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Document;

import javax.xml.parsers.SAXParserFactory;

import org.apache.xmlbeans.impl.newstore.Saaj;
import org.apache.xmlbeans.impl.newstore.Saaj.SaajCallback;

public abstract class Master implements DOMImplementation, SaajCallback
{
    protected abstract Xcur        newCur         ( );
    protected abstract LoadContext newLoadContext ( );

    protected static abstract class LoadContext
    {
        protected abstract void startElement ( QName name                             );
        protected abstract void endElement   (                                        );
        protected abstract void xmlns        ( String prefix, String uri              );
        protected abstract void attr         ( String local, String uri, String value );
        protected abstract void comment      ( char[] buff, int off, int cch          );
        protected abstract void procInst     ( String target, String value            );
        protected abstract void text         ( char[] buff, int off, int cch          );
        protected abstract Xcur finish       (                                        );
    }

    //
    //
    //
    
    Master ( )
    {
        _noSync = true;
        _tempFrames = new Xcur [ 4 ];
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

    public static void copyChars ( Object src, int off, int cch, char[] buf, int bufStart )
    {
        assert src == null || src instanceof String || src instanceof char[];

        if (src != null && cch > 0)
        {
            if (src instanceof String)
                ((String) src).getChars( off, off + cch, buf, bufStart );
            else
                System.arraycopy( (char[]) src, off, buf, bufStart, cch );
        }
    }

    public Object insertChars (
        int i, Object src, int off, int cch, Object srcInsert, int offInsert, int cchInsert )
    {
        assert i >= 0 && i <= cch;
        
        // TODO - this is cheap and dirty impl ... make better

        String s = makeString( src, off, cch );
        String sInsert = makeString( srcInsert, offInsert, cchInsert );

        _offSrc = 0;
        _cchSrc = cch + cchInsert;

        return s.substring( 0, i ) + sInsert + s.substring( i );
    }
    
    public static String makeString ( Object src, int off, int cch )
    {
        assert src == null || src instanceof String || src instanceof char[];

        if (src == null || cch == 0)
            return "";

        if (src instanceof String)
        {
            String s = (String) src;

            if (off == 0 && cch == s.length())
                return s;

            return s.substring( off, off + cch );
        }

        return new String( (char[]) src, off, cch );
    }

    public final long version ( )
    {
        return _versionAll;
    }

    public final Xcur permCur ( )
    {
        return getCur( null, Xcur.PERM );
    }
    
    public final Xcur tempCur ( )
    {
        return addTempCur( getCur( null, Xcur.TEMP ) );
    }
    
    public final Xcur weakCur ( Object o )
    {
        assert o != null && !(o instanceof Ref);
        return getCur( o, Xcur.WEAK );
    }

    private final Xcur getCur ( Object obj, int curKind )
    {
        if (_pool == null)
        {
            Xcur x = newCur();
            _pool = x.listInsert( _pool, Xcur.POOLED );

            assert _poolCount == 0;
            _poolCount++;
        }

        Xcur x = _pool;

        _pool = x.listRemove( _pool );

        _poolCount--;
        assert _poolCount >= 0;

        _unembedded = x.listInsert( _unembedded, Xcur.UNEMBEDDED );

        assert x._obj == null;

        if (obj != null)
            x._obj = new Ref( x, obj );

        x._curKind = curKind;

        return x;
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

                    ref._xcur.release();
                }
            }
        }
    }

    public final void exit ( )
    {
        assert _numTempFrames > 0;

        _numTempFrames--;
        
        Xcur x = _tempFrames[ _numTempFrames ];
        _tempFrames[ _numTempFrames ] = null;

        while ( x != null )
        {
            assert x._tempPtrFrame == _numTempFrames;
            
            Xcur next = x._nextTemp;
            
            x._nextTemp = null;
            x._tempPtrFrame = -1;
            
            x.release();

            x = next;
        }
    }

    private final Xcur addTempCur ( Xcur x )
    {
        int frame = _numTempFrames - 1;

        assert x != null && frame >= 0;
        assert x._tempPtrFrame == -1 || x._tempPtrFrame == frame;
        
        if (x._tempPtrFrame < 0)
        {
            if (_numTempFrames >= _tempFrames.length)
            {
                Xcur[] newTempFrames = new Xcur [ _tempFrames.length * 2 ];
                System.arraycopy( _tempFrames, 0, newTempFrames, 0, _tempFrames.length );
                _tempFrames = newTempFrames;
            }

            x._nextTemp = _tempFrames[ frame ];
            x._tempPtrFrame = frame;
            _tempFrames[ frame ] = x;
        }

        return x;
    }

    public final boolean noSync ( )
    {
        return _noSync;
    }

    final ReferenceQueue refQueue ( )
    {
        if (_refQueue == null)
            _refQueue = new ReferenceQueue();

        return _refQueue;
    }

    final static class Ref extends PhantomReference
    {
        Ref ( Xcur xcur, Object obj )
        {
            super( obj, xcur._master().refQueue() );

            _xcur = xcur;
        }

        final Xcur _xcur;
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

        public Xcur load ( Master m, InputSource is )
        {
            _master = m;
            _context = m.newLoadContext();
            
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
            _context.startElement( _master.makeQualifiedQName( uri, qName ) );

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

        private Master      _master;
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
    //
    //

    private ReferenceQueue _refQueue;
    
    Xcur _pool;
    int  _poolCount;

    long _versionAll;
    long _versionSansText;

    Xcur _unembedded;
    
    private boolean _noSync;
    
    private int    _entryCount;
    private Xcur[] _tempFrames;
    private int    _numTempFrames;

    public Dom _ownerDoc;
    public static Saaj _saaj;
    
    public int _offSrc;
    public int _cchSrc;
}