/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package org.apache.xmlbeans.impl.store;

import java.util.ConcurrentModificationException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.ChangeStamp;

public class XMLStreamReaderImpl implements XMLStreamReader
{
    public XMLStreamReaderImpl ( XmlCursor cursor )
    {
        assert cursor != null;
        assert !cursor.isAnyAttr();
        
        _cursor = cursor;
        _stamp = cursor.getDocChangeStamp();

        if (!cursor.isStartdoc())
        {
            _stopper = cursor.newCursor();

            if (cursor.isStart())
                _stopper.toEndToken();
        }
    }

    private void checkStamp ( )
    {
        if (_stamp.hasChanged())
        {
            throw
                new ConcurrentModificationException(
                    "Document changed while streaming" );
        }
    }

    //
    // Core
    //
    
    private static int getEventTypeHelper ( TokenType tt )
    {
        switch ( tt.intValue() )
        {
        case TokenType.INT_STARTDOC :
            return START_DOCUMENT;
            
        case TokenType.INT_ENDDOC :
            return END_DOCUMENT;
            
        case TokenType.INT_START :
            return START_ELEMENT;
            
        case TokenType.INT_END :
            return END_ELEMENT;
            
        case TokenType.INT_TEXT :
            return CHARACTERS;
            
        case TokenType.INT_COMMENT :
            return COMMENT;
            
        case TokenType.INT_PROCINST :
            return PROCESSING_INSTRUCTION;
            
        case TokenType.INT_ATTR :
        case TokenType.INT_NAMESPACE :
            throw new RuntimeException( "Unexpected attribute token" );
            
        default :
            throw new RuntimeException( "Unexpected token type" );
        }
    }
    
    public int getEventType ( )
    {
        checkStamp();

        return getEventTypeHelper( _cursor.currentTokenType() );
    }
    
    public int next ( ) throws XMLStreamException
    {
        checkStamp();
        
        assert !_cursor.isAnyAttr();

        if (_stopper != null && _cursor.isAtSamePositionAs( _stopper ))
            throw new XMLStreamException( "No next token" );

        TokenType tt;
        
        if (_cursor.isContainer())
            tt = _cursor.toFirstContentToken();
        else if ((tt = _cursor.toNextToken()).isNone())
            throw new XMLStreamException( "No next token" );

        return getEventTypeHelper( tt );
    }
    
    public boolean hasNext ( ) throws XMLStreamException
    {
        checkStamp();
        
        return
            _stopper != null
                ? _cursor.isAtSamePositionAs( _stopper )
                : _cursor.isEnddoc();
    }

    public QName getName ( )
    {
        checkStamp();

        QName name = null;

        switch ( _cursor.currentTokenType().intValue() )
        {
        case TokenType.INT_PROCINST :
        case TokenType.INT_START :
            name = _cursor.getName();
            break;
            
        case TokenType.INT_END :
            _cursor.push();
            _cursor.toParent();
            name = _cursor.getName();
            _cursor.pop();
            break;
        }

        return name;
    }
    
    public String getLocalName ( )
    {
        QName qn = getName();
        
        return qn == null ? null : qn.getLocalPart();
    }
    
    public boolean hasName ( )
    {
        checkStamp();
        
        boolean hasName = false;

        switch ( _cursor.currentTokenType().intValue() )
        {
        case TokenType.INT_PROCINST :
        case TokenType.INT_START :
        case TokenType.INT_END :
            return true;

        default :
            return false;
        }
    }
    
    public String getNamespaceURI ( )
    {
        QName qn = getName();
        
        return qn == null ? null : qn.getNamespaceURI();
    }
    
    public String getPrefix ( )
    {
        checkStamp();

        String uri = getNamespaceURI();

        if (uri == null)
            return null;

        // 
        // Calling prefixForNamespace could change the document.
        // 

        String prefix = _cursor.prefixForNamespace( uri );

        if (_stamp.hasChanged())
            _stamp = _cursor.getDocChangeStamp();

        return prefix;
    }
    
    //
    //
    //
    
