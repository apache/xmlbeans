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

import java.io.PrintStream;

import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.xml.stream.XMLInputStream;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.ChangeStamp;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlDocumentProperties;

import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.common.GlobalLock;

import java.util.Map;
import java.util.Collection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;

import org.apache.xmlbeans.impl.newstore2.Saver.TextSaver;
import org.apache.xmlbeans.impl.newstore2.Locale.ChangeListener;
import org.apache.xmlbeans.impl.newstore2.Path.PathEngine;

public final class Cursor implements XmlCursor, ChangeListener
{
    static final int ROOT     = Cur.ROOT;
    static final int ELEM     = Cur.ELEM;
    static final int ATTR     = Cur.ATTR;
    static final int COMMENT  = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;
    static final int TEXT     = Cur.TEXT;

    Cursor ( Cur c )
    {
        _cur = c.weakCur( this );
        _currentSelection = -1;
    }

    private static boolean isValid ( Cur c )
    {
        if (c.kind() <= 0)
        {
            c.push();
            
            if (c.toParentRaw())
            {
                int pk = c.kind();

                if (pk == COMMENT || pk == PROCINST || pk == ATTR)
                    return false;
            }
                
            c.pop();
        }
        
        return true;
    }
    
    private boolean isValid ( )
    {
        return isValid( _cur );
    }

    Locale locale ( )
    {
        return _cur._locale;
    }
    
    Cur tempCur ( )
    {
        return _cur.tempCur();
    }

    public void dump ( PrintStream o )
    {
        _cur.dump( o );
    }
    
    public void dump ( )
    {
        dump( System.out );
    }

    static void validateLocalName ( QName name )
    {
        if (name == null)
            throw new IllegalArgumentException( "QName is null" );

        validateLocalName( name.getLocalPart() );
    }

    static void validateLocalName ( String name )
    {
        if (name == null)
            throw new IllegalArgumentException( "Name is null" );

        if (name.length() == 0)
            throw new IllegalArgumentException( "Name is empty" );

        if (!XMLChar.isValidNCName( name ))
            throw new IllegalArgumentException( "Name is not valid" );
    }

    static void validatePrefix ( String name )
    {
        if (name == null)
            throw new IllegalArgumentException( "Prefix is null" );

        if (name.length() == 0)
            throw new IllegalArgumentException( "Prefix is empty" );

        if (Locale.beginsWithXml( name ))
            throw new IllegalArgumentException( "Prefix begins with 'xml'" );

        if (!XMLChar.isValidNCName( name ))
            throw new IllegalArgumentException( "Prefix is not valid" );
    }

    private static void complain ( String msg )
    {
        throw new IllegalArgumentException( msg );
    }

    private void checkInsertionValidity ( Cur thisStuff )
    {
        int thisKind = thisStuff.kind();

        if (thisKind < 0)
            complain( "Can't move/copy/insert an end token." );

        if (thisKind == ROOT)
            complain( "Can't move/copy/insert a whole document." );

        int k = _cur.kind();

        if (k == ROOT)
            complain( "Can't insert before the start of the document." );

        if (thisKind == ATTR)
        {
            _cur.prev();
            int pk = _cur.kind();
            _cur.next();

            if (pk != ELEM && pk != ROOT && pk != -ATTR)
            {
                complain(
                    "Can only insert attributes before other attributes or after containers." );
            }
        }
    }
    
    private void insertNode ( Cur thisStuff, String text )
    {
        assert thisStuff.isNode();
        assert isValid( thisStuff );
        assert isValid();

        if (text != null && text.length() > 0)
        {
            thisStuff.next();
            thisStuff.insertString( text );
            thisStuff.toParent();
        }

        checkInsertionValidity( thisStuff );

        thisStuff.moveNode( _cur );

        _cur.toEnd();
        _cur.nextWithAttrs();
    }
    
    //
    //
    //

    // TODO - deal with cursors moving to other documents upon release?
    // Can I move the ref from one q to another?  If not I will have to
    // change from a phantom ref to a soft/weak ref so I can know what
    // to do when I dequeue from the old q.
    
    public void _dispose ( )
    {
        _cur.release();
        _cur = null;
    }
    
    public XmlCursor _newCursor ( )
    {
        return new Cursor( _cur );
    }

    public QName _getName ( )
    {
        // TODO - consider taking this out of the gateway
        return _cur.getName();
    }
    
    public void _setName ( QName name )
    {
        if (name == null)
            throw new IllegalArgumentException( "Name is null" );

        switch ( _cur.kind() )
        {
        case ELEM :
        case ATTR :
        {
            validateLocalName( name.getLocalPart() );
            break;
        }
                    
        case PROCINST :
        {
            validatePrefix( name.getLocalPart() );

            if (name.getNamespaceURI().length() > 0)
                throw new IllegalArgumentException( "Procinst name must have no URI" );
            
            if (name.getPrefix().length() > 0)
                throw new IllegalArgumentException( "Procinst name must have no prefix" );

            break;
        }

        default :
            throw
                new IllegalStateException(
                    "Can set name on element, atrtribute and procinst only" );
        }

        _cur.setName( name );
    }
    
    public TokenType _currentTokenType ( )
    {
        assert isValid();
        
        switch ( _cur.kind() )
        {
        case   ROOT     : return TokenType.STARTDOC;
        case - ROOT     : return TokenType.ENDDOC;
        case   ELEM     : return TokenType.START;
        case - ELEM     : return TokenType.END;
        case   TEXT     : return TokenType.TEXT;
        case   ATTR     : return _cur.isXmlns() ? TokenType.NAMESPACE : TokenType.ATTR;
        case   COMMENT  : return TokenType.COMMENT;
        case   PROCINST : return TokenType.PROCINST;

        default :
            throw new IllegalStateException();
        }
    }

