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

package org.apache.xmlbeans.impl.store;

import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.common.GlobalLock;
import org.apache.xmlbeans.impl.store.Root.ChangeListener;
import org.apache.xmlbeans.impl.store.Saver.XmlInputStreamImpl;
import org.apache.xmlbeans.impl.store.Splay.Annotation;
import org.apache.xmlbeans.impl.store.Splay.Attr;
import org.apache.xmlbeans.impl.store.Splay.Begin;
import org.apache.xmlbeans.impl.store.Splay.Comment;
import org.apache.xmlbeans.impl.store.Splay.CursorGoober;
import org.apache.xmlbeans.impl.store.Splay.Goober;
import org.apache.xmlbeans.impl.store.Splay.Procinst;
import org.apache.xmlbeans.impl.store.Splay.Xmlns;
import org.apache.xmlbeans.XmlCursor.ChangeStamp;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlRuntimeException;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;
import org.apache.xmlbeans.xml.stream.XMLInputStream;

public final class Cursor implements XmlCursor, ChangeListener
{
    Cursor ( Root r, Splay s )        { assert s != null; _data = CursorData.getOne( r ); set( s ); }
    Cursor ( Root r, Splay s, int p ) { assert s != null; _data = CursorData.getOne( r ); set( s, p ); }

    //
    //
    //

    public Object monitor()
    {
        return getRoot();
    }

    Root  getRoot ( ) { return _data._goober.getRoot(); }

    Splay getSplay ( ) { return _data._goober.getSplay(); }
    int   getPos   ( ) { return _data._goober.getPos(); }
    
    void set ( Splay s, int p ) { _data._goober.set( s, p ); }
    void set ( Splay s        ) { _data._goober.set( s, 0 ); }
    void set ( int p          ) { _data._goober.set( p ); }
    void set ( Goober g       ) { _data._goober.set( g ); }

    int getPostCch ( )
    {
        int p = getPos();

        if (p == 0)
            return 0;

        Splay s = getSplay();
        
        int pa = s.getPosAfter();

        assert p >= pa || s.isLeaf();
        
        return p >= pa ? s.getCchAfter() - p + pa : s.getPosLeafEnd() - p;
    }
    
    int getPreCch ( )
    {
        // TODO - quick and dirty impl, improve

        Splay sOrig = getSplay();
        int   pOrig = getPos();

        int n = toPrevChar( -1 );

        set( sOrig, pOrig );

        return n;
    }
    
    private void checkDisposed ( )
    {
        checkDisposed( this );
    }
    
    private static void checkDisposed ( Cursor c )
    {
        if (c.isDisposed())
            throw new IllegalStateException( "Cursor has been disposed" );
    }
    
    boolean isDisposed ( )
    {
        return _data == null;
    }

    //
    // XmlCursor Methods
    //

    public void dispose ( )
    {
        synchronized ( monitor() )
        {
            if (!isDisposed())
            {
                _data.release(true);
                _data = null;
            }
        }
    }

    public XmlObject getObject ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            Root r = getRoot();
            
            if (getPos() > 0)
                return null;
    
            Splay s = getSplay();
    
            if (!s.isTypeable())
                return null;
    
            Type t = s.getType( r );
    
            assert t != null;
    
