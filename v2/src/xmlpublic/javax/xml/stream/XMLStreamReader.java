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

import javax.xml.namespace.QName;

// Can't find this interface
//import javax.xml.namespace.NamespaceContext;

/**
 *  The XMLStreamReader interface allows forward, read-only access to XML.
 *  It is designed to be the lowest level and most efficient way to
 *  read XML data.
 *
 * <p> The XMLStreamReader is designed to iterate over XML using
 * next() and hasNext().  The data can be accessed using methods such as getEventType(),
 * getNamespaceURI(), getLocalName() and getText();
 *
 * <p> <a href="#next()">next()</a> causes the reader to read the next parse event.
 * next() returns an integer which identifies the type of event just read
 * <p> The event type can be determined using <a href="#getEventType()">getEventType()</a>.
 * <p> Parsing events are defined as the XML Declaration, a DTD,
 * start tag, character data, white space, end tag, comment,
 * or processing instruction
 *
 * <p>For XML 1.0 compliance an XML processor must pass the
 * identifiers of declared unparsed entities and their
 * associated identifiers to the application.  This information is
 * provided through the property API on this interface.
 * The following two properties allow access to this information:
 * javax.xml.stream.notations and javax.xml.stream.entities.
 * When the current event is a DTD the following call will return a
 * list of Notations
 *  <code>List l = (List) getProperty("javax.xml.stream.notations");</code>
 * The following call will return a list of entity declarations:
 * <code>List l = (List) getProperty("javax.xml.stream.entities");</code>
 * These properties can only be accessed during a DTD event and
 * are defined to return null if the information is not available.
 *
 * <p>The following table describes which methods are valid in what state.
 * If a method is called in an invalid state the method will throw a
 * java.lang.IllegalStateException.
 *
 * <table border="2" rules="all" cellpadding="4">
 *   <thead>
 *     <tr>
 *       <th align="center" colspan="2">
 *         Valid methods for each state
 *       </th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <th>Event Type</th>
 *       <th>Valid Methods</th>
 *     </tr>
 *     <tr>
 *       <td> All States  </td>
 *       <td> getProperty(), hasNext(), require(), close(),
 *            getNamespaceURI(), isStartElement(),
 *            isEndElement(), isCharacters(), isWhiteSpace(),
 *            getNamespaceContext(), getEventType(),getLocation(),
 *            hasText()
 *       </td>
 *     </tr>
 *     <tr>
 *       <td> START_ELEMENT  </td>
 *       <td> next(), getLocalName(), hasName(), getPrefix(),
 *            getNamespaceURI(),
 *            getAttributeXXX(), isAttributeSpecified(),
 *            getNamespaceXXX(),
 *            getElementText(), nextTag()
 *       </td>
 *     </tr>
 *     <tr>
 *       <td> END_ELEMENT  </td>
 *       <td> next(), getLocalName(), hasName(), getPrefix(),
 *            getNamespaceURI() , getNamespaceXXX(), nextTag()
 *      </td>
 *     </tr>
 *     <tr>
 *       <td> CHARACTERS  </td>
 *       <td> next(), getTextXXX(), nextTag() </td>
 *     </tr>
 *     <tr>
 *       <td> CDATA  </td>
 *       <td> next(), getTextXXX(), nextTag() </td>
 *     </tr>
 *     <tr>
 *       <td> COMMENT  </td>
 *       <td> next(), getTextXXX(), nextTag() </td>
 *     </tr>
 *     <tr>
 *       <td> SPACE  </td>
 *       <td> next(), getTextXXX(), nextTag() </td>
 *     </tr>
 *     <tr>
 *       <td> START_DOCUMENT  </td>
 *       <td> next(), getEncoding(), next(), getPrefix(), getVersion(), isStandalone(), standaloneSet(),
 *            getCharacterEncodingScheme(), nextTag()</td>
 *     </tr>
 *     <tr>
 *       <td> END_DOCUMENT  </td>
 *       <td> close()</td>
 *     </tr>
 *     <tr>
 *       <td> PROCESSING_INSTRUCTION  </td>
 *       <td> next(), getPITarget(), getPIData(), nextTag() </td>
 *     </tr>
 *     <tr>
 *       <td> ENTITY_REFERENCE  </td>
 *       <td> next(), getLocalName(), getText(), nextTag() </td>
 *     </tr>
 *     <tr>
 *       <td> DTD  </td>
 *       <td> next(), getText(), nextTag() </td>
 *     </tr>
 *   </tbody>
 *  </table>
 *
 * @version 0.6
 * @author Copyright (c) 2003 by BEA Systems. All Rights Reserved.
 * @see javax.xml.stream.events.XMLEvent
 * @see XMLInputFactory
 * @see XMLStreamWriter
 */
