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

public final class CharUtil
{
    public CharUtil ( int charBufSize )
    {
        _charBufSize = charBufSize;
    }

    public CharIterator getCharIterator ( Object src, int off, int cch )
    {
        _charIter.init( src, off, cch );
        return _charIter;
    }
    
    public static CharUtil getThreadLocalCharUtil ( )
    {
        return (CharUtil) tl_charUtil.get();
    }

    public static void getString ( StringBuffer sb, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch == 0)
            return;

        if (src instanceof char[])
            sb.append( (char[]) src, off, cch );
        else if (src instanceof String)
        {
            String s = (String) src;
            
            if (off == 0 && cch == s.length())
                sb.append( (String) src );
            else
                sb.append( s.substring( off, off + cch ) );
        }
        else
            ((CharJoin) src).getString( sb, off, cch );
    }
    
    public static void getChars ( char[] chars, int start, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );
        assert chars != null && start >= 0 && start <= chars.length;

        if (cch == 0)
            return;

        if (src instanceof char[])
            System.arraycopy( (char[]) src, off, chars, start, cch );
        else if (src instanceof String)
            ((String) src).getChars( off, off + cch, chars, start );
        else
            ((CharJoin) src).getChars( chars, start, off, cch );
    }
    
    public static String getString ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch == 0)
            return "";

        if (src instanceof char[])
            return new String( (char[]) src, off, cch );

        if (src instanceof String)
        {
            String s = (String) src;

            if (off == 0 && cch == s.length())
                return s;

            return s.substring( off, off + cch );
        }

        StringBuffer sb = new StringBuffer();
        
        ((CharJoin) src).getString( sb, off, cch );
        
        return sb.toString();
    }

    public static final boolean isWhiteSpace ( char ch )
    {
        switch ( ch )
        {
            case ' ': case '\t': case '\n': case '\r': return true;
            default                                  : return false;
        }
    }

    public final boolean isWhiteSpace ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch > 0)
        {
            if (src instanceof char[])
            {
                for ( char[] chars = (char[]) src ; cch > 0 ; cch-- )
                    if (!isWhiteSpace( chars[ off++ ] ))
                        return false;
            }
            else if (src instanceof String)
            {
                for ( String s = (String) src ; cch > 0 ; cch-- )
                    if (!isWhiteSpace( s.charAt( off++ ) ))
                        return false;
            }
            else
            {
                _charIter.init( src, off, cch );
                
                while ( isWhiteSpace( _charIter.currChar() ) && --cch > 0 )
                    _charIter.next();

                if (cch != 0)
                    return false;
            }
        }
        
        return true;
    }

    public Object stripLeft ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch > 0)
        {
            if (src instanceof char[])
            {
                char[] chars = (char[]) src;

                while ( cch > 0 && isWhiteSpace( chars[ off ] ) )
                    { cch--; off++; }
            }
            else if (src instanceof String)
            {
                String s = (String) src;

                while ( cch > 0 && isWhiteSpace( s.charAt( off ) ) )
                    { cch--; off++; }
            }
            else
            {
                int oldCch = cch;
            
                _charIter.init( src, off, cch );
                
                while ( isWhiteSpace( _charIter.currChar() ) && --cch > 0 )
                    _charIter.next();
                
                off += oldCch - cch;
            }
        }

        if (cch == 0)
        {
            _offSrc = 0;
            _cchSrc = 0;
            
            return null;
        }

        _offSrc = off;
        _cchSrc = cch;

        return src;
    }

    public Object stripRight ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );
        
        if (cch > 0)
        {
            int oldCch = cch;

            _charIter.init( src, off, cch );
            _charIter.setPos( cch - 1 );

            while ( isWhiteSpace( _charIter.currChar() ) && --cch > 0 )
                _charIter.prev();
        }
        
        if (cch == 0)
        {
            _offSrc = 0;
            _cchSrc = 0;
            
            return null;
        }

        _offSrc = off;
        _cchSrc = cch;

        return src;
    }
    
    public Object insertChars (
        int posInsert,
        Object src, int off, int cch,
        Object srcInsert, int offInsert, int cchInsert )
    {
        assert isValid( src, off, cch );
        assert isValid( srcInsert, offInsert, cchInsert );
        assert posInsert >= 0 && posInsert <= cch;

        // TODO - at some point, instead of creating joins, I should
        // normalize all the text into a single buffer to stop large
        // tree;s from being built when many modifications happen...

        // TODO - actually, I should see if the size of the new char
        // sequence is small enough to simply allocate a new contigous
        // sequence, either in a common char[] managed by the master,
        // or just create a new string ... this goes for remove chars
        // as well.

        if (cchInsert == 0)
        {
            _cchSrc = cch;
            _offSrc = off;
            return src;
        }

        if (cch == 0)
        {
            _cchSrc = cchInsert;
            _offSrc = offInsert;
            return srcInsert;
        }

        _cchSrc = cch + cchInsert;

        Object newSrc;

        if (_cchSrc <= MAX_COPY && canAllocate( _cchSrc ))
        {
            char[] chars = allocate( _cchSrc );

            getChars( chars, _offSrc, src, off, posInsert );
            
            getChars( chars, _offSrc + posInsert, srcInsert, offInsert, cchInsert );
            
            getChars(
                chars, _offSrc + posInsert + cchInsert, src, off + posInsert, cch - posInsert );

            newSrc = chars;
        }
        else
        {
            _offSrc = 0;

            CharJoin newJoin;

            if (posInsert == 0)
                newJoin = new CharJoin( srcInsert, offInsert, cchInsert, src, off, cch );
            else if (posInsert == cch)
                newJoin = new CharJoin( src, off, cch, srcInsert, offInsert, cchInsert );
            else
            {
                newJoin =
                    new CharJoin( 
                        new CharJoin( src, off, posInsert, srcInsert, offInsert, cchInsert ),
                        0, posInsert + cchInsert,
                        src, off + posInsert, cch - posInsert );
            }
            
            if (newJoin._depth > CharJoin.MAX_DEPTH)
                newSrc = saveChars( newJoin, _offSrc, _cchSrc, null, 0, 0 );
            else
                newSrc = newJoin;
        }

        assert isValid( newSrc, _offSrc, _cchSrc );

        return newSrc;
    }

    public Object removeChars ( int posRemove, int cchRemove, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );
        assert posRemove >= 0 && posRemove <= cch;
        assert cchRemove >= 0 && posRemove + cchRemove <= cch;

        Object newSrc;

        _cchSrc = cch - cchRemove;
        
        if (_cchSrc == 0)
        {
            newSrc = null;
            _offSrc = 0;
        }
        else if (posRemove == 0)
        {
            newSrc = src;
            _offSrc = off + cchRemove;
        }
        else if (posRemove + cchRemove == cch)
        {
            newSrc = src;
            _offSrc = off;
        }
        else
        {
            int cchAfter = cch - cchRemove;
            
            if (cchAfter <= MAX_COPY && canAllocate( cchAfter ))
            {
                char[] chars = allocate( cchAfter );

                getChars( chars, _offSrc, src, off, posRemove );

                getChars(
                    chars, _offSrc + posRemove,
                    src, off + posRemove + cchRemove, cch - posRemove - cchRemove );

                newSrc = chars;
                _offSrc = _offSrc;
            }
            else
            {
                CharJoin newJoin =
                    new CharJoin(
                        src, off, posRemove,
                        src, off + posRemove + cchRemove, cch - posRemove - cchRemove );

                if (newJoin._depth > CharJoin.MAX_DEPTH)
                    newSrc = saveChars( newJoin, 0, _cchSrc, null, 0, 0 );
                else
                {
                    newSrc = newJoin;
                    _offSrc = 0;
                }
            }
        }
        
        assert isValid( newSrc, _offSrc, _cchSrc );
        
        return newSrc;
    }

    private boolean canAllocate ( int cch )
    {
        return _currentBuffer == null || _currentBuffer.length - _currentOffset >= cch;
    }
    
    private char[] allocate ( int cch )
    {
        assert _currentBuffer == null || _currentBuffer.length - _currentOffset > 0;
        
        if (_currentBuffer == null)
        {
            _currentBuffer = new char [ Math.max( cch, _charBufSize ) ];
            _currentOffset = 0;
        }

        _offSrc = _currentOffset;
        _cchSrc = Math.min( _currentBuffer.length - _currentOffset, cch );

        char[] retBuf = _currentBuffer;

        if ((_currentOffset += _cchSrc) == _currentBuffer.length)
        {
            _currentBuffer = null;
            _currentOffset = 0;
        }

        return retBuf;
    }
    
    public Object saveChars (
        Object src, int off, int cch,
        Object srcPrev, int offPrev, int cchPrev )
    {
        assert isValid( src, off, cch );
        assert isValid( srcPrev, offPrev, cchPrev );

        char[] srcAlloc = allocate( cch );

        getChars( srcAlloc, _offSrc, src, off, _cchSrc );

        Object srcNew;
        int offNew;

        if (cchPrev == 0)
        {
            srcNew = srcAlloc;
            offNew = _offSrc;
        }
        else if (srcPrev == srcAlloc && offPrev + cchPrev == _offSrc)
        {
            srcNew = srcPrev;
            offNew = offPrev;
        }
        else
        {
            srcNew = new CharJoin( srcPrev, offPrev, cchPrev, srcAlloc, _offSrc, _cchSrc );
            offNew = 0;
        }
        
        int cchNew = _cchSrc + cchPrev;
        
        int cchR = cch - _cchSrc;
        
        if (cchR > 0)
        {
            int cchL = _cchSrc;

            srcAlloc = allocate( cchR );
            
            assert _cchSrc == cchR;
            assert _offSrc == 0;

            getChars( srcAlloc, _offSrc, src, off + cchL, _cchSrc );

            srcNew = new CharJoin( srcNew, offNew, cchNew, srcAlloc, _offSrc, cchR );
            offNew = 0;
            cchNew += cchR;
        }

        _offSrc = offNew;
        _cchSrc = cchNew;
        
        assert isValid( srcNew, _offSrc, _cchSrc );
        
        return srcNew;
    }

    private static void dumpText ( PrintStream o, String s )
    {
        o.print( "\"" );

        for ( int i = 0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt( i );

            if (i == 36)
            {
                o.print( "..." );
                break;
            }

            if      (ch == '\n') o.print( "\\n" );
            else if (ch == '\r') o.print( "\\r" );
            else if (ch == '\t') o.print( "\\t" );
            else if (ch == '\f') o.print( "\\f" );
            else if (ch == '\f') o.print( "\\f" );
            else if (ch == '"' ) o.print( "\\\"" );
            else                 o.print( ch );
        }

        o.print( "\"" );
    }

    public static void dump ( Object src, int off, int cch )
    {
        dumpChars( System.out, src, off, cch );
        System.out.println();
    }
    
    public static void dumpChars ( PrintStream p, Object src, int off, int cch )
    {
        p.print( "cch=" + cch + ", off=" + off + ": " );
        
        if (src == null)
            p.print( "<null-src>" );
        else if (src instanceof String)
        {
            String s = (String) src;

            p.print( "String" );

            if (off != 0 || cch != s.length())
            {
                if (off < 0 || off > s.length() || off + cch < 0 || off + cch > s.length())
                {
                    p.print( " (Error)" );
                    return;
                }
            }

            p.print( ": " );
            dumpText( p, s.substring( off, off + cch ) );
        }
        else if (src instanceof char[])
        {
            char[] chars = (char[]) src;

            p.print( "char[]" );

            if (off != 0 || cch != chars.length)
            {
                if (off < 0 || off > chars.length || off + cch < 0 || off + cch > chars.length)
                {
                    p.print( " (Error)" );
                    return;
                }
            }

            p.print( ": " );
            dumpText( p, new String( chars, off, cch ) );
        }
        else if (src instanceof CharJoin)
        {
            p.print( "CharJoin" );

            ((CharJoin) src).dumpChars( p, off, cch );
        }
        else
        {
            p.print( "Unknown text source" );
        }
    }

    public static boolean isValid ( Object src, int off, int cch )
    {
        if (cch < 0 || off < 0)
            return false;

        if (src == null)
            return off == 0 && cch == 0;

        if (src instanceof char[])
        {
            char[] c = (char[]) src;
            return off <= c.length && off + cch <= c.length;
        }

        if (src instanceof String)
        {
            String s = (String) src;
            return off <= s.length() && off + cch <= s.length();
        }

        if (src instanceof CharJoin)
        {
            return ((CharJoin) src).isValid( off, cch );
        }

        return false;
    }

    //
    // Private stuff
    //
    
    private static final class CharJoin
    {
        public CharJoin (
            Object srcLeft,  int offLeft,  int cchLeft,
            Object srcRight, int offRight, int cchRight )
        {
            _srcLeft  = srcLeft;  _offLeft  = offLeft;  _cchLeft  = cchLeft;
            _srcRight = srcRight; _offRight = offRight; _cchRight = cchRight;

            int depth = 0;
            
            if (srcLeft instanceof CharJoin)
                depth = ((CharJoin) srcLeft)._depth;
            
            if (srcRight instanceof CharJoin)
            {
                int rightDepth = ((CharJoin) srcRight)._depth;
                
                if (rightDepth > depth)
                    depth = rightDepth;
            }
            
            _depth = depth;

            assert _depth <= MAX_DEPTH + 1;
        }
        
        final Object _srcLeft;
        final int    _offLeft;
        final int    _cchLeft;

        final Object _srcRight;
        final int    _offRight;
        final int    _cchRight;

        final int _depth;

        static final int MAX_DEPTH = 64;

        public int length ( ) { return _cchLeft + _cchRight; }

        public boolean isValid ( int off, int cch )
        {
            if (off < 0 || cch < 0 || off > length() || off + cch > length())
                return false;

            if (!CharUtil.isValid( _srcRight, _offRight, _cchRight ))
                return false;

            if (!CharUtil.isValid( _srcLeft, _offLeft, _cchLeft ))
                return false;

            return true;
        }

        private void getString ( StringBuffer sb, int off, int cch )
        {
            assert cch > 0;
            
            if (off < _cchLeft)
            {
                int cchL = _cchLeft - off;

                if (cchL > cch)
                    cchL = cch;

                CharUtil.getString( sb, _srcLeft, _offLeft + off, cchL );

                cch -= cchL;

                if (cch > 0)
                    CharUtil.getString( sb, _srcRight, _offRight, cch );
            }
            else
                CharUtil.getString( sb, _srcRight, _offRight + off - _cchLeft, cch );
        }

        private void getChars ( char[] chars, int start, int off, int cch )
        {
            assert cch > 0;

            if (off < _cchLeft)
            {
                int cchL = _cchLeft - off;

                if (cchL > cch)
                    cchL = cch;

                CharUtil.getChars( chars, start, _srcLeft, _offLeft + off, cchL );

                start += cchL;
                cch -= cchL;

                if (cch > 0)
                    CharUtil.getChars( chars, start, _srcRight, _offRight, cch );
            }
            else
                CharUtil.getChars( chars, start, _srcRight, _offRight + off - _cchLeft, cch );
        }

        private void dumpChars( int off, int cch )
        {
            dumpChars( System.out, off, cch );
        }
        
        private void dumpChars( PrintStream p, int off, int cch )
        {
            p.print( "( " );
            CharUtil.dumpChars( p, _srcLeft, _offLeft, _cchLeft );
            p.print( ", " );
            CharUtil.dumpChars( p, _srcRight, _offRight, _cchRight );
            p.print( " )" );
        }
    }
    
    public final static class CharIterator
    {
        void init ( Object src, int off, int cch )
        {
            assert cch > 0;
            assert isValid( src, off, cch );
            
            _src = src;
            _off = off;
            _cch = cch;

            _top = -1;

            while ( _src instanceof CharJoin )
            {
                CharJoin cj = (CharJoin) _src;

                _joins[ ++_top ] = cj;
                _poses[   _top ] = 0;

                if (cj._cchLeft > 0)
                {
                    _src = cj._srcLeft;
                    _off = cj._offLeft;
                    _cch = cj._cchLeft;
                }
                else
                {
                    _src = cj._srcRight;
                    _off = cj._offRight;
                    _cch = cj._cchRight;
                }
            }

            _currPos  = 0;
            _startPos = 0;

            cacheLeaf();
        }

        char currChar ( )
        {
            assert _src instanceof String || _src instanceof char[];

            int index = _off + _currPos - _startPos;

            return _chars == null ? _string.charAt( index ) : _chars[ index ];
        }

        void prev ( )
        {
            setPos( _currPos - 1 );
        }
        
        void next ( )
        {
            setPos( _currPos + 1 );
        }

        void setPos ( final int newPos )
        {
            assert newPos >= 0;
            assert !(_src instanceof CharJoin);

            if (newPos < _startPos || newPos >= _startPos + _cch)
            {
                _startPos = _poses[ _top ];

                while ( newPos < _startPos || newPos >= _startPos + _joins[ _top ].length() )
                {
                    _joins[ _top-- ] = null;
                    _startPos = _poses[ _top ];
                }

                CharJoin cj = _joins[ _top ];

                for ( ; ; )
                {
                    if (newPos < _startPos + cj._cchLeft)
                    {
                        _src = cj._srcLeft;
                        _off = cj._offLeft;
                        _cch = cj._cchLeft;
                    }
                    else
                    {
                        _src = cj._srcRight;
                        _off = cj._offRight;
                        _cch = cj._cchRight;

                        _startPos += cj._cchLeft;
                    }

                    if (!(_src instanceof CharJoin))
                        break;

                    cj = (CharJoin) _src;
                    
                    _joins[ ++_top ] = cj;
                    _poses[   _top ] = _startPos;
                }

                cacheLeaf();
            }

            assert newPos >= _startPos && newPos < _startPos + _cch;

            _currPos = newPos;
        }

        private void cacheLeaf ( )
        {
            assert _src instanceof String || _src instanceof char[];
            
            if (_src instanceof char[])
            {
                _chars = (char[]) _src;
                _string = null;
            }
            else
            {
                _string = (String) _src;
                _chars = null;
            }
        }
            
        private Object _src;
        private int    _off;
        private int    _cch;
        
        private int    _startPos;
        private int    _currPos;

        private String _string;
        private char[] _chars;
        
        private CharJoin[] _joins = new CharJoin[ CharJoin.MAX_DEPTH + 1 ];
        private int[]      _poses = new int     [ CharJoin.MAX_DEPTH + 1 ];
        private int        _top;
    }
    
    private static ThreadLocal tl_charUtil =
        new ThreadLocal() { protected Object initialValue() { return new CharUtil( 1024 * 32 ); } };

    private CharIterator _charIter = new CharIterator();

    // TODO - 64 is kinda arbitrary.  Perhaps It should be configurable.
    private static final int MAX_COPY = 64;

    // Current char buffer we're allcoating new chars to

    private int    _charBufSize;
    private int    _currentOffset;
    private char[] _currentBuffer;
    
    // These members are used to communicate offset and character count
    // information back to a caller of various methods on CharUtil.
    // Usually, the methods returns the src Object, and these two hold
    // the offset and the char count.
    
    public int _offSrc;
    public int _cchSrc;
} 