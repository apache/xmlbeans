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

import java.util.HashMap;

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

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameCache;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.XmlDocumentProperties;

import javax.xml.namespace.QName;

final class Locale implements DOMImplementation, SaajCallback
{
    static final int ROOT     = Cur.ROOT;
    static final int ELEM     = Cur.ELEM;
    static final int ATTR     = Cur.ATTR;
    static final int COMMENT  = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;
    static final int TEXT     = Cur.TEXT;

    static final String _xsi         = "http://www.w3.org/2001/XMLSchema-instance";
    static final String _schema      = "http://www.w3.org/2001/XMLSchema";
    static final String _openFragUri = "http://www.openuri.org/fragment";
    static final String _xml1998Uri  = "http://www.w3.org/XML/1998/namespace";
    static final String _xmlnsUri    = "http://www.w3.org/2000/xmlns/";
    
    static final QName _xsiNil          = new QName( _xsi, "nil" );
    static final QName _xsiType         = new QName( _xsi, "type" );
    static final QName _openuriFragment = new QName( _openFragUri, "fragment" );
    static final QName _xmlFragment     = new QName( "xml-fragment" );

    // TODO (ericvas ) - have a qname factory here so that the same factory may be
    // used by the parser.  This factory would probably come from my
    // high speed parser.  Otherwise, use a thread local one
    
    QName makeQName ( String uri, String localPart )
    {
        assert localPart != null && localPart.length() > 0;
        // TODO - make sure name is a well formed name?

        return _qnameFactory.getQName( uri, localPart );
    }

    QName makeQName ( String uri, String local, String prefix )
    {
        return _qnameFactory.getQName( uri, local, prefix == null ? "" : prefix );
    }

    QName makeQualifiedQName ( String uri, String qname )
    {
        if (qname == null)
            qname = "";

        int i = qname.indexOf( ':' );

        return i < 0
            ? _qnameFactory.getQName( uri, qname )
            : _qnameFactory.getQName( uri, qname.substring( i + 1 ), qname.substring( 0, i ) );
    }

    static private class DocProps extends XmlDocumentProperties
    {
        private HashMap _map = new HashMap();

        public Object put    ( Object key, Object value ) { return _map.put( key, value ); }
        public Object get    ( Object key )               { return _map.get( key ); }
        public Object remove ( Object key )               { return _map.remove( key ); }
    }

    static XmlDocumentProperties getDocProps ( Cur c, boolean ensure )
    {
        c.push();

        while ( c.toParent() )
            ;

        DocProps props = (DocProps) c.getBookmark( DocProps.class );

        if (props == null)
            c.setBookmark( DocProps.class, props = new DocProps() );

        c.pop();

        return props;
    }

    static boolean pushToContainer ( Cur c )
    {
        if (c.isContainer())
            return true;

        c.push();

        boolean move = false;

        if (c.isAttr())
        {
            c.toParent();
            c.next();
        }

        loop:
        for ( ; ; )
        {
            assert c.isContainer() || c.isFinish() || c.isComment() || c.isProcinst() || c.isText();
            
            switch ( c.kind() )
            {
            case ROOT :
            case ELEM :
                move = true;
                break loop;

            case - ROOT :
            case - ELEM :
                break loop;

            case COMMENT :
            case PROCINST :
                c.toEnd();
                // Fall thru

            default :
                c.next();
                break;
            }
        }

        if (move)
            return true;

        c.pop();

        return false;
    }

    static boolean toChild ( Cur c, String uri, String local, int i )
    {
        return toChild( c, c._locale.makeQName( uri, local ), i );
    }
    
