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

package javax.xml.stream;

/**
 * This interface declares the constants used in this API.
 */

public interface XMLStreamConstants {
  /**
   * Indicates an event is a start element
   * @see javax.xml.stream.events.StartElement
   */
  public static final int START_ELEMENT=1;
  /**
   * Indicates an event is an end element
   * @see javax.xml.stream.events.EndElement
   */
  public static final int END_ELEMENT=2;
  /**
   * Indicates an event is a processing instruction
   * @see javax.xml.stream.events.ProcessingInstruction
   */
  public static final int PROCESSING_INSTRUCTION=3;

  /**
   * Indicates an event is characters
   * @see javax.xml.stream.events.Characters
   */
  public static final int CHARACTERS=4;

  /**
   * Indicates an event is a comment
   * @see javax.xml.stream.events.Comment
   */
  public static final int COMMENT=5;

  /**
   * Indicates an event is a space, events are only
   * reported as SPACE if they are ignorable white
   * space.  Otherwise they are reported as CHARACTERS.
   * @see javax.xml.stream.events.Characters
   */
  public static final int SPACE=6;

  /**
   * Indicates an event is a start document
   * @see javax.xml.stream.events.StartDocument
   */
  public static final int START_DOCUMENT=7;

  /**
   * Indicates an event is an end document
   * @see javax.xml.stream.events.EndDocument
   */
  public static final int END_DOCUMENT=8;

  /**
   * Indicates an event is an entity reference
   * @see javax.xml.stream.events.EntityReference
   */
  public static final int ENTITY_REFERENCE=9;

  /**
   * Indicates an event is an attribute
   * @see javax.xml.stream.events.Attribute
   */
  public static final int ATTRIBUTE=10;

  /**
   * Indicates an event is a DTD
   * @see javax.xml.stream.events.DTD
   */
  public static final int DTD=11;

  /**
   * Indicates an event is a CDATA
   * @see javax.xml.stream.events.Characters
   */
  public static final int CDATA=12;

  /**
   * Indicates an event is a namepsace
   * 
   * @see javax.xml.stream.events.Namespace
   */
  public static final int NAMESPACE=13;

  /**
   * Indicates an event is the start of a resolved entity
   * 
   * @see javax.xml.stream.events.StartEntity
   */
  public static final int START_ENTITY=14;

  /**
   * Indicates an event is the end of a resolved entity
   * 
   * @see javax.xml.stream.events.EndEntity
   */
  public static final int END_ENTITY=15;

  /**
   * Indicates a Notation 
   * @see javax.xml.stream.events.NotationDeclaration
   */
  public static final int NOTATION_DECLARATION=16;

  /**
   * Indicates a Entity Declaration
   * @see javax.xml.stream.events.NotationDeclaration
   */
  public static final int ENTITY_DECLARATION=17;
}
