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

package org.apache.xmlbeans.impl.newstore.pub.store;

import javax.xml.namespace.QName;
import org.w3c.dom.Node;
import java.io.PrintStream;
import org.apache.xmlbeans.XmlCursor;

import org.apache.xmlbeans.impl.newstore.pub.store.Dom.CharNode;
import org.apache.xmlbeans.impl.newstore.Cursor;

public abstract class Cur
{
    public static final int TEMP = 0;
    public static final int PERM = 1;
    public static final int WEAK = 2;

    public static final int POOLED     = 0;
    public static final int UNEMBEDDED = 1;
    public static final int EMBEDDED   = 2;
    public static final int DISPOSED   = 3;

    public static final int NONE     =                0;
    public static final int ROOT     =                1;
    public static final int ELEM     =                2;
    public static final int LEAF     =                3;
    public static final int ATTR     =                4;
    public static final int TEXT     =                5;
    public static final int DOMDOC   = ROOT +  (1 << 3);
    public static final int DOMFRAG  = ROOT +  (2 << 3);
    public static final int XMLNS    = ATTR +  (1 << 3);
    public static final int COMMENT  = LEAF +  (1 << 3);
    public static final int PROCINST = LEAF +  (2 << 3);

    //
    //
    //

    protected abstract int      _kind           (                              );
    protected abstract void     _dispose        (                              );
    protected abstract Locale   _locale         (                              );
    protected abstract boolean  _isPositioned   (                              );
    protected abstract void     _moveToCur      ( Cur c                        );
    protected abstract void     _moveToCharNode ( CharNode node                );
    protected abstract void     _moveToDom      ( Dom d                        );
    protected abstract boolean  _isSamePosition ( Cur that                     );
    protected abstract boolean  _next           (                              );
    protected abstract void     _toEnd          (                              );
    protected abstract boolean  _toParent       ( boolean raw                  );
    protected abstract boolean  _toNextSibling  (                              );
    protected abstract boolean  _toFirstChild   (                              );
    protected abstract boolean  _toLastChild    (                              );
    protected abstract boolean  _toFirstAttr    (                              );
    protected abstract void     _create         ( int k, QName name            );
    protected abstract void     _createElement  ( QName name, QName parentName );
    protected abstract void     _moveNode       ( Cur to                       );
    protected abstract void     _copyNode       ( Cur to                       );
    protected abstract Object   _moveChars      ( Cur to, int cch              );
    protected abstract void     _insertChars    ( Object src, int off, int cch );
    protected abstract QName    _getName        (                              );
    protected abstract void     _setName        ( QName n                      );
    protected abstract String   _getValueString (                              );
    protected abstract Object   _getChars       ( int cch                      );
    protected abstract String   _getString      ( int cch                      );
    protected abstract Dom      _getDom         (                              );
    protected abstract CharNode _getCharNodes   (                              );
    protected abstract void     _setCharNodes   ( CharNode nodes               );
    protected abstract void     _setBookmark    ( Class c, Object o            );
    protected abstract Object   _getBookmark    ( Class c                      );
    
    //
    //
    //
    
    public final Locale locale ( ) { return _locale(); }
    
    public final int kind ( ) { return _kind(); }
    public final int type ( ) { return _kind() % 8; }

    public static boolean typeIsContainer ( int t ) { return t > 0 && t <= ELEM; }

    public final boolean isRoot      ( ) { return type() == ROOT; }
    public final boolean isElem      ( ) { return type() == ELEM; }
    public final boolean isAttr      ( ) { return type() == ATTR; }
    public final boolean isLeaf      ( ) { return type() == LEAF; }
    public final boolean isContainer ( ) { return typeIsContainer( type() ); }

    public final Cur tempCur ( )
    {
        Cur c = locale().tempCur();
        c.moveToCur( this );
        return c;
    }

    public final Cur weakCur ( Object o )
    {
        Cur c = locale().weakCur( o );
        c.moveToCur( this );
        return c;
    }