    static boolean toChild ( Cur c, QName name, int i )
    {
//        if (!c.pushToContainer())
//            return false;
//
        throw new RuntimeException( "Not implemented" );
//        c.pop....
    }

//    private final class NthChildCache
//    {
//        private boolean namesSame ( QName pattern, QName name )
//        {
//            return pattern == null || pattern.equals( name );
//        }
//
//        private boolean setsSame ( QNameSet patternSet, QNameSet set)
//        {
//            // value equality is probably too expensive. Since the use case
//            // involves QNameSets that are generated by the compiler, we
//            // can use identity comparison.
//            return patternSet != null && patternSet == set;
//        }
//
//        private boolean nameHit(QName namePattern,  QNameSet setPattern, QName name)
//        {
//            if (setPattern == null)
//                return namesSame(namePattern, name);
//            else
//                return setPattern.contains(name);
//        }
//
//        private boolean cacheSame (QName namePattern,  QNameSet setPattern)
//        {
//            return setPattern == null ? namesSame(namePattern, _name) :
//                setsSame(setPattern, _set);
//        }
//
//        int distance ( Splay parent, QName name, QNameSet set, int n )
//        {
//            assert n >= 0;
//
//            if (_version != Root.this.getVersion())
//                return Integer.MAX_VALUE - 1;
//
//            if (parent != _parent || !cacheSame(name, set))
//                return Integer.MAX_VALUE;
//
//            return n > _n ? n - _n : _n - n;
//        }
//
//        Begin fetch ( Splay parent, QName name, QNameSet set, int n )
//        {
//            assert n >= 0;
//
//            if (_version != Root.this.getVersion() || _parent != parent ||
//                  ! cacheSame(name, set) || n == 0)
//            {
//                _version = Root.this.getVersion();
//                _parent = parent;
//                _name = name;
//                _child = null;
//                _n = -1;
//
//                if (!parent.isLeaf())
//                {
//                    loop:
//                    for ( Splay s = parent.nextSplay() ; ; s = s.nextSplay() )
//                    {
//                        switch ( s.getKind() )
//                        {
//                        case END  :
//                        case ROOT : break loop;
//
//                        case BEGIN :
//                            if (nameHit( name, set, s.getName() ))
//                            {
//                                _child = s;
//                                _n = 0;
//                                break loop;
//                            }
//
//                            s = s.getFinishSplay();
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (_n < 0)
//                return null;
//
//            if (n > _n)
//            {
//                while ( n > _n )
//                {
//                    for ( Splay s = _child.getFinishSplay().nextSplay() ; ;
//                          s = s.nextSplay() )
//                    {
//                        if (s.isFinish())
//                            return null;
//
//                        if (s.isBegin())
//                        {
//                            if (nameHit( name, set, s.getName() ))
//                            {
//                                _child = s;
//                                _n++;
//                                break;
//                            }
//
//                            s = s.getFinishSplay();
//                        }
//                    }
//                }
//            }
//            else if (n < _n)
//            {
//                while ( n < _n )
//                {
//                    Splay s = _child;
//
//                    for ( ; ; )
//                    {
//                        s = s.prevSplay();
//
//                        if (s.isLeaf() || s.isEnd())
//                        {
//                            if (s.isEnd())
//                                s = s.getContainer();
//
//                            if (nameHit( name, set, s.getName() ))
//                            {
//                                _child = s;
//                                _n--;
//                                break;
//                            }
//                        }
//                        else if (s.isContainer())
//                            return null;
//                    }
//                }
//            }
//
//            return (Begin) _child;
//        }
//
//        private long     _version;
//        private Splay    _parent;
//        private QName    _name;
//        private QNameSet _set;
//        
//        private Splay _child;
//        private int   _n;
//    }
    
    //
    // 
    //

    Locale ( )
    {
        _noSync = true;
        _tempFrames = new Cur [ _numTempFramesLeft = 8 ];
        _charUtil = CharUtil.getThreadLocalCharUtil();
        _qnameFactory = new DefaultQNameFactory();
    }

