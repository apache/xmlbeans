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

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.xml.stream.ReferenceResolver;
import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLName;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

public class GenericXmlInputStream implements XMLInputStream
{
    public GenericXmlInputStream ( )
    {
        _master = this;
        _elementCount = 1; // Go all the way
    }

    private GenericXmlInputStream ( GenericXmlInputStream master )
    {
        (_master = master).ensureInit();
        _nextEvent = master._nextEvent;
    }
    
    //
    // The source for all events
    //

    protected XMLEvent nextEvent ( ) throws XMLStreamException
    {
        throw new RuntimeException( "nextEvent not overridden" );
    }

    //
    //
    //

    private class EventItem
    {
        EventItem ( XMLEvent e )
        {
            _event = e;
        }
                   
        int     getType ( ) { return _event.getType(); }
        boolean hasName ( ) { return _event.hasName(); }
        XMLName getName ( ) { return _event.getName(); }
        
        final XMLEvent _event;
        
        EventItem _next;
    }

    private void ensureInit ( )
    {
        if (!_master._initialized)
        {
            try
            {
                _master._nextEvent = getNextEvent();
            }
            catch ( XMLStreamException e )
            {
                throw new RuntimeException( e );
            }
            
            _master._initialized = true;
        }
    }

    private EventItem getNextEvent ( ) throws XMLStreamException
    {
        XMLEvent e = nextEvent();

        return e == null ? null : new EventItem( e );
    }

    public XMLEvent next ( ) throws XMLStreamException
    {
        ensureInit();
        
        EventItem currentEvent = _nextEvent;

        if (_nextEvent != null)
        {
            if (_nextEvent._next == null)
                _nextEvent._next = _master.getNextEvent();

            _nextEvent = _nextEvent._next;
        }

        if (currentEvent == null)
            return null;

        if (currentEvent.getType() == XMLEvent.END_ELEMENT)
        {
            if (--_elementCount <= 0)
                _nextEvent = null;
        }
        else if (currentEvent.getType() == XMLEvent.START_ELEMENT)
            _elementCount++;

        return currentEvent._event;
    }

    public boolean hasNext ( ) throws XMLStreamException
    {
        ensureInit();
        
        return _nextEvent != null;
    }

    public void skip ( ) throws XMLStreamException
    {
        next();
    }

    public void skipElement ( ) throws XMLStreamException
    {
        ensureInit();
        
        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.getType() == XMLEvent.START_ELEMENT)
                break;
        }

        int count = 0;

        for ( ; _nextEvent != null ; next() )
        {
            int type = next().getType();

            if (type == XMLEvent.START_ELEMENT)
                count++;
            else if (type == XMLEvent.END_ELEMENT && --count == 0)
                break;
        }
    }

    public XMLEvent peek ( ) throws XMLStreamException
    {
        ensureInit();
        
        return _nextEvent._event;
    }

    public boolean skip ( int eventType ) throws XMLStreamException
    {
        ensureInit();
        
        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.getType() == eventType)
                return true;
        }

        return false;
    }

    public boolean skip ( XMLName name ) throws XMLStreamException
    {
        ensureInit();
        
        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.hasName() && _nextEvent.getName().equals( name ))
                return true;
        }

        return false;
    }

    public boolean skip ( XMLName name, int eventType ) throws XMLStreamException
    {
        ensureInit();
        
        for ( ; _nextEvent != null ; next() )
        {
            if (_nextEvent.getType() == eventType &&
                  _nextEvent.hasName() &&
                    _nextEvent.getName().equals( name ))
            {
                return true;
            }
        }

        return false;
    }

    public XMLInputStream getSubStream ( ) throws XMLStreamException
    {
        ensureInit();
        
        GenericXmlInputStream subStream = new GenericXmlInputStream( this );

        subStream.skip( XMLEvent.START_ELEMENT );

        return subStream;
    }

    public void close ( ) throws XMLStreamException
    {
        // BUGBUG - can I do anything here, really?
        // SHould I count the number of open sub streams?
        // I have no destructor, how can I close properly?
    }

    public ReferenceResolver getReferenceResolver ( )
    {
        ensureInit();
        
        throw new RuntimeException( "Not impl" );
    }

    public void setReferenceResolver ( ReferenceResolver resolver )
    {
        ensureInit();
        
        throw new RuntimeException( "Not impl" );
    }

    private boolean               _initialized;
    private EventItem             _nextEvent;
    private int                   _elementCount;
    private GenericXmlInputStream _master;
}
