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

package org.apache.xmlbeans.xml.stream;

/**
 * This is the base element interface for handling markup events.
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.ProcessingInstruction
 * @see org.apache.xmlbeans.xml.stream.StartElement
 * @see org.apache.xmlbeans.xml.stream.EndElement
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.XMLName
 * @see org.apache.xmlbeans.xml.stream.StartDocument
 */

public interface XMLEvent {
  /**
   * A constant which identifies an XMLEvent
   * @see org.apache.xmlbeans.xml.stream.XMLEvent
   */
  public static final int XML_EVENT=0x00000001;
  /**
   * A constant which identifies a StartElement
   * @see org.apache.xmlbeans.xml.stream.StartElement
   */
  public static final int START_ELEMENT=0x00000002;
  /**
   * A constant which identifies an EndElement
   * @see org.apache.xmlbeans.xml.stream.EndElement
   */
  public static final int END_ELEMENT=0x00000004;
  /**
   * A constant which identifies a ProcessingInstruction
   * @see org.apache.xmlbeans.xml.stream.ProcessingInstruction
   */
  public static final int PROCESSING_INSTRUCTION=0x00000008;
  /**
   * A constant which identifies a CharacterData Event
   * @see org.apache.xmlbeans.xml.stream.CharacterData
   */
  public static final int CHARACTER_DATA=0x00000010;
  /**
   * A constant which identifies a Comment
   * @see org.apache.xmlbeans.xml.stream.Comment
   */
  public static final int COMMENT=0x00000020;
  /**
   * A constant which identifies a Space
   * @see org.apache.xmlbeans.xml.stream.Space
   */
  public static final int SPACE=0x00000040;
  /**
   * A constant which identifies a NullElement
   */
  public static final int NULL_ELEMENT=0x00000080;
  /**
   * A constant which identifies a StartDocument
   * @see org.apache.xmlbeans.xml.stream.StartDocument
   */
  public static final int START_DOCUMENT=0x00000100;
  /**
   * A constant which identifies an EndDocument
   * @see org.apache.xmlbeans.xml.stream.EndDocument
   */
  public static final int END_DOCUMENT=0x00000200;
  /**
   * A constant which identifies a StartPrefixMapping
   * @see org.apache.xmlbeans.xml.stream.StartPrefixMapping
   */
  public static final int START_PREFIX_MAPPING=0x00000400;
  /**
   * A constant which identifies a EndPrefixMapping
   * @see org.apache.xmlbeans.xml.stream.EndPrefixMapping
   */
  public static final int END_PREFIX_MAPPING=0x00000800;
  /**
   * A constant which identifies a ChangePrefixMapping
   * @see org.apache.xmlbeans.xml.stream.ChangePrefixMapping
   */
  public static final int CHANGE_PREFIX_MAPPING=0x00001000;
  /**
   * A constant which identifies an EntityReference 
   * @see org.apache.xmlbeans.xml.stream.EntityReference
   */
  public static final int ENTITY_REFERENCE=0x00002000;
  /**
   * Get the event type of the current element,
   * returns an integer so that switch statements
   * can be written on the result
   */
  public int getType();
  /**
   * Get the event type of the current element,
   * returns an integer so that switch statements
   * can be written on the result
   */
  public XMLName getSchemaType();
  /**
   * Get the string value of the type name
   */
  public String getTypeAsString();
  /**
   * Get the XMLName of the current element
   * @see org.apache.xmlbeans.xml.stream.XMLName
   */
  public XMLName getName();

  /**
   * Check if this Element has a name
   */
  public boolean hasName();

  /**
   * Return the location of this Element
   */
  public Location getLocation();

  /**
   * Method access to the elements type
   */
  public boolean isStartElement();
  public boolean isEndElement();
  public boolean isEntityReference();
  public boolean isStartPrefixMapping();
  public boolean isEndPrefixMapping();
  public boolean isChangePrefixMapping();
  public boolean isProcessingInstruction();
  public boolean isCharacterData();
  public boolean isSpace();
  public boolean isNull();
  public boolean isStartDocument();
  public boolean isEndDocument();
}
