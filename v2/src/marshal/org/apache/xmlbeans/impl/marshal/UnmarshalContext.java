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


package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.XmlName;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collection;

/**
 * An UnmarshalContext holds the mutable state using by an Unmarshaller
 * during unmarshalling.  Example contents are an id -> object table
 * for href processing, and the position in the xml document.
 *
 * The UnmarshalContext is purposefullly unsynchronized.
 * Only one thread should ever be accessing this object, and a new one will be
 * required for each unmarshalling pass.
 */
final class UnmarshalContext
{
    private final XMLStreamReader baseReader;
    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;
    private final Collection errors;

    public UnmarshalContext(XMLStreamReader baseReader,
                             BindingLoader bindingLoader,
                             RuntimeBindingTypeTable typeTable,
                             Collection errors)
    {
        this.baseReader = baseReader;
        this.bindingLoader = bindingLoader;
        this.errors = errors;
        this.typeTable = typeTable;
    }


    XMLStreamReader getXmlStream()
    {
        return baseReader;
    }

    RuntimeBindingTypeTable getTypeTable()
    {
        return typeTable;
    }


    TypeUnmarshaller getTypeUnmarshaller(QName xsi_type)
    {
        XmlName xname = XmlName.forTypeNamed(xsi_type);
        BindingType binding_type =
            bindingLoader.getBindingType(bindingLoader.lookupPojoFor(xname));
        if (binding_type == null) {
            String msg = "unable to locate binding type for " + xsi_type;
            throw new XmlRuntimeException(msg);
        }
        TypeUnmarshaller um =
            typeTable.getTypeUnmarshaller(binding_type);
        if (um == null) {
            String msg = "unable to locate unmarshaller for " + binding_type;
            throw new XmlRuntimeException(msg);
        }
        return um;
    }


    public BindingLoader getBindingLoader()
    {
        return bindingLoader;
    }

    //package only -- get read/write version of error collection

    /**
     * package only -- get read/write version of error collection
     *
     * @return  read/write version of error collection
     */
    Collection getErrorCollection()
    {
        return errors;
    }



    // ======================= xml access methods =======================

    CharSequence getElementText()
    {
        try {
            return MarshalStreamUtils.getContent(baseReader, errors);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    /**
     * special marker qname object used to indicate that we noticed
     * an xsi:nil="true" value while we were looking for an xsi:type attribute
     *
     * note that such a value would never be returned by getXsiType since
     * it is illegal to declare types in the instance namespace.
     */
    static final QName XSI_NIL_MARKER = MarshalStreamUtils.XSI_NIL_MARKER;

    /**
     * return:
     * XSI_NIL_MARKER for xsi:nil="true"
     * or the QName value found for xsi:type
     * or null if neither one was found
     */
    QName getXsiType()
    {
        return MarshalStreamUtils.getXsiType(baseReader, errors);
    }

    /**
     *
     * @return  false if we hit an end element (any end element at all)
     */
    public boolean advanceToNextStartElement()
    {
        try {
            //move past our current start element
            baseReader.next();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
        return MarshalStreamUtils.advanceToNextStartElement(baseReader);
    }

    public int getAttributeCount()
    {
        assert baseReader.isStartElement();

        return baseReader.getAttributeCount();
    }

    public String getLocalName()
    {
        return baseReader.getLocalName();
    }

    public String getNamespaceURI()
    {
        return baseReader.getNamespaceURI();
    }

    public String getAttributeNamespaceURI(int att_idx)
    {
        return baseReader.getAttributeNamespace(att_idx);
    }

    public String getAttributeLocalName(int att_idx)
    {
        return baseReader.getAttributeLocalName(att_idx);
    }

    public String getAttributeValue(int att_idx)
    {
        return baseReader.getAttributeValue(att_idx);
    }

    public boolean isXsiNilTrue(int att_idx)
    {
        return MarshalStreamUtils.isXsiNilTrue(baseReader, att_idx, errors);
    }

    public void skipElement()
    {
        MarshalStreamUtils.skipElement(baseReader);
    }

}

