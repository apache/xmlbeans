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

package org.apache.xmlbeans;


import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * A Marshaller object is used to convert Java objects to XML documents.
 */
public interface Marshaller
{
    /**
     * Get an XMLStreamReader object that represents the Java object as XML.
     * Note that the object's contents are accessed on demand, so modifying
     * the object while reading from the reader will produce undefined results.
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * As of this writing (11/22/2003), the returned reader will NOT contain
     * a START_DOCUMENT or END_DOCUMENT element.
     * The reader's first event is a START_ELEMENT event.
     *
     * @param obj
     * @param nscontext  initial NamespaceContext representing initial defined namespaces
     * @param context
     * @return  XMLStreamReader representing the XML content
     * @throws XmlException
     */
    XMLStreamReader marshall(Object obj,
                             NamespaceContext nscontext,
                             MarshalContext context)
        throws XmlException;

    /**
     * Write an XML representation of the Java object to the provided writer.
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * As of this writing (11/22/2003), this method will NOT write
     * a START_DOCUMENT or END_DOCUMENT element.
     * The first event written will be a START_ELEMENT event.
     *
     * @param obj
     * @param writer
     * @throws XmlException
     */
    void marshall(XMLStreamWriter writer,
                  Object obj,
                  MarshalContext context)
        throws XmlException;


    /**
     * Get an XMLStreamReader object that represents the given java type.
     *
     * As of this writing (11/22/2003), the returned reader will NOT contain
     * a START_DOCUMENT or END_DOCUMENT element.
     * The reader's first event is a START_ELEMENT event.
     *
     * @param obj
     * @param elementName
     * @param schemaType
     * @param javaType
     * @param context
     * @return
     * @throws XmlException
     */
    XMLStreamReader marshallType(Object obj,
                                 QName elementName,
                                 QName schemaType,
                                 String javaType,
                                 MarshalContext context)
        throws XmlException;


    /**
     * Write an XML representation of the Java object to the provided writer.
     *
     * As of this writing (11/22/2003), this method will NOT write
     * a START_DOCUMENT or END_DOCUMENT element.
     * The first event written will be a START_ELEMENT event.
     *
     * @param writer
     * @param obj
     * @param elementName
     * @param schemaType
     * @param javaType
     * @throws XmlException
     */
    void marshallType(XMLStreamWriter writer,
                      Object obj,
                      QName elementName,
                      QName schemaType,
                      String javaType,
                      MarshalContext context)
        throws XmlException;

}
