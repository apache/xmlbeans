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

import org.apache.xmlbeans.impl.newstore.CharUtil;
import org.apache.xmlbeans.impl.newstore.pub.store.Cur;
import org.apache.xmlbeans.impl.newstore.pub.store.Locale;

public class Jsr173
{
    public static XMLStreamReader newXmlStreamReader ( Cur c, Object src, int off, int cch )
    {
        XMLStreamReader xs = new XMLStreamReaderForString( c, src, off, cch );
        
        if (c.locale().noSync())
            return new UnsyncedJsr173( c.locale(), xs );
        else
            return new SyncedJsr173( c.locale(), xs );
    }
    
    public static XMLStreamReader newXmlStreamReader ( Cur c )
    {
        XMLStreamReader xs;

        int k = c.kind();
        
        if (k == Cur.TEXT)
            xs = new XMLStreamReaderForString( c, c.getChars( -1 ), c._offSrc, c._cchSrc );
        else
            xs = new XMLStreamReaderForNode( c );
        
        if (c.locale().noSync())
            return new UnsyncedJsr173( c.locale(), xs );
        else
            return new SyncedJsr173( c.locale(), xs );
    }
    
    //
    //
    //

    //
    //
    //
    
    private static final class XMLStreamReaderForNode extends XMLStreamReaderBase
    {
        public XMLStreamReaderForNode ( Cur c )
        {
            super( c );

            assert c.type() != Cur.TEXT;

            _cur = c.weakCur( this );
            _last = c.weakCur( this );
            
            if (_cur.isContainer())
                _last.toEnd();
        }

        protected Cur getCur ( )
        {
            return _cur;
        }

        //
        //
        //

        public boolean hasNext ( ) throws XMLStreamException
        {
            checkChanged();

            return !_cur.isSamePosition( _last );
        }

        public int getEventType ( )
        {
            switch ( _cur.type() )
            {
                case  Cur.ROOT : return START_DOCUMENT;
                case -Cur.ROOT : return END_DOCUMENT;
                case  Cur.ELEM : return START_ELEMENT;
                case -Cur.ELEM : return END_ELEMENT;
                case  Cur.ATTR : return _cur.kind() == Cur.XMLNS ? NAMESPACE : ATTRIBUTE;
                case  Cur.TEXT : return CHARACTERS;
                case  Cur.LEAF : return _cur.kind() == Cur.COMMENT ? COMMENT : PROCESSING_INSTRUCTION;
                default         : throw new IllegalStateException();
            }
        }

        public int next ( ) throws XMLStreamException
        {
            checkChanged();

            if (!hasNext())
                throw new IllegalStateException();

            if (_cur.isLeaf())
                _cur.toEnd();

            _cur.next();

            _textFetched = false;
            _srcFetched = false;
            
            return getEventType();
        }

