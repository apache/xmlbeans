package org.apache.xmlbeans.impl.newstore;

import java.io.Reader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ConcurrentModificationException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;

import org.apache.xmlbeans.impl.newstore.xcur.Xcur;
import org.apache.xmlbeans.impl.newstore.xcur.Master;

public class Jsr173
{
    public static XMLStreamReader newXmlStreamReader ( Xcur x, Object src, int off, int cch )
    {
        assert src == null || src instanceof String || src instanceof char[];

        XMLStreamReader xs = new XMLStreamReaderForString( x, src, off, cch );
        
        if (x.master().noSync())
            return new UnsyncedJsr173( x.master(), xs );
        else
            return new SyncedJsr173( x.master(), xs );
    }
    
    public static XMLStreamReader newXmlStreamReader ( Xcur x )
    {
        XMLStreamReader xs;

        int k = x.kind();
        
        if (k == Xcur.TEXT)
            xs = new XMLStreamReaderForString( x, x.getChars( -1 ), x._offSrc, x._cchSrc );
        else
            xs = new XMLStreamReaderForNode( x );
        
        if (x.master().noSync())
            return new UnsyncedJsr173( x.master(), xs );
        else
            return new SyncedJsr173( x.master(), xs );
    }
    
    //
    //
    //

    //
    //
    //
    
    private static final class XMLStreamReaderForNode extends XMLStreamReaderBase
    {
        public XMLStreamReaderForNode ( Xcur x )
        {
            super( x );

            assert x.type() != Xcur.TEXT;

            _xcur = x.weakCur( this );
            _xlast = x.weakCur( this );
            
            if (_xcur.isContainer())
                _xlast.toEnd();
        }

        protected Xcur getCur ( )
        {
            return _xcur;
        }

        //
        //
        //

        public boolean hasNext ( ) throws XMLStreamException
        {
            checkChanged();

            return !_xcur.isSamePosition( _xlast );
        }

        public int getEventType ( )
        {
            switch ( _xcur.type() )
            {
                case  Xcur.ROOT : return START_DOCUMENT;
                case -Xcur.ROOT : return END_DOCUMENT;
                case  Xcur.ELEM : return START_ELEMENT;
                case -Xcur.ELEM : return END_ELEMENT;
                case  Xcur.ATTR : return _xcur.kind() == Xcur.XMLNS ? NAMESPACE : ATTRIBUTE;
                case  Xcur.TEXT : return CHARACTERS;
                case  Xcur.LEAF : return _xcur.kind() == Xcur.COMMENT ? COMMENT : PROCESSING_INSTRUCTION;
                default         : throw new IllegalStateException();
            }
        }

        public int next ( ) throws XMLStreamException
        {
            checkChanged();

            if (!hasNext())
                throw new IllegalStateException();

            if (_xcur.isLeaf())
                _xcur.toEnd();

            _xcur.next();

            _textFetched = false;
            _srcFetched = false;
            
            return getEventType();
        }

        public String getText ( )
        {
            checkChanged();

            int k = _xcur.kind();

            if (k == Xcur.COMMENT)
                return _xcur.getValueString();

            if (k == Xcur.TEXT)
                return _xcur.getString( -1 );

            throw new IllegalStateException();
        }

        public boolean isStartElement ( )
        {
            return getEventType() == START_ELEMENT;
        }

        public boolean isEndElement ( )
        {
            return getEventType() == END_ELEMENT;
        }

        public boolean isCharacters ( )
        {
            return getEventType() == CHARACTERS;
        }

        public String getElementText ( ) throws XMLStreamException
        {
            checkChanged();

            if (!isStartElement())
                throw new IllegalStateException();

            StringBuffer sb = new StringBuffer();

            for ( int depth = 1 ; depth > 0 ; )
            {
                if (!hasNext())
                    throw new XMLStreamException();

                int e = next();

                if (e == END_ELEMENT)
                {
                    depth--;
                    continue;
                }

                if (e == START_ELEMENT)
                    depth++;

                if (e != CHARACTERS)
                    throw new XMLStreamException();

                sb.append( getText() );
            }

            return sb.toString();
        }

        public int nextTag ( ) throws XMLStreamException
        {
            checkChanged();

            for ( ; ; )
            {
                if (isStartElement() || isEndElement())
                    return getEventType();

                if (!isWhiteSpace())
                    throw new XMLStreamException();

                if (!hasNext())
                    throw new XMLStreamException();

                next();
            }
        }