    public boolean _isStartdoc   ( ){ return _currentTokenType().isStartdoc();  }
    public boolean _isEnddoc     ( ){ return _currentTokenType().isEnddoc();    }
    public boolean _isStart      ( ){ return _currentTokenType().isStart();     }
    public boolean _isEnd        ( ){ return _currentTokenType().isEnd();       }
    public boolean _isText       ( ){ return _currentTokenType().isText();      }
    public boolean _isAttr       ( ){ return _currentTokenType().isAttr();      }
    public boolean _isNamespace  ( ){ return _currentTokenType().isNamespace(); }
    public boolean _isComment    ( ){ return _currentTokenType().isComment();   }
    public boolean _isProcinst   ( ){ return _currentTokenType().isProcinst();  }
    public boolean _isContainer  ( ){ return _currentTokenType().isContainer(); }
    public boolean _isFinish     ( ){ return _currentTokenType().isFinish();    }
    public boolean _isAnyAttr    ( ){ return _currentTokenType().isAnyAttr();   }
    
    public TokenType _toNextToken ( )
    {
        assert isValid();
        
        switch ( _cur.kind() )
        {
        case ROOT :
        case ELEM :
        {
            if (!_cur.toFirstAttr())
                _cur.next();

            break;
        }
        
        case ATTR :
        {
            if (!_cur.toNextSibling())
            {
                _cur.toParent();
                _cur.next();
            }

            break;
        }

        case COMMENT :
        case PROCINST :
        {
            _cur.skip();
            break;
        }
        
        default :
        {
            if (!_cur.next())
                return TokenType.NONE;
                        
            break;
        }
        }

        return _currentTokenType();
    }

    public TokenType _toPrevToken ( )
    {
        assert isValid();

        if (!_cur.prev())
        {
            if (_cur.kind() == ATTR)
                _cur.toParent();
            else
                return TokenType.NONE;
        }

        int k = _cur.kind();

        if (k == -COMMENT || k == -PROCINST)
            _cur.toParent();
        else if (_cur.isContainer())
            _cur.toLastAttr();
        
        return _currentTokenType();
    }
    
    public Object _monitor ( )
    {
        // TODO - some of these methods need not be protected by a
        // gatway.  This is one of them.  Inline this.

        return _cur._locale;
    }
    
    public boolean _toParent ( )
    {
        Cur c = _cur.tempCur();

        if (!c.toParent())
            return false;

        _cur.moveToCur( c );

        c.release();

        return true;
    }

    private static final class ChangeStampImpl implements ChangeStamp
    {
        ChangeStampImpl ( Locale l )
        {
            _locale = l;
            _versionStamp = _locale.version();
        }

        public boolean hasChanged ( )
        {
            return _versionStamp != _locale.version();
        }

        private final Locale _locale;
        private final long   _versionStamp;
    }

    public ChangeStamp _getDocChangeStamp ( )
    {
        return new ChangeStampImpl( _cur._locale );
    }
    
    public XmlDocumentProperties _documentProperties ( )
    {
        return Locale.getDocProps( _cur, true );
    }
    
    public XMLStreamReader _newXMLStreamReader ( )
    {
        return _newXMLStreamReader( null );
    }
    
    public XMLStreamReader _newXMLStreamReader ( XmlOptions options )
    {
        return Jsr173.newXmlStreamReader( _cur, options );
    }

    public XMLInputStream _newXMLInputStream ( )
    {
        return _newXMLInputStream( null );
    }
    
