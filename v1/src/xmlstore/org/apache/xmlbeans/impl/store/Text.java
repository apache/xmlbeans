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

//import org.apache.xmlbeans.impl.store.Root.WriteContext;
//import org.apache.xmlbeans.impl.store.Root.ReadContext;
import java.io.IOException;

public final class Text
{
    char[] _buf;
    int    _gap;    // Where the gap starts
    int    _gapLen; // The length of the gap

    int length ( )
    {
        return bufLen() - _gapLen;
    }

    private int bufLen ( )
    {
        return _buf == null ? 0 : _buf.length;
    }

    private void copy ( char[] newBuf )
    {
        assert _buf != null && newBuf.length >= length();
        
        System.arraycopy( _buf, 0, newBuf, 0, _gap );

        int lenAfterGap = _buf.length - _gap - _gapLen;

        System.arraycopy(
            _buf, _gap + _gapLen,
            newBuf, newBuf.length - (lenAfterGap), lenAfterGap );
    }

    void resize ( int cch )
    {
        assert cch > _gapLen;
        
        int newSize = length() + cch;
        int newLen = _buf == null ? 1024 : _buf.length * 2;

        while ( newLen < newSize )
            newLen *= 2;

        char[] newBuf = new char [ newLen ];

        if (_buf != null)
        {
            copy( newBuf );
            _gapLen += newBuf.length - _buf.length;
        }
        else
            _gapLen += newBuf.length;

        _buf = newBuf;
    }

    void trim ( )
    {
        if (_buf != null && _gapLen != 0)
        {
            char[] newBuf = new char [ length() ];

            copy( newBuf );

            _buf = newBuf;
            _gap = 0;
            _gapLen = 0;
        }
    }

    void move ( int pos, Text src, int srcPos, int cch )
    {
        insert( pos, src, srcPos, cch );

        if (src == this && srcPos >= pos)
            srcPos += cch;

        src.remove( srcPos, cch );
    }

    void insert ( int pos, Object txt, int off, int cch )
    {
        if (txt != null)
        {
            if (txt instanceof String)
                insert( pos, (String) txt, off, cch );
            else if (txt instanceof Text)
                insert( pos, (Text) txt, off, cch );
            else
            {
                assert txt instanceof char[];
                insert( pos, (char[]) txt, off, cch );
            }
        }
    }
    
    void insert ( int pos, Text src, int srcPos, int cch )
    {
        //
        // This can deal with copying from itself
        //

        if (cch > 0)
        {
            if (cch > _gapLen)
                resize( cch );
            
            moveGap( pos );

            if (srcPos + cch < src._gap)
                System.arraycopy( src._buf, srcPos, _buf, _gap, cch );
            else if (srcPos >= src._gap)
            {
                System.arraycopy(
                    src._buf, srcPos + src._gapLen, _buf, _gap, cch );
            }
            else
            {
                int leftLen = src._gap - srcPos;
            
                System.arraycopy( src._buf, srcPos, _buf, _gap, leftLen );
            
                System.arraycopy(
                    src._buf, src._gap + src._gapLen,
                    _buf, _gap + leftLen, cch - leftLen );
            }

            _gap += cch;
            _gapLen -= cch;
        }
    }
    
    void insert ( int pos, char[] chars, int off, int cch )
    {
        assert chars != _buf;
        assert pos >= 0 && pos <= length();
        
        if (cch > 0)
        {
            if (cch > _gapLen)
                resize( cch );

            moveGap( pos );

            System.arraycopy( chars, off, _buf, _gap, cch );

            _gap += cch;
            _gapLen -= cch;
        }
    }

    void insert ( int pos, String s )
    {
        insert( pos, s, 0, s.length() );
    }
    
    void insert ( int pos, String s, int off, int cch )
    {
        assert pos >= 0 && pos <= length();
        
        if (cch > 0)
        {
            assert off >= 0 && off < s.length();
            assert cch <= s.length() - off;
            
            if (cch > _gapLen)
                resize( cch );
            
            moveGap( pos );

            s.getChars( off, off + cch, _buf, _gap );

            _gap += cch;
            _gapLen -= cch;
        }
    }

    void remove ( int pos, int cch )
    {
        remove( pos, cch, null, 0 );
    }
    
    void remove ( int pos, int cch, char[] retBuf, int off )
    {
        assert pos >= 0 && pos + cch <= length();

        moveGap( pos );

        assert retBuf == null || retBuf.length - off >= cch;

        if (cch > 0 && retBuf != null)
            System.arraycopy( _buf, _gap + _gapLen, retBuf, off, cch );
        
        _gapLen += cch;
    }

    void moveGap( int pos )
    {
        if (pos < _gap)
            System.arraycopy( _buf, pos, _buf, pos + _gapLen, _gap - pos );
        else if (pos > _gap)
            System.arraycopy( _buf, _gap + _gapLen, _buf, _gap, pos - _gap);

        _gap = pos;
    }

    int unObscure ( int pos, int cch )
    {
        assert cch >= 0;
        assert pos >= 0 && pos + cch <= length();

        if (cch > 0 && (pos < _gap && pos + cch > _gap))
            moveGap( pos + cch );

        return pos < _gap ? pos : pos + _gapLen;
    }

    void fetch ( StringBuffer sb, int pos, int cch )
    {
        assert pos >= 0 && pos + cch <= length();

        if (cch == 0)
            return;

        if (pos + cch <= _gap)
        {
            sb.append( _buf, pos, cch );
        }
        else
        {
            if (pos >= _gap)
            {
                sb.append( _buf, pos + _gapLen, cch );
            }
            else
            {
                sb.append( _buf, pos, _gap - pos );
                sb.append( _buf, _gap + _gapLen, cch - _gap + pos );
            }
        }
    }

    String fetch ( int pos, int cch )
    {
        assert pos >= 0 && pos + cch <= length();

        if (cch == 0)
            return "";

        if (pos + cch <= _gap)
            return new String( _buf, pos, cch );

        if (pos >= _gap)
            return new String( _buf, pos + _gapLen, cch );

        StringBuffer sb = new StringBuffer();

        sb.append( _buf, pos, _gap - pos );
        sb.append( _buf, _gap + _gapLen, cch - _gap + pos );

        return sb.toString();
    }

    void fetch ( char[] buf, int off, int pos, int cch )
    {
        assert off >= 0;
        assert pos >= 0 && pos + cch <= length();
        assert buf.length - off >= cch;

        if (cch == 0)
            return;
        
        if (pos + cch <= _gap)
            System.arraycopy( _buf, pos, buf, off, cch );
        else if (pos >= _gap)
            System.arraycopy( _buf, pos + _gapLen, buf, off, cch );
        else
        {
            int chunk = _gap - pos;
            
            System.arraycopy( _buf, pos, buf, off, chunk );
            
            System.arraycopy(
                _buf, _gap + _gapLen, buf, off + chunk, cch - chunk );
        }
    }

    public String toString ( )
    {
        return fetch( 0, length() );
    }
}