        private static boolean matchAttr ( Xcur x, String uri, String local )
        {
            assert x.kind() == Xcur.ATTR;

            QName name = x.getName();

            return
                name.getLocalPart().equals( local ) &&
                    (uri == null || name.getNamespaceURI().equals( uri ));
        }

        private static Xcur toAttr ( Xcur x, String uri, String local )
        {
            if (uri == null || local == null || local.length() == 0)
                throw new IllegalArgumentException();

            Xcur xa = x.tempCur();
            boolean match = false;

            if (x.isContainer())
            {
                if (xa.toFirstAttr())
                {
                    do
                    {
                        if (xa.kind() == Xcur.ATTR && matchAttr( xa, uri, local ))
                        {
                            match = true;
                            break;
                        }
                    }
                    while ( xa.toNextSibling() );
                }
            }
            else if (x.kind() == Xcur.ATTR)
                match = matchAttr( x, uri, local );
            else
                throw new IllegalStateException();

            if (!match)
            {
                xa.release();
                xa = null;
            }

            return xa;
        }
        
        public String getAttributeValue ( String uri, String local )
        {
            Xcur xa = toAttr( _xcur, uri, local );

            String value = null;

            if (xa != null)
            {
                value = xa.getValueString();
                xa.release();
            }

            return value;
        }

        private static Xcur toAttr ( Xcur x, int i )
        {
            if (i < 0)
                throw new IndexOutOfBoundsException( "Attribute index is negative" );

            Xcur xa = x.tempCur();
            boolean match = false;

            if (x.isContainer())
            {
                if (xa.toFirstAttr())
                {
                    do
                    {
                        if (xa.kind() == Xcur.ATTR && i-- == 0)
                        {
                            match = true;
                            break;
                        }
                    }
                    while ( xa.toNextSibling() );
                }
            }
            else if (x.kind() == Xcur.ATTR)
                match = i == 0;
            else
                throw new IllegalStateException();

            if (!match)
            {
                xa.release();
                throw new IndexOutOfBoundsException( "Attribute index is too large" );
            }

            return xa;
        }

        public int getAttributeCount ( )
        {
            int n = 0;
            
            if (_xcur.isContainer())
            {
                Xcur xa = _xcur.tempCur();
                
                if (xa.toFirstAttr())
                {
                    do
                    {
                        if (xa.kind() == Xcur.ATTR)
                            n++;
                    }
                    while ( xa.toNextSibling() );
                }

                xa.release();
            }
            else if (_xcur.kind() == Xcur.ATTR)
                n++;
            else
                throw new IllegalStateException();

            return n;
        }

        public QName getAttributeName ( int index )
        {
            Xcur xa = toAttr( _xcur, index );
            QName name = xa.getName();
            xa.release();
            return name;
        }

        public String getAttributeNamespace ( int index )
        {
            return getAttributeName( index ).getNamespaceURI();
        }

        public String getAttributeLocalName ( int index )
        {
            return getAttributeName( index ).getLocalPart();
        }

        public String getAttributePrefix ( int index )
        {
            return getAttributeName( index ).getPrefix();
        }

        public String getAttributeType ( int index )
        {
            toAttr( _xcur, index ).release();
            return "CDATA";
        }

        public String getAttributeValue ( int index )
        {
            Xcur xa = toAttr( _xcur, index );

            String value = null;

            if (xa != null)
            {
                value = xa.getValueString();
                xa.release();
            }

            return value;
        }

        public boolean isAttributeSpecified ( int index )
        {
            return false;
        }

        public int getNamespaceCount ( )
        {
            int n = 0;

            if (_xcur.isContainer())
            {
                Xcur xa = _xcur.tempCur();

                if (xa.toFirstAttr())
                {
                    do
                    {
                        if (xa.kind() == Xcur.XMLNS)
                            n++;
                    }
                    while ( xa.toNextSibling() );
                }

                xa.release();
            }
            else if (_xcur.kind() == Xcur.ATTR)
                n++;
            else
                throw new IllegalStateException();

            return n;
        }

