/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.newstore2;

import java.util.ArrayList;

import java.io.PrintStream;

public final class CharUtil
{
    public static CharUtil getThreadLocalCharUtil ( )
    {
        return (CharUtil) tl_charUtil.get();
    }
    
    public CharUtil ( int charBufSize )
    {
        _charBufSize = charBufSize;
    }
    
    public static final class CharJoin
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
        }
        
        public final Object _srcLeft;
        public final int    _offLeft;
        public final int    _cchLeft;

        public final Object _srcRight;
        public final int    _offRight;
        public final int    _cchRight;

        public final int _depth;

        public int length ( ) { return _cchLeft + _cchRight; }

        public boolean isValid ( int off, int cch )
        {
            // Eliminate left recursion
            
            for ( CharJoin cj = this ; ; cj = (CharJoin) cj._srcLeft )
            {
                if (off < 0 || cch < 0 || off > cj.length() || off + cch > cj.length())
                    return false;

                if (!CharUtil.isValid( cj._srcRight, cj._offRight, cj._cchRight ))
                    return false;

                if (!(cj._srcLeft instanceof CharJoin))
                      return CharUtil.isValid( cj._srcLeft, cj._offLeft, cj._cchLeft );

                off = cj._offLeft;
                cch = cj._cchLeft;
            }
        }

        private void getString ( StringBuffer sb, int d, int off, int cch )
        {
            assert cch > 0;
            
            // Avoid deep left recursion
            
            if (d++ < 32)
            {
                if (off < _cchLeft)
                {
                    int cchL = _cchLeft - off;

                    if (cchL > cch)
                        cchL = cch;
                        
                    CharUtil.getString( sb, d, _srcLeft, _offLeft + off, cchL );
                    
                    cch -= cchL;
                }

                if (cch > 0)
                    CharUtil.getString( sb, d, _srcRight, _offRight, cch );
            }
            else
            {
                ArrayList left = new ArrayList();
                
                for ( CharJoin cj = this ; ; cj = (CharJoin) cj._srcLeft )
                {
                    left.add( cj );
                    
                    if (!(cj._srcLeft instanceof CharJoin))
                    {
                        if (off < cj._cchLeft)
                        {
                            int cchL = cj._cchLeft - off;

                            if (cchL > cch)
                                cchL = cch;

                            CharUtil.getString( sb, d, cj._srcLeft, cj._offLeft + off, cchL );

                            cch -= cchL;
                        }
                        
                        break;
                    }
                }

                for ( int i = left.size() - 1 ; i >= 0 ; i-- )
                {
                    if (cch <= 0)
                        break;
                    
                    CharJoin cj = (CharJoin) left.get( i );

                    int cchR = cch < cj._cchRight ? cch : cj._cchRight;

                    CharUtil.getString( sb, d, cj._srcRight, cj._offRight, cchR );

                    cch -= cchR;
                }
            }
        }

        private void getChars ( char[] chars, int start, int d, int off, int cch )
        {
            assert cch > 0;

            if (d++ < 32)
            {
                if (off < _cchLeft)
                {
                    int cchL = _cchLeft - off;

                    if (cchL > cch)
                        cchL = cch;

                    CharUtil.getChars( chars, start, d, _srcLeft, _offLeft + off, cchL );

                    start += cchL;
                    cch -= cchL;
                }

                if (cch > 0)
                    CharUtil.getChars( chars, start, d, _srcRight, _offRight, cch );
            }
            else
            {
                ArrayList left = new ArrayList();

                for ( CharJoin cj = this ; ; cj = (CharJoin) cj._srcLeft )
                {
                    left.add( cj );

                    if (!(cj._srcLeft instanceof CharJoin))
                    {
                        if (off < cj._cchLeft)
                        {
                            int cchL = cj._cchLeft - off;

                            if (cchL > cch)
                                cchL = cch;

                            CharUtil.getChars(
                                chars, start, d, cj._srcLeft, cj._offLeft + off, cchL );

                            start += cchL;
                            cch -= cchL;
                        }

                        break;
                    }
                }

                for ( int i = left.size() - 1 ; i >= 0 ; i-- )
                {
                    if (cch <= 0)
                        break;

                    CharJoin cj = (CharJoin) left.get( i );

                    int cchR = cch < cj._cchRight ? cch : cj._cchRight;

                    CharUtil.getChars( chars, start, d, cj._srcRight, cj._offRight, cchR );

                    start += cchR;
                    cch -= cchR;
                }
            }
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

    private static void getString ( StringBuffer sb, int d, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch == 0)
            return;

        if (src instanceof String)
        {
            String s = (String) src;
            
            if (off == 0 && cch == s.length())
                sb.append( (String) src );
            else
                sb.append( s.substring( off, off + cch ) );
        }
        else if (src instanceof char[])
            sb.append( (char[]) src, off, cch );
        else
            ((CharJoin) src).getString( sb, d, off, cch );
    }
    
    public static void getString ( StringBuffer sb, Object src, int off, int cch )
    {
        getString( sb, 0, src, off, cch );
    }
    
    public static void getChars ( char[] chars, int start, int d, Object src, int off, int cch )
    {
        assert isValid( src, off, cch );
        assert chars != null && start >= 0 && start <= chars.length;

        if (cch == 0)
            return;

        if (src instanceof String)
            ((String) src).getChars( off, off + cch, chars, start );
        else if (src instanceof char[])
            System.arraycopy( (char[]) src, off, chars, start, cch );
        else
            ((CharJoin) src).getChars( chars, start, d, off, cch );
    }
    
    public static void getChars ( char[] chars, int start, Object src, int off, int cch )
    {
        getChars( chars, start, 0, src, off, cch );
    }
    
    public static String getString ( Object src, int off, int cch )
    {
        assert isValid( src, off, cch );

        if (cch == 0)
            return "";

        if (src instanceof String)
        {
            String s = (String) src;

            if (off == 0 && cch == s.length())
                return s;

            return s.substring( off, off + cch );
        }

        if (src instanceof char[])
            return new String( (char[]) src, off, cch );

        StringBuffer sb = new StringBuffer();
        
        ((CharJoin) src).getString( sb, 0, off, cch );
        
        return sb.toString();
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

        _cchSrc = cch + cchInsert;

        Object newSrc;

        if (_cchSrc <= 32 && canAllocate( _cchSrc ))
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
            
            if (posInsert == 0)
                newSrc = new CharJoin( src, off, cch, srcInsert, offInsert, cchInsert );
            else if (posInsert == cch)
                newSrc = new CharJoin( srcInsert, offInsert, cchInsert, src, off, cch );
            else
            {
                newSrc =
                    new CharJoin( 
                        new CharJoin( src, off, posInsert, srcInsert, offInsert, cchInsert ),
                        0, posInsert + cchInsert,
                        src, off + posInsert, cch - posInsert );
            }
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
        int newOff;

        if (cch == cchRemove)
        {
            newSrc = null;
            newOff = 0;
        }
        else if (posRemove == 0)
        {
            newSrc = src;
            newOff = off + cchRemove;
        }
        else if (posRemove + cchRemove != cch)
        {
            int cchAfter = cch - cchRemove;
            
            if (cchAfter <= 32 && canAllocate( cchAfter ))
            {
                char[] chars = allocate( cchAfter );

                getChars( chars, _offSrc, src, off, posRemove );

                getChars(
                    chars, _offSrc + posRemove,
                    src, off + posRemove + cchRemove, cch - posRemove - cchRemove );

                newSrc = chars;
                newOff = 0;
            }
            else
            {
                newSrc = 
                    new CharJoin(
                        src, off, posRemove,
                        src, off + posRemove + cchRemove, cch - posRemove - cchRemove );
            }
            
            newOff = 0;
        }
        else
        {
            newSrc = src;
            newOff = off;
        }
        
        _offSrc = newOff;
        _cchSrc = cch - cchRemove;

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

    public static void dumpChars ( PrintStream p, Object src, int off, int cch )
    {
        if (src == null)
            p.print( "<null>" );
        else if (src instanceof String)
        {
            String s = (String) src;

            p.print( "String" );

            if (off != 0 || cch != s.length())
            {
                p.print( " offf: " + off + ", cch: " + cch );

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
                p.print( " off: " + off + ", cch: " + cch );

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

        if (src instanceof String)
        {
            String s = (String) src;
            return off <= s.length() && off + cch <= s.length();
        }

        if (src instanceof char[])
        {
            char[] c = (char[]) src;
            return off <= c.length && off + cch <= c.length;
        }

        if (src instanceof CharJoin)
        {
            CharJoin cj = (CharJoin) src;
            return cj.isValid( off, cch );
        }

        return false;
    }
    
    private static ThreadLocal tl_charUtil =
        new ThreadLocal() { protected Object initialValue() { return new CharUtil( 1024 * 32 ); } };

    public int _offSrc;
    public int _cchSrc;

    private int    _charBufSize;
    private int    _currentOffset;
    private char[] _currentBuffer;
}