public interface XMLStreamReader extends XMLStreamConstants {
    /**
     * Get the value of a feature/property from the underlying implementation
     * @param name The name of the property, may not be null
     * @return The value of the property
     * @throws IllegalArgumentException if name is null
     */
    public Object getProperty(java.lang.String name) throws java.lang.IllegalArgumentException;

    /**
     * Get next parsing event - a processor may may return all contiguous
     * character data in a single chunk, or it may split it into several chunks.
     * If the property javax.xml.stream.isCoalescing is set to true
     * element content must be coalesced and only one CHARACTERS event
     * must be returned for contiguous element content or
     * CDATA Sections.  
     *
     * By default entity references must be 
     * expanded and reported transparently to the application.
     * An exception will be thrown if an entity reference cannot be expanded.
     * If element content is empty (i.e. content is "") then no CHARACTERS event will be reported.
     *
     * <p>Given the following XML:<br>
     * &lt;foo>&lt;!--description-->content text&lt;![CDATA[&lt;greeting>Hello&lt;/greeting>]]>other content&lt;/foo><br>
     * The behavior of calling next() when being on foo will be:<br>
     * 1- the comment (COMMENT)<br>
     * 2- then the characters section (CHARACTERS)<br>
     * 3- then the CDATA section (another CHARACTERS)<br>
     * 4- then the next characters section (another CHARACTERS)<br>
     * 5- then the END_ELEMENT<br>
     *
     * <p><b>NOTE:</b> empty element (such as &lt;tag/>) will be reported
     *  with  two separate events: START_ELEMENT, END_ELEMENT - This preserves
     *   parsing equivalency of empty element to &lt;tag>&lt;/tag>.
     *
     * This method will throw an XMLStreamException if it is called after hasNext() returns false.
     * @see javax.xml.stream.events.XMLEvent
     * @return the integer code corresponding to the current parse event
     * @throws XMLStreamException if this is called when hasNext() returns false
     */
    public int next() throws XMLStreamException;

    /**
     * Test if the current event is of the given type and if the namespace and name do match.
     * @param type the event type
     * @param namespaceURI the uri of the event, may be null
     * @param localName the localName of the event, may not be null
     * @throws XMLStreamException if the required values are not matched.
     */
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException;

    /**
     * Reads the content of a text-only element. Precondition:
     * the current event is START_ELEMENT. Postcondition:
     * The current event is the corresponding END_ELEMENT.
     * @throws XMLStreamException if the current event is not a START_ELEMENT
     */
    public String getElementText() throws XMLStreamException;

    /**
     * Skips any insignificant events until a START_ELEMENT or
     * END_ELEMENT is reached. If other than space characters are
     * encountered, an exception is thrown. This method should
     * be used when processing element-only content because
     * the parser is not able to recognize ignorable whitespace if
     * then DTD is missing or not interpreted.
     * @throws XMLStreamException if the current event is not white space
     */
    public int nextTag() throws XMLStreamException;

    /**
     * Returns true if there are more parsing events and false
     * if there are no more events.  This method will return
     * false if the current state of the XMLStreamReader is
     * END_DOCUMENT
     * @return true if there are more events, false otherwise
     */
    public boolean hasNext() throws XMLStreamException;

    /**
     * Frees any resources associated with this Reader.  This method does not close the
     * underlying input source.
     */
    public void close() throws XMLStreamException;

    /**
     * Return the uri for the given prefix.
     * The uri returned depends on the current state of the parser
     *
     * <p>Throws an exception if the prefix is not bound.
     *
     * <p><strong>NOTE:</strong>The 'xml' prefix is bound as defined in
     * <a href="http://www.w3.org/TR/REC-xml-names/#ns-using">Namespaces in XML</a>
     * specification to "http://www.w3.org/XML/1998/namespace".
     *
     * <p><strong>NOTE:</strong> The 'xmlns' prefix must be resolved to following namespace
     * <a href="http://www.w3.org/2000/xmlns/">http://www.w3.org/2000/xmlns/</a>
     * @param prefix The prefix to lookup, may not be null
     * @return the uri bound to the given prefix or null if it is not bound
     */
    public String getNamespaceURI(String prefix) throws XMLStreamException;