    public Object getProperty ( java.lang.String name )
        throws java.lang.IllegalArgumentException
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public void require ( int type, String namespaceURI, String localName )
        throws XMLStreamException
    {
        checkStamp();

        if (localName == null)
            throw new IllegalArgumentException( "locaName is null" );

        if (getEventType() != type)
        {
            throw
                new XMLStreamException(
                    "Token type mismatch, required " + type +
                        ", have " + getEventType() );
        }

        if (!getLocalName().equals( localName ))
        {
            throw
                new XMLStreamException(
                    "Token local name mismatch, required " + localName +
                        ", have " + getLocalName() );
        }
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getElementText ( ) throws XMLStreamException
    {
        checkStamp();

        if (!hasNext() || !_cursor.isContainer())
        {
            throw
                new XMLStreamException(
                    "Current token does not containt text" );
        }

        String text = _cursor.getTextValue();

        _cursor.toEndToken();

        return text;
    }
    
    public int nextTag ( ) throws XMLStreamException
    {
        checkStamp();

        while ( hasNext() )
        {
            switch ( getEventType() )
            {
            case START_ELEMENT :
            case END_ELEMENT :
                break;
                
            case CHARACTERS :
                if (!Splay.isWhiteSpace( _cursor.getChars() ))
                {
                    throw
                        new XMLStreamException(
                            "Non white space text encountered" );
                }
                
                // Fall through

            case START_DOCUMENT :
            case END_DOCUMENT :
            case COMMENT :
            case PROCESSING_INSTRUCTION :
                next();
                break;
            }
        }

        return getEventType();
    }
    
    public void close ( ) throws XMLStreamException
    {
        checkStamp();

        // Hmmm ... dispose cursor???
    }
    
    public String getNamespaceURI ( String prefix ) throws XMLStreamException
    {
        checkStamp();

        if (prefix == null)
            throw new IllegalArgumentException( "prefix is null" );

        if (prefix.equals( "xmlns" ))
            return Splay._xmlnsUri;
        
        if (prefix.equals( "xml" ))
            return Splay._xml1998Uri;

        _cursor.push();

        if (!_cursor.isContainer())
            _cursor.toParent();

        String uri = _cursor.namespaceForPrefix( prefix );

        _cursor.pop();

        return uri;
    }
    
    public boolean isStartElement ( )
    {
        checkStamp();

        return getEventType() == START_ELEMENT;
    }
    
    public boolean isEndElement ( )
    {
        checkStamp();
        
        return getEventType() == END_ELEMENT;
    }
    
    public boolean isCharacters ( )
    {
        checkStamp();
        
        return getEventType() == CHARACTERS;
    }
    
    public boolean isWhiteSpace ( )
    {
        checkStamp();

        // Som day do shitespace?
        return false;
    }

    //
    // Attributes
    //
    // Note, caching attribute information would be useful
    //
    
    public String getAttributeValue ( String namespaceURI, String localName )
    {
        checkStamp();

        if (!_cursor.isContainer())
        {
            throw
                new IllegalStateException( "Current token has no attributes" );
        }

        // THis is stupid ... 
        if (namespaceURI != null)
        {
            return 
                _cursor.getAttributeText(
                    XmlBeans.getQName( namespaceURI, localName ) );
        }
        
        String value = null;

        _cursor.push();

        for ( _cursor.toNextToken() ; _cursor.isAnyAttr() ;
              _cursor.toNextToken() )
        {
            if (_cursor.isAttr())
            {
                if (_cursor.getName().getLocalPart().equals( localName ))
                {
                    value = _cursor.getTextValue();
                    break;
                }
            }
        }

        _cursor.pop();

        return value;
    }
    
    public int getAttributeCount ( )
    {
        checkStamp();

        if (!_cursor.isContainer())
        {
            throw
                new IllegalStateException( "Current token has no attributes" );
        }
        
        int count = 0;

        _cursor.push();

        for ( _cursor.toNextToken() ; _cursor.isAnyAttr() ;
              _cursor.toNextToken() )
        {
            if (_cursor.isAttr())
                count++;
        }

        _cursor.pop();

        return count;
    }

    private void toAttr ( int i )
    {
        if (i < 0)
            throw new IllegalArgumentException( "Negative attribute index" );
        
        if (!_cursor.isContainer())
        {
            throw
                new IllegalStateException( "Current token has no attributes" );
        }
        
        for ( _cursor.toNextToken() ; _cursor.isAnyAttr() ;
              _cursor.toNextToken() )
        {
            if (_cursor.isAttr() && i-- <= 0)
                return;
        }

        throw new IllegalArgumentException( "No such attribute index: " + i );
    }
    
    public QName getAttributeQName ( int index )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public int getAttributeTextCharacters (
        int index, int sourceStart, char[] myCharArray, int targetStart,
        int length )
            throws XMLStreamException
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getAttributeNamespace ( int index )
    {
        checkStamp();

        _cursor.push();

        try
        {
            toAttr( index );
            return _cursor.getName().getNamespaceURI();
        }
        finally
        {
            _cursor.pop();
        }
    }
    
    public String getAttributeName ( int index )
    {
        checkStamp();

        _cursor.push();

        try
        {
            toAttr( index );
            return _cursor.getName().getLocalPart();
        }
        finally
        {
            _cursor.pop();
        }
    }
    
    public String getAttributePrefix ( int index )
    {
        checkStamp();

        String prefix =
            _cursor.prefixForNamespace( getAttributeNamespace( index ) );

        if (_stamp.hasChanged())
            _stamp = _cursor.getDocChangeStamp();

        return prefix;
    }
    
    public String getAttributeType ( int index )
    {
        checkStamp();

        return "CDATA";
    }
    
    public String getAttributeValue ( int index )
    {
        checkStamp();

        _cursor.push();

        try
        {
            toAttr( index );
            return _cursor.getTextValue();
        }
        finally
        {
            _cursor.pop();
        }
    }
    
    public int getAttributeValue ( int index, char[] chars )
    {
        checkStamp();

        _cursor.push();

        try
        {
            toAttr( index );
            return _cursor.getTextValue( chars, 0, chars.length );
        }
        finally
        {
            _cursor.pop();
        }
    }
    
    public boolean isAttributeSpecified ( int index )
    {
        checkStamp();

        // Dunno .... do this sometime?
        return true;
    }
    
    private void toXmlns ( int i )
    {
        if (i < 0)
        {
            throw
                new IllegalArgumentException(
                    "Negative namesapce attribute index" );
        }
        
        if (!_cursor.isContainer())
        {
            throw
                new IllegalStateException(
                    "Current token has no namespace attributes" );
        }
        
        for ( _cursor.toNextToken() ; _cursor.isAnyAttr() ;
              _cursor.toNextToken() )
        {
            if (_cursor.isNamespace() && i-- <= 0)
                return;
        }

        throw
            new IllegalArgumentException(
                "No such namesapce attribute index: " + i );
    }
    
    public int getNamespaceCount ( )
    {
        checkStamp();

        if (!_cursor.isContainer() && !_cursor.isFinish())
        {
            throw
                new IllegalStateException( "Current token has no attributes" );
        }
        
        _cursor.push();

        if (_cursor.isFinish())
            _cursor.toParent();
        
        int count = 0;

        for ( _cursor.toNextToken() ; _cursor.isAnyAttr() ;
              _cursor.toNextToken() )
        {
            if (_cursor.isNamespace())
                count++;
        }

        _cursor.pop();

        return count;
    }
    
    public String getNamespacePrefix ( int index )
    {
        checkStamp();

        _cursor.push();

        try
        {
            toXmlns( index );
            return _cursor.getName().getLocalPart();
        }
        finally
        {
            _cursor.pop();
        }
    }
    
    public String getNamespaceURI ( int index )
    {
        checkStamp();

        _cursor.push();

        try
        {
            toXmlns( index );
            return _cursor.getName().getNamespaceURI();
        }
        finally
        {
            _cursor.pop();
        }
    }

    //
    // Text
    //
    
    public String getText ( )
    {
        checkStamp();

        if (_cursor.isComment())
            return _cursor.getTextValue();

        if (_cursor.isText())
            return _cursor.getChars();

        throw new IllegalStateException( "Token does not have text" );
    }

    public char[] getTextCharacters ( )
    {
        checkStamp();
        
        // Very hacked, very slow ...
        return getText().toCharArray();
    }

    
    public int getTextStart ( )
    {
        checkStamp();

        return 0;
    }
    
    public int getTextLength ( )
    {
        checkStamp();

        return getTextCharacters().length;
    }

    public boolean hasText ( )
    {
        checkStamp();
        
        return _cursor.isComment() || _cursor.isText();
    }
    
    public int getTextCharacters (
        char[] myCharArray, int targetStart, int length )
            throws XMLStreamException
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public java.io.Reader getTextReader ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    //
    // Misc
    //
    
    public String getEncoding ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public Location getLocation ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getVersion ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isStandalone ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean standaloneSet ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getCharacterEncodingScheme ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getPITarget ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getPIData ( )
    {
        checkStamp();
        
        throw new RuntimeException( "Not implemented" );
    }

    private XmlCursor   _cursor;
    private XmlCursor   _stopper;
    private ChangeStamp _stamp;
    private char[]      _chars;
}