package org.apache.xmlbeans.impl.newstore.xcur;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.newstore.xcur.Master;
import org.apache.xmlbeans.impl.newstore.xcur.Xcur;
import org.apache.xmlbeans.impl.newstore.DomImpl.Dom;
import org.apache.xmlbeans.impl.newstore.DomImpl.CharNode;

public class Fcur extends Xcur
{
    protected final Master _master ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _dispose ( )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final void _toEnd ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _moveToCur ( Xcur x )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _insertChars ( Object src, int off, int cch )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _moveNode ( Xcur to )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _copyNode ( Xcur to )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final Object _moveChars ( Xcur to, int cch )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final boolean _ancestorOf ( Xcur that )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final int _kind ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _create ( int kind, QName name )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected boolean _isSamePosition ( Xcur that )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final boolean _isPositioned ( )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final Dom _getDom ( )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final void _moveToDom ( Dom d )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _setName ( QName n )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final boolean _next ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final CharNode _getCharNodes ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _setCharNodes ( CharNode nodes )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final void _moveToCharNode ( CharNode node )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final boolean _toFirstChild ( )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final boolean _toLastChild ( )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final boolean _toNextSibling ( )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final boolean _toParent ( boolean raw )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final Object _getChars ( int cch )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final String _getString ( int cch )
    {
        throw new RuntimeException( "Not impl" );
    }

    protected final String _getValueString ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final boolean _toFirstAttr ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final QName _getName ( )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final void _setBookmark ( Class c, Object o )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected final Object _getBookmark ( Class c )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    protected void _createElement ( QName name, QName parentName )
    {
        throw new RuntimeException( "Not impl" );
    }
    
    //
    //
    //
    
    private static class Fmaster extends Master
    {
        protected Xcur newCur ( )
        {
            throw new RuntimeException( "Not impl" );
        }
        
        protected LoadContext newLoadContext ( )
        {
            throw new RuntimeException( "Not impl" );
        }
    }

    public static Master newMaster ( )
    {
        return new Fmaster();
    }
}