        private static Xcur toXmlns ( Xcur x, int i )
        {
            if (i < 0)
                throw new IndexOutOfBoundsException( "Namespace index is negative" );

            Xcur xa = x.tempCur();
            boolean match = false;

            if (x.isContainer())
            {
                if (xa.toFirstAttr())
                {
                    do
                    {
                        if (xa.kind() == Xcur.XMLNS && i-- == 0)
                        {
                            match = true;
                            break;
                        }
                    }
                    while ( xa.toNextSibling() );
                }
            }
            else if (x.kind() == Xcur.XMLNS)
                match = i == 0;
            else
                throw new IllegalStateException();

            if (!match)
            {
                xa.release();
                throw new IndexOutOfBoundsException( "Namespace index is too large" );
            }

            return xa;
        }

        public String getNamespacePrefix ( int index )
        {
            Xcur xa = toXmlns( _xcur, index );
            QName name = xa.getName();
            xa.release();
            return name.getLocalPart();
        }

        public String getNamespaceURI ( int index )
        {
            Xcur xa = toXmlns( _xcur, index );
            String uri = xa.getValueString();
            xa.release();
            return uri;
        }

        private void fetchChars ( )
        {
            if (!_textFetched)
            {
                int k = _xcur.kind();

                Xcur xText = null;

                if (k == Xcur.COMMENT)
                {
                    xText = _xcur.tempCur();
                    xText.next();
                }
                else if (k == Xcur.TEXT)
                    xText = _xcur;
                else
                    throw new IllegalStateException();

                Object src = xText.getChars( -1 );

                if (src == null)
                {
                    ensureCharBufLen( 0 );
                    _offChars = 0;
                    _cchChars = 0;
                }
                else if (src instanceof char[])
                {
                    ensureCharBufLen( _cchChars = xText._cchSrc );
                    char[] chars = (char[]) src;
                    System.arraycopy( chars, xText._offSrc, _chars, _offChars = 0, _cchChars );
                }
                else
                {
                    assert src instanceof String;
                    ensureCharBufLen( _cchChars = xText._cchSrc );
                    String s = (String) src;
                    s.getChars( xText._offSrc, xText._offSrc + _cchChars, _chars, _offChars = 0 );
                }

                if (xText != _xcur)
                    xText.release();

                _textFetched = true;
            }
        }
        
        private void ensureCharBufLen ( int cch )
        {
            if (_chars == null || _chars.length < cch)
            {
                int l = 256;

                while ( l < cch )
                    l *= 2;
                
                _chars = new char [ l ];
            }
        }

        public char[] getTextCharacters ( )
        {
            checkChanged();

            fetchChars();

            return _chars;
        }

        public int getTextStart ( )
        {
            checkChanged();

            fetchChars();

            return _offChars;
        }

        public int getTextLength ( )
        {
            checkChanged();

            fetchChars();

            return _cchChars;
        }

        public int getTextCharacters (
            int sourceStart, char[] target, int targetStart, int length )
                throws XMLStreamException
        {
            if (length < 0)
                throw new IndexOutOfBoundsException();

            if (!_srcFetched)
            {
                int k = _xcur.kind();

                Xcur xText = null;

                if (k == Xcur.COMMENT)
                {
                    xText = _xcur.tempCur();
                    xText.next();
                }
                else if (k == Xcur.TEXT)
                    xText = _xcur;
                else
                    throw new IllegalStateException();
            
                _src = xText.getChars( -1 );
                _offSrc = xText._offSrc;
                _cchSrc = xText._cchSrc;
                         
                if (xText != _xcur)
                    xText.release();
                
                _srcFetched = true;
            }

            if (_src == null)
            {
                if (sourceStart > 0)
                    throw new IndexOutOfBoundsException();
                
                length = 0;
            }
            else if (_src instanceof char[])
            {
                if (sourceStart > _cchSrc)
                    throw new IndexOutOfBoundsException();

                if (length > _cchSrc - sourceStart)
                    length = _cchSrc - sourceStart;
                
                System.arraycopy(
                    (char[]) _src, _offSrc + sourceStart, target, targetStart, length );
            }
            else
            {
                assert _src instanceof String;
                
                if (sourceStart > _cchSrc)
                    throw new IndexOutOfBoundsException();

                if (length > _cchSrc - sourceStart)
                    length = _cchSrc;

                String s = (String) _src;

                if (length > _cchSrc - sourceStart)
                    length = _cchSrc - sourceStart;

                int i = _offSrc + sourceStart;
                
                s.getChars( i, i + length, target, targetStart );
            }
            
            return length;
        }