            XmlObject result = t.getXmlObject();
            assert result != null;
            return result;
        }
    }
    
    public boolean toCursor ( XmlCursor moveTo )
    {
        if (moveTo == null)
            throw new IllegalArgumentException( "Invalid destination cursor" );

        if (monitor() == moveTo.monitor())
        {
            synchronized ( monitor() )
            {
                return toCursorImpl( moveTo );
            }
        }
        else
        {
            boolean acquired = false;
            try
            {
                GlobalLock.acquire();
                acquired = true;
                synchronized ( monitor() )
                {
                    synchronized (moveTo.monitor())
                    {
                        GlobalLock.release();
                        acquired = false;
                        
                        return toCursorImpl( moveTo );
                    }
                }
            }
            catch (InterruptedException e)
            {
                throw new XmlRuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (acquired)
                    GlobalLock.release();
            }
        }
    }
    
    private boolean toCursorImpl ( XmlCursor moveTo )
    {
        checkDisposed();
        
        Cursor c = null;
        
        if (moveTo instanceof Cursor)
        {
            c = (Cursor) moveTo;

            checkDisposed( c );
            
            if (c.getRoot() != getRoot())
                c = null;
        }
        
        if (c == null)
            return false;

        set( c._data._goober );

        return true;
    }
    
    public XmlDocumentProperties documentProperties ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            return getRoot().documentProperties();
        }
    }
    
    public XmlCursor newCursor ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            return new Cursor( getRoot(), getSplay(), getPos() );
        }
    }
    
    public boolean toBookmark ( XmlBookmark bm )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            if (bm == null)
                return false;
    
            if (!(bm._currentMark instanceof Annotation))
                return false;
    
            Annotation a = (Annotation) bm._currentMark;
    
            if (a.getRoot() != getRoot())
                return false;
    
            assert a.getSplay() != null;
    
            set( a );
    
            return true;
        }
    }
    
    public XmlBookmark toNextBookmark ( Object key )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (key == null)
                return null;
    
            Splay sOrig = getSplay();
            int   pOrig = getPos();
    
            TokenType tt = currentTokenType();
    
            // Advance the cursor past the current spot by the minimun amount
    
            if (tt.isText())
            {
                toNextChar( 1 );
                tt = currentTokenType();
            }
            else if ((tt = toNextToken()).isNone())
            {
                set( sOrig, pOrig );
                return null;
            }
    
            for ( ; ; )
            {
                XmlBookmark bm = getBookmark( key );
    
                if (bm != null)
                    return bm;
    
                int postCch;
    
                if (tt.isText() && (postCch = getPostCch()) > 1)
                {
                    Splay s = getSplay();
                    int   p = getPos();
                    int   d = postCch;
    
                    for ( Goober g = s.firstGoober() ; g != null ;
                          g = s.nextGoober( g ) )
                    {
                        int dist;
                        XmlBookmark mark;
    
                        if (g.isAnnotation() && (dist = g.getPos() - p) > 1 &&
                                dist < d && (mark = g.getBookmark()) != null &&
                                    mark.getKey().equals( key ))
                        {
                            bm = mark;
                            d = dist;
                        }
                    }
    
                    if (bm != null)
                    {
                        set( s, p + d );
                        return bm;
                    }
                }
                
                if ((tt = toNextToken()).isNone())
                {
                    set( sOrig, pOrig );
                    return null;
                }
            }
        }
    }

    public XmlBookmark toPrevBookmark ( Object key )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (key == null)
                return null;
    
            Splay sOrig = getSplay();
            int   pOrig = getPos();
    
            TokenType tt = prevTokenType();
    
            // Retreat the cursor past the current spot by the minimun amount
    
            if (tt.isText())
            {
                toPrevChar( 1 );
                tt = prevTokenType();
            }
            else if (toPrevToken().isNone())
            {
                set( sOrig, pOrig );
                return null;
            }
            else
                tt = prevTokenType();
    
            for ( ; ; )
            {
                XmlBookmark bm = getBookmark( key );
    
                if (bm != null)
                    return bm;
    
                int preCch;
    
                if (tt.isText() && (preCch = getPreCch()) > 1)
                {
                    Splay s;
                    int   p;
    
                    if (getPos() == 0)
                    {
                        s = getSplay().prevNonAttrSplay();
                        p = s.getEndPos();
                    }
                    else
                    {
                        s = getSplay();
                        p = getPos();
                    }
                    
                    int d = preCch;
    
                    for ( Goober g = s.firstGoober() ; g != null ;
                          g = s.nextGoober( g ) )
                    {
                        int dist;
                        XmlBookmark mark;
    
                        if (g.isAnnotation() && (dist = p - g.getPos()) > 1 &&
                                dist < d && (mark = g.getBookmark()) != null &&
                                    mark.getKey().equals( key ))
                        {
                            bm = mark;
                            d = dist;
                        }
                    }
    
                    if (bm != null)
                    {
                        set( s, p - d );
                        return bm;
                    }
                }
    
                if (tt.isText())
                {
                    toPrevChar( -1 );
                    tt = prevTokenType();
                }
                else if (toPrevToken().isNone())
                {
                    set( sOrig, pOrig );
                    return null;
                }
                else
                    tt = prevTokenType();
            }
        }
    }
    
    public TokenType currentTokenType ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            return getSplay().getTokenType( getPos() );
        }
    }
    
    public boolean isStartdoc  ( ) { return currentTokenType().isStartdoc(); }
    public boolean isEnddoc    ( ) { return currentTokenType().isEnddoc(); }
    public boolean isStart     ( ) { return currentTokenType().isStart(); }
    public boolean isEnd       ( ) { return currentTokenType().isEnd(); }
    public boolean isText      ( ) { return currentTokenType().isText(); }
    public boolean isAttr      ( ) { return currentTokenType().isAttr(); }
    public boolean isNamespace ( ) { return currentTokenType().isNamespace(); }
    public boolean isComment   ( ) { return currentTokenType().isComment(); }
    public boolean isProcinst  ( ) { return currentTokenType().isProcinst(); }
    public boolean isContainer ( ) { return currentTokenType().isContainer(); }
    public boolean isFinish    ( ) { return currentTokenType().isFinish(); }
    public boolean isAnyAttr   ( ) { return currentTokenType().isAnyAttr(); }
    
    public TokenType prevTokenType ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            // TODO - quick and dirty implementation, improve
    
            Splay sOrig = getSplay();
            int   pOrig = getPos();
    
            TokenType tt;
    
            if (toPrevChar( 1 ) == 1)
                tt = TokenType.TEXT;
            else if (!(tt = toPrevToken()).isNone())
                tt = currentTokenType();
    
            set( sOrig, pOrig );
    
            return tt;
        }
    }
    
    public TokenType toNextToken ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            Splay os = getSplay(); // Orignal splay
            Splay s = os;
            int   p = getPos();
    
            if (p == 0)
            {
                if (s.isRoot())
                    return TokenType.NONE;
    
                //
                // Look see if there is an attr we should visit before visiting
                // any following content in this container.
                //
                
                if (s.isContainer())
                {
                    Splay t = s.nextSplay();
    
                    if (t.isAttr())
                    {
                        set( t, 0 );
                        return currentTokenType();
                    }
                        
                    //
                    // Now we're going into the content of this container.  Flush
                    // out any cached type value.
                    //
    
                    s.ensureContentValid();
                }
    
                if (s.getMaxPos() > 0)
                    p = 1;
                else
                {
                    s = s.nextSplay();
                    p = 0;
                }
            }
            else
            {
                assert p > 0;
                assert !s.isRoot();
                
                if (p >= s.getPosAfter() && s.getCchAfter() > 0)
                {
                    s = s.nextSplay();
                    p = 0;
                }
                else
                {
                    assert s.isLeaf();
                    assert p < s.getPosAfter();
    
                    if (p != s.getPosLeafEnd())
                        p = s.getPosLeafEnd();
                    else if (s.getCchAfter() > 0)
                        p = s.getPosAfter();
                    else
                    {
                        s = s.nextSplay();
                        p = 0;
                    }
                }
            }
    
            //
            // If we are transitioning from an attr to a non attr, see if there
            // is content in a DOC or BEGIN which needs to be visited after
            // the attributes.
            //
            // Also, if we are transitioning from an attr container (BEGIN or
            // DOC) to an attr, where the attr container has interior content,
            // we have already visited the attrs and must skip them now.
            //
            // Also, if we are transitioning from pos 0 to pos 1 on an attr
            // container, we need to visit any attributes before visiting the
            // interior content of the attr container.
            //
    
            if (p == 0)
            {
                if (!s.isAttr() && os.isAttr())
                {
                    Splay t = os.prevNonAttrSplay();
    
                    assert t.isContainer();
    
                    //
                    // We're navigating to the content of a container.  Flush
                    // out any cached type value.
                    //
                    
                    t.ensureContentValid();
    
                    if (t.getMaxPos() > 0)
                    {
                        s = t;
                        p = 1;
                    }
                }
                else if (s.isAttr() && !os.isAttr() && os.getMaxPos() > 0)
                {
                    assert os.isContainer();
    
                    s = s.nextNonAttrSplay();
                }
            }
    
            set( s, p );
    
            return currentTokenType();
        }
    }

    public TokenType toPrevToken ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
    // TODO - This code is not as compact as it can be, there is some redundancy
    // -- rethink it later ...
    
            Splay s = getSplay();
            int   p = getPos();
    
            if (p == 1 && s.isInvalid())
            {
                assert s.isLeaf();
                p += s.ensureContentValid();
            }
    
            if (p == 1 && s.isContainer())
            {
                Splay t = s.nextSplay();
    
                if (t.isAttr())
                {
                    s = t;
                    
                    for ( t = t.nextSplay() ; t.isAttr() ; t = t.nextSplay() )
                        s = t;
    
                    set( s, 0 );
                    
                    return currentTokenType();
                }
            }
    
            if (p == 0 && !s.isAttr())
            {
                if (s.isDoc())
                    return TokenType.NONE;
    
                Splay t = s.prevSplay();
    
                if (t.isAttr())
                {
                    t = t.prevNonAttrSplay();
    
                    assert t.isContainer();
    
                    if (t.isDoc())
                        t.ensureContentValid();
    
                    if (t.getMaxPos() > 0)
                    {
                        set(
                            t,
                            t.getCchAfter() > 0 ? t.getPosAfter() : t.getMaxPos() );
    
                        return currentTokenType();
                    }
                }
            }
    
            if (s.isAttr())
            {
                assert p == 0;
    
                Splay t = s.prevSplay();
    
                if (!t.isAttr())
                {
                    assert t.isContainer();
    
                    set( t, 0 );
                    return currentTokenType();
                }
            }
    
            if (p == 0)
            {
                if (s.isDoc())
                    return TokenType.NONE;
    
                s = s.prevSplay();
    
                if (s.isDoc())
                    s.ensureContentValid();
    
                p = s.getCchAfter() > 0 ? s.getPosAfter() : s.getMaxPos();
            }
            else
            {
                assert p > 0;
                assert !s.isRoot();
    
                int posAfter = s.getPosAfter();
    
                if (p >= posAfter)
                {
                    assert s.getCchAfter() > 0;
                    p = posAfter - 1;
                }
                else
                {
                    assert s.isValid();
                    assert s.isLeaf();
                    
                    p = p > 1 && p == posAfter - 1 ? 1 : 0;
                }
            }
    
            set( s, p );
    
            return currentTokenType();
        }
    }

    public void insertChars ( String text )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            Splay s = getSplay();
            int   p = getPos();
            
            if (p == 0)
            {
                if (s.isDoc() || s.isAttr())
                    throw new IllegalStateException( "Invalid location for text" );
    
                s = s.prevNonAttrSplay();
                p = s.getEndPos();
            }
    
            if (text == null)
                return;
    
            int cch = text.length();
    
            if (cch > 0)
                s.insertChars( p, getRoot(), text, 0, cch );
        }
    }

    private static void validateLocalName ( QName name )
    {
        if (name == null)
            throw new IllegalArgumentException( "QName is null" );
        
        validateLocalName( name.getLocalPart() );
    }

    private static void validateLocalName ( String name )
    {
        if (name == null)
            throw new IllegalArgumentException( "Name is null" );

        if (name.length() == 0)
            throw new IllegalArgumentException( "Name is empty" );

        if (!XMLChar.isValidNCName( name ))
            throw new IllegalArgumentException( "Name is not valid" );
    }

    private static void validatePrefix ( String name )
    {
        if (name == null)
            throw new IllegalArgumentException( "Prefix is null" );

        if (name.length() == 0)
            throw new IllegalArgumentException( "Prefix is empty" );

        if (Splay.beginsWithXml( name ))
            throw new IllegalArgumentException( "Prefix begins with 'xml'" );

        if (!XMLChar.isValidNCName( name ))
            throw new IllegalArgumentException( "Prefix is not valid" );
    }

    private void insertAttribute ( QName name, String value )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            insert( new Attr( name ), value );
        }
    }

    public void insertAttribute ( String name )
    {
        insertAttributeWithValue( name, null, null );
    }

    public void insertAttribute ( String name, String uri )
    {
        insertAttributeWithValue( name, uri, null );
    }

    public void insertAttributeWithValue ( String name, String value )
    {
        insertAttributeWithValue( name, null, value );
    }

    public void insertAttributeWithValue (
        String name, String uri, String value )
    {
        validateLocalName( name );

        insertAttribute( new QName( uri, name ), value );
    }

    public void insertAttribute ( QName name )
    {
        validateLocalName( name );

        insertAttribute( name, null );
    }

    public void insertAttributeWithValue ( QName name, String value )
    {
        validateLocalName( name );

        insertAttribute( name, value );
    }

    public void insertNamespace ( String prefix, String namespace )
    {
        synchronized ( monitor() )
        {
            if (prefix == null)
                prefix = "";
            else if (prefix.length() > 0)
                validatePrefix( prefix );
    
            if (namespace == null)
                namespace = "";
    
            if (namespace.length() == 0 && prefix.length() > 0)
            {
                throw
                    new IllegalArgumentException(
                        "Can't map a prefix to no namespace" );
            }
    
            insert( new Xmlns( new QName( namespace, prefix ) ), null );
        }
    }

    public void insertComment ( String value )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            insert( new Comment(), value );
        }
    }

    public void insertProcInst ( String target, String value )
    {
        validateLocalName( target ); // used becuase "<?xml...?> is disallowed

        if (Splay.beginsWithXml( target ) && target.length() == 3)
            throw new IllegalArgumentException( "Target begins with 'xml'" );
    
        synchronized ( monitor() )
        {
            checkDisposed();
    
            insert( new Procinst( target ), value );
        }
    }

    public void insertElement ( String name )
    {
        insertElementWithText( name, null, null );
    }

    public void insertElementWithText ( String name, String text )
    {
        insertElementWithText( name, null, text );
    }

    public void insertElement ( String name, String uri )
    {
        insertElementWithText( name, uri, null );
    }
    
    public void insertElement ( QName name )
    {
        insertElementWithText( name, null );
    }
    
    public void beginElement ( QName name )
    {
        insertElement( name );
        toPrevToken();
    }
    
    public void beginElement ( String name )
    {
        insertElement( name );
        toPrevToken();
    }
    
    public void beginElement ( String name, String uri )
    {
        insertElement( name, uri );
        toPrevToken();
    }

    public void insertElementWithText ( QName name, String text )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            validateLocalName( name.getLocalPart() );
    
            Begin b = new Begin( name, null );
            
            b.toggleIsLeaf();
            
            insert( b, text );
        }
    }
    
    public void insertElementWithText ( String name, String uri, String text )
    {
        insertElementWithText( new QName( uri, name ), text );
    }

    void insert ( Splay sInsert, String value )
    {
        assert !isDisposed();
        assert Root.dv > 0 || sInsert.getRootSlow() == null;
        assert sInsert.getCch() == 0;

        if (value != null)
            sInsert.adjustCch( value.length() );

        Splay s = getSplay();
        int   p = getPos();

        sInsert.checkInsertionValidity( 0, s, p, false );

        if (value != null)
            s.insert( getRoot(), p, sInsert, value, 0, value.length(), true );
        else
            s.insert( getRoot(), p, sInsert, null, 0, 0, true );

        assert validate();
    }

    public String getTextValue ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
    
            if (getPos() > 0 || s.isFinish() || s.isXmlns())
            {
                throw new IllegalStateException(
                    "Can't get text value, current token can have no text value" );
            }
    
            return getSplay().getText( getRoot() );
        }
    }

    public int getTextValue ( char[] buf, int off, int cch )
    {
//        synchronized ( monitor() )
//        {
//            checkDisposed();
//    
//            Splay s = getSplay();
//    
//            if (getPos() > 0 || s.isFinish() || s.isXmlns())
//            {
//                throw new IllegalStateException(
//                    "Can't get text value, current token can have no text value" );
//            }
//    
//            return getSplay().getText( getRoot() );
//        }

        // Hack impl for now
        
        String s = getTextValue();

        int n = s.length();

        if (n > cch)
            n = cch;

        if (n <= 0)
            return 0;

        s.getChars( 0, n, buf, off );

        return n;
    }
    
    public void setTextValue ( String text )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
            int   p = getPos();
    
            if (p > 0 || s.isXmlns() || s.isFinish())
            {
                throw new IllegalStateException(
                    "Can't set text value, current token can have no text value" );
            }
    
    
            s.setText( getRoot(), text, 0, text == null ? 0 : text.length() );
        }
    }

    public void setTextValue ( char[] buf, int off, int len )
    {
        setTextValue( String.copyValueOf( buf, off, len ) );
    }
    
    public String getChars ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            int cch = -1;
    
            int postCch = getPostCch();
    
            if (cch < 0 || cch > postCch)
                cch = postCch;
    
            return
                getRoot()._text.fetch(
                    getSplay().getCpForPos( getRoot(), getPos() ), cch );
        }
    }

    public int getChars ( char[] buf, int off, int cch )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            int postCch = getPostCch();
    
            if (cch < 0 || cch > postCch)
                cch = postCch;
    
            if (buf == null || off >= buf.length)
                return 0;
    
            if (buf.length - off < cch)
                cch = buf.length - off;
    
            getRoot()._text.fetch(
                buf, off, getSplay().getCpForPos( getRoot(), getPos() ), cch );
    
            return cch;
        }
    }

    public void setBookmark ( XmlBookmark annotation )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (annotation == null)
                return;
    
            clearBookmark( annotation.getKey() );
    
            Annotation a = new Annotation( getRoot(), annotation );
    
            if (a._key == null)
                throw new IllegalArgumentException( "Annotation key is null" );
    
            a.set( _data._goober );
            annotation._currentMark = a;
        }
    }

    public XmlBookmark getBookmark ( Object key )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (key == null)
                return null;
    
            Splay s = getSplay();
            int   p = getPos();
    
            for ( Goober g = s.firstGoober() ; g != null ; g = s.nextGoober( g ) )
            {
                if (g.getKind() == Splay.ANNOTATION && g.getPos() == p)
                {
                    Annotation a = (Annotation) g;
    
                    XmlBookmark xa = a.getXmlBookmark();
    
                    if (xa != null && a._key.equals( key ))
                        return xa;
                }
            }
    
            return null;
        }
    }

    public void clearBookmark ( Object key )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (key == null)
                return;
    
            Splay s = getSplay();
            int   p = getPos();
    
            for ( Goober g = s.firstGoober() ; g != null ; g = s.nextGoober( g ) )
            {
                if (g.getKind() == Splay.ANNOTATION && g.getPos() == p)
                {
                    Annotation a = (Annotation) g;
    
                    XmlBookmark xa = a.getXmlBookmark();
    
                    if (xa != null && a._key.equals( key ))
                    {
                        g.set( null, 0 );
                        return;
                    }
                }
            }
        }
    }

    public void getAllBookmarkRefs ( Collection listToFill )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (listToFill == null)
                return;
    
            Splay s = getSplay();
            int   p = getPos();
    
            for ( Goober g = s.firstGoober() ; g != null ; g = s.nextGoober( g ) )
            {
                if (g.getKind() == Splay.ANNOTATION && g.getPos() == p)
                    listToFill.add( ((Annotation) g).getXmlBookmark() );
            }
        }
    }

    public boolean hasNextToken ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            assert !getSplay().isRoot() || getPos() == 0;
            return !getSplay().isRoot();
        }
    }

    public boolean hasPrevToken ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();

            return !getSplay().isDoc() || getPos() > 0;
        }
    }

    public QName getName ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (getPos() > 0)
                return null;
    
            Splay s = getSplay();
    
            switch ( s.getKind() )
            {
            case Splay.BEGIN :
            case Splay.ATTR :
            case Splay.PROCINST :
                return s.getName();
            }
    
            return null;
        }
    }

    public void setName ( QName name )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (name == null)
                throw new IllegalArgumentException( "Name is null" );
            
            Splay s = getSplay();
            
            if (getPos() > 0 || !(s.isBegin() || s.isAttr() || s.isProcinst()))
            {
                throw
                    new IllegalStateException(
                        "Can't set name here: " + currentTokenType() );
            }
    
            if (s.isProcinst())
            {
                validatePrefix( name.getLocalPart() );
    
                if (name.getNamespaceURI().length() > 0)
                {
                    throw
                        new IllegalArgumentException(
                            "Procinst name must have no URI" );
                }
            }
            else if (s.isXmlns())
            {
                if (name.getLocalPart().length() > 0)
                    validatePrefix( name.getLocalPart() );
            }
            else
                validateLocalName( name.getLocalPart() );
    
            s.setName( getRoot(), name );
        }
    }
    
    public int toNextChar ( int cch )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
            int   p = getPos();
    
            int maxCch = getPostCch();
    
            if (maxCch == 0 || cch == 0)
                return 0;
    
            if (cch < 0 || cch > maxCch)
                cch = maxCch;
    
            assert p + cch <= s.getEndPos();
    
            if (p + cch == s.getEndPos())
                toNextToken();
            else
                set( p + cch );
    
            return cch;
        }
    }

    public int toPrevChar ( int cch )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
            int   p = getPos();
    
            Splay sText = s;  // The splay and pos where the text exists
            int   pText = p;
            int   maxCch = 0; // Max chars to move over
    
            if (p == 0)
            {
                if (!s.isDoc() && !s.isAttr())
                {
                    sText = s.prevNonAttrSplay();
                    pText = sText.getEndPos();
                    maxCch = sText.getCchAfter();
                }
            }
            else if (s.isLeaf() && p <= s.getPosLeafEnd())
            {
                int dCch = s.ensureContentValid();
                p += dCch;
                pText = p;
                maxCch = p - 1;
            }
            else
                maxCch = p - s.getPosAfter();
    
            assert pText <= sText.getEndPos();
    
            if (maxCch == 0 || cch == 0)
                return 0;
    
            if (cch < 0 || cch > maxCch)
                cch = maxCch;
    
            set( sText, pText - cch );
    
            return cch;
        }
    }

    public void toEndDoc ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            set( getRoot(), 0 );
        }
    }

    public void toStartDoc ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            set( getRoot()._doc, 0 );
        }
    }

    public TokenType toFirstContentToken ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
    
            if (getPos() > 0 || !s.isContainer())
                return TokenType.NONE;
    
            s.ensureContentValid();
    
            if (s.getCch() > 0 || s.isLeaf())
                set( 1 );
            else
                set( s.nextNonAttrSplay(), 0 );
    
            return currentTokenType();
        }
    }

    public TokenType toEndToken ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
    
            if (getPos() > 0 || !s.isContainer())
                return TokenType.NONE;
    
            if (s.isLeaf())
                set( s.getPosLeafEnd() );
            else
                set( s.getFinishSplay() );
    
            return currentTokenType();
        }
    }

    public boolean toParent ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
            int   p = getPos();
    
            if (p == 0 && s.isDoc())
                return false;
    
            set( s.getContainer( p ), 0 );
    
            return true;
        }
    }

    public boolean toNextSibling ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
            int   p = getPos();
    
            if (p == 0)
            {
                if (s.isDoc())
                    return false;
    
                if (s.isBegin())
                    s = s.getFinishSplay().nextSplay();
            }
            else
            {
                if (s.isLeaf() && p <= s.getPosLeafEnd())
                    return false;
    
                s = s.nextSplay();
            }
    
            for ( ; !s.isBegin() ; s = s.nextSplay() )
            {
                if (s.isFinish())
                    return false;
            }
    
            set( s, 0 );
    
            return true;
        }
    }

    public boolean toNextSibling ( String name )
    {
        return toNextSibling( new QName( name ) );
    }

    public boolean toNextSibling ( String namespace, String name )
    {
        return toNextSibling( new QName( namespace, name ) );
    }

    public boolean toNextSibling ( QName name )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            Splay sOriginal = getSplay();
            int   pOriginal = getPos();
            
            for ( ; ; )
            {
                if (!toNextSibling())
                    break;
    
                if (getName().equals( name ))
                    return true;
            }
            
            set( sOriginal, pOriginal );
            
            return false;
        }
    }
    
    public boolean toPrevSibling ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
            int   p = getPos();
    
            if (p == 0)
            {
                if (s.isDoc() || s.isAttr())
                    return false;
    
                s = s.prevSplay();
            }
            else
            {
                assert p > 0;
    
                if (s.isContainer())
                {
                    if (s.isLeaf())
                    {
                        if (p <= s.getPosLeafEnd())
                            return false;
    
                        set( 0 );
    
                        return true;
                    }
    
                    return false;
                }
            }
    
            for ( ; ; )
            {
                if (s.isEnd())
                {
                    s = s.getContainer();
                    break;
                }
    
                if (s.isLeaf())
                    break;
    
                if (s.isContainer())
                    return false;
    
                s = s.prevSplay();
            }
    
            set( s, 0 );
    
            return true;
        }
    }

    private Splay getStart ( )
    {
        checkDisposed();

        Splay s = getSplay();

        if (!s.isContainer() || getPos() != 0)
        {
            push();

            if (toNextSibling())
                s = getSplay();
            else
                s = null;

            pop();
        }

        return s;
    }

    public boolean toFirstChild ( )
    {
        return toChild( (QName) null );
    }

    public boolean toChild ( String name )
    {
        return toChild( new QName( name ) );
    }

    public boolean toChild ( String namespace, String name )
    {
        return toChild( new QName( namespace, name ) );
    }

    public boolean toChild ( QName name )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getRoot().findNthBegin( getStart(), name, null, 0 );
    
            if (s == null)
                return false;
    
            set( s, 0 );
    
            return true;
        }
    }

    public boolean toChild ( int n )
    {
        return toChild( null, n );
    }

    public boolean toChild ( QName name, int n )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getRoot().findNthBegin( getStart(), name, null, n );
    
            if (s == null)
                return false;
    
            set( s, 0 );
    
            return true;
        }
    }

    public boolean toLastChild ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay sOriginal = getSplay();
            int   pOriginal = getPos();
            
            if (!sOriginal.isContainer() || pOriginal != 0)
            {
                if (!toNextSibling())
                    return false;
            }
    
            if (!toEndToken().isNone() && toPrevSibling())
                return true;
    
            set( sOriginal, pOriginal );
            
            return false;
        }
    }

    public boolean toFirstAttribute ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay sOriginal = getSplay();
            int   pOriginal = getPos();
            
            if (!sOriginal.isContainer() || pOriginal != 0)
                return false;
    
            for ( Splay s = sOriginal.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isNormalAttr())
                {
                    set( s, 0 );
                    return true;
                }
            }
    
            set( sOriginal, pOriginal );
            return false;
        }
    }

    public boolean toLastAttribute ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay sOriginal = getSplay();
            int   pOriginal = getPos();
            
            if (!sOriginal.isContainer() || pOriginal != 0)
                return false;
    
            Splay lastNormalAttr = null;
    
            for ( Splay s = sOriginal.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isNormalAttr())
                    lastNormalAttr = s;
            }
    
            if (lastNormalAttr != null)
            {
                set( lastNormalAttr, 0 );
                return true;
            }
    
            set( sOriginal, pOriginal );
            return false;
        }
    }

    public boolean toNextAttribute ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
    
            if (!s.isAttr())
                return false;
    
            for ( s = s.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isNormalAttr())
                {
                    set( s, 0 );
                    return true;
                }
            }
    
            return false;
        }
    }

    public boolean toPrevAttribute ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
    
            if (!s.isAttr())
                return false;
    
            for ( s = s.prevSplay() ; s.isAttr() ; s = s.prevSplay() )
            {
                if (s.isNormalAttr())
                {
                    set( s, 0 );
                    return true;
                }
            }
    
            return false;
        }
    }

    public String getAttributeText ( QName attrName )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (attrName == null)
                throw new IllegalArgumentException( "Attr name is null" );
    
            if (getPos() > 0)
                return null;
    
            Splay s = getSplay().getAttr( attrName );
    
            return s == null ? null : s.getText( getRoot() );
        }
    }

    public boolean setAttributeText ( QName attrName, String value )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (attrName == null)
                throw new IllegalArgumentException( "Attr name is null" );
    
            validateLocalName( attrName.getLocalPart() );
            
            if (getPos() > 0)
                return false;
    
            Splay s = getSplay();
    
            if (!s.isContainer())
                return false;
    
            if (value == null)
                value = "";
    
            s = getSplay().getAttr( attrName );
    
            if (s == null)
            {
                XmlCursor c = newCursor();
    
                try
                {
                    // Insert the new attr at the end
                    
                    do {
                        c.toNextToken();
                    } while ( c.isAnyAttr() );
                    
                    c.insertAttributeWithValue( attrName, value );
                }
                finally
                {
                    c.dispose();
                }
            }
            else
                s.setText( getRoot(), value, 0, value.length() );
    
            return true;
        }
    }

    public boolean removeAttribute ( QName attrName )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (attrName == null)
                throw new IllegalArgumentException( "Attr name is null" );
    
            if (getPos() > 0)
                return false;
    
            boolean removed = false;
            
            for ( ; ; )
            {
                Splay s = getSplay().getAttr( attrName );
    
                if (s == null)
                    break;
                
                s.remove( getRoot(), true );
    
                removed = true;
            }
    
            return removed;
        }
    }

    public int removeChars ( int cch )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            int postCch = getPostCch();
    
            if (postCch == 0 || cch == 0)
                return 0;
    
            if (cch < 0 || cch > postCch)
                cch = postCch;
    
            return getSplay().removeChars( getRoot(), getPos(), cch );
        }
    }
    
    public int moveChars ( int cch, XmlCursor dst )
    {
        if (dst == null)
            throw new IllegalArgumentException( "Destination is null" );
            
        if (monitor() == dst.monitor())
        {
            synchronized ( monitor() )
            {
                return moveCharsImpl( cch, dst );
            }
        }
        else
        {
            boolean acquired = false;
            try
            {
                GlobalLock.acquire();
                acquired = true;
                synchronized ( monitor() )
                {
                    synchronized (dst.monitor())
                    {
                        GlobalLock.release();
                        acquired = false;
                        
                        return moveCharsImpl( cch, dst );
                    }
                }
            }
            catch (InterruptedException e)
            {
                throw new XmlRuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (acquired)
                    GlobalLock.release();
            }
        }
    }
    
    private int moveCharsImpl ( int cch, XmlCursor dst )
    {
        checkDisposed();

        if (dst == null || !(dst instanceof Cursor))
            throw new IllegalArgumentException( "Invalid destination cursor" );

        Cursor cDst = (Cursor) dst;

        checkDisposed( cDst );

        Root  rDst = cDst.getRoot();
        Splay sDst = cDst.getSplay();
        int   pDst = cDst.getPos();

        if (pDst == 0 && (sDst.isDoc() || sDst.isAttr()))
            throw new IllegalArgumentException( "Invalid destination" );

        return
            getSplay().moveChars(
                getRoot(), getPos(), cch, rDst, sDst, pDst, false );
    }

    public int copyChars ( int cch, XmlCursor dst )
    {
        if (dst == null)
            throw new IllegalArgumentException( "Destination is null" );
        
        if (dst.monitor() == monitor())
        {
            synchronized ( monitor() )
            {
                return copyCharsImpl( cch, dst );
            }
        }
        else
        {
            boolean acquired = false;
            try
            {
                GlobalLock.acquire();
                acquired = true;
                synchronized ( monitor() )
                {
                    synchronized (dst.monitor())
                    {
                        GlobalLock.release();
                        acquired = false;
                        
                        return copyCharsImpl( cch, dst );
                    }
                }
            }
            catch (InterruptedException e)
            {
                throw new XmlRuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (acquired)
                    GlobalLock.release();
            }
        }
    }
    
    private int copyCharsImpl ( int cch, XmlCursor dst )
    {
        checkDisposed();

        if (dst == null || !(dst instanceof Cursor))
            throw new IllegalArgumentException( "Invalid destination cursor" );

        Cursor cDst = (Cursor) dst;

        checkDisposed( cDst );

        Root  rDst = cDst.getRoot();
        Splay sDst = cDst.getSplay();
        int   pDst = cDst.getPos();

        if (pDst == 0)
        {
            if (sDst.isDoc() || sDst.isAttr())
                throw new IllegalArgumentException( "Invalid destination" );
            
            sDst = sDst.prevNonAttrSplay();
            pDst = sDst.getEndPos();
        }

        return
            getSplay().copyChars( getRoot(), getPos(), cch, rDst, sDst, pDst );
    }

    public String namespaceForPrefix ( String prefix )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
    
            if (getPos() > 0 || !s.isContainer())
                throw new IllegalStateException( "Not on a container" );
    
            return s.namespaceForPrefix( prefix, true );
        }
    }

    public String prefixForNamespace ( String ns )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (ns == null || ns.length() == 0)
                throw new IllegalArgumentException( "Must specify a namespace" );
    
            Splay s = getSplay();
    
            if (getPos() > 0 || !s.isContainer())
                throw new IllegalStateException( "Not on a container" );
    
            String result = s.prefixForNamespace( getRoot(), ns, null, true);

            assert result != null;

            return result;
        }
    }

    public void getAllNamespaces ( Map addToThis )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            Splay s = getSplay();
    
            if (getPos() > 0 || !s.isContainer())
                throw new IllegalStateException( "Not on a container" );
    
            // Do this with cursor for now...
            
            XmlCursor c = newCursor();
    
            do
            {
                assert c.isContainer();
    
                QName cName = c.getName();
                
                while ( !c.toNextToken().isNone() && c.isAnyAttr() )
                {
                    if (c.isNamespace())
                    {
                        String prefix = c.getName().getLocalPart();
                        String uri    = c.getName().getNamespaceURI();
    
                        // Here I check to see if there is a default namespace
                        // mapping which is not empty on a non root container which
                        // is in a namespace.  This this case, I do not want to add
                        // this mapping because it could not be persisted out this
                        // way.
                        
                        if (prefix.length() == 0 && uri.length() > 0 &&
                                cName != null && cName.getNamespaceURI().length()>0)
                        {
                            continue;
                        }
    
                        if (!addToThis.containsKey( prefix ))
                            addToThis.put( prefix, uri );
                    }
                }
    
                c.toParent();
            }
            while ( c.toParent() );
    
            c.dispose();
        }
    }
    
    /**
     * Returns:
     *
     *   -1 is this is left of that
     *    0 is this is left is at same position as that
     *    1 is this is right of that
     */

    public int comparePosition ( XmlCursor xthat )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            if (xthat == null || !(xthat instanceof Cursor))
                throw new IllegalArgumentException( "Invalid that cursor" );
    
            Cursor that = (Cursor) xthat;
            
            Root r = getRoot();
    
            if (r != that.getRoot())
                throw new IllegalArgumentException( "Cursors not in same document" );
    
            checkDisposed( that );
    
            return
                getSplay().compare( r, getPos(), that.getSplay(), that.getPos() );
        }
    }
    
    public boolean isLeftOf ( XmlCursor that )
    {
        return comparePosition( that ) < 0;
    }
    
    public boolean isAtSamePositionAs ( XmlCursor that )
    {
        return comparePosition( that ) == 0;
    }
    
    public boolean isRightOf ( XmlCursor that )
    {
        return comparePosition( that ) > 0;
    }

    public InputStream newInputStream ( XmlOptions options )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            return
                new Saver.InputStreamSaver(
                    getRoot(), getSplay(), getPos(), options );
        }
    }
    
    public InputStream newInputStream ( )
    {
        return newInputStream( null );
    }

    public Reader newReader ( )
    {
        return newReader( null );
    }
    
    public Reader newReader ( XmlOptions options )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            return new Saver.TextReader( getRoot(), getSplay(), getPos(), options );
        }
    }
    
    public XMLInputStream newXMLInputStream ( XmlOptions options )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            return
                new XmlInputStreamImpl( getRoot(), getSplay(), getPos(), options );
        }
    }
    
    public XMLInputStream newXMLInputStream ( )
    {
        return newXMLInputStream( null );
    }
    
    private static final XmlOptions _toStringOptions =
        buildPrettyOptions();

    static final XmlOptions buildPrettyOptions ( )
    {
        XmlOptions options = new XmlOptions();
        options.put( XmlOptions.SAVE_PRETTY_PRINT );
        options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
        options.put( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );
        return options;
    }

    public String toString (  )
    {
        return xmlText( _toStringOptions );
    }
    
    public String xmlText (  )
    {
        return xmlText( null );
    }
    
    public String xmlText ( XmlOptions options )
    {
        Saver.TextSaver saver;
        synchronized ( monitor() )
        {
            checkDisposed();
            
            saver = new Saver.TextSaver(
                    getRoot(), getSplay(), getPos(), options, null );
        }
        return saver.saveToString();
    }

    static class ChangeStampImpl implements ChangeStamp
    {
        ChangeStampImpl ( Root r )
        {
            _root = r;
            _versionStamp = _root.getVersion();
        }

        public boolean hasChanged ( )
        {
            return _versionStamp != _root.getVersion();
        }

        private final Root _root;
        private final long _versionStamp;
    }
    
    public ChangeStamp getDocChangeStamp ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
        
            return new ChangeStampImpl( getRoot() );
        }
    }
    
    public boolean removeXml ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            Splay s = getSplay();
            int   p = getPos();
    
            assert p < s.getEndPos();
    
            if (p > 0)
            {
                if (s.isLeaf() && p == s.getPosLeafEnd())
                    return false;
    
                int cchRemove = removeChars( getPostCch() );
    
                assert cchRemove > 0;
    
                return true;
            }
            else if (s.isDoc())
            {
                throw
                    new IllegalStateException(
                        "Can't remove a whole document." );
            }
            else if (s.isFinish())
                return false;
    
            s.remove( getRoot(), true );
    
            return true;
        }
    }
    
    public boolean moveXml ( XmlCursor dst )
    {
        if (dst == null)
            throw new IllegalArgumentException( "Destination is null" );
        
        if (dst.monitor() == monitor())
        {
            synchronized ( monitor() )
            {
                return moveXmlImpl( dst );
            }
        }
        else
        {
            boolean acquired = false;
            try
            {
                GlobalLock.acquire();
                acquired = true;
                synchronized ( monitor() )
                {
                    synchronized (dst.monitor())
                    {
                        GlobalLock.release();
                        acquired = false;
                        
                        return moveXmlImpl ( dst );
                    }
                }
            }
            catch (InterruptedException e)
            {
                throw new XmlRuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (acquired)
                    GlobalLock.release();
            }
        }
    }
    
    private boolean moveXmlImpl  ( XmlCursor dst )
    {
        checkDisposed();
                    
        if (dst == null || !(dst instanceof Cursor))
        {
            throw
                new IllegalArgumentException(
                    "Can't move to a foreign document" );
        }
            
        Cursor cDst = (Cursor) dst;
                    
        checkDisposed( cDst );
                    
        Root  rDst = cDst.getRoot();
        Splay sDst = cDst.getSplay();
        int   pDst = cDst.getPos();
            
        Root  rSrc = getRoot();
        Splay sSrc = getSplay();
        int   pSrc = getPos();
            
        if (sSrc.checkInsertionValidity( pSrc, sDst, pDst, true ))
        {
            return
                sSrc.moveChars(
                    rSrc, pSrc, getPostCch(), rDst, sDst, pDst, false ) > 0;
        }
                    
        assert pSrc == 0;
            
        // Check for a movement of stuff into itself!  This case is basically
        // a no-op
            
        if (rSrc == rDst && sDst.between( rDst, pDst, sSrc ))
        {
// TODO - I might have to orphan types in the range here ....
            return false;
        }
            
        assert pSrc == 0;
            
        sSrc.move( rSrc, cDst.getRoot(), cDst.getSplay(), cDst.getPos(), true );
            
        return true;
    }
    
    public boolean copyXml ( XmlCursor dst )
    {
        if (dst == null)
            throw new IllegalArgumentException( "Destination is null" );
        
        if (dst.monitor() == monitor())
        {
            synchronized ( monitor() )
            {
                return copyXmlImpl( dst );
            }
        }
        else
        {
            boolean acquired = false;
            try
            {
                GlobalLock.acquire();
                acquired = true;
                synchronized ( monitor() )
                {
                    synchronized (dst.monitor())
                    {
                        GlobalLock.release();
                        acquired = false;
                        
                        return copyXmlImpl( dst );
                    }
                }
            }
            catch (InterruptedException e)
            {
                throw new XmlRuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (acquired)
                    GlobalLock.release();
            }
        }
    }
    
    private boolean copyXmlImpl ( XmlCursor dst )
    {
        checkDisposed();
                    
        if (dst == null || !(dst instanceof Cursor))
        {
            throw
                new IllegalArgumentException( "Can't copy to a foreign document" );
        }
            
        Cursor cDst = (Cursor) dst;
                    
        checkDisposed( cDst );
            
        Splay sDst = cDst.getSplay();
        int   pDst = cDst.getPos();
                    
        Splay s = getSplay();
        int   p = getPos();
            
        if (s.checkInsertionValidity( p, sDst, pDst, true ))
            return copyCharsImpl( getPostCch(), dst ) > 0;
            
        assert p == 0;
            
        // Need to make a splay copy before getting the text because the copy
        // will validate invalid contents/values
                    
        Root r = getRoot();
        Root rDst = cDst.getRoot();
                    
        Splay copy = s.copySplay();

        Object txt = r._text;
        int cp = r.getCp( s );
        int cch = copy.getCchLeft() + copy.getCch();

        //
        // Remove text after which might be between leaf value and first attr value
        //
        
        if (s.isLeaf() && s.getCchAfter() > 0)
        {
            int cchValue = s.getCchValue();
            int cchAfter = s.getCchAfter();
            
            if (cchValue == 0)
                cp += cchAfter;
            else if (s.nextSplay().isAttr())
            {
                char[] buf = new char [ cch ];
                r._text.fetch( buf, 0, cp, cchValue );
                r._text.fetch( buf, cchValue, cp + cchValue + cchAfter, cch - cchValue );

                txt = buf;
                cp = 0;
            }
        }
            
        sDst.insert( rDst, pDst, copy, txt, cp, cch, true );
            
        return true;
    }
    
    public boolean removeXmlContents ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            // TODO - should implement this with internals
            
            if (!isContainer())
                return false;
    
            TokenType tt = toFirstContentToken();
            assert !tt.isNone();
    
            boolean removed = !isFinish();
            
            try
            {
                while ( !isFinish() )
                {
                    boolean b = removeXml();
                    assert b;
                }
            }
            finally
            {
                toParent();
            }
    
            return removed;
        }
    }

    private boolean contains ( XmlCursor dst )
    {
        if (isInSameDocument( dst ))
        {
            dst.push();

            for ( ; ; )
            {
                if (dst.isAtSamePositionAs( this ))
                {
                    dst.pop();
                    return true;
                }

                if (!dst.toParent())
                    break;
            }

            dst.pop();
        }

        return false;
    }
    
    public boolean moveXmlContents ( XmlCursor dst )
    {
        if (dst == null)
            throw new IllegalArgumentException( "Destination is null" );
        
        if (dst.monitor() == monitor())
        {
            synchronized ( monitor() )
            {
                return moveXmlContentsImpl( dst );
            }
        }
        else
        {
            boolean acquired = false;
            try
            {
                GlobalLock.acquire();
                acquired = true;
                synchronized ( monitor() )
                {
                    synchronized (dst.monitor())
                    {
                        GlobalLock.release();
                        acquired = false;
                        
                        return moveXmlContentsImpl( dst );
                    }
                }
            }
            catch (InterruptedException e)
            {
                throw new XmlRuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (acquired)
                    GlobalLock.release();
            }
        }
    }
    
    private boolean moveXmlContentsImpl ( XmlCursor dst )
    {
        checkDisposed();
                    
        if (!isContainer())
            return false;
            
        // Check to see if dst is in src!  In this case, there is nothing to
        // do.
                    
        if (contains( dst ))
            return false;
            
        TokenType tt = toFirstContentToken();
        assert !tt.isNone();
                    
        boolean moved = !isFinish();
                    
        try
        {
            if (!moveXmlImpl( dst ))
                return false;
                        
            while ( !isFinish() )
            {
                boolean b = moveXmlImpl( dst );
                assert b;
            }
        }
        finally
        {
            toParent();
        }
            
        return moved;
    }

    public boolean copyXmlContents ( XmlCursor dst )
    {
        if (dst == null)
            throw new IllegalArgumentException( "Destination is null" );
        
        if (dst.monitor() == monitor())
        {
            synchronized ( monitor() )
            {
                return copyXmlContentsImpl( dst );
            }
        }
        else
        {
            boolean acquired = false;
            try
            {
                GlobalLock.acquire();
                acquired = true;
                synchronized ( monitor() )
                {
                    synchronized (dst.monitor())
                    {
                        GlobalLock.release();
                        acquired = false;
                        
                        return copyXmlContentsImpl( dst );
                    }
                }
            }
            catch (InterruptedException e)
            {
                throw new XmlRuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (acquired)
                    GlobalLock.release();
            }
        }
    }
    
    private boolean copyXmlContentsImpl ( XmlCursor dst )
    {
        checkDisposed();
                    
        if (!isContainer())
            return false;
            
        // Check to see if dst is in src!  In this case, copy the src to a new
        // document and then move the copied contents to the destination.
            
        if (contains( dst ))
        {
            XmlCursor cTmp = XmlObject.Factory.newInstance().newCursor();
            
            cTmp.toNextToken();
            
            if (!copyXmlContentsImpl( cTmp ))
            {
                cTmp.dispose();
                return false;
            }
            
            cTmp.toStartDoc();
            ((Cursor)cTmp).moveXmlContentsImpl( dst );
            cTmp.dispose();
            return true;
        }
            
        TokenType tt = toFirstContentToken();
        assert !tt.isNone();
                    
        boolean copied = !isFinish();
                    
        try
        {
            if (!copyXmlImpl( dst ))
                return false;
            
            for ( ; ; )
            {
                if (isStart())
                    toEndToken();
                            
                toNextToken();
                            
                if (isFinish())
                    break;
            
                boolean b = copyXmlImpl( dst );
                assert b;
            }
        }
        finally
        {
            toParent();
        }
            
        return copied;
    }

    public boolean isInSameDocument ( XmlCursor xthat )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (xthat == null || !(xthat instanceof Cursor))
                return false;
    
            Cursor that = (Cursor) xthat;
    
            checkDisposed( that );
            
            return getRoot() == that.getRoot();
        }
    }

    public void push ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            if (_data._stack == null)
                _data._stack = new Selections();
    
            _data._stack.add( getRoot(), getSplay(), getPos() );
    
            getRoot().registerForChange( this );
        }
    }

    public boolean pop ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            if (_data._stack == null || _data._stack.size() == 0)
                return false;
    
            _data._stack.setCursor( this, _data._stack.size() - 1 );
    
            _data._stack.pop();
    
            return true;
        }
    }
    
    public int getSelectionCount ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            return _data._selections == null ? 0 : _data._selections.size();
        }
    }
    
    public boolean toSelection ( int i )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (_data._selections != null && i >= 0 && _data._selections.setCursor( this, i ))
            {
                _data._currentSelection = i;
                return true;
            }
    
            return false;
        }
    }
    
    public boolean hasNextSelection ( )
    {
        synchronized ( monitor() )
        {
            push();
            int currentSelection = _data._currentSelection;
    
            try
            {
                return toNextSelection();
            }
            finally
            {
                pop();
                _data._currentSelection = currentSelection;
            }
        }
    }
    
    public boolean toNextSelection ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (_data._selections == null || _data._currentSelection < -1)
                return false;
    
            int nextSelection = _data._currentSelection + 1;
    
            if (!_data._selections.setCursor( this, nextSelection ))
            {
                _data._currentSelection = -2;
                return false;
            }
    
            _data._currentSelection = nextSelection;
    
            return true;
        }
    }

    public void clearSelections (  )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            _data.clearSelections();
        }
    }

    public void addToSelection ( )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
            
            if (_data._selections == null)
                _data._selections = Path.newSelections();
    
            // Force any selection engine to search all...
            _data._selections.size();
    
            _data._selections.add( getRoot(), getSplay(), getPos() );
    
            getRoot().registerForChange( this );
        }
    }
    
    public void changeNotification ( )
    {
        if (!isDisposed())
        {
            if (_data._selections != null)
            {
                _data._selections.size();  // Force a full selection
                _data._selections.cursify( getRoot() );
            }

            if (_data._stack != null)
                _data._stack.cursify( getRoot() );
        }
    }
    
    public void selectPath ( String path, XmlOptions options )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            if (_data._selections == null)
                _data._selections = Path.newSelections();
            else
                _data._selections.dispose();
    
            _data._selections.init( 
                Path.select( getRoot(), getSplay(), getPos(), path, options ) );
    
            push();
    
            if (_data._selections.setCursor( this, 0 ))
            {
                getRoot().registerForChange( this );
                _data._currentSelection = -1;
            }
            else
                _data._currentSelection = -2;
    
            pop();
        }
    }
    
    public void selectPath ( String path )
    {
        selectPath( path, null );
    }
    
    public XmlCursor execQuery ( String queryExpr, XmlOptions options )
    {
        synchronized ( monitor() )
        {
            checkDisposed();
    
            return Path.query( this, queryExpr, options );
        }
    }
    
    public XmlCursor execQuery ( String query )
    {
        return execQuery( query, null );
    }

    public Node newDomNode ( )
    {
        return newDomNode( null );
    }
    
    public Node newDomNode ( XmlOptions options )
    {
        try
        {
            Saver.DomSaver saver;
            
            synchronized ( monitor() )
            {
                checkDisposed();
                
                saver = new Saver.DomSaver(
                    getRoot(), getSplay(), getPos(), !isFragment(), options );
            }
            
            return saver.exportDom();
        }
        catch ( Exception e )
        {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            
            throw new RuntimeException( e.getMessage(), e );
        }
    }
    
    private boolean isFragment()
    {
        if (! isStartdoc())
            return true;

        boolean seenElement = false;

        XmlCursor c = newCursor();
        int token = c.toNextToken().intValue();

        try {

            LOOP:
            while (true)
            {
                SWITCH:
                switch (token)
                {
                    case TokenType.INT_START:
                        if (seenElement) return true;
                        seenElement = true;
                        token = c.toEndToken().intValue();
                        break SWITCH;

                    case TokenType.INT_TEXT:
                        if (! Splay.isWhiteSpace(c.getChars()))
                            return true;
                        token = c.toNextToken().intValue();
                        break SWITCH;


                    case TokenType.INT_NONE:
                    case TokenType.INT_ENDDOC:
                        break LOOP;


                    case TokenType.INT_ATTR:
                    case TokenType.INT_NAMESPACE:
                        return true;

                    case TokenType.INT_END:
                    case TokenType.INT_COMMENT:
                    case TokenType.INT_PROCINST:
                        token = c.toNextToken().intValue();
                        break SWITCH;

                    case TokenType.INT_STARTDOC:
                        assert false;
                        break LOOP;

                }

            }
        }

        finally {
            c.dispose();
        }

        return ! seenElement;

    }

    public void save ( ContentHandler ch, LexicalHandler lh )
        throws SAXException
    {
        save( ch, lh, null );
    }
    
    public void save ( File file ) throws IOException
    {
        save( file, null );
    }
    
    public void save ( OutputStream os ) throws IOException
    {
        save( os, null );
    }

    public void save ( Writer w ) throws IOException
    {
        save( w, null );
    }

    public void save ( ContentHandler ch, LexicalHandler lh, XmlOptions options)
        throws SAXException
    {
        // todo: don't hold the monitor all this long.
        // Ideally, we'd release the monitor each time we're about to call the
        // ch or lh objects.  However, the saver code isn't _quite_ structure
        // just right to do that.  So instead, the current version will hold
        // the monitor for the duration of the entire SAX save.
        synchronized ( monitor() )
        {
            checkDisposed();
            
            new Saver.SaxSaver( getRoot(), getSplay(), getPos(), options, ch, lh );
        }
    }
    
    public void save ( File file, XmlOptions options ) throws IOException
    {
        OutputStream os = new FileOutputStream( file );

        try
        {
            save( os, options );
        }
        finally
        {
            os.close();
        }
    }
    
    public void save ( OutputStream os, XmlOptions options ) throws IOException
    {
        // note that we do not hold the monitor for the duration of a save.
        // Instead, a concurrent modification exception is thrown if the
        // document is modified while the save is in progress. If the user
        // wishes to protect against this, he can synchronize on the monitor
        // himself.
        InputStream is = newInputStream( options );

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
    
    public void save ( Writer w, XmlOptions options ) throws IOException
    {
        Reader r = newReader( options );

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

    //
    //
    //
    
    private boolean validate ( )
    {
        assert _data._goober.getRoot().validate();
        return true;
    }

    public void dump ( ) { _data._goober.getRoot().dump(); }
    public void dump ( boolean verbose ) { _data._goober.getRoot().dump( verbose ); }

    interface PathEngine
    {
        boolean next ( Selections selections );
    }

    static class Selections
    {
        void init ( PathEngine pathEngine )
        {
            dispose();
            
            _pathEngine = pathEngine;
        }
        
        void add ( Root r, Splay s )
        {
            add( r, s, 0 );
        }
        
        void add ( Root r, Splay s, int p )
        {
            assert s.getRootSlow() == r;
            
            if (_cursors != null)
            {
                CursorGoober g = new CursorGoober( r );

                g.set( s, p );
                
                _cursors.add( g );

                return;
            }
            
            if (_splays == null)
            {
                assert _count == 0;
                _splays = new Splay [ 16 ];
                _positions = new int [ 16 ];
            }
            else if (_count == _splays.length)
            {
                Splay[] newSplays = new Splay [ _count * 2 ];
                int[]   newPositions = new int [ _count * 2 ];
                
                System.arraycopy( _splays, 0, newSplays, 0, _count );
                System.arraycopy( _positions, 0, newPositions, 0, _count );
                
                _splays = newSplays;
                _positions = newPositions;
            }

            _splays[ _count ] = s;
            _positions[ _count ] = p;

            _count++;
        }

        void pop ( )
        {
            assert size() > 0;

            if (_cursors != null)
            {
                int i = _cursors.size() - 1;
                ((CursorGoober) _cursors.get( i )).set( null, 0 );
                _cursors.remove( i );
            }
            else
                _count--;
        }

        void cursify ( Root r )
        {
            if (_cursors != null || _count <= 0)
                return;

            _cursors = new ArrayList();
            
            for ( int i = 0 ; i < _count ; i++ )
            {
                CursorGoober g = new CursorGoober( r );

                g.set( _splays[ i ], _positions[ i ] );
                
                _cursors.add( g );
            }

            _count = 0;
        }

        int size ( )
        {
            if (_pathEngine != null)
            {
                while ( _pathEngine.next( this ) )
                    ;

                _pathEngine = null;
            }

            return currentSize();
        }

        int currentSize ( )
        {
            return _cursors != null ? _cursors.size() : _count;
        }

        boolean setCursor ( Cursor c, int i )
        {
            assert i >= 0;

            while ( _pathEngine != null && currentSize() <= i )
            {
                if (!_pathEngine.next( this ))
                    _pathEngine = null;
            }

            if (i >= currentSize())
                return false;
            
            if (_cursors != null)
            {
                assert i < _cursors.size();
                c.set( (CursorGoober) _cursors.get( i ) );
            }
            else
            {
                assert i < _count;
                c.set( _splays[ i ], _positions[ i ] );
            }

            return true;
        }

        void dispose ( )
        {
            if (_cursors != null)
            {
                for ( int i = 0 ; i < _cursors.size() ; i++ )
                    ((CursorGoober) _cursors.get( i )).set( null, 0 );
                
                _cursors.clear();
                
                _cursors = null;
            }
            
            _count = 0;
                
            // TODO - cache unused Seleciton objects for later reuse
        }

        private Splay[] _splays;
        private int[]   _positions;
        private int     _count;

        private ArrayList _cursors;

        private PathEngine _pathEngine;
    }
    
    //
    //
    //

    CursorData _data;
}
