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

package weblogic.xml.stream;

/**
 *
 * This is the top level interface for iterating over XML Events
 * in an XML document.
 *
 * <p> Difference from SAX </p>
 * <p> An event stream can be thought of encapsulating SAX
 * events. It applies an iterator/pull metaphor to the parser 
 * allowing procedural, stream-based, handling of input XML rather than
 * having to write chained event handlers to handle complex XML
 * documents.
 * <p> Difference from DOM </p>
 * <p>The pull metaphor allows single-pass, stream-based  parsing of the document rather
 * than tree based manipulation.</p>
 *
 * @since XMLInputStream 1.0
 * @version 1.0
 * @see weblogic.xml.stream.XMLEvent
 * @see weblogic.xml.stream.CharacterData
 * @see weblogic.xml.stream.ProcessingInstruction
 * @see weblogic.xml.stream.StartElement
 * @see weblogic.xml.stream.EndElement
 * @see weblogic.xml.stream.CharacterData
 * @see weblogic.xml.stream.XMLName
 */

// REVIEW pdapkus@bea.com 2002-Sep-13 -- while I like the convenience
// of many of these methods, it strikes me that many of these methods
// could implemented as static methods in a utility class.  the down
// side to having them in this base interface is that it makes the
// contract for implementers unnecessarily steap and results in
// duplicated code in methods that can't extend one of the common base
// classes.  

public interface XMLInputStream {
  /**
   * Get the next XMLEvent on the stream
   * @see weblogic.xml.stream.XMLEvent
   */
  public XMLEvent next() throws XMLStreamException;
  /**
   * Check if there are more events to pull of the stream
   * @see weblogic.xml.stream.XMLEvent
   */
  public boolean hasNext() throws XMLStreamException;
  /**
   * Skip the next stream event
   */
  public void skip() throws XMLStreamException;
  /**
   * Skips the entire next start tag / end tag pair.
   */
  public void skipElement() throws XMLStreamException;
  /**
   * Check the next XMLEvent without reading it from the stream.
   * Returns null if the stream is at EOF or has no more XMLEvents.
   * @see weblogic.xml.stream.XMLEvent
   */
  public XMLEvent peek() throws XMLStreamException;
  /**
   * Position the stream at the next XMLEvent of this type.  The method
   * returns true if the stream contains another XMLEvent of this type
   * and false otherwise.
   * @param eventType An integer code that indicates the element type.
   * @see weblogic.xml.stream.XMLEvent
   */
  public boolean skip(int eventType) throws XMLStreamException;
  /**
   * Position the stream at the next element of this name.  The method
   * returns true if the stream contains another element with this name
   * and false otherwise.  Skip is a forward operator only.  It does
   * not look backward in the stream.
   * @param name An object that defines an XML name.
   * If the XMLName.getNameSpaceName() method on the XMLName argument returns
   * null the XMLName will match just the local name.  Prefixes are
   * not checked for equality.
   * @see weblogic.xml.stream.XMLName
   */
  public boolean skip(XMLName name) throws XMLStreamException;
  /**
   * Position the stream at the next element of this name and this type.
   * The method returns true if the stream contains another element 
   * with this name of this type and false otherwise.  
   * @param name An object that defines an XML name.
   * If the XMLName.getNameSpaceName() method on the XMLName argument returns
   * null the XMLName will match just the local name.  Prefixes are
   * not checked for equality.
   * @param eventType An integer code that indicates the element type.
   * @see weblogic.xml.stream.XMLEvent
   * @see weblogic.xml.stream.XMLName
   */
  public boolean skip(XMLName name, int eventType) throws XMLStreamException;
  /**
   * getSubStream() returns a stream which points to the entire next element in the
   * current stream.  For example: take a document that has a root node A, where the children
   * of A are B, C, and D. If the stream is pointing to the start element of A, getSubStream() will return 
   * A, B, C and D including the start element of A and the end element of A.  The position of the parent
   * stream is not changed and the events read by the substream are written back to its parent.   
   */
  public XMLInputStream getSubStream() throws XMLStreamException;
  /**
   * Closes this input stream and releases any system resources associated with the stream.
   */
  public void close() throws XMLStreamException;

  /**
   * Returns the reference resolver that was set for this stream,
   * returns null if no ReferenceResolver has been set.
   * @see weblogic.xml.stream.ReferenceResolver
   */
  public ReferenceResolver getReferenceResolver();
  /**
   * Provides a way to set the ReferenceResolver of the stream,
   * this is mostly needed for handle references to other parts of the
   * document.
   * @see weblogic.xml.stream.ReferenceResolver
   */
  public void setReferenceResolver(ReferenceResolver resolver);
}