    /**
     * Returns true if the cursor points to a start tag (otherwise false)
     * @return true if the cursor points to a start tag, false otherwise
     */
    public boolean isStartElement();

    /**
     * Returns true if the cursor points to an end tag (otherwise false)
     * @return true if the cursor points to an end tag, false otherwise
     */
    public boolean isEndElement();

    /**
     * Returns true if the cursor points to a character data event
     * @return true if the cursor points to character data, false otherwise
     */
    public boolean isCharacters();

    /**
     * Returns true if the cursor points to a character data event
     * that consists of all whitespace
     * @return true if the cursor points to all whitespace, false otherwise
     */
    public boolean isWhiteSpace();

    /**
     * Returns the normalized attribute value of the
     * attribute with the namespace and localName
     * If the namespaceURI is null the namespace
     * is not checked for equality
     * @param namespaceURI the namespace of the attribute, can be null
     * @param localName the local name of the attribute, cannot be null
     * @return returns the value of the attribute , returns null if not found
     * @throws IllegalStateException if this is not a START_ELEMENT
     */
    public String getAttributeValue(String namespaceURI,
                                    String localName);

    /**
     * Returns the count of attributes on this START_ELEMENT,
     * this method is only valid on a START_ELEMENT.  This
     * count excludes namespace definitions.
     * @return returns the number of attributes
     * @throws IllegalStateException if this is not a START_ELEMENT
     */
    public int getAttributeCount();

    /**
     * Returns the namespace of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the namespace URI (can be null)
     * @throws IllegalStateException if this is not a START_ELEMENT
     */
    public String getAttributeNamespace(int index);

    /**
     * Returns the localName of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the localName of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT
     */
    public String getAttributeName(int index);
    
    public QName getAttributeQName(int index);

    public int getAttributeTextCharacters (
        int index, int sourceStart, char[] myCharArray, int targetStart,
        int length )
            throws XMLStreamException;
            
    /**
     * Returns the prefix of this attribute at the
     * provided index
     * @param index the position of the attribute
     * @return the prefix of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT
     */
    public String getAttributePrefix(int index);

    /**
     * Returns the XML type of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the XML type of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT
     */
    public String getAttributeType(int index);

    /**
     * Returns the value of the attribute at the
     * index
     * @param index the position of the attribute
     * @return the attribute value
     * @throws IllegalStateException if this is not a START_ELEMENT
     */
    public String getAttributeValue(int index);

    /**
     * Returns a boolean which indicates if this
     * attribute was created by default
     * @param index the position of the attribute
     * @return true if this is a default attribute
     * @throws IllegalStateException if this is not a START_ELEMENT
     */

    public boolean isAttributeSpecified(int index);

    /**
     * Returns the count of namespaces declared on this START_ELEMENT or END_ELEMENT,
     * this method is only valid on a START_ELEMENT or END_ELEMENT. On
     * an END_ELEMENT the count is of the namespaces that are about to go
     * out of scope.  This is the equivalent of the information reported
     * by SAX callback for an end element event.
     * @return returns the number of namespace declarations on this specific element
     * @throws IllegalStateException if this is not a START_ELEMENT or END_ELEMENT
     */
    public int getNamespaceCount();

    /**
     * Returns the prefix for the namespace declared at the
     * index.  Returns null if this is the default namespace
     * declaration
     *
     * @param index the position of the namespace declaration
     * @return returns the namespace prefix
     * @throws IllegalStateException if this is not a START_ELEMENT or END_ELEMENT
     */
    public String getNamespacePrefix(int index);

    /**
     * Returns the uri for the namespace declared at the
     * index.
     *
     * @param index the position of the namespace declaration
     * @return returns the namespace uri
     * @throws IllegalStateException if this is not a START_ELEMENT or END_ELEMENT
     */
    public String getNamespaceURI(int index);

//    /**
//     * Returns a read only namespace context for the current
//     * position
//     * @return return a namespace context
//     */
//    public NamespaceContext getNamespaceContext();

