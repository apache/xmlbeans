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

import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.events.ElementTypeNames;

/**
 * Abstract base class which implements the type part XMLEvent.
 */

public abstract class XmlEventBase implements XMLEvent
{
    public XmlEventBase ( )
    {
    }
    
    public XmlEventBase ( int type )
    {
        _type = type;
    }

    public void setType ( int type )
    {
        _type = type;
    }
    
    public int getType ( )
    {
        return _type;
    }

    public String getTypeAsString ( )
    {
        return ElementTypeNames.getName( _type );
    }
            
    public boolean isStartElement ( )
    {
        return _type == XMLEvent.START_ELEMENT;
    }
    
    public boolean isEndElement ( )
    {
        return _type == XMLEvent.END_ELEMENT;
    }
    
    public boolean isEntityReference ( )
    {
        return _type == XMLEvent.ENTITY_REFERENCE;
    }
    
    public boolean isStartPrefixMapping ( )
    {
        return _type == XMLEvent.START_PREFIX_MAPPING;
    }
    
    public boolean isEndPrefixMapping ( )
    {
        return _type == XMLEvent.END_PREFIX_MAPPING;
    }
    
    public boolean isChangePrefixMapping ( )
    {
        return _type == XMLEvent.CHANGE_PREFIX_MAPPING;
    }
    
    public boolean isProcessingInstruction ( )
    {
        return _type == XMLEvent.PROCESSING_INSTRUCTION;
    }
    
    public boolean isCharacterData ( )
    {
        return _type == XMLEvent.CHARACTER_DATA;
    }
    
    public boolean isSpace ( )
    {
        return _type == XMLEvent.SPACE;
    }
    
    public boolean isNull ( )
    {
        return _type == XMLEvent.NULL_ELEMENT;
    }
    
    public boolean isStartDocument ( )
    {
        return _type == XMLEvent.START_DOCUMENT;
    }
    
    public boolean isEndDocument ( )
    {
        return _type == XMLEvent.END_DOCUMENT;
    }

    private int _type;
}