    public final boolean isPositioned ( )
    {
        return _isPositioned();
    }

    public final void moveToCur ( Cur c )
    {
        _moveToCur( c );
    }

    public final void moveToDom ( Dom d )
    {
        assert d != null;
        _moveToDom( d );
    }

    public final void moveToCharNode ( CharNode node )
    {
        _moveToCharNode( node );
    }

    public boolean isSamePosition ( Cur that )
    {
        return _isSamePosition( that );
    }

    public final boolean next ( )
    {
        return _next();
    }

    public final void toEnd ( )
    {
        _toEnd();
    }

    public final boolean toParentRaw ( )
    {
        return _toParent( true );
    }

    public final boolean toParent ( )
    {
        return _toParent( false );
    }

    public final boolean toFirstChild ( )
    {
        return _toFirstChild();
    }

    public final boolean toLastChild ( )
    {
        return _toLastChild();
    }

    public final boolean toFirstAttr ( )
    {
        return _toFirstAttr();
    }

    public final boolean toNextSibling ( )
    {
        return _toNextSibling();
    }

    public final void moveNode ( Cur to )
    {
        _moveNode( to );
    }

    public final void copyNode ( Cur to )
    {
        _copyNode( to );
    }

    public final Object moveChars ( Cur to, int cch )
    {
        return _moveChars( to, cch );
    }

    public final void insertChars ( Object src, int off, int cch )
    {
        _insertChars( src, off, cch );
    }

    public final void setName ( QName name )
    {
        _setName( name );
    }

    public final QName getName ( )
    {
        return _getName();
    }

    public final String getValueString ( )
    {
        return _getValueString();
    }

    public final String getString ( int cch )
    {
        return _getString( cch );
    }

    public final Object getChars ( int cch )
    {
        return _getChars( cch );
    }

    public final Dom getDom ( )
    {
        assert isPositioned() && kind() != TEXT;
        return _getDom();
    }

    public final CharNode getCharNodes ( )
    {
        assert isPositioned();
        return _getCharNodes();
    }

    public final void setCharNodes ( CharNode nodes )
    { 
        assert isPositioned();
        _setCharNodes( nodes );
    }

    public final void setBookmark ( Class c, Object o )
    {
        _setBookmark( c, o );
    }

    public final Object getBookmark ( Class c )
    {
        return _getBookmark( c );
    }

    public final void createRoot ( int k )
    {
        Locale l = _locale();

        assert k == ROOT || k == DOMDOC || k == DOMFRAG;
        assert !isPositioned();
        assert k != DOMDOC || l._ownerDoc == null;

        if (k == DOMDOC || (k == ROOT && l._ownerDoc == null))
            _create( DOMDOC, null );
        else
            _create( k, null );
    }

    public final void createElement ( QName name, QName parentName )
    {
        _createElement( name, parentName );
    }

    public final void createElement ( QName name )
    {
        _create( ELEM, name );
    }

    public final void createComment ( )
    {
        _create( COMMENT, null );
    }

    public final void createAttr ( QName name )
    {
        _create( "xmlns".equals( name.getPrefix() ) ? XMLNS : ATTR, name );
    }

    public final String namespaceForPrefix ( String prefix )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public final String prefixForNamespace ( String ns )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public final boolean toNearestContainer ( )
    {
        int t = type();

        switch ( t )
        {
            case ROOT :
            case ELEM :
                return true;

            case TEXT :
                next();
                break;

            case ATTR:
                if (!toParentRaw() || !toFirstChild())
                    return false;

                break;
        }

        while ( !isContainer() )
            if (!toNextSibling())
                return false;

        return true;
    }

    public final boolean toFirstChildElem ( )
    {
        assert isContainer();

        if (!toFirstChild())
            return false;

        while ( !isElem() )
            if (!toNextSibling())
                return false;

        return true;
    }