        public boolean hasText ( )
        {
            int k = _xcur.kind();
            
            return k == Xcur.COMMENT || k == Xcur.TEXT;
        }

        public boolean hasName ( )
        {
            int k = _xcur.kind();
            return k == Xcur.ELEM || k == -Xcur.ELEM;
        }

        public QName getName ( )
        {
            if (!hasName())
                throw new IllegalStateException();

            return _xcur.getName();
        }

        public String getNamespaceURI ( )
        {
            return getName().getNamespaceURI();
        }

        public String getLocalName ( )
        {
            return getName().getLocalPart();
        }

        public String getPrefix ( )
        {
            return getName().getPrefix();
        }

        public String getPITarget ( )
        {
            return _xcur.kind() == Xcur.PROCINST ? _xcur.getName().getLocalPart() : null;
        }

        public String getPIData ( )
        {
            return _xcur.kind() == Xcur.PROCINST ? _xcur.getValueString() : null;
        }

        //
        //
        //

        private Xcur _xcur;
        private Xcur _xlast;

        private boolean _srcFetched;
        private Object  _src;
        private int     _offSrc;
        private int     _cchSrc;
        
        private boolean _textFetched;
        private char[]  _chars;
        private int     _offChars;
        private int     _cchChars;
    }
    
    //
    //
    //

    private static abstract class XMLStreamReaderBase
        implements XMLStreamReader, NamespaceContext, Location
    {
        XMLStreamReaderBase ( Xcur xcur )
        {
            _master = xcur.master();
            _version = _master.version();
        }

        protected final void checkChanged ( )
        {
            if (_version != _master.version())
                throw new ConcurrentModificationException( "Document changed while streaming" );
        }

        //
        // XMLStreamReader methods
        //

        public void close ( ) throws XMLStreamException
        {
            checkChanged();
        }

        public boolean isWhiteSpace ( )
        {
            throw new RuntimeException( "Not implemented" );

//            checkChanged();
//
//            // TODO - avoid creating a string here
//            String s = getText();
//
//            for ( int i = 0 ; i < s.length() ; i++ )
//            {
//                if (!Splay.isWhiteSpace( s.charAt( i ) ))
//                    return false;
//            }
//
//            return true;
        }

        public Location getLocation ( )
        {
            checkChanged();
            
            throw new RuntimeException( "Not implemented" );

//            XmlCursor c = getCursor();
//
//            XmlLineNumber ln = (XmlLineNumber) c.getBookmark( XmlLineNumber.class );
//
//            // BUGBUG - put source name here
//            _uri = null;
//
//            if (ln != null)
//            {
//                _line = ln.getLine();
//                _column = ln.getColumn();
//                _offset = ln.getOffset();
//            }
//            else
//            {
//                _line = -1;
//                _column = -1;
//                _offset = -1;
//            }
//
//            return this;
        }


        public NamespaceContext getNamespaceContext ( )
        {
            checkChanged();

            return this;
        }

        public Object getProperty ( String name )
        {
            checkChanged();

            throw new RuntimeException( "Not implemented" );
        }

        public String getCharacterEncodingScheme ( )
        {
            checkChanged();

            // TODO - implement this properly
            return "utf-8";
//            XmlDocumentProperties props = getCursor().documentProperties();
//
//            return props == null ? null : props.getEncoding();
        }

        public String getEncoding ( )
        {
            checkChanged();
            // TODO - implement this properly
            return "utf-8";
        }

        public String getVersion ( )
        {
            checkChanged();
            // TODO - implement this properly
            return "1.0";
//            XmlDocumentProperties props = getCursor().documentProperties();
//
//            return props == null ? null : props.getVersion();
        }

        public boolean isStandalone ( )
        {
            checkChanged();

            throw new RuntimeException( "Not implemented" );
            
//            return false;
        }

        public boolean standaloneSet ( )
        {
            checkChanged();
            
            throw new RuntimeException( "Not implemented" );
//
//            return false;
        }

        public void require ( int type, String namespaceURI, String localName )
            throws XMLStreamException
        {
            checkChanged();

            if (type != getEventType())
                throw new XMLStreamException();

            if (namespaceURI != null && !getNamespaceURI().equals( namespaceURI ))
                throw new XMLStreamException();

            if (localName != null && !getLocalName().equals( localName ))
                throw new XMLStreamException();
        }

        //
        // Location and NamespaceContext methods
        //

        public int    getCharacterOffset ( ) { return _offset; }
        public int    getColumnNumber    ( ) { return _column; }
        public int    getLineNumber      ( ) { return _line;   }
        public String getLocationURI     ( ) { return _uri;    }
        
        public String getPublicId() { throw new UnsupportedOperationException("NYI"); }
        public String getSystemId() { throw new UnsupportedOperationException("NYI"); }

        public String getNamespaceURI ( String prefix )
        {
            checkChanged();

            Xcur x = getCur();
            Xcur xParent = null;

            if (!x.isContainer())
            {
                x = xParent = x.tempCur();
                boolean b = xParent.toParent();
                assert b;
            }

            String ns = x.namespaceForPrefix( prefix );

            if (xParent != null)
                xParent.release();

            return ns;
        }

        public String getPrefix ( String namespaceURI )
        {
            checkChanged();

            Xcur x = getCur();
            Xcur xParent = null;

            if (!x.isContainer())
            {
                x = xParent = x.tempCur();
                boolean b = xParent.toParent();
                assert b;
            }

            String prefix = x.prefixForNamespace( namespaceURI );

            if (xParent != null)
                xParent.release();

            return prefix;
        }

        public Iterator getPrefixes ( String namespaceURI )
        {
            checkChanged();

            // BUGBUG - get only one for now ...

            HashMap map = new HashMap();

            map.put( namespaceURI, getPrefix( namespaceURI ) );

            return map.values().iterator();
        }

        //
        //
        //

        protected abstract Xcur getCur ( );

        //
        //
        //

        private Master _master;
        private long _version;
        
        String _uri;
        int _line, _column, _offset;
    }
    
