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

package org.apache.xmlbeans.impl.newstore.pub;

import java.util.ArrayList;
import java.io.PrintStream;

public final class Chars
{
//    public Chars ( int charBufSize )
//    {
//        _charBufSize = charBufSize;
//    }
//
//    public static Chars getThreadLocalChars ( )
//    {
//        return (Chars) tl_chars.get();
//    }
//
//    public static final int cch ( long num )
//    {
//        return (int) num;
//    }
//    
//    private static final int off ( long num )
//    {
//        return (int) (num >> 32);
//    }
//
//    private static long cons ( Object obj, int cch, int off )
//    {
//        return ((long) cch) + (((long) off) << 32);
//    }
//
//    private static final boolean isJoin ( Object obj, long num )
//    {
//        return obj instanceof Join;
//    }
//
//    private static final int depth ( Object obj, long num )
//    {
//        return isJoin( obj, num ) ? ((Join) obj)._depth : 0;
//    }
//    
//    private static final class Join
//    {
//        public Join ( Object objLeft, long numLeft, Object objRight, long numRight )
//        {
//            _objLeft  = objLeft;
//            _numLeft  = numLeft;
//            
//            _objRight = objRight;
//            _numRight = numRight;
//            
//            _depth = Math.max( depth( objLeft, numLeft ), depth( objRight, numRight ) ) + 1;
//        }
//        
//        public final Object _objLeft;
//        public final long   _numLeft;
//
//        public final Object _objRight;
//        public final long   _numRight;
//
//        public final int    _depth;
//
//        private int length ( ) { return cch( _numLeft ) + cch( _numRight ); }
//
//        public boolean isValid ( int off, int cch )
//        {
//            // Eliminate left recursion
//            
//            for ( Join cj = this ; ; cj = (Join) cj._objLeft )
//            {
//                if (off < 0 || cch < 0 || off > cj.length() || off + cch > cj.length())
//                    return false;
//
//                if (!Chars.isValid( cj._objRight, cj._numRight ))
//                    return false;
//
//                if (!isJoin( cj._objLeft, cj._numLeft ))
//                    return Chars.isValid( cj._objLeft, cj._numLeft );
//
//                off = off( cj._numLeft );
//                off = cch( cj._numLeft );
//            }
//        }
//
//        private void getString ( StringBuffer sb, int d, int off, int cch )
//        {
//            int cchLeft = cch( _numLeft );
//            
//            assert cch > 0;
//            
//            // Avoid deep left recursion
//            
//            if (d++ < 32)
//            {
//                if (off < cchLeft)
//                {
//                    int cchL = cchLeft - off;
//
//                    if (cchL > cch)
//                        cchL = cch;
//                        
//                    Chars.getString(
//                        sb, d, _objLeft, cons( _objLeft, cchL, off( _numLeft ) + off ) );
//                    
//                    cch -= cchL;
//                }
//
//                if (cch > 0)
//                    Chars.getString( sb, d, _objRight, cons( _objRight, cch, off( _numRight ) ) );
//            }
//            else
//            {
//                ArrayList left = new ArrayList();
//                
//                for ( Join cj = this ; ; cj = (Join) cj._objLeft )
//                {
//                    left.add( cj );
//                    
//                    if (!(cj._objLeft instanceof Join))
//                    {
//                        int cchCj = cch( cj._numLeft );
//                        
//                        if (off < cchCj)
//                        {
//                            int cchL = cchCj - off;
//
//                            if (cchL > cch)
//                                cchL = cch;
//
//                            Chars.getString(
//                                sb, d, cj._objLeft,
//                                cons( cj._objLeft, cchL, off( cj._numLeft ) + off ) );
//
//                            cch -= cchL;
//                        }
//                        
//                        break;
//                    }
//                }
//
//                for ( int i = left.size() - 1 ; i >= 0 ; i-- )
//                {
//                    if (cch <= 0)
//                        break;
//                    
//                    Join cj = (Join) left.get( i );
//
//                    int cchR = cch < cj._cchRight ? cch : cj._cchRight;
//
//                    Chars.getString( sb, d, cj._objRight, cj._offRight, cchR );
//
//                    cch -= cchR;
//                }
//            }
//        }
//
//        private void getChars ( char[] chars, int start, int d, int off, int cch )
//        {
//            assert cch > 0;
//
//            if (d++ < 32)
//            {
//                if (off < _cchLeft)
//                {
//                    int cchL = _cchLeft - off;
//
//                    if (cchL > cch)
//                        cchL = cch;
//
//                    Chars.getChars( chars, start, d, _objLeft, _offLeft + off, cchL );
//
//                    start += cchL;
//                    cch -= cchL;
//                }
//
//                if (cch > 0)
//                    Chars.getChars( chars, start, d, _objRight, _offRight, cch );
//            }
//            else
//            {
//                ArrayList left = new ArrayList();
//
//                for ( Join cj = this ; ; cj = (Join) cj._objLeft )
//                {
//                    left.add( cj );
//
//                    if (!(cj._objLeft instanceof Join))
//                    {
//                        if (off < cj._cchLeft)
//                        {
//                            int cchL = cj._cchLeft - off;
//
//                            if (cchL > cch)
//                                cchL = cch;
//
//                            Chars.getChars(
//                                chars, start, d, cj._objLeft, cj._offLeft + off, cchL );
//
//                            start += cchL;
//                            cch -= cchL;
//                        }
//
//                        break;
//                    }
//                }
//
//                for ( int i = left.size() - 1 ; i >= 0 ; i-- )
//                {
//                    if (cch <= 0)
//                        break;
//
//                    Join cj = (Join) left.get( i );
//
//                    int cchR = cch < cj._cchRight ? cch : cj._cchRight;
//
//                    Chars.getChars( chars, start, d, cj._objRight, cj._offRight, cchR );
//
//                    start += cchR;
//                    cch -= cchR;
//                }
//            }
//        }
//
//        private void dumpChars( PrintStream p, int off, int cch )
//        {
//            p.print( "( " );
//            Chars.dumpChars( p, _objLeft, _offLeft, _cchLeft );
//            p.print( ", " );
//            Chars.dumpChars( p, _objRight, _offRight, _cchRight );
//            p.print( " )" );
//        }
//    }
//
//    private static void getString ( StringBuffer sb, int d, Object obj, long num )
//    {
//        assert isValid( obj, off, cch );
//
//        if (cch == 0)
//            return;
//
//        if (obj instanceof String)
//        {
//            String s = (String) obj;
//            
//            if (off == 0 && cch == s.length())
//                sb.append( (String) obj );
//            else
//                sb.append( s.substring( off, off + cch ) );
//        }
//        else if (obj instanceof char[])
//            sb.append( (char[]) obj, off, cch );
//        else
//            ((Join) obj).getString( sb, d, off, cch );
//    }
//    
//    public static void getString ( StringBuffer sb, Object obj, int off, int cch )
//    {
//        getString( sb, 0, obj, off, cch );
//    }
//    
//    public static void getChars ( char[] chars, int start, int d, Object obj, int off, int cch )
//    {
//        assert isValid( obj, off, cch );
//        assert chars != null && start >= 0 && start <= chars.length;
//
//        if (cch == 0)
//            return;
//
//        if (obj instanceof String)
//            ((String) obj).getChars( off, off + cch, chars, start );
//        else if (obj instanceof char[])
//            System.arraycopy( (char[]) obj, off, chars, start, cch );
//        else
//            ((Join) obj).getChars( chars, start, d, off, cch );
//    }
//    
//    public static void getChars ( char[] chars, int start, Object obj, int off, int cch )
//    {
//        getChars( chars, start, 0, obj, off, cch );
//    }
//    
//    public static String getString ( Object obj, int off, int cch )
//    {
//        assert isValid( obj, off, cch );
//
//        if (cch == 0)
//            return "";
//
//        if (obj instanceof String)
//        {
//            String s = (String) obj;
//
//            if (off == 0 && cch == s.length())
//                return s;
//
//            return s.substring( off, off + cch );
//        }
//
//        if (obj instanceof char[])
//            return new String( (char[]) obj, off, cch );
//
//        StringBuffer sb = new StringBuffer();
//        
//        ((Join) obj).getString( sb, 0, off, cch );
//        
//        return sb.toString();
//    }
//
//    public Object insertChars (
//        int posInsert,
//        Object obj, int off, int cch,
//        Object objInsert, int offInsert, int cchInsert )
//    {
//        assert isValid( obj, off, cch );
//        assert isValid( objInsert, offInsert, cchInsert );
//        assert posInsert >= 0 && posInsert <= cch;
//
//        // TODO - at some point, instead of creating joins, I should
//        // normalize all the text into a single buffer to stop large
//        // tree;s from being built when many modifications happen...
//
//        // TODO - actually, I should see if the size of the new char
//        // sequence is small enough to simply allocate a new contigous
//        // sequence, either in a common char[] managed by the master,
//        // or just create a new string ... this goes for remove chars
//        // as well.
//
//        _offSrc = 0;
//        _cchSrc = cch + cchInsert;
//
//        if (cch == 0)
//            return obj;
//
//        if (posInsert == 0)
//            return new Join( obj, off, cch, objInsert, offInsert, cchInsert );
//
//        if (posInsert == cch)
//            return new Join( objInsert, offInsert, cchInsert, obj, off, cch );
//
//        return
//            new Join( 
//                new Join( obj, off, posInsert, objInsert, offInsert, cchInsert ),
//                0, posInsert + cchInsert,
//                obj, off + posInsert, cch - posInsert );
//    }
//
//    public Object removeChars ( int posRemove, int cchRemove, Object obj, int off, int cch )
//    {
//        assert isValid( obj, off, cch );
//        assert posRemove >= 0 && posRemove <= cch;
//        assert cchRemove >= 0 && posRemove + cchRemove <= cch;
//
//        Object newSrc;
//        int newOff;
//
//        if (cch == cchRemove)
//        {
//            newSrc = null;
//            newOff = 0;
//        }
//        else if (posRemove == 0)
//        {
//            newSrc = obj;
//            newOff = off + cchRemove;
//        }
//        else if (posRemove + cchRemove != cch)
//        {
//            newSrc = 
//                new Join(
//                    obj, off, posRemove,
//                    obj, off + posRemove + cchRemove, cch - posRemove - cchRemove );
//            
//            newOff = 0;
//        }
//        else
//        {
//            newSrc = obj;
//            newOff = off;
//        }
//        
//        _offSrc = newOff;
//        _cchSrc = cch - cchRemove;
//
//        return newSrc;
//    }
//
//    private char[] allocate ( int cch )
//    {
//        assert _currentBuffer == null || _currentBuffer.length - _currentOffset > 0;
//        
//        if (_currentBuffer == null)
//        {
//            _currentBuffer = new char [ Math.max( cch, _charBufSize ) ];
//            _currentOffset = 0;
//        }
//
//        _offSrc = _currentOffset;
//        _cchSrc = Math.min( _currentBuffer.length - _currentOffset, cch );
//
//        char[] retBuf = _currentBuffer;
//
//        if ((_currentOffset += _cchSrc) == _currentBuffer.length)
//        {
//            _currentBuffer = null;
//            _currentOffset = 0;
//        }
//
//        return retBuf;
//    }
//    
//    public Object saveChars (
//        Object obj, int off, int cch,
//        Object objPrev, int offPrev, int cchPrev )
//    {
//        assert isValid( obj, off, cch );
//        assert isValid( objPrev, offPrev, cchPrev );
//
//        char[] objAlloc = allocate( cch );
//
//        getChars( objAlloc, _offSrc, obj, off, _cchSrc );
//
//        Object objNew;
//        int offNew;
//
//        if (cchPrev == 0)
//        {
//            objNew = objAlloc;
//            offNew = _offSrc;
//        }
//        else if (objPrev == objAlloc && offPrev + cchPrev == _offSrc)
//        {
//            objNew = objPrev;
//            offNew = offPrev;
//        }
//        else
//        {
//            objNew = new Join( objPrev, offPrev, cchPrev, objAlloc, _offSrc, _cchSrc );
//            offNew = 0;
//        }
//        
//        int cchNew = _cchSrc + cchPrev;
//        
//        int cchR = cch - _cchSrc;
//        
//        if (cchR > 0)
//        {
//            int cchL = _cchSrc;
//
//            objAlloc = allocate( cchR );
//            
//            assert _cchSrc == cchR;
//            assert _offSrc == 0;
//
//            getChars( objAlloc, _offSrc, obj, off + cchL, _cchSrc );
//
//            objNew = new Join( objNew, offNew, cchNew, objAlloc, _offSrc, cchR );
//            offNew = 0;
//            cchNew += cchR;
//        }
//
//        _offSrc = offNew;
//        _cchSrc = cchNew;
//        
//        return objNew;
//    }
//
//    private static void dumpText ( PrintStream o, String s )
//    {
//        o.print( "\"" );
//
//        for ( int i = 0 ; i < s.length() ; i++ )
//        {
//            char ch = s.charAt( i );
//
//            if (i == 36)
//            {
//                o.print( "..." );
//                break;
//            }
//
//            if      (ch == '\n') o.print( "\\n" );
//            else if (ch == '\r') o.print( "\\r" );
//            else if (ch == '\t') o.print( "\\t" );
//            else if (ch == '\f') o.print( "\\f" );
//            else if (ch == '\f') o.print( "\\f" );
//            else if (ch == '"' ) o.print( "\\\"" );
//            else                 o.print( ch );
//        }
//
//        o.print( "\"" );
//    }
//
//    public static void dumpChars ( PrintStream p, Object obj, int off, int cch )
//    {
//        if (obj == null)
//            p.print( "<null>" );
//        else if (obj instanceof String)
//        {
//            String s = (String) obj;
//
//            p.print( "String" );
//
//            if (off != 0 || cch != s.length())
//            {
//                p.print( " offf: " + off + ", cch: " + cch );
//
//                if (off < 0 || off > s.length() || off + cch < 0 || off + cch > s.length())
//                {
//                    p.print( " (Error)" );
//                    return;
//                }
//            }
//
//            p.print( ": " );
//            dumpText( p, s.substring( off, off + cch ) );
//        }
//        else if (obj instanceof char[])
//        {
//            char[] chars = (char[]) obj;
//
//            p.print( "char[]" );
//
//            if (off != 0 || cch != chars.length)
//            {
//                p.print( " off: " + off + ", cch: " + cch );
//
//                if (off < 0 || off > chars.length || off + cch < 0 || off + cch > chars.length)
//                {
//                    p.print( " (Error)" );
//                    return;
//                }
//            }
//
//            p.print( ": " );
//            dumpText( p, new String( chars, off, cch ) );
//        }
//        else if (obj instanceof Join)
//        {
//            p.print( "Join" );
//
//            ((Join) obj).dumpChars( p, off, cch );
//        }
//        else
//        {
//            p.print( "Unknown text source" );
//        }
//    }
//
//    public static boolean isValid ( Object obj, long num )
//    {
//        int cch = cch( num );
//        
//        if (cch < 0 || off < 0)
//            return false;
//
//        if (obj == null)
//            return off == 0 && cch == 0;
//
//        if (obj instanceof String)
//        {
//            String s = (String) obj;
//            return off <= s.length() && off + cch <= s.length();
//        }
//
//        if (obj instanceof char[])
//        {
//            char[] c = (char[]) obj;
//            return off <= c.length && off + cch <= c.length;
//        }
//
//        if (obj instanceof Join)
//        {
//            Join cj = (Join) obj;
//            return cj.isValid( off, cch );
//        }
//
//        return false;
//    }
//    
//    private Chars ( )
//    {
//        this( 1024 * 32 );
//    }
//
//    private static ThreadLocal tl_chars =
//        new ThreadLocal() { protected Object initialValue() { return new Chars(); } };
//
//    public int _offSrc;
//    public int _cchSrc;
//
//    private int    _charBufSize;
//    private int    _currentOffset;
//    private char[] _currentBuffer;
}