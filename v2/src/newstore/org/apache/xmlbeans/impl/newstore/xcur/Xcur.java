package org.apache.xmlbeans.impl.newstore.xcur;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.newstore.DomImpl;
import org.apache.xmlbeans.impl.newstore.DomImpl.Dom;
import org.apache.xmlbeans.impl.newstore.DomImpl.CharNode;
import org.apache.xmlbeans.impl.newstore.DomImpl.TextNode;

import org.w3c.dom.Node;

import java.io.PrintStream;

public abstract class Xcur
{
    static final int TEMP = 0;
    static final int PERM = 1;
    static final int WEAK = 2;
    
    static final int POOLED     = 0;
    static final int UNEMBEDDED = 1;
    static final int EMBEDDED   = 2;
    static final int DISPOSED   = 3;

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

    protected abstract Master   _master         (                              );
    protected abstract int      _kind           (                              );
    protected abstract void     _dispose        (                              );
    protected abstract void     _create         ( int k, QName name            );
    protected abstract void     _createElement  ( QName name, QName parentName );
    protected abstract boolean  _isPositioned   (                              );
    protected abstract boolean  _isSamePosition ( Xcur that                    );
    protected abstract Dom      _getDom         (                              );
    protected abstract void     _moveToCur      ( Xcur x                       );
    protected abstract void     _moveToDom      ( Dom d                        );
    protected abstract boolean  _toParent       ( boolean raw                  );
    protected abstract boolean  _toNextSibling  (                              );
    protected abstract boolean  _toFirstChild   (                              );
    protected abstract boolean  _toLastChild    (                              );
    protected abstract boolean  _toFirstAttr    (                              );
    protected abstract void     _toEnd          (                              );
    protected abstract QName    _getName        (                              );
    protected abstract void     _setName        ( QName n                      );
    protected abstract boolean  _next           (                              );
    protected abstract void     _insertChars    ( Object src, int off, int cch );
    protected abstract void     _moveNode       ( Xcur to                      );
    protected abstract void     _copyNode       ( Xcur to                      );
    protected abstract Object   _moveChars      ( Xcur to, int cch             );
    protected abstract boolean  _ancestorOf     ( Xcur that                    );
    protected abstract CharNode _getCharNodes   (                              );
    protected abstract void     _setCharNodes   ( CharNode nodes               );
    protected abstract void     _moveToCharNode ( CharNode node                );
    protected abstract Object   _getChars       ( int cch                      );
    protected abstract String   _getString      ( int cch                      );
    protected abstract String   _getValueString (                              );
    protected abstract void     _setBookmark    ( Class c, Object o            );
    protected abstract Object   _getBookmark    ( Class c                      );

    //
    //
    //

    public Xcur ( )
    {
        _state = -1;
        _tempPtrFrame = -1;
    }

    public final Master master ( ) { return _master(); }

    public final int kind ( ) { return _kind(); }
    public final int type ( ) { return _kind() % 8; }

    public final boolean isRoot      ( ) { return type() == ROOT; }
    public final boolean isAttr      ( ) { return type() == ATTR; }
    public final boolean isLeaf      ( ) { return type() == LEAF; }
    public final boolean isContainer ( ) { int t = type(); return t > 0 && t <= ELEM; }

    public final Xcur tempCur ( )
    {
        Xcur x = master().tempCur();
        x.moveToCur( this );
        return x;
    }

    public final Xcur weakCur ( Object o )
    {
        Xcur x = master().weakCur( o );
        x.moveToCur( this );
        return x;
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

    public void setBookmark ( Class c, Object o )
    {
        _setBookmark( c, o );
    }
    
    public Object getBookmark ( Class c )
    {
        return _getBookmark( c );
    }
    
    public final void moveToDom ( Dom d )
    {
        assert d != null;
        _moveToDom( d );
    }
    
    public final void moveToCur ( Xcur x )
    {
        _moveToCur( x );
    }

    public final void toEnd ( )
    {
        _toEnd();
    }

    public final void insertChars ( Object src, int off, int cch )
    {
        _insertChars( src, off, cch );
    }

    public final void moveToCharNode ( CharNode node )
    {
        _moveToCharNode( node );
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

    public final void createRoot ( int k )
    {
        Master m = _master();
        
        assert k == ROOT || k == DOMDOC || k == DOMFRAG;
        assert !isPositioned();
        assert k != DOMDOC || m._ownerDoc == null;

        if (k == DOMDOC || (k == ROOT && m._ownerDoc == null))
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

    public final void createAttr ( QName name )
    {
        _create( ATTR, name );
    }
    
    public final void createComment ( )
    {
        _create( COMMENT, null );
    }

    public final Object moveChars ( Xcur to, int cch )
    {
        return _moveChars( to, cch );
    }
    
    public final void moveNode ( Xcur to )
    {
        _moveNode( to );
    }

    public final void copyNode ( Xcur to )
    {
        _copyNode( to );
    }

    public final boolean next ( )
    {
        return _next();
    }
    
    public boolean isSamePosition ( Xcur that )
    {
        return _isSamePosition( that );
    }

    public final boolean isPositioned ( )
    {
        return _isPositioned();
    }

    public String namespaceForPrefix ( String prefix )
    {
        throw new RuntimeException( "Not implemented" );
    }
            
    public String prefixForNamespace ( String ns )
    {
        throw new RuntimeException( "Not implemented" );
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

    public final void release ( )
    {
        assert _state != POOLED || _nextTemp == null;

        if (_state == POOLED || _state == DISPOSED)
            return;
        
        _moveToCur( null );

        if (_obj instanceof Master.Ref)
            ((Master.Ref) _obj).clear();
        
        _obj = null;
        _curKind = -1;

        assert _state == UNEMBEDDED;

        Master m = _master();

        m._unembedded = listRemove( m._unembedded );

        if (m._poolCount < 16)
        {
            m._pool = listInsert( m._pool, POOLED );
            m._poolCount++;
        }
        else
        {
            _dispose();
            _state = DISPOSED;
        }
    }

    final boolean isOnList ( Xcur head )
    {
        for ( ; head != null ; head = head._next )
            if (head == this)
                return true;

        return false;
    }

    final Xcur listInsert ( Xcur head, int state )
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

    final Xcur listRemove ( Xcur head )
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
        Master m, Object src, CharNode nodes, int cch )
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
            node = DomImpl.createTextNode( m );
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
    
    protected int _state;
    protected int _curKind;
    
    Object _obj;
    
    Xcur _next, _prev;

    int _tempPtrFrame;
    Xcur _nextTemp;

    // Se when getChars is called to communicate the off and cch and
    // when _move chars is called to return the chas moved
    public int _offSrc;
    public int _cchSrc;
}