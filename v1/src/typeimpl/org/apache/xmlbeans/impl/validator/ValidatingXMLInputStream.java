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

package org.apache.xmlbeans.impl.validator;

import org.apache.xmlbeans.impl.common.Chars;
import org.apache.xmlbeans.impl.common.XMLNameHelper;
import org.apache.xmlbeans.impl.common.GenericXmlInputStream;
import org.apache.xmlbeans.impl.common.ValidatorListener.Event;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XMLStreamValidationException;
import java.util.Map;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Collections;

import weblogic.xml.stream.Attribute;
import weblogic.xml.stream.AttributeIterator;
import weblogic.xml.stream.CharacterData;
import weblogic.xml.stream.StartElement;
import weblogic.xml.stream.XMLEvent;
import weblogic.xml.stream.XMLInputStream;
import weblogic.xml.stream.XMLName;
import weblogic.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;

public final class ValidatingXMLInputStream
    extends GenericXmlInputStream implements Event
{
    public ValidatingXMLInputStream (
        XMLInputStream xis,
        SchemaTypeLoader typeLoader, SchemaType sType, XmlOptions options )
            throws XMLStreamException
    {
        _source = xis;

        // Figure out the root type

        options = XmlOptions.maskNull( options );
        
        SchemaType type = (SchemaType) options.get( XmlOptions.DOCUMENT_TYPE );

        if (type == null)
            type = sType;

        if (type == null)
        {
            type = BuiltinSchemaTypeSystem.ST_ANY_TYPE;

            xis = xis.getSubStream();

            if (xis.skip( XMLEvent.START_ELEMENT ))
            {
                SchemaType docType =
                    typeLoader.findDocumentType(
                        XMLNameHelper.getQName( xis.next().getName() ) );

                if (docType != null)
                    type = docType;
            }

            xis.close();
        }

        // Create the validator

        _validator =
            new Validator(
                type, null, typeLoader, options, new ExceptionXmlErrorListener() );

        nextEvent( ValidatorListener.BEGIN );
    }

    // TODO - this is the quick and dirty impl of streaming validation,
    // may objects are created (like strings) which can be optimized
    
    protected XMLEvent nextEvent ( ) throws XMLStreamException
    {
        XMLEvent e = _source.next();

        if (e == null)
        {
            if (!_finished)
            {
                flushText();
                nextEvent( ValidatorListener.END );
                _finished = true;
            }
        }
        else
        {
            switch ( e.getType() )
            {
            case XMLEvent.CHARACTER_DATA :
            case XMLEvent.SPACE :
            {
                CharacterData cd = (CharacterData) e;

                if (cd.hasContent())
                    _text.append( cd.getContent() );

                break;
            }
            case XMLEvent.START_ELEMENT :
            {
                StartElement se = (StartElement) e;
                
                flushText();

                // Used for prefix to namespace mapping
                _startElement = se;

                // Prepare the xsi:* values
                
                AttributeIterator attrs = se.getAttributes();

                while ( attrs.hasNext() )
                {
                    Attribute attr = attrs.next();

                    XMLName attrName = attr.getName();

                    if ("http://www.w3.org/2001/XMLSchema-instance".equals(
                            attrName.getNamespaceUri() ))
                    {
                        String local = attrName.getLocalName();

                        if (local.equals( "type" ))
                            _xsiType = attr.getValue();
                        else if (local.equals( "nil" ))
                            _xsiNil = attr.getValue();
                        else if (local.equals( "schemaLocation" ))
                            _xsiLoc = attr.getValue();
                        else if (local.equals( "noNamespaceSchemaLocation" ))
                            _xsiNoLoc = attr.getValue();
                    }
                }

                // Emit the START

                // TODO - should delay the aquisition of the name
                _name = e.getName();

                nextEvent( ValidatorListener.BEGIN );
                
                // Emit the attrs
                
                attrs = se.getAttributes();

                while ( attrs.hasNext() )
                {
                    Attribute attr = attrs.next();

                    XMLName attrName = attr.getName();

                    if ("http://www.w3.org/2001/XMLSchema-instance".equals(
                            attrName.getNamespaceUri() ))
                    {
                        String local = attrName.getLocalName();

                        if (local.equals( "type" ))
                            continue;
                        else if (local.equals( "nil" ))
                            continue;
                        else if (local.equals( "schemaLocation" ))
                            continue;
                        else if (local.equals( "noNamespaceSchemaLocation" ))
                            continue;
                    }

                    // TODO - God, this is lame :-)

                    _text.append( attr.getValue() );
                    _name = attr.getName();
                    
                    nextEvent( ValidatorListener.ATTR );
                }

                clearText();

                _startElement = null;

                break;
            }

            case XMLEvent.END_ELEMENT :
            {
                flushText();
                
                nextEvent( ValidatorListener.END );

                break;
            }
            }
        }

        return e;
    }

    private void clearText ( )
    {
        _text.delete( 0, _text.length() );
    }
    
    private void flushText ( ) throws XMLStreamException
    {
        if (_text.length() > 0)
        {
            nextEvent( ValidatorListener.TEXT );
            clearText();
        }
    }
    
    public String getNamespaceForPrefix ( String prefix )
    {
        if (_startElement == null)
            return null;

        Map map = _startElement.getNamespaceMap();

        if (map == null)
            return null;

        return (String) map.get( prefix );
    }

    public XmlCursor getLocationAsCursor ( )
    {
        return null;
    }
    
    public boolean getXsiType ( Chars chars )
    {
        if (_xsiType == null)
            return false;

        chars.string = _xsiType;
        chars.buffer = null;

        return true;
    }
    
    public boolean getXsiNil ( Chars chars )
    {
        if (_xsiNil == null)
            return false;

        chars.string = _xsiNil;
        chars.buffer = null;

        return true;
    }

    public boolean getXsiLoc ( Chars chars )
    {
        if (_xsiLoc == null)
            return false;

        chars.string = _xsiLoc;
        chars.buffer = null;

        return true;
    }

    public boolean getXsiNoLoc ( Chars chars )
    {
        if (_xsiNoLoc == null)
            return false;

        chars.string = _xsiNoLoc;
        chars.buffer = null;

        return true;
    }

    public QName getName ( )
    {
        return XMLNameHelper.getQName( _name );
    }

    public void getText ( Chars chars )
    {
        chars.string = _text.toString();
        chars.buffer = null;
    }

    public void getText ( Chars chars, int wsr )
    {
        chars.string = XmlWhitespace.collapse( _text.toString(), wsr );
        chars.buffer = null;
    }

    // TODO - very expensive to get a string here
    public boolean textIsWhitespace ( )
    {
        for ( int i = 0 ; i < _text.length() ; i++ )
        {
            switch ( _text.charAt( i ) )
            {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    break;

                default :
                    return false;
            }
        }
        
        return true;
    }
    
    private final class ExceptionXmlErrorListener extends AbstractCollection
    {
        public boolean add(Object o)
        {
            assert ValidatingXMLInputStream.this._exception == null;
            
            ValidatingXMLInputStream.this._exception = 
                new XMLStreamValidationException( (XmlError)o );

            return false;
        }

        public Iterator iterator()
        {
            return Collections.EMPTY_LIST.iterator();
        }

        public int size()
        {
            return 0;
        }
    }

    private void nextEvent ( int kind )
        throws XMLStreamException
    {
        assert _exception == null;
        
        _validator.nextEvent( kind, this );

        if (_exception != null)
            throw _exception;
    }
    
    private XMLStreamValidationException _exception;

    private XMLInputStream _source;
    private Validator      _validator;
    private StringBuffer   _text = new StringBuffer();
    private boolean        _finished;
    private String         _xsiType;
    private String         _xsiNil;
    private String         _xsiLoc;
    private String         _xsiNoLoc;
    private XMLName        _name;
    private StartElement   _startElement;
}