    public XMLInputStream _newXMLInputStream ( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String _xmlText ( )
    {
        return _xmlText( null );
    }
    
    public String _xmlText ( XmlOptions options )
    {
        assert isValid();
        
        return new TextSaver( _cur, options, null ).saveToString();
    }
    
    public InputStream _newInputStream ( XmlOptions options )
    {
        return new Saver.InputStreamSaver( _cur, options );
    }
    
    public InputStream _newInputStream ( )
    {
        return _newInputStream( null );
    }
    
    public Reader _newReader ( )
    {
        return _newReader( null );
    }
    
    public Reader _newReader( XmlOptions options )
    {
        return new Saver.TextReader( _cur, options );
    }
    
    public Node _newDomNode ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _save ( ContentHandler ch, LexicalHandler lh ) throws SAXException
    {
        _save( ch, lh, null );
    }
    
    public void _save ( File file ) throws IOException
    {
        _save( file, null );
    }
    
    public void _save ( OutputStream os ) throws IOException
    {
        _save( os, null );
    }
    
    public void _save ( Writer w ) throws IOException
    {
        _save( w, null );
    }
    
    public void _save ( ContentHandler ch, LexicalHandler lh, XmlOptions options ) throws SAXException
    {
        throw new RuntimeException( "Not implemented" );
//        new Saver.SaxSaver( _cur, options, ch, lh );
    }
    
    public void _save ( File file, XmlOptions options ) throws IOException
    {
        OutputStream os = new FileOutputStream( file );

        try
        {
            _save( os, options );
        }
        finally
        {
            os.close();
        }
    }
    
    public void _save ( OutputStream os, XmlOptions options ) throws IOException
    {
        InputStream is = _newInputStream( options );

        try
        {
            byte[] bytes = new byte[ 8192 ];

            for ( ; ; )
            {
                int n = is.read( bytes );

                if (n < 0)
                    break;

                os.write( bytes, 0, n );
            }
        }
        finally
        {
            is.close();
        }
    }
    
    public void _save ( Writer w, XmlOptions options ) throws IOException
    {
        Reader r = _newReader( options );

        try
        {
            char[] chars = new char[ 8192 ];

            for ( ; ; )
            {
                int n = r.read( chars );

                if (n < 0)
                    break;

                w.write( chars, 0, n );
            }
        }
        finally
        {
            r.close();
        }
    }
    
    public Node _newDomNode ( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public Node _getDomNode ( )
    {
        return (Node) _cur.getDom();
    }
    
    public boolean _toCursor ( XmlCursor moveTo )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _push ( )
    {
        _cur.push();
    }
    
    public boolean _pop ( )
    {
        return _cur.pop();
    }
    
    public void notifyChange ( )
    {
        // TODO - need to exhaust the selection here ....
        throw new RuntimeException( "Not implemented" );
    }

    public void setNextChangeListener ( ChangeListener listener )
    {
        _nextChangeListener = listener;
    }
        
    public ChangeListener getNextChangeListener ( )
    {
        return _nextChangeListener;
    }
    
    public void _selectPath ( String path )
    {
        _selectPath( path, null );
    }
    
    public void _selectPath ( String pathExpr, XmlOptions options )
    {
        _cur.clearSelection();
        _pathEngine = Path.getCompiledPath( pathExpr, options ).execute( _cur );
    }
    
    public boolean _hasNextSelection ( )
    {
        push();

        try
        {
            return toNextSelection();
        }
        finally
        {
            pop();
        }
    }
    
    public boolean _toNextSelection ( )
    {
        return _toSelection( _currentSelection + 1 );
    }
    
    public boolean _toSelection ( int i )
    {
        while ( i >= _cur.selectionCount() )
        {
            if (_pathEngine == null || !_pathEngine.next( _cur ))
                return false;
        }

        _cur.moveToSelection( _currentSelection = i );
        
        return true;
    }
    
    public int _getSelectionCount ( )
    {
        _toSelection( Integer.MAX_VALUE );
        
        return _cur.selectionCount();
    }
    
    public void _addToSelection ( )
    {
        _toSelection( Integer.MAX_VALUE );

        _cur.addToSelection();
    }
    
    public void _clearSelections ( )
    {
        _cur.clearSelection();
        _pathEngine.release();
        _pathEngine = null;
        _currentSelection = 0;
    }
    
    public boolean _toBookmark ( XmlBookmark bookmark )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlBookmark _toNextBookmark ( Object key )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlBookmark _toPrevBookmark ( Object key )
    {
        // TODO - implement me!
        return null;
    }
    
    public String _namespaceForPrefix ( String prefix )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String _prefixForNamespace ( String namespaceURI )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _getAllNamespaces ( Map addToThis )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlObject _getObject ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType _prevTokenType ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _hasNextToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _hasPrevToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType _toFirstContentToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType _toEndToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int _toNextChar ( int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int _toPrevChar ( int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toNextSibling ( )
    {
        assert isValid();
        
        return Locale.toNextSiblingElement( _cur );
    }
    
    public boolean _toPrevSibling ( )
    {
        assert isValid();

        if (!_cur.hasParent())
            return false;

        Cur c = tempCur();

        boolean moved = false;
        
        int k = c.kind();

        if (k != ATTR)
        {
            for ( ; ; )
            {
                if (!c.prev())
                    break;

                k = c.kind();

                if (k == ROOT || k == ELEM)
                    break;

                if (c.kind() == -ELEM)
                {
                    c.toParent();

                    _cur.moveToCur( c );
                    moved = true;

                    break;
                }
            }
        }

        c.release();

        return moved;
    }
    
    public boolean _toLastChild ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toFirstChild ( )
    {
        return Locale.toFirstChildElement( _cur );
    }

    public boolean _toChild ( String local )
    {
        return Locale.toChild( _cur, null, local, 0 );
    }
    
    public boolean _toChild ( QName name )
    {
        return Locale.toChild( _cur, name, 0 );
    }
    
    public boolean _toChild ( String namespace, String local )
    {
        return Locale.toChild( _cur, namespace, local, 0 );
    }
    
    public boolean _toChild ( int index )
    {
        return Locale.toChild( _cur, null, null, index );
    }
    
    public boolean _toChild ( QName name, int index )
    {
        return Locale.toChild( _cur, name.getNamespaceURI(), name.getLocalPart(), index );
    }
    
    public boolean _toNextSibling ( String name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toNextSibling ( String namespace, String name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toNextSibling ( QName name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toFirstAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toLastAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toNextAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _toPrevAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String _getAttributeText ( QName attrName )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _setAttributeText ( QName attrName, String value )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _removeAttribute ( QName attrName )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String _getTextValue ( )
    {
        if (_cur.isText())
            return _getChars();
        
        if (!_cur.isNode())
        {
            throw new IllegalStateException(
                "Can't get text value, current token can have no text value" );
        }

        return Locale.getTextValue( _cur, Locale.WS_PRESERVE );
    }
    
    public int _getTextValue ( char[] returnedChars, int offset, int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _setTextValue ( String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _setTextValue ( char[] sourceChars, int offset, int length )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String _getChars ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int _getChars ( char[] returnedChars, int offset, int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _toStartDoc ( )
    {
        while ( _cur.toParent() )
            ;
    }
    
    public void _toEndDoc ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int _comparePosition ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _isLeftOf ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _isAtSamePositionAs ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _isRightOf ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlCursor _execQuery ( String query )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlCursor _execQuery ( String query, XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _setBookmark ( XmlBookmark bookmark )
    {
        if (bookmark != null)
        {
            if (bookmark.getKey() == null)
                throw new IllegalArgumentException( "Annotation key is null" );
            
            _clearBookmark( bookmark.getKey() );

            // TODO - I Don't do weak bookmarks yet ...
            _cur.setBookmark( bookmark.getKey(), bookmark );
        }
    }
    
    public XmlBookmark _getBookmark ( Object key )
    {
        // TODO - I Don't do weak bookmarks yet ...
        return key == null ? null : (XmlBookmark) _cur.getBookmark( key );
    }
    
    public void _clearBookmark ( Object key )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _getAllBookmarkRefs ( Collection listToFill )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _removeXml ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _moveXml ( Cursor to )
    {
        to.checkInsertionValidity( _cur );

        // Check for a no-op
        
        if (_cur.isText())
        {
            if (_cur.inChars( to._cur, -1, true ))
                return false;

            _cur.moveChars( to._cur, -1 );
        }
        else if (_cur.contains( to._cur ) ||
                    _cur.isSamePos( to._cur ) || to._cur.isJustAfterEnd( _cur ))
        {
            return false;
        }
        
        _cur.moveNode( to._cur );

        return true;
    }
    
    public boolean _copyXml ( XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _removeXmlContents ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _moveXmlContents ( XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean _copyXmlContents ( XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int _removeChars ( int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int _moveChars ( int maxCharacterCount, XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int _copyChars ( int maxCharacterCount, XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void _insertChars ( String text )
    {
        int l = text == null ? 0 : text.length();
        
        if (l > 0)
        {
            if (_cur.isRoot() || _cur.isAttr())
                complain( "Can't insert before the document or an attribute." );

            _cur.insertChars( text, 0, l );
            _cur.nextChars( l );
        }
    }

    //
    // Inserting elements
    //
    
    public void _beginElement          ( String localName                          ) { _insertElementWithText( localName, null, null ); _toPrevToken(); }
    public void _beginElement          ( String localName, String uri              ) { _insertElementWithText( localName, uri ); _toPrevToken(); }
    public void _beginElement          ( QName  name                               ) { _insertElementWithText( name, null ); _toPrevToken(); }
    public void _insertElement         ( String localName                          ) { _insertElementWithText( localName, null, null ); }
    public void _insertElement         ( String localName, String uri              ) { _insertElementWithText( localName, uri, null ); }
    public void _insertElement         ( QName  name                               ) { _insertElementWithText( name, null ); }
    public void _insertElementWithText ( String localName, String text             ) { _insertElementWithText( localName, null, text ); }
    public void _insertElementWithText ( String localName, String uri, String text ) { _insertElementWithText( _cur._locale.makeQName( uri, localName ), text ); }
    
    public void _insertElementWithText ( QName name, String text )
    {
        validateLocalName( name.getLocalPart() );

        Cur c = _cur._locale.tempCur();

        c.createElement( name );

        insertNode( c, text );

        c.release();
    }

    //
    //
    //
    
    public void _insertAttribute          ( String localName )                           { _insertAttributeWithValue( localName, null ); }
    public void _insertAttribute          ( String localName, String uri )               { _insertAttributeWithValue( localName, uri, null ); }
    public void _insertAttribute          ( QName name )                                 { _insertAttributeWithValue( name, null ); }
    public void _insertAttributeWithValue ( String localName, String value )             { _insertAttributeWithValue( localName, null, value ); }
    public void _insertAttributeWithValue ( String localName, String uri, String value ) { _insertAttributeWithValue( _cur._locale.makeQName( uri, localName ), value ); }
    
    public void _insertAttributeWithValue ( QName name, String text )
    {
        Cur c = _cur._locale.tempCur();

        c.createAttr( name );

        insertNode( c, text );
        
        c.release();
    }

    //
    //
    //
    
    public void _insertNamespace ( String prefix, String namespace )
    {
        _insertAttributeWithValue( _cur._locale.createXmlns( prefix ), namespace );
    }
    
    public void _insertComment ( String text )
    {
        Cur c = _cur._locale.tempCur();

        c.createComment();
        
        insertNode( c, text );
        
        c.release();
    }
    
    public void _insertProcInst ( String target, String text )
    {
        validateLocalName( target );

        if (Locale.beginsWithXml( target ) && target.length() == 3)
            throw new IllegalArgumentException( "Target is 'xml'" );
        
        Cur c = _cur._locale.tempCur();

        c.createProcinst( target );

        insertNode( c, text );
        
        c.release();
    }

    //
    //
    //
    //
    //
    //
    //

    public void dispose ( )
    {
        if (preCheck())
        {
            Locale l = _cur._locale;
            
            l.enter();

            try
            {
                _dispose();
            }
            finally
            {
                l.exit();
            }
        }
        else
        {
            Locale l = _cur._locale;
            
            synchronized ( l )
            {
                l.enter();

                try
                {
                    _dispose();
                }
                finally
                {
                    l.exit();
                }
            }
        }
    }
    
    private void checkThisCursor ( )
    {
        if (_cur == null)
            throw new IllegalStateException( "This cursor has been disposed" );
    }
    
    private Cursor checkCursors ( XmlCursor xOther )
    {
        checkThisCursor();

        if (xOther == null)
            throw new IllegalArgumentException( "Other cursor is <null>" );
        
        if (!(xOther instanceof Cursor))
            throw new IllegalArgumentException( "Incompatible cursors: " + xOther );
            
        Cursor other = (Cursor) xOther;
        
        if (other._cur == null)
            throw new IllegalStateException( "Other cursor has been disposed" );

        return other;
    }
    
    public boolean toCursor ( XmlCursor xOther )
    {
        throw new RuntimeException( "Not implemented" );
        
//        Cursor other = checkCursors( xOther );
//
//        if (preCheck())
//        {
//            _locale.enter();
//
//            try
//            {
//                return _toCursor( moveTo );
//            }
//            finally
//            {
//                _locale.exit();
//            }
//        }
//        else
//        {
//            synchronized ( _locale )
//            {
//                _locale.enter();
//
//                try
//                {
//                    return _toCursor( moveTo );
//                }
//                finally
//                {
//                    _locale.exit();
//                }
//            }
//        }
    }
    
    public boolean isInSameDocument ( XmlCursor xOther )
    {
        Cursor other = checkCursors( xOther );
        
        throw new RuntimeException( "Not implemented" );
    }

    private static final int MOVE_XML          = 0;
    private static final int COPY_XML          = 1;
    private static final int MOVE_XML_CONTENTS = 2;
    private static final int COPY_XML_CONTENTS = 3;
    private static final int MOVE_CHARS        = 4;
    private static final int COPY_CHARS        = 5;
    
    private int twoCursorOp ( XmlCursor xOther, int op, int arg )
    {
        Cursor other = checkCursors( xOther );

        Locale locale = _cur._locale;
        Locale otherLocale = other._cur._locale;
        
        if (locale == otherLocale)
        {
            if (locale.noSync())
                return twoCursorOp( other, op, arg );
            else
            {
                synchronized ( locale )
                {
                    return twoCursorOp( other, op, arg );
                }
            }
        }
        
        if (locale.noSync())
        {
            if (otherLocale.noSync())
                return twoCursorOp( other, op, arg );
            else
            {
                synchronized ( otherLocale )
                {
                    return twoCursorOp( other, op, arg );
                }
            }
        }
        else if (otherLocale.noSync())
        {
            synchronized ( locale )
            {
                return twoCursorOp( other, op, arg );
            }
        }
        
        boolean acquired = false;

        try
        {
            GlobalLock.acquire();
            acquired = true;
            
            synchronized ( locale )
            {
                synchronized ( otherLocale )
                {
                    GlobalLock.release();
                    acquired = false;
                    
                    return twoCursorOp( other, op, arg );
                }
            }
        }
        catch ( InterruptedException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
        finally
        {
            if (acquired)
                GlobalLock.release();
        }
    }
    
    private int twoCursorOp ( Cursor other, int op, int arg )
    {
        Locale locale = _cur._locale;
        Locale otherLocale = other._cur._locale;
        
        locale.enter( otherLocale );
        
        try
        {
            switch ( op )
            {
                case MOVE_XML          : return _moveXml         ( other ) ? 1 : 0;
                case COPY_XML          : return _copyXml         ( other ) ? 1 : 0;
                case MOVE_XML_CONTENTS : return _moveXmlContents ( other ) ? 1 : 0;
                case COPY_XML_CONTENTS : return _copyXmlContents ( other ) ? 1 : 0;
                case MOVE_CHARS        : return _moveChars       ( arg, other );
                case COPY_CHARS        : return _copyChars       ( arg, other );
                                         
                default : throw new RuntimeException( "Unknown operation: " + op );
            }
        }
        finally
        {
            locale.exit( otherLocale );
        }
    }
    
    public boolean moveXml ( XmlCursor xTo )
    {
        return twoCursorOp( xTo, MOVE_XML, 0 ) == 1;
    }
    
    public boolean copyXml ( XmlCursor xTo )
    {
        return twoCursorOp( xTo, COPY_XML, 0 ) == 1;
    }
    
    public boolean moveXmlContents ( XmlCursor xTo )
    {
        return twoCursorOp( xTo, MOVE_XML_CONTENTS, 0 ) == 1;
    }
    
    public boolean copyXmlContents ( XmlCursor xTo )
    {
        return twoCursorOp( xTo, COPY_XML_CONTENTS, 0 ) == 1;
    }
    
    public int moveChars ( int maxCharacterCount, XmlCursor xTo )
    {
        return twoCursorOp( xTo, MOVE_CHARS, maxCharacterCount );
    }
    
    public int copyChars ( int maxCharacterCount, XmlCursor xTo )
    {
        return twoCursorOp( xTo, COPY_CHARS, maxCharacterCount );
    }
    
    private boolean preCheck ( )
    {
        checkThisCursor();
        return _cur._locale.noSync();
    }

    // TODO - make sure _cur._locale does not change between operations ... some of these
    // methods might actually be two Locale (or multiple Locale) operations.  In particular,
    // clearSelection might be a multiple Locale operation
    
    public Object monitor ( ) { if (preCheck()) { _cur._locale.enter(); try { return _monitor(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _monitor(); } finally { _cur._locale.exit(); } } }
    public XmlDocumentProperties documentProperties ( ) { if (preCheck()) { _cur._locale.enter(); try { return _documentProperties(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _documentProperties(); } finally { _cur._locale.exit(); } } }
    public XmlCursor newCursor ( ) { if (preCheck()) { _cur._locale.enter(); try { return _newCursor(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newCursor(); } finally { _cur._locale.exit(); } } }
    public XMLStreamReader newXMLStreamReader ( ) { if (preCheck()) { _cur._locale.enter(); try { return _newXMLStreamReader(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newXMLStreamReader(); } finally { _cur._locale.exit(); } } }
    public XMLStreamReader newXMLStreamReader ( XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { return _newXMLStreamReader( options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newXMLStreamReader( options ); } finally { _cur._locale.exit(); } } }
    public XMLInputStream newXMLInputStream ( ) { if (preCheck()) { _cur._locale.enter(); try { return _newXMLInputStream(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newXMLInputStream(); } finally { _cur._locale.exit(); } } }
    public String xmlText ( ) { if (preCheck()) { _cur._locale.enter(); try { return _xmlText(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _xmlText(); } finally { _cur._locale.exit(); } } }
    public InputStream newInputStream ( ) { if (preCheck()) { _cur._locale.enter(); try { return _newInputStream(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newInputStream(); } finally { _cur._locale.exit(); } } }
    public Reader newReader ( ) { if (preCheck()) { _cur._locale.enter(); try { return _newReader(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newReader(); } finally { _cur._locale.exit(); } } }
    public Node newDomNode ( ) { if (preCheck()) { _cur._locale.enter(); try { return _newDomNode(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newDomNode(); } finally { _cur._locale.exit(); } } }
    public Node getDomNode ( ) { if (preCheck()) { _cur._locale.enter(); try { return _getDomNode(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newDomNode(); } finally { _cur._locale.exit(); } } }
    public void save ( ContentHandler ch, LexicalHandler lh ) throws SAXException { if (preCheck()) { _cur._locale.enter(); try { _save( ch, lh ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( ch, lh ); } finally { _cur._locale.exit(); } } }
    public void save ( File file ) throws IOException { if (preCheck()) { _cur._locale.enter(); try { _save( file ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( file ); } finally { _cur._locale.exit(); } } }
    public void save ( OutputStream os ) throws IOException { if (preCheck()) { _cur._locale.enter(); try { _save( os ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( os ); } finally { _cur._locale.exit(); } } }
    public void save ( Writer w ) throws IOException { if (preCheck()) { _cur._locale.enter(); try { _save( w ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( w ); } finally { _cur._locale.exit(); } } }
    public XMLInputStream newXMLInputStream ( XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { return _newXMLInputStream( options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newXMLInputStream( options ); } finally { _cur._locale.exit(); } } }
    public String xmlText ( XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { return _xmlText( options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _xmlText( options ); } finally { _cur._locale.exit(); } } }
    public InputStream newInputStream ( XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { return _newInputStream( options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newInputStream( options ); } finally { _cur._locale.exit(); } } }
    public Reader newReader( XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { return _newReader( options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newReader( options ); } finally { _cur._locale.exit(); } } }
    public Node newDomNode ( XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { return _newDomNode( options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _newDomNode( options ); } finally { _cur._locale.exit(); } } }
    public void save ( ContentHandler ch, LexicalHandler lh, XmlOptions options ) throws SAXException { if (preCheck()) { _cur._locale.enter(); try { _save( ch, lh, options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( ch, lh, options ); } finally { _cur._locale.exit(); } } }
    public void save ( File file, XmlOptions options ) throws IOException { if (preCheck()) { _cur._locale.enter(); try { _save( file, options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( file, options ); } finally { _cur._locale.exit(); } } }
    public void save ( OutputStream os, XmlOptions options ) throws IOException { if (preCheck()) { _cur._locale.enter(); try { _save( os, options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( os, options ); } finally { _cur._locale.exit(); } } }
    public void save ( Writer w, XmlOptions options ) throws IOException { if (preCheck()) { _cur._locale.enter(); try { _save( w, options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _save( w, options ); } finally { _cur._locale.exit(); } } }
    public void push ( ) { if (preCheck()) { _cur._locale.enter(); try { _push(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _push(); } finally { _cur._locale.exit(); } } }
    public boolean pop ( ) { if (preCheck()) { _cur._locale.enter(); try { return _pop(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _pop(); } finally { _cur._locale.exit(); } } }
    public void selectPath ( String path ) { if (preCheck()) { _cur._locale.enter(); try { _selectPath( path ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _selectPath( path ); } finally { _cur._locale.exit(); } } }
    public void selectPath ( String path, XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { _selectPath( path, options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _selectPath( path, options ); } finally { _cur._locale.exit(); } } }
    public boolean hasNextSelection ( ) { if (preCheck()) { _cur._locale.enter(); try { return _hasNextSelection(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _hasNextSelection(); } finally { _cur._locale.exit(); } } }
    public boolean toNextSelection ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextSelection(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextSelection(); } finally { _cur._locale.exit(); } } }
    public boolean toSelection ( int i ) { if (preCheck()) { _cur._locale.enter(); try { return _toSelection( i ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toSelection( i ); } finally { _cur._locale.exit(); } } }
    public int getSelectionCount ( ) { if (preCheck()) { _cur._locale.enter(); try { return _getSelectionCount(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getSelectionCount(); } finally { _cur._locale.exit(); } } }
    public void addToSelection ( ) { if (preCheck()) { _cur._locale.enter(); try { _addToSelection(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _addToSelection(); } finally { _cur._locale.exit(); } } }
    public void clearSelections ( ) { if (preCheck()) { _cur._locale.enter(); try { _clearSelections(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _clearSelections(); } finally { _cur._locale.exit(); } } }
    public boolean toBookmark ( XmlBookmark bookmark ) { if (preCheck()) { _cur._locale.enter(); try { return _toBookmark( bookmark ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toBookmark( bookmark ); } finally { _cur._locale.exit(); } } }
    public XmlBookmark toNextBookmark ( Object key ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextBookmark( key ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextBookmark( key ); } finally { _cur._locale.exit(); } } }
    public XmlBookmark toPrevBookmark ( Object key ) { if (preCheck()) { _cur._locale.enter(); try { return _toPrevBookmark( key ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toPrevBookmark( key ); } finally { _cur._locale.exit(); } } }
    public QName getName ( ) { if (preCheck()) { _cur._locale.enter(); try { return _getName(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getName(); } finally { _cur._locale.exit(); } } }
    public void setName ( QName name ) { if (preCheck()) { _cur._locale.enter(); try { _setName( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _setName( name ); } finally { _cur._locale.exit(); } } }
    public String namespaceForPrefix ( String prefix ) { if (preCheck()) { _cur._locale.enter(); try { return _namespaceForPrefix( prefix ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _namespaceForPrefix( prefix ); } finally { _cur._locale.exit(); } } }
    public String prefixForNamespace ( String namespaceURI ) { if (preCheck()) { _cur._locale.enter(); try { return _prefixForNamespace( namespaceURI ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _prefixForNamespace( namespaceURI ); } finally { _cur._locale.exit(); } } }
    public void getAllNamespaces ( Map addToThis ) { if (preCheck()) { _cur._locale.enter(); try { _getAllNamespaces( addToThis ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _getAllNamespaces( addToThis ); } finally { _cur._locale.exit(); } } }
    public XmlObject getObject ( ) { if (preCheck()) { _cur._locale.enter(); try { return _getObject(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getObject(); } finally { _cur._locale.exit(); } } }
    public TokenType currentTokenType ( ) { if (preCheck()) { _cur._locale.enter(); try { return _currentTokenType(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _currentTokenType(); } finally { _cur._locale.exit(); } } }
    public boolean isStartdoc ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isStartdoc(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isStartdoc(); } finally { _cur._locale.exit(); } } }
    public boolean isEnddoc ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isEnddoc(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isEnddoc(); } finally { _cur._locale.exit(); } } }
    public boolean isStart ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isStart(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isStart(); } finally { _cur._locale.exit(); } } }
    public boolean isEnd ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isEnd(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isEnd(); } finally { _cur._locale.exit(); } } }
    public boolean isText ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isText(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isText(); } finally { _cur._locale.exit(); } } }
    public boolean isAttr ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isAttr(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isAttr(); } finally { _cur._locale.exit(); } } }
    public boolean isNamespace ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isNamespace(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isNamespace(); } finally { _cur._locale.exit(); } } }
    public boolean isComment ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isComment(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isComment(); } finally { _cur._locale.exit(); } } }
    public boolean isProcinst ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isProcinst(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isProcinst(); } finally { _cur._locale.exit(); } } }
    public boolean isContainer ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isContainer(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isContainer(); } finally { _cur._locale.exit(); } } }
    public boolean isFinish ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isFinish(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isFinish(); } finally { _cur._locale.exit(); } } }
    public boolean isAnyAttr ( ) { if (preCheck()) { _cur._locale.enter(); try { return _isAnyAttr(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isAnyAttr(); } finally { _cur._locale.exit(); } } }
    public TokenType prevTokenType ( ) { if (preCheck()) { _cur._locale.enter(); try { return _prevTokenType(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _prevTokenType(); } finally { _cur._locale.exit(); } } }
    public boolean hasNextToken ( ) { if (preCheck()) { _cur._locale.enter(); try { return _hasNextToken(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _hasNextToken(); } finally { _cur._locale.exit(); } } }
    public boolean hasPrevToken ( ) { if (preCheck()) { _cur._locale.enter(); try { return _hasPrevToken(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _hasPrevToken(); } finally { _cur._locale.exit(); } } }
    public TokenType toNextToken ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextToken(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextToken(); } finally { _cur._locale.exit(); } } }
    public TokenType toPrevToken ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toPrevToken(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toPrevToken(); } finally { _cur._locale.exit(); } } }
    public TokenType toFirstContentToken ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toFirstContentToken(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toFirstContentToken(); } finally { _cur._locale.exit(); } } }
    public TokenType toEndToken ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toEndToken(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toEndToken(); } finally { _cur._locale.exit(); } } }
    public int toNextChar ( int maxCharacterCount ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextChar( maxCharacterCount ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextChar( maxCharacterCount ); } finally { _cur._locale.exit(); } } }
    public int toPrevChar ( int maxCharacterCount ) { if (preCheck()) { _cur._locale.enter(); try { return _toPrevChar( maxCharacterCount ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toPrevChar( maxCharacterCount ); } finally { _cur._locale.exit(); } } }
    public boolean toNextSibling ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextSibling(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextSibling(); } finally { _cur._locale.exit(); } } }
    public boolean toPrevSibling ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toPrevSibling(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toPrevSibling(); } finally { _cur._locale.exit(); } } }
    public boolean toParent ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toParent(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toParent(); } finally { _cur._locale.exit(); } } }
    public boolean toFirstChild ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toFirstChild(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toFirstChild(); } finally { _cur._locale.exit(); } } }
    public boolean toLastChild ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toLastChild(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toLastChild(); } finally { _cur._locale.exit(); } } }
    public boolean toChild ( String name ) { if (preCheck()) { _cur._locale.enter(); try { return _toChild( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toChild( name ); } finally { _cur._locale.exit(); } } }
    public boolean toChild ( String namespace, String name ) { if (preCheck()) { _cur._locale.enter(); try { return _toChild( namespace, name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toChild( namespace, name ); } finally { _cur._locale.exit(); } } }
    public boolean toChild ( QName name ) { if (preCheck()) { _cur._locale.enter(); try { return _toChild( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toChild( name ); } finally { _cur._locale.exit(); } } }
    public boolean toChild ( int index ) { if (preCheck()) { _cur._locale.enter(); try { return _toChild( index ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toChild( index ); } finally { _cur._locale.exit(); } } }
    public boolean toChild ( QName name, int index ) { if (preCheck()) { _cur._locale.enter(); try { return _toChild( name, index ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toChild( name, index ); } finally { _cur._locale.exit(); } } }
    public boolean toNextSibling ( String name ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextSibling( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextSibling( name ); } finally { _cur._locale.exit(); } } }
    public boolean toNextSibling ( String namespace, String name ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextSibling( namespace, name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextSibling( namespace, name ); } finally { _cur._locale.exit(); } } }
    public boolean toNextSibling ( QName name ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextSibling( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextSibling( name ); } finally { _cur._locale.exit(); } } }
    public boolean toFirstAttribute ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toFirstAttribute(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toFirstAttribute(); } finally { _cur._locale.exit(); } } }
    public boolean toLastAttribute ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toLastAttribute(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toLastAttribute(); } finally { _cur._locale.exit(); } } }
    public boolean toNextAttribute ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toNextAttribute(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toNextAttribute(); } finally { _cur._locale.exit(); } } }
    public boolean toPrevAttribute ( ) { if (preCheck()) { _cur._locale.enter(); try { return _toPrevAttribute(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _toPrevAttribute(); } finally { _cur._locale.exit(); } } }
    public String getAttributeText ( QName attrName ) { if (preCheck()) { _cur._locale.enter(); try { return _getAttributeText( attrName ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getAttributeText( attrName ); } finally { _cur._locale.exit(); } } }
    public boolean setAttributeText ( QName attrName, String value ) { if (preCheck()) { _cur._locale.enter(); try { return _setAttributeText( attrName, value ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _setAttributeText( attrName, value ); } finally { _cur._locale.exit(); } } }
    public boolean removeAttribute ( QName attrName ) { if (preCheck()) { _cur._locale.enter(); try { return _removeAttribute( attrName ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _removeAttribute( attrName ); } finally { _cur._locale.exit(); } } }
    public String getTextValue ( ) { if (preCheck()) { _cur._locale.enter(); try { return _getTextValue(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getTextValue(); } finally { _cur._locale.exit(); } } }
    public int getTextValue ( char[] returnedChars, int offset, int maxCharacterCount ) { if (preCheck()) { _cur._locale.enter(); try { return _getTextValue( returnedChars, offset, maxCharacterCount ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getTextValue( returnedChars, offset, maxCharacterCount ); } finally { _cur._locale.exit(); } } }
    public void setTextValue ( String text ) { if (preCheck()) { _cur._locale.enter(); try { _setTextValue( text ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _setTextValue( text ); } finally { _cur._locale.exit(); } } }
    public void setTextValue ( char[] sourceChars, int offset, int length ) { if (preCheck()) { _cur._locale.enter(); try { _setTextValue( sourceChars, offset, length ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _setTextValue( sourceChars, offset, length ); } finally { _cur._locale.exit(); } } }
    public String getChars ( ) { if (preCheck()) { _cur._locale.enter(); try { return _getChars(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getChars(); } finally { _cur._locale.exit(); } } }
    public int getChars ( char[] returnedChars, int offset, int maxCharacterCount ) { if (preCheck()) { _cur._locale.enter(); try { return _getChars( returnedChars, offset, maxCharacterCount ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getChars( returnedChars, offset, maxCharacterCount ); } finally { _cur._locale.exit(); } } }
    public void toStartDoc ( ) { if (preCheck()) { _cur._locale.enter(); try { _toStartDoc(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _toStartDoc(); } finally { _cur._locale.exit(); } } }
    public void toEndDoc ( ) { if (preCheck()) { _cur._locale.enter(); try { _toEndDoc(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _toEndDoc(); } finally { _cur._locale.exit(); } } }
    
    public int comparePosition ( XmlCursor cursor ) { if (preCheck()) { _cur._locale.enter(); try { return _comparePosition( cursor ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _comparePosition( cursor ); } finally { _cur._locale.exit(); } } }
    public boolean isLeftOf ( XmlCursor cursor ) { if (preCheck()) { _cur._locale.enter(); try { return _isLeftOf( cursor ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isLeftOf( cursor ); } finally { _cur._locale.exit(); } } }
    public boolean isAtSamePositionAs ( XmlCursor cursor ) { if (preCheck()) { _cur._locale.enter(); try { return _isAtSamePositionAs( cursor ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isAtSamePositionAs( cursor ); } finally { _cur._locale.exit(); } } }
    public boolean isRightOf ( XmlCursor cursor ) { if (preCheck()) { _cur._locale.enter(); try { return _isRightOf( cursor ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _isRightOf( cursor ); } finally { _cur._locale.exit(); } } }
    
    public XmlCursor execQuery ( String query ) { if (preCheck()) { _cur._locale.enter(); try { return _execQuery( query ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _execQuery( query ); } finally { _cur._locale.exit(); } } }
    public XmlCursor execQuery ( String query, XmlOptions options ) { if (preCheck()) { _cur._locale.enter(); try { return _execQuery( query, options ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _execQuery( query, options ); } finally { _cur._locale.exit(); } } }
    public ChangeStamp getDocChangeStamp ( ) { if (preCheck()) { _cur._locale.enter(); try { return _getDocChangeStamp(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getDocChangeStamp(); } finally { _cur._locale.exit(); } } }
    public void setBookmark ( XmlBookmark bookmark ) { if (preCheck()) { _cur._locale.enter(); try { _setBookmark( bookmark ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _setBookmark( bookmark ); } finally { _cur._locale.exit(); } } }
    public XmlBookmark getBookmark ( Object key ) { if (preCheck()) { _cur._locale.enter(); try { return _getBookmark( key ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _getBookmark( key ); } finally { _cur._locale.exit(); } } }
    public void clearBookmark ( Object key ) { if (preCheck()) { _cur._locale.enter(); try { _clearBookmark( key ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _clearBookmark( key ); } finally { _cur._locale.exit(); } } }
    public void getAllBookmarkRefs ( Collection listToFill ) { if (preCheck()) { _cur._locale.enter(); try { _getAllBookmarkRefs( listToFill ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _getAllBookmarkRefs( listToFill ); } finally { _cur._locale.exit(); } } }
    public boolean removeXml ( ) { if (preCheck()) { _cur._locale.enter(); try { return _removeXml(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _removeXml(); } finally { _cur._locale.exit(); } } }

    public boolean removeXmlContents ( ) { if (preCheck()) { _cur._locale.enter(); try { return _removeXmlContents(); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _removeXmlContents(); } finally { _cur._locale.exit(); } } }
    public int removeChars ( int maxCharacterCount ) { if (preCheck()) { _cur._locale.enter(); try { return _removeChars( maxCharacterCount ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { return _removeChars( maxCharacterCount ); } finally { _cur._locale.exit(); } } }
    public void insertChars ( String text ) { if (preCheck()) { _cur._locale.enter(); try { _insertChars( text ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertChars( text ); } finally { _cur._locale.exit(); } } }
    public void insertElement ( QName name ) { if (preCheck()) { _cur._locale.enter(); try { _insertElement( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertElement( name ); } finally { _cur._locale.exit(); } } }
    public void insertElement ( String localName ) { if (preCheck()) { _cur._locale.enter(); try { _insertElement( localName ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertElement( localName ); } finally { _cur._locale.exit(); } } }
    public void insertElement ( String localName, String uri ) { if (preCheck()) { _cur._locale.enter(); try { _insertElement( localName, uri ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertElement( localName, uri ); } finally { _cur._locale.exit(); } } }
    public void beginElement ( QName name ) { if (preCheck()) { _cur._locale.enter(); try { _beginElement( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _beginElement( name ); } finally { _cur._locale.exit(); } } }
    public void beginElement ( String localName ) { if (preCheck()) { _cur._locale.enter(); try { _beginElement( localName ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _beginElement( localName ); } finally { _cur._locale.exit(); } } }
    public void beginElement ( String localName, String uri ) { if (preCheck()) { _cur._locale.enter(); try { _beginElement( localName, uri ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _beginElement( localName, uri ); } finally { _cur._locale.exit(); } } }
    public void insertElementWithText ( QName name, String text ) { if (preCheck()) { _cur._locale.enter(); try { _insertElementWithText( name, text ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertElementWithText( name, text ); } finally { _cur._locale.exit(); } } }
    public void insertElementWithText ( String localName, String text ) { if (preCheck()) { _cur._locale.enter(); try { _insertElementWithText( localName, text ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertElementWithText( localName, text ); } finally { _cur._locale.exit(); } } }
    public void insertElementWithText ( String localName, String uri, String text ) { if (preCheck()) { _cur._locale.enter(); try { _insertElementWithText( localName, uri, text ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertElementWithText( localName, uri, text ); } finally { _cur._locale.exit(); } } }
    public void insertAttribute ( String localName ) { if (preCheck()) { _cur._locale.enter(); try { _insertAttribute( localName ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertAttribute( localName ); } finally { _cur._locale.exit(); } } }
    public void insertAttribute ( String localName, String uri ) { if (preCheck()) { _cur._locale.enter(); try { _insertAttribute( localName, uri ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertAttribute( localName, uri ); } finally { _cur._locale.exit(); } } }
    public void insertAttribute ( QName name ) { if (preCheck()) { _cur._locale.enter(); try { _insertAttribute( name ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertAttribute( name ); } finally { _cur._locale.exit(); } } }
    public void insertAttributeWithValue ( String Name, String value ) { if (preCheck()) { _cur._locale.enter(); try { _insertAttributeWithValue( Name, value ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertAttributeWithValue( Name, value ); } finally { _cur._locale.exit(); } } }
    public void insertAttributeWithValue ( String name, String uri, String value ) { if (preCheck()) { _cur._locale.enter(); try { _insertAttributeWithValue( name, uri, value ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertAttributeWithValue( name, uri, value ); } finally { _cur._locale.exit(); } } }
    public void insertAttributeWithValue ( QName name, String value ) { if (preCheck()) { _cur._locale.enter(); try { _insertAttributeWithValue( name, value ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertAttributeWithValue( name, value ); } finally { _cur._locale.exit(); } } }
    public void insertNamespace ( String prefix, String namespace ) { if (preCheck()) { _cur._locale.enter(); try { _insertNamespace( prefix, namespace ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertNamespace( prefix, namespace ); } finally { _cur._locale.exit(); } } }
    public void insertComment ( String text ) { if (preCheck()) { _cur._locale.enter(); try { _insertComment( text ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertComment( text ); } finally { _cur._locale.exit(); } } }
    public void insertProcInst ( String target, String text ) { if (preCheck()) { _cur._locale.enter(); try { _insertProcInst( target, text ); } finally { _cur._locale.exit(); } } else synchronized ( _cur._locale ) { _cur._locale.enter(); try { _insertProcInst( target, text ); } finally { _cur._locale.exit(); } } }
    
    //
    //
    //

    private Cur        _cur;
    private PathEngine _pathEngine;
    private int        _currentSelection;

    private ChangeListener _nextChangeListener;
} 