    //
    //
    //

    private static final class XMLStreamReaderForString extends XMLStreamReaderBase
    {
        XMLStreamReaderForString ( Xcur xcur, Object src, int off, int cch )
        {
            super( xcur );

            _src = src;
            _off = off;
            _cch = cch;

            _xcur = xcur;
        }

        protected Xcur getCur ( )
        {
            return _xcur;
        }

        //
        // Legal stream methods
        //

        public String getText ( )
        {
            checkChanged();
            
            return Master.makeString( _src, _off, _cch );
        }
        
        public char[] getTextCharacters ( )
        {
            checkChanged();

            if (_src == null)
                return new char[ 0 ];

            return _src instanceof char[] ? (char[]) _src : ((String) _src).toCharArray();
        }
        public int getTextStart ( )
        {
            checkChanged();

            return _off;
        }
        public int getTextLength ( )
        {
            checkChanged();

            return _cch;
        }
        
        public int getTextCharacters ( int sourceStart, char[] target, int targetStart, int length )
        {
            checkChanged();

            int sourceEnd = sourceStart + length;

            if (sourceEnd >= _cch)
                sourceEnd = _cch;

            int cchCopy = sourceEnd - sourceStart;

            Master.copyChars( _src, _off + sourceStart, cchCopy, target, targetStart );

            return cchCopy;
        }

        public int     getEventType      ( ) { checkChanged(); return CHARACTERS; }
        public boolean hasName           ( ) { checkChanged(); return false;      }
        public boolean hasNext           ( ) { checkChanged(); return false;      }
        public boolean hasText           ( ) { checkChanged(); return true;       }
        public boolean isCharacters      ( ) { checkChanged(); return true;       }
        public boolean isEndElement      ( ) { checkChanged(); return false;      }
        public boolean isStartElement    ( ) { checkChanged(); return false;      }

        //
        // Illegal stream methods
        //

