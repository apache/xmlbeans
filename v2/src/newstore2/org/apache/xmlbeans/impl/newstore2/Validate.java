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

import org.apache.xmlbeans.impl.common.ValidatorListener;
import javax.xml.stream.Location;
import org.apache.xmlbeans.XmlCursor;
import javax.xml.namespace.QName;

final class Validate implements ValidatorListener.Event
{
    Validate ( Cur c, ValidatorListener sink )
    {
        if (!c.isUserNode())
            throw new IllegalStateException( "Inappropriate location to validate" );

        _cur = c;

        c.push();

        sink.nextEvent( ValidatorListener.BEGIN, this );
        
        if (c.isAttr())
        {
            c.next();

            if (c.isText())
                sink.nextEvent( ValidatorListener.TEXT, this );
        }
        else
        {
            assert c.isContainer();

            doAttrs( sink, c );

            for ( c.next() ; ! c.isAtEndOfLastPush() ; c.next() )
            {
                switch ( c.kind() )
                {
                case Cur.ELEM :
                    doAttrs( sink, c );
                    sink.nextEvent( ValidatorListener.BEGIN, this );
                    break;
                
                case - Cur.ELEM :
                    sink.nextEvent( ValidatorListener.END, this );
                    break;
                
                case Cur.TEXT :
                    sink.nextEvent( ValidatorListener.TEXT, this );
                    break;
                    
                case Cur.COMMENT  :
                case Cur.PROCINST :
                    c.skip();
                    break;

                default :
                    throw new RuntimeException( "Unexpected kind: " + c.kind() );
                }
            }
        }
        
        sink.nextEvent( ValidatorListener.END, this );

        c.pop();
    }

    private void doAttrs ( ValidatorListener sink, Cur c )
    {
        if (c.toFirstAttr())
        {
            do
            {
                if (c.isNormalAttr() && !c.getUri().equals( Locale._xsi ))
                    sink.nextEvent( ValidatorListener.ATTR, this );
            }
            while ( c.toNextAttr() );

            c.toParent();
        }
        
        sink.nextEvent( ValidatorListener.ENDATTRS, this );
    }

    public String getNamespaceForPrefix ( String prefix )
    {
        throw new RuntimeException( "Not implemeneted" );
    }

    public XmlCursor getLocationAsCursor ( )
    {
        throw new RuntimeException( "Not implemeneted" );
    }

    public Location getLocation ( )
    {
        throw new RuntimeException( "Not implemeneted" );
    }

    public String getXsiType ( )
    {
        return _cur.getAttrValue( Locale._xsiType );
    }

    public String getXsiNil ( )
    {
        return _cur.getAttrValue( Locale._xsiNil );
    }

    public String getXsiLoc ( )
    {
        return _cur.getAttrValue( Locale._xsiLoc );
    }

    public String getXsiNoLoc ( )
    {
        return _cur.getAttrValue( Locale._xsiNoLoc );
    }

    public QName getName ( )
    {
        return _cur.isAtLastPush() ? null : _cur.getName();
    }

    public String getText ( )
    {
        return _cur.isAttr() ? _cur.getValueAsString() : _cur.getString( -1 );
    }

    public String getText ( int wsr )
    {
        return _cur.isAttr() ? _cur.getValueAsString( wsr ) : _cur.getString( -1, wsr );
    }

    public boolean textIsWhitespace ( )
    {
        return
            CharUtil.isWhiteSpace(
                _cur.isAttr() ? _cur.getValueChars() : _cur.getChars( -1 ),
                _cur._offSrc, _cur._cchSrc );
    }

    private Cur _cur;
}