    long version ( )
    {
        return _versionAll;
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
        
        Cur c;
        
        if (_curPool == null)
        {
            c = new Cur( this );
            c._state = Cur.POOLED;
            c._tempFrame = -1;
        }
        else
        {
            c = _curPool;
            _curPool = c.listRemove( _curPool );
            _curPoolCount--;
        }

        assert c._prev == null && c._next == null;
        assert !c.isPositioned();
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
    
    static final boolean isWhiteSpace ( char ch )
    {
        switch ( ch )
        {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                return true;
            default:
                return false;
        }
    }

    static final boolean isWhiteSpace ( String s )
    {
        int l = s.length();

        while ( l-- > 0)
            if (!isWhiteSpace( s.charAt( l )))
                  return false;

        return true;
    }

    static final boolean isWhiteSpace ( StringBuffer sb )
    {
        int l = sb.length();

        while ( l-- > 0)
            if (!isWhiteSpace( sb.charAt( l )))
                  return false;

        return true;
    }

    static boolean beginsWithXml ( String name )
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

    static boolean isXmlns ( QName name )
    {
        String prefix = name.getPrefix();

        if (prefix.equals( "xmlns" ))
            return true;

        return prefix.length() == 0 && name.getLocalPart().equals( "xmlns" );
    }

    QName createXmlns ( String prefix )
    {
        if (prefix == null)
            prefix = "";
        
        return
            prefix.length() == 0
                ? makeQName( _xmlnsUri, "xmlns", "" )
                : makeQName( _xmlnsUri, prefix, "xmlns" );
    }
    
    static String xmlnsPrefix ( QName name )
    {
        return name.getPrefix().equals( "xmlns" ) ? name.getLocalPart() : "";
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

    private static ThreadLocal tl_saxLoadersDefaultResolver =
        new ThreadLocal ( ) { protected Object initialValue ( ) { return newSaxLoader(); } };

    private static ThreadLocal tl_saxLoaders =
        new ThreadLocal ( ) { protected Object initialValue ( ) { return newSaxLoader(); } };
    
    private static SaxLoader getSaxLoader ( XmlOptions options )
    {
        //
        // XMLReader.setEntityResolver() cannot be passed null.
        // Because of this, I cannot reset the entity resolver to be
        // that which was default.  Thus, I need to cache two
        // SaxLoaders.  One which uses the default entity resolver and
        // one which I can change the resolve to whatever I want for a
        // given parse.
        //

        options = XmlOptions.maskNull( options );

        EntityResolver er = (EntityResolver) options.get( XmlOptions.ENTITY_RESOLVER );

        if (er == null)
            er = ResolverUtil.getGlobalEntityResolver();

        if (er == null && options.hasOption( XmlOptions.LOAD_USE_DEFAULT_RESOLVER ))
            return (SaxLoader) tl_saxLoadersDefaultResolver.get();

        SaxLoader sl = (SaxLoader) tl_saxLoaders.get();

        if (er == null)
            er = sl;

        sl.setEntityResolver( er );

        return sl;
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
            }
            catch ( Throwable e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        void setEntityResolver ( EntityResolver er )
        {
            _xr.setEntityResolver( er );
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

    private Dom load ( InputSource is, XmlOptions options )
    {
        return getSaxLoader( options ).load( this, is ).getDom();
    }

    public Dom load ( Reader r )
    {
        return load( new InputSource( r ), null );
    }

    public Dom load ( String s )
    {
        return load( new InputSource( new StringReader( s ) ), null );
    }

    public Dom load ( InputStream in )
    {
        return load( in, null );
    }

    public Dom load ( InputStream in, XmlOptions options )
    {
        return load( new InputSource( in ), options );
    }

    public Dom load ( String s, XmlOptions options )
    {
        return load( new InputSource( new StringReader( s ) ), options );
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
        return DomImpl._domImplementation_hasFeature( this, feature, version );
    }

    public Object getFeature ( String feature, String version )
    {
        throw new RuntimeException( "DOM Level 3 Not implemented" );
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

    
    private static final class DefaultQNameFactory implements QNameFactory
    {
        private QNameCache _cache = XmlBeans.getQNameCache();
        
        public QName getQName ( String uri, String local )
        {
            return _cache.getName( uri, local, "" );
        }

        public QName getQName ( String uri, String local, String prefix )
        {
            return _cache.getName( uri, local, prefix );
        }

        public QName getQName (
            char[] uriSrc,   int uriPos,   int uriCch,
            char[] localSrc, int localPos, int localCch )
        {
            return
                _cache.getName(
                    new String( uriSrc, uriPos, uriCch ),
                    new String( localSrc, localPos, localCch ),
                    "" );
        }

        public QName getQName (
            char[] uriSrc,    int uriPos,    int uriCch,
            char[] localSrc,  int localPos,  int localCch,
            char[] prefixSrc, int prefixPos, int prefixCch )
        {
            return
                _cache.getName(
                    new String( uriSrc, uriPos, uriCch ),
                    new String( localSrc, localPos, localCch ),
                    new String( prefixSrc, prefixPos, prefixCch ) );
        }
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
    
    Saaj _saaj;
    
    Dom _ownerDoc;

    QNameFactory _qnameFactory;

    boolean _validateOnSet;
}