    public final void release ( )
    {
        assert _state != POOLED || _nextTemp == null;

        if (_state == POOLED || _state == DISPOSED)
            return;

        _moveToCur( null );

        if (_obj instanceof Locale.Ref)
            ((Locale.Ref) _obj).clear();

        _obj = null;
        _curKind = -1;

        assert _state == UNEMBEDDED;

        Locale l = _locale();

        l._unembedded = listRemove( l._unembedded );

        if (l._poolCount < 16)
        {
            l._pool = listInsert( l._pool, POOLED );
            l._poolCount++;
        }
        else
        {
            _dispose();
            _state = DISPOSED;
        }
    }

    public final boolean isOnList ( Cur head )
    {
        for ( ; head != null ; head = head._next )
            if (head == this)
                return true;

        return false;
    }

    public final Cur listInsert ( Cur head, int state )
    {
        assert _next == null && _prev == null;

        if (head == null)
            head = _prev = this;
        else
        {
            _prev = head._prev;
            head._prev = head._prev._next = this;
        }

        _state = state;

        return head;
    }

    public final Cur listRemove ( Cur head )
    {
        assert _prev != null && isOnList( head );

        if (_prev == this)
            head = null;
        else
        {
            if (head == this)
                head = _next;
            else
                _prev._next = _next;

            if (_next == null)
                head._prev = _prev;
            else
            {
                _next._prev = _prev;
                _next = null;
            }
        }

        _prev = null;
        _state = -1;

        return head;
    }

    protected final static CharNode updateCharNodes (
        Locale l, Object src, CharNode nodes, int cch )
    {
        CharNode node = nodes;
        int i = 0;

        while ( node != null && cch > 0 )
        {
            assert node._src == src;

            if (node._cch > cch)
                node._cch = cch;

            node._off = i;
            i += node._cch;
            cch -= node._cch;

            node = node._next;
        }

        if (cch <= 0)
        {
            for ( ; node != null ; node = node._next )
            {
                assert node._src == src;

                if (node._cch != 0)
                    node._cch = 0;

                node._off = i;
            }
        }
        else
        {
            node = l.createTextNode();
            node._src = src;
            node._cch = cch;
            node._off = i;
            nodes = CharNode.appendNode( nodes, node );
        }

        return nodes;
    }

    public static String kindName ( int kind )
    {
        switch ( kind )
        {
            case ROOT     : return "ROOT";
            case ELEM     : return "ELEM";
            case LEAF     : return "LEAF";
            case ATTR     : return "ATTR";
            case TEXT     : return "TEXT";
            case DOMDOC   : return "DOMDOC";
            case DOMFRAG  : return "DOMFRAG";
            case XMLNS    : return "XMLNS";
            case COMMENT  : return "COMMENT";
            case PROCINST : return "PROCINST";
            default       : return "<< Unknown Kind (" + kind + ") >>";
        }
    }

    public static String typeName ( int type )
    {
        switch ( type )
        {
            case ROOT : return "ROOT";
            case ELEM : return "ELEM";
            case LEAF : return "LEAF";
            case ATTR : return "ATTR";
            case TEXT : return "TEXT";
            default   : return "<< Unknown type (" + type + ") >>";
        }
    }

    //
    //
    //

    public static void dump ( PrintStream o, Dom d )
    {
        d.dump( o );
    }

    public static void dump ( Dom d )
    {
        dump( System.out, d );
    }

    public static void dump ( XmlCursor xc )
    {
        Cursor c = (Cursor) xc;
        c.dump();
    }

    public static void dump ( Node n )
    {
        dump( System.out, n );
    }

    public static void dump ( PrintStream o, Node n )
    {
        dump( o, (Dom) n );
    }

    public void dump ( )
    {
        dump( System.out );
    }

    public void dump ( PrintStream o )
    {
        o.println( "Dump not implemented" );
    }

    //
    //
    //

    public int _state;
    public int _curKind;

    public Cur _next;
    public Cur _prev;

    public Object _obj;
    
    int _tempFrame;
    Cur _nextTemp;

    public int _offSrc;
    public int _cchSrc;
}