        public String getText ( )
        {
            checkChanged();

            int k = _cur.kind();

            if (k == Cur.COMMENT)
                return _cur.getValueString();

            if (k == Cur.TEXT)
                return _cur.getString( -1 );

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

        private static boolean matchAttr ( Cur c, String uri, String local )
        {
            assert c.kind() == Cur.ATTR;

            QName name = c.getName();

            return
                name.getLocalPart().equals( local ) &&
                    (uri == null || name.getNamespaceURI().equals( uri ));
        }

        private static Cur toAttr ( Cur c, String uri, String local )
        {
            if (uri == null || local == null || local.length() == 0)
                throw new IllegalArgumentException();

            Cur ca = c.tempCur();
            boolean match = false;

            if (c.isContainer())
            {
                if (ca.toFirstAttr())
                {
                    do
                    {
                        if (ca.kind() == Cur.ATTR && matchAttr( ca, uri, local ))
                        {
                            match = true;
                            break;
                        }
                    }
                    while ( ca.toNextSibling() );
                }
            }
            else if (c.kind() == Cur.ATTR)
                match = matchAttr( c, uri, local );
            else
                throw new IllegalStateException();

            if (!match)
            {
                ca.release();
                ca = null;
            }

            return ca;
        }
        
        public String getAttributeValue ( String uri, String local )
        {
            Cur ca = toAttr( _cur, uri, local );

            String value = null;

            if (ca != null)
            {
                value = ca.getValueString();
                ca.release();
            }

            return value;
        }

        private static Cur toAttr ( Cur c, int i )
        {
            if (i < 0)
                throw new IndexOutOfBoundsException( "Attribute index is negative" );

            Cur ca = c.tempCur();
            boolean match = false;

            if (c.isContainer())
            {
                if (ca.toFirstAttr())
                {
                    do
                    {
                        if (ca.kind() == Cur.ATTR && i-- == 0)
                        {
                            match = true;
                            break;
                        }
                    }
                    while ( ca.toNextSibling() );
                }
            }
            else if (c.kind() == Cur.ATTR)
                match = i == 0;
            else
                throw new IllegalStateException();

            if (!match)
            {
                ca.release();
                throw new IndexOutOfBoundsException( "Attribute index is too large" );
            }

            return ca;
        }

        public int getAttributeCount ( )
        {
            int n = 0;
            
            if (_cur.isContainer())
            {
                Cur ca = _cur.tempCur();
                
                if (ca.toFirstAttr())
                {
                    do
                    {
                        if (ca.kind() == Cur.ATTR)
                            n++;
                    }
                    while ( ca.toNextSibling() );
                }

                ca.release();
            }
            else if (_cur.kind() == Cur.ATTR)
                n++;
            else
                throw new IllegalStateException();

            return n;
        }

        public QName getAttributeName ( int index )
        {
            Cur ca = toAttr( _cur, index );
            QName name = ca.getName();
            ca.release();
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
            toAttr( _cur, index ).release();
            return "CDATA";
        }

        public String getAttributeValue ( int index )
        {
            Cur ca = toAttr( _cur, index );

            String value = null;

            if (ca != null)
            {
                value = ca.getValueString();
                ca.release();
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

            if (_cur.isContainer())
            {
                Cur ca = _cur.tempCur();

                if (ca.toFirstAttr())
                {
                    do
                    {
                        if (ca.kind() == Cur.XMLNS)
                            n++;
                    }
                    while ( ca.toNextSibling() );
                }

                ca.release();
            }
            else if (_cur.kind() == Cur.ATTR)
                n++;
            else
                throw new IllegalStateException();

            return n;
        }

        private static Cur toXmlns ( Cur c, int i )
        {
            if (i < 0)
                throw new IndexOutOfBoundsException( "Namespace index is negative" );

            Cur ca = c.tempCur();
            boolean match = false;

            if (c.isContainer())
            {
                if (ca.toFirstAttr())
                {
                    do
                    {
                        if (ca.kind() == Cur.XMLNS && i-- == 0)
                        {
                            match = true;
                            break;
                        }
                    }
                    while ( ca.toNextSibling() );
                }
            }
            else if (c.kind() == Cur.XMLNS)
                match = i == 0;
            else
                throw new IllegalStateException();

            if (!match)
            {
                ca.release();
                throw new IndexOutOfBoundsException( "Namespace index is too large" );
            }

            return ca;
        }

        public String getNamespacePrefix ( int index )
        {
            Cur ca = toXmlns( _cur, index );
            QName name = ca.getName();
            ca.release();
            return name.getLocalPart();
        }

        public String getNamespaceURI ( int index )
        {
            Cur ca = toXmlns( _cur, index );
            String uri = ca.getValueString();
            ca.release();
            return uri;
        }

        private void fetchChars ( )
        {
            if (!_textFetched)
            {
                int k = _cur.kind();

                Cur cText = null;

                if (k == Cur.COMMENT)
                {
                    cText = _cur.tempCur();
                    cText.next();
                }
                else if (k == Cur.TEXT)
                    cText = _cur;
                else
                    throw new IllegalStateException();

                Object src = cText.getChars( -1 );
                
                ensureCharBufLen( cText._cchSrc );

                CharUtil.getChars(
                    _chars, _offChars = 0, src, cText._offSrc, _cchChars = cText._cchSrc );

                if (cText != _cur)
                    cText.release();

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
                int k = _cur.kind();

                Cur cText = null;

                if (k == Cur.COMMENT)
                {
                    cText = _cur.tempCur();
                    cText.next();
                }
                else if (k == Cur.TEXT)
                    cText = _cur;
                else
                    throw new IllegalStateException();
            
                _src = cText.getChars( -1 );
                _offSrc = cText._offSrc;
                _cchSrc = cText._cchSrc;
                         
                if (cText != _cur)
                    cText.release();
                
                _srcFetched = true;
            }

            if (sourceStart > _cchSrc)
                throw new IndexOutOfBoundsException();

            if (sourceStart + length > _cchSrc)
                length = _cchSrc - sourceStart;

            CharUtil.getChars( target, targetStart, _src, _offSrc, length );
            
            return length;
        }

        public boolean hasText ( )
        {
            int k = _cur.kind();
            
            return k == Cur.COMMENT || k == Cur.TEXT;
        }

        public boolean hasName ( )
        {
            int k = _cur.kind();
            return k == Cur.ELEM || k == -Cur.ELEM;
        }

        public QName getName ( )
        {
            if (!hasName())
                throw new IllegalStateException();

            return _cur.getName();
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
            return _cur.kind() == Cur.PROCINST ? _cur.getName().getLocalPart() : null;
        }

        public String getPIData ( )
        {
            return _cur.kind() == Cur.PROCINST ? _cur.getValueString() : null;
        }

        //
        //
        //

        private Cur _cur;
        private Cur _last;

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
        XMLStreamReaderBase ( Cur c )
        {
            _locale = c.locale();
            _version = _locale.version();
        }

        protected final void checkChanged ( )
        {
            if (_version != _locale.version())
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
            
            return this;

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
        
        public String getPublicId() { return null; }
        public String getSystemId() { return null; }

        public String getNamespaceURI ( String prefix )
        {
            checkChanged();

            Cur c = getCur();
            Cur cParent = null;

            if (!c.isContainer())
            {
                c = cParent = c.tempCur();
                boolean b = cParent.toParent();
                assert b;
            }

            String ns = c.namespaceForPrefix( prefix );

            if (cParent != null)
                cParent.release();

            return ns;
        }

        public String getPrefix ( String namespaceURI )
        {
            checkChanged();

            Cur c = getCur();
            Cur cParent = null;

            if (!c.isContainer())
            {
                c = cParent = c.tempCur();
                boolean b = cParent.toParent();
                assert b;
            }

            String prefix = c.prefixForNamespace( namespaceURI );

            if (cParent != null)
                cParent.release();

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

        protected abstract Cur getCur ( );

        //
        //
        //

        private Locale _locale;
        private long   _version;
        
        String _uri;
        int _line=-1;
        int _column=-1;
        int _offset=-1;
    }
    
    //
    //
    //

    private static final class XMLStreamReaderForString extends XMLStreamReaderBase
    {
        XMLStreamReaderForString ( Cur c, Object src, int off, int cch )
        {
            super( c );

            _src = src;
            _off = off;
            _cch = cch;

            _cur = c;
        }

        protected Cur getCur ( )
        {
            return _cur;
        }

        //
        // Legal stream methods
        //

        public String getText ( )
        {
            checkChanged();

            return CharUtil.getString( _src, _off, _cch );
        }
        
        public char[] getTextCharacters ( )
        {
            checkChanged();

            char[] chars = new char [ _cch ];

            CharUtil.getChars( chars, 0, _src, _off, _cch );

            return chars;
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

            if (length < 0)
                throw new IndexOutOfBoundsException();
            
            if (sourceStart > _cch)
                throw new IndexOutOfBoundsException();

            if (sourceStart + length > _cch)
                length = _cch - sourceStart;
            
            CharUtil.getChars( target, targetStart, _src, _off + sourceStart, length );

            return length;
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

        private Cur    _cur;
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
        public SyncedJsr173 ( Locale l, XMLStreamReader xs ) { _l = l; _xs = xs; }
        
        public Object getProperty ( java.lang.String name ) { synchronized ( _l ) { _l.enter(); try { return _xs.getProperty( name ); } finally { _l.exit(); } } }
        public int next ( ) throws XMLStreamException { synchronized ( _l ) { _l.enter(); try { return _xs.next(); } finally { _l.exit(); } } }
        public void require ( int type, String namespaceURI, String localName ) throws XMLStreamException { synchronized ( _l ) { _l.enter(); try { _xs.require( type, namespaceURI, localName ); } finally { _l.exit(); } } }
        public String getElementText ( ) throws XMLStreamException { synchronized ( _l ) { _l.enter(); try { return _xs.getElementText(); } finally { _l.exit(); } } }
        public int nextTag ( ) throws XMLStreamException { synchronized ( _l ) { _l.enter(); try { return _xs.nextTag(); } finally { _l.exit(); } } }
        public boolean hasNext ( ) throws XMLStreamException { synchronized ( _l ) { _l.enter(); try { return _xs.hasNext(); } finally { _l.exit(); } } }
        public void close ( ) throws XMLStreamException { synchronized ( _l ) { _l.enter(); try { _xs.close(); } finally { _l.exit(); } } }
        public String getNamespaceURI ( String prefix ) { synchronized ( _l ) { _l.enter(); try { return _xs.getNamespaceURI ( prefix ); } finally { _l.exit(); } } }
        public boolean isStartElement ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.isStartElement(); } finally { _l.exit(); } } }
        public boolean isEndElement ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.isEndElement(); } finally { _l.exit(); } } }
        public boolean isCharacters ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.isCharacters(); } finally { _l.exit(); } } }
        public boolean isWhiteSpace ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.isWhiteSpace(); } finally { _l.exit(); } } }
        public String getAttributeValue ( String namespaceURI, String localName ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributeValue ( namespaceURI, localName ); } finally { _l.exit(); } } }
        public int getAttributeCount ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributeCount(); } finally { _l.exit(); } } }
        public QName getAttributeName ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributeName ( index ); } finally { _l.exit(); } } }
        public String getAttributeNamespace ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributeNamespace ( index ); } finally { _l.exit(); } } }
        public String getAttributeLocalName ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributeLocalName ( index ); } finally { _l.exit(); } } }
        public String getAttributePrefix ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributePrefix ( index ); } finally { _l.exit(); } } }
        public String getAttributeType ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributeType ( index ); } finally { _l.exit(); } } }
        public String getAttributeValue ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getAttributeValue ( index ); } finally { _l.exit(); } } }
        public boolean isAttributeSpecified ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.isAttributeSpecified ( index ); } finally { _l.exit(); } } }
        public int getNamespaceCount ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getNamespaceCount(); } finally { _l.exit(); } } }
        public String getNamespacePrefix ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getNamespacePrefix ( index ); } finally { _l.exit(); } } }
        public String getNamespaceURI ( int index ) { synchronized ( _l ) { _l.enter(); try { return _xs.getNamespaceURI ( index ); } finally { _l.exit(); } } }
        public NamespaceContext getNamespaceContext ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getNamespaceContext(); } finally { _l.exit(); } } }
        public int getEventType ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getEventType(); } finally { _l.exit(); } } }
        public String getText ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getText(); } finally { _l.exit(); } } }
        public char[] getTextCharacters ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getTextCharacters(); } finally { _l.exit(); } } }
        public int getTextCharacters ( int sourceStart, char[] target, int targetStart, int length ) throws XMLStreamException { synchronized ( _l ) { _l.enter(); try { return _xs.getTextCharacters ( sourceStart, target, targetStart, length ); } finally { _l.exit(); } } }
        public int getTextStart ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getTextStart(); } finally { _l.exit(); } } }
        public int getTextLength ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getTextLength(); } finally { _l.exit(); } } }
        public String getEncoding ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getEncoding(); } finally { _l.exit(); } } }
        public boolean hasText ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.hasText(); } finally { _l.exit(); } } }
        public Location getLocation ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getLocation(); } finally { _l.exit(); } } }
        public QName getName ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getName(); } finally { _l.exit(); } } }
        public String getLocalName ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getLocalName(); } finally { _l.exit(); } } }
        public boolean hasName ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.hasName(); } finally { _l.exit(); } } }
        public String getNamespaceURI ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getNamespaceURI(); } finally { _l.exit(); } } }
        public String getPrefix ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getPrefix(); } finally { _l.exit(); } } }
        public String getVersion ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getVersion(); } finally { _l.exit(); } } }
        public boolean isStandalone ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.isStandalone(); } finally { _l.exit(); } } }
        public boolean standaloneSet ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.standaloneSet(); } finally { _l.exit(); } } }
        public String getCharacterEncodingScheme ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getCharacterEncodingScheme(); } finally { _l.exit(); } } }
        public String getPITarget ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getPITarget(); } finally { _l.exit(); } } }
        public String getPIData ( ) { synchronized ( _l ) { _l.enter(); try { return _xs.getPIData(); } finally { _l.exit(); } } }

        private Locale          _l;
        private XMLStreamReader _xs;
    }

    private static final class UnsyncedJsr173 implements XMLStreamReader
    {
        public UnsyncedJsr173 ( Locale l, XMLStreamReader xs ) { _l = l; _xs = xs; }
        
        public Object getProperty ( java.lang.String name ) { try { _l.enter(); return _xs.getProperty( name ); } finally { _l.exit(); } }
        public int next ( ) throws XMLStreamException { try { _l.enter(); return _xs.next(); } finally { _l.exit(); } }
        public void require ( int type, String namespaceURI, String localName ) throws XMLStreamException { try { _l.enter(); _xs.require( type, namespaceURI, localName ); } finally { _l.exit(); } }
        public String getElementText ( ) throws XMLStreamException { try { _l.enter(); return _xs.getElementText(); } finally { _l.exit(); } }
        public int nextTag ( ) throws XMLStreamException { try { _l.enter(); return _xs.nextTag(); } finally { _l.exit(); } }
        public boolean hasNext ( ) throws XMLStreamException { try { _l.enter(); return _xs.hasNext(); } finally { _l.exit(); } }
        public void close ( ) throws XMLStreamException { try { _l.enter(); _xs.close(); } finally { _l.exit(); } }
        public String getNamespaceURI ( String prefix ) { try { _l.enter(); return _xs.getNamespaceURI ( prefix ); } finally { _l.exit(); } }
        public boolean isStartElement ( ) { try { _l.enter(); return _xs.isStartElement(); } finally { _l.exit(); } }
        public boolean isEndElement ( ) { try { _l.enter(); return _xs.isEndElement(); } finally { _l.exit(); } }
        public boolean isCharacters ( ) { try { _l.enter(); return _xs.isCharacters(); } finally { _l.exit(); } }
        public boolean isWhiteSpace ( ) { try { _l.enter(); return _xs.isWhiteSpace(); } finally { _l.exit(); } }
        public String getAttributeValue ( String namespaceURI, String localName ) { try { _l.enter(); return _xs.getAttributeValue ( namespaceURI, localName ); } finally { _l.exit(); } }
        public int getAttributeCount ( ) { try { _l.enter(); return _xs.getAttributeCount(); } finally { _l.exit(); } }
        public QName getAttributeName ( int index ) { try { _l.enter(); return _xs.getAttributeName ( index ); } finally { _l.exit(); } }
        public String getAttributeNamespace ( int index ) { try { _l.enter(); return _xs.getAttributeNamespace ( index ); } finally { _l.exit(); } }
        public String getAttributeLocalName ( int index ) { try { _l.enter(); return _xs.getAttributeLocalName ( index ); } finally { _l.exit(); } }
        public String getAttributePrefix ( int index ) { try { _l.enter(); return _xs.getAttributePrefix ( index ); } finally { _l.exit(); } }
        public String getAttributeType ( int index ) { try { _l.enter(); return _xs.getAttributeType ( index ); } finally { _l.exit(); } }
        public String getAttributeValue ( int index ) { try { _l.enter(); return _xs.getAttributeValue ( index ); } finally { _l.exit(); } }
        public boolean isAttributeSpecified ( int index ) { try { _l.enter(); return _xs.isAttributeSpecified ( index ); } finally { _l.exit(); } }
        public int getNamespaceCount ( ) { try { _l.enter(); return _xs.getNamespaceCount(); } finally { _l.exit(); } }
        public String getNamespacePrefix ( int index ) { try { _l.enter(); return _xs.getNamespacePrefix ( index ); } finally { _l.exit(); } }
        public String getNamespaceURI ( int index ) { try { _l.enter(); return _xs.getNamespaceURI ( index ); } finally { _l.exit(); } }
        public NamespaceContext getNamespaceContext ( ) { try { _l.enter(); return _xs.getNamespaceContext(); } finally { _l.exit(); } }
        public int getEventType ( ) { try { _l.enter(); return _xs.getEventType(); } finally { _l.exit(); } }
        public String getText ( ) { try { _l.enter(); return _xs.getText(); } finally { _l.exit(); } }
        public char[] getTextCharacters ( ) { try { _l.enter(); return _xs.getTextCharacters(); } finally { _l.exit(); } }
        public int getTextCharacters ( int sourceStart, char[] target, int targetStart, int length ) throws XMLStreamException { try { _l.enter(); return _xs.getTextCharacters ( sourceStart, target, targetStart, length ); } finally { _l.exit(); } }
        public int getTextStart ( ) { try { _l.enter(); return _xs.getTextStart(); } finally { _l.exit(); } }
        public int getTextLength ( ) { try { _l.enter(); return _xs.getTextLength(); } finally { _l.exit(); } }
        public String getEncoding ( ) { try { _l.enter(); return _xs.getEncoding(); } finally { _l.exit(); } }
        public boolean hasText ( ) { try { _l.enter(); return _xs.hasText(); } finally { _l.exit(); } }
        public Location getLocation ( ) { try { _l.enter(); return _xs.getLocation(); } finally { _l.exit(); } }
        public QName getName ( ) { try { _l.enter(); return _xs.getName(); } finally { _l.exit(); } }
        public String getLocalName ( ) { try { _l.enter(); return _xs.getLocalName(); } finally { _l.exit(); } }
        public boolean hasName ( ) { try { _l.enter(); return _xs.hasName(); } finally { _l.exit(); } }
        public String getNamespaceURI ( ) { try { _l.enter(); return _xs.getNamespaceURI(); } finally { _l.exit(); } }
        public String getPrefix ( ) { try { _l.enter(); return _xs.getPrefix(); } finally { _l.exit(); } }
        public String getVersion ( ) { try { _l.enter(); return _xs.getVersion(); } finally { _l.exit(); } }
        public boolean isStandalone ( ) { try { _l.enter(); return _xs.isStandalone(); } finally { _l.exit(); } }
        public boolean standaloneSet ( ) { try { _l.enter(); return _xs.standaloneSet(); } finally { _l.exit(); } }
        public String getCharacterEncodingScheme ( ) { try { _l.enter(); return _xs.getCharacterEncodingScheme(); } finally { _l.exit(); } }
        public String getPITarget ( ) { try { _l.enter(); return _xs.getPITarget(); } finally { _l.exit(); } }
        public String getPIData ( ) { try { _l.enter(); return _xs.getPIData(); } finally { _l.exit(); } }

        private Locale          _l;
        private XMLStreamReader _xs;
    }
}