        public int     getAttributeCount ( ) { throw new IllegalStateException(); }
        public String  getAttributeLocalName ( int index ) { throw new IllegalStateException(); }
        public QName   getAttributeName ( int index ) { throw new IllegalStateException(); }
        public String  getAttributeNamespace ( int index ) { throw new IllegalStateException(); }
        public String  getAttributePrefix ( int index ) { throw new IllegalStateException(); }
        public String  getAttributeType ( int index ) { throw new IllegalStateException(); }
        public String  getAttributeValue ( int index ) { throw new IllegalStateException(); }
        public String  getAttributeValue ( String namespaceURI, String localName ) { throw new IllegalStateException(); }
        public String  getElementText ( ) { throw new IllegalStateException(); }
        public String  getLocalName ( ) { throw new IllegalStateException(); }
        public QName   getName ( ) { throw new IllegalStateException(); }
        public int     getNamespaceCount ( ) { throw new IllegalStateException(); }
        public String  getNamespacePrefix ( int index ) { throw new IllegalStateException(); }
        public String  getNamespaceURI ( int index ) { throw new IllegalStateException(); }
        public String  getNamespaceURI ( ) { throw new IllegalStateException(); }
        public String  getPIData ( ) { throw new IllegalStateException(); }
        public String  getPITarget ( ) { throw new IllegalStateException(); }
        public String  getPrefix ( ) { throw new IllegalStateException(); }
        public boolean isAttributeSpecified ( int index ) { throw new IllegalStateException(); }
        public int     next ( ) { throw new IllegalStateException(); }
        public int     nextTag ( ) { throw new IllegalStateException(); }
        public String  getPublicId() { throw new IllegalStateException();  }
        public String  getSystemId() { throw new IllegalStateException();  }

        private Xcur   _xcur;
        
        private Object _src;
        private int    _off;
        private int    _cch;
    }

    //
    //
    //

    private static final class SyncedJsr173 implements XMLStreamReader
    {
        public SyncedJsr173 ( XMLStreamReader xs ) { _xs = xs; }
        public SyncedJsr173 ( Master m, XMLStreamReader xs ) { _m = m; _xs = xs; }
        