    /**
     * Returns a reader that points to the current start element
     * and all of its contents.  Throws an XMLStreamException if the
     * cursor does not point to a START_ELEMENT.<p>
     * The sub stream is read from it MUST be read before the parent stream is
     * moved on, if not any call on the sub stream will cause an XMLStreamException to be
     * thrown.   The parent stream will always return the same result from next()
     * whatever is done to the sub stream.
     * @return an XMLStreamReader which points to the next element
     */
    //  public XMLStreamReader subReader() throws XMLStreamException;

    /**
     * Allows the implementation to reset and reuse any underlying tables
     */
    //  public void recycle() throws XMLStreamException;

    /**
     * Returns an integer code that indicates the type
     * of the event the cursor is pointing to.
     */
    public int getEventType();

    /**
     * Returns the current value of the parse event as a string,
     * this returns the string value of a CHARACTERS event,
     * returns the value of a COMMENT, the replacement value
     * for an ENTITY_REFERENCE,
     * or the String value of the DTD
     * @return the current text or null
     * @throws java.lang.IllegalStateException if this statex is not
     * a valid text state.
     */
    public String getText();

    /**
     * Returns an array which contains the characters from this event.
     * This array should be treated as read-only and transient. I.e. the array will
     * contain the text characters until the XMLStreamReader moves on to the next event.
     * Attempts to hold onto the character array beyond that time or modify the
     * contents of the array are breaches of the contract for this interface.
     * @return the current text or null
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public char[] getTextCharacters();

    /**
     * Returns the offset into the TextCharacter array where the first
     * character (of this text event) is stored.
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public int getTextStart();

    /**
     * Returns the length of the sequence of characters within for this
     * Text event within the TextCharacterArray.
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public int getTextLength();

    public int getTextCharacters (
        char[] myCharArray, int targetStart, int length )
            throws XMLStreamException;

    public java.io.Reader getTextReader ( );
            
    /**
     * Return input encoding if known or null if unknown.
     * @return the encoding of this instance or null
     */
    public String getEncoding();

    /**
     * Return true if the current event has text, false otherwise
     * The following events have text:
     * CHARACTERS,DTD ,ENTITY_REFERENCE, COMMENT
     */
    public boolean hasText();


    /**
     * Return the current location of the processor.
     * If the Location is unknown the processor should return
     * an implementation of Location that returns -1 for the
     * location and null for the publicId and systemId
     * The location information is only valid until next() is
     * called.
     */
    public Location getLocation();

    public QName getName ( );
    
    /**
     * Returns the (local) name of the current event.
     * For START_ELEMENT or END_ELEMENT returns the (local) name of the current element.
     * For ENTITY_REFERENCE it returns entity name.
     * For PROCESSING_INSTRUCTION it returns the target.
     * The current event must be START_ELEMENT or END_ELEMENT, 
     * PROCESSING_INSTRUCTION, or ENTITY_REFERENCE, otherwise null is returned.
     * @return the localName or null if none is available
     */
    public String getLocalName();

    /**
     * returns true if the current event has a name, START_ELEMENT, END_ELEMENT,
     * ENTITY_REFERENCE, and PROCESSING_INSTRUCTION have a name
     * returns false otherwise
     */
    public boolean hasName();

    /**
     * If the current event is a START_ELEMENT or END_ELEMENT  this method
     * returns the URI of the prefix or the default namespace.
     * Returns null if the event does not have a prefix.
     * @return the URI bound to this elements prefix, the default namespace, or null
     */
    public String getNamespaceURI();

    /**
     * Returns the prefix of the current event or null if the event does not have a prefix
     * @return the prefix or null
     */
    public String getPrefix();

    /**
     * Get the xml version declared on the xml declaration
     * Returns null if none was declared
     * @return the XML version or null
     */
    public String getVersion();

    /**
     * Get the standalone declaration from the xml declaration
     * @return true if this is standalone, or false otherwise
     */
    public boolean isStandalone();

    /**
     * Checks if standalone was set in the document
     * @return true if standalone was set in the document, or false otherwise
     */
  public boolean standaloneSet();

    /**
     * Returns the character encoding declared on the xml declaration
     * Returns null if none was declared
     * @return the encoding declared in the document or null
     */
    public String getCharacterEncodingScheme();

    /**
     * Get the target of a processing instruction
     * @return the target or null
     */
    public String getPITarget();

    /**
     * Get the data section of a processing instruction
     * @return the data or null
     */
    public String getPIData();
}