        public Object getProperty ( java.lang.String name ) { try { synchronized ( _m ) { _m.enter(); return _xs.getProperty( name ); } } finally { _m.exit(); } }
        public int next ( ) throws XMLStreamException { try { synchronized ( _m ) { _m.enter(); return _xs.next(); } } finally { _m.exit(); } }
        public void require ( int type, String namespaceURI, String localName ) throws XMLStreamException { try { synchronized ( _m ) { _m.enter(); _xs.require( type, namespaceURI, localName ); } } finally { _m.exit(); } }
        public String getElementText ( ) throws XMLStreamException { try { synchronized ( _m ) { _m.enter(); return _xs.getElementText(); } } finally { _m.exit(); } }
        public int nextTag ( ) throws XMLStreamException { try { synchronized ( _m ) { _m.enter(); return _xs.nextTag(); } } finally { _m.exit(); } }
        public boolean hasNext ( ) throws XMLStreamException { try { synchronized ( _m ) { _m.enter(); return _xs.hasNext(); } } finally { _m.exit(); } }
        public void close ( ) throws XMLStreamException { try { synchronized ( _m ) { _m.enter(); _xs.close(); } } finally { _m.exit(); } }
        public String getNamespaceURI ( String prefix ) { try { synchronized ( _m ) { _m.enter(); return _xs.getNamespaceURI ( prefix ); } } finally { _m.exit(); } }
        public boolean isStartElement ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.isStartElement(); } } finally { _m.exit(); } }
        public boolean isEndElement ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.isEndElement(); } } finally { _m.exit(); } }
        public boolean isCharacters ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.isCharacters(); } } finally { _m.exit(); } }
        public boolean isWhiteSpace ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.isWhiteSpace(); } } finally { _m.exit(); } }
        public String getAttributeValue ( String namespaceURI, String localName ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributeValue ( namespaceURI, localName ); } } finally { _m.exit(); } }
        public int getAttributeCount ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributeCount(); } } finally { _m.exit(); } }
        public QName getAttributeName ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributeName ( index ); } } finally { _m.exit(); } }
        public String getAttributeNamespace ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributeNamespace ( index ); } } finally { _m.exit(); } }
        public String getAttributeLocalName ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributeLocalName ( index ); } } finally { _m.exit(); } }
        public String getAttributePrefix ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributePrefix ( index ); } } finally { _m.exit(); } }
        public String getAttributeType ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributeType ( index ); } } finally { _m.exit(); } }
        public String getAttributeValue ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getAttributeValue ( index ); } } finally { _m.exit(); } }
        public boolean isAttributeSpecified ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.isAttributeSpecified ( index ); } } finally { _m.exit(); } }
        public int getNamespaceCount ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getNamespaceCount(); } } finally { _m.exit(); } }
        public String getNamespacePrefix ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getNamespacePrefix ( index ); } } finally { _m.exit(); } }
        public String getNamespaceURI ( int index ) { try { synchronized ( _m ) { _m.enter(); return _xs.getNamespaceURI ( index ); } } finally { _m.exit(); } }
        public NamespaceContext getNamespaceContext ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getNamespaceContext(); } } finally { _m.exit(); } }
        public int getEventType ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getEventType(); } } finally { _m.exit(); } }
        public String getText ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getText(); } } finally { _m.exit(); } }
        public char[] getTextCharacters ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getTextCharacters(); } } finally { _m.exit(); } }
        public int getTextCharacters ( int sourceStart, char[] target, int targetStart, int length ) throws XMLStreamException { try { synchronized ( _m ) { _m.enter(); return _xs.getTextCharacters ( sourceStart, target, targetStart, length ); } } finally { _m.exit(); } }
        public int getTextStart ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getTextStart(); } } finally { _m.exit(); } }
        public int getTextLength ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getTextLength(); } } finally { _m.exit(); } }
        public String getEncoding ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getEncoding(); } } finally { _m.exit(); } }
        public boolean hasText ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.hasText(); } } finally { _m.exit(); } }
        public Location getLocation ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getLocation(); } } finally { _m.exit(); } }
        public QName getName ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getName(); } } finally { _m.exit(); } }
        public String getLocalName ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getLocalName(); } } finally { _m.exit(); } }
        public boolean hasName ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.hasName(); } } finally { _m.exit(); } }
        public String getNamespaceURI ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getNamespaceURI(); } } finally { _m.exit(); } }
        public String getPrefix ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getPrefix(); } } finally { _m.exit(); } }
        public String getVersion ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getVersion(); } } finally { _m.exit(); } }
        public boolean isStandalone ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.isStandalone(); } } finally { _m.exit(); } }
        public boolean standaloneSet ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.standaloneSet(); } } finally { _m.exit(); } }
        public String getCharacterEncodingScheme ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getCharacterEncodingScheme(); } } finally { _m.exit(); } }
        public String getPITarget ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getPITarget(); } } finally { _m.exit(); } }
        public String getPIData ( ) { try { synchronized ( _m ) { _m.enter(); return _xs.getPIData(); } } finally { _m.exit(); } }

        private Master          _m;
        private XMLStreamReader _xs;
    }

    private static final class UnsyncedJsr173 implements XMLStreamReader
    {
        public UnsyncedJsr173 ( Master m, XMLStreamReader xs ) { _m = m; _xs = xs; }
        
        public Object getProperty ( java.lang.String name ) { try { _m.enter(); return _xs.getProperty( name ); } finally { _m.exit(); } }
        public int next ( ) throws XMLStreamException { try { _m.enter(); return _xs.next(); } finally { _m.exit(); } }
        public void require ( int type, String namespaceURI, String localName ) throws XMLStreamException { try { _m.enter(); _xs.require( type, namespaceURI, localName ); } finally { _m.exit(); } }
        public String getElementText ( ) throws XMLStreamException { try { _m.enter(); return _xs.getElementText(); } finally { _m.exit(); } }
        public int nextTag ( ) throws XMLStreamException { try { _m.enter(); return _xs.nextTag(); } finally { _m.exit(); } }
        public boolean hasNext ( ) throws XMLStreamException { try { _m.enter(); return _xs.hasNext(); } finally { _m.exit(); } }
        public void close ( ) throws XMLStreamException { try { _m.enter(); _xs.close(); } finally { _m.exit(); } }
        public String getNamespaceURI ( String prefix ) { try { _m.enter(); return _xs.getNamespaceURI ( prefix ); } finally { _m.exit(); } }
        public boolean isStartElement ( ) { try { _m.enter(); return _xs.isStartElement(); } finally { _m.exit(); } }
        public boolean isEndElement ( ) { try { _m.enter(); return _xs.isEndElement(); } finally { _m.exit(); } }
        public boolean isCharacters ( ) { try { _m.enter(); return _xs.isCharacters(); } finally { _m.exit(); } }
        public boolean isWhiteSpace ( ) { try { _m.enter(); return _xs.isWhiteSpace(); } finally { _m.exit(); } }
        public String getAttributeValue ( String namespaceURI, String localName ) { try { _m.enter(); return _xs.getAttributeValue ( namespaceURI, localName ); } finally { _m.exit(); } }
        public int getAttributeCount ( ) { try { _m.enter(); return _xs.getAttributeCount(); } finally { _m.exit(); } }
        public QName getAttributeName ( int index ) { try { _m.enter(); return _xs.getAttributeName ( index ); } finally { _m.exit(); } }
        public String getAttributeNamespace ( int index ) { try { _m.enter(); return _xs.getAttributeNamespace ( index ); } finally { _m.exit(); } }
        public String getAttributeLocalName ( int index ) { try { _m.enter(); return _xs.getAttributeLocalName ( index ); } finally { _m.exit(); } }
        public String getAttributePrefix ( int index ) { try { _m.enter(); return _xs.getAttributePrefix ( index ); } finally { _m.exit(); } }
        public String getAttributeType ( int index ) { try { _m.enter(); return _xs.getAttributeType ( index ); } finally { _m.exit(); } }
        public String getAttributeValue ( int index ) { try { _m.enter(); return _xs.getAttributeValue ( index ); } finally { _m.exit(); } }
        public boolean isAttributeSpecified ( int index ) { try { _m.enter(); return _xs.isAttributeSpecified ( index ); } finally { _m.exit(); } }
        public int getNamespaceCount ( ) { try { _m.enter(); return _xs.getNamespaceCount(); } finally { _m.exit(); } }
        public String getNamespacePrefix ( int index ) { try { _m.enter(); return _xs.getNamespacePrefix ( index ); } finally { _m.exit(); } }
        public String getNamespaceURI ( int index ) { try { _m.enter(); return _xs.getNamespaceURI ( index ); } finally { _m.exit(); } }
        public NamespaceContext getNamespaceContext ( ) { try { _m.enter(); return _xs.getNamespaceContext(); } finally { _m.exit(); } }
        public int getEventType ( ) { try { _m.enter(); return _xs.getEventType(); } finally { _m.exit(); } }
        public String getText ( ) { try { _m.enter(); return _xs.getText(); } finally { _m.exit(); } }
        public char[] getTextCharacters ( ) { try { _m.enter(); return _xs.getTextCharacters(); } finally { _m.exit(); } }
        public int getTextCharacters ( int sourceStart, char[] target, int targetStart, int length ) throws XMLStreamException { try { _m.enter(); return _xs.getTextCharacters ( sourceStart, target, targetStart, length ); } finally { _m.exit(); } }
        public int getTextStart ( ) { try { _m.enter(); return _xs.getTextStart(); } finally { _m.exit(); } }
        public int getTextLength ( ) { try { _m.enter(); return _xs.getTextLength(); } finally { _m.exit(); } }
        public String getEncoding ( ) { try { _m.enter(); return _xs.getEncoding(); } finally { _m.exit(); } }
        public boolean hasText ( ) { try { _m.enter(); return _xs.hasText(); } finally { _m.exit(); } }
        public Location getLocation ( ) { try { _m.enter(); return _xs.getLocation(); } finally { _m.exit(); } }
        public QName getName ( ) { try { _m.enter(); return _xs.getName(); } finally { _m.exit(); } }
        public String getLocalName ( ) { try { _m.enter(); return _xs.getLocalName(); } finally { _m.exit(); } }
        public boolean hasName ( ) { try { _m.enter(); return _xs.hasName(); } finally { _m.exit(); } }
        public String getNamespaceURI ( ) { try { _m.enter(); return _xs.getNamespaceURI(); } finally { _m.exit(); } }
        public String getPrefix ( ) { try { _m.enter(); return _xs.getPrefix(); } finally { _m.exit(); } }
        public String getVersion ( ) { try { _m.enter(); return _xs.getVersion(); } finally { _m.exit(); } }
        public boolean isStandalone ( ) { try { _m.enter(); return _xs.isStandalone(); } finally { _m.exit(); } }
        public boolean standaloneSet ( ) { try { _m.enter(); return _xs.standaloneSet(); } finally { _m.exit(); } }
        public String getCharacterEncodingScheme ( ) { try { _m.enter(); return _xs.getCharacterEncodingScheme(); } finally { _m.exit(); } }
        public String getPITarget ( ) { try { _m.enter(); return _xs.getPITarget(); } finally { _m.exit(); } }
        public String getPIData ( ) { try { _m.enter(); return _xs.getPIData(); } finally { _m.exit(); } }

        private Master          _m;
        private XMLStreamReader _xs;
    }
}
