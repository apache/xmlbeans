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

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.XmlRuntimeException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;

final class ByNameUnmarshaller implements TypeUnmarshaller
{
    private final ByNameRuntimeBindingType type;

    public ByNameUnmarshaller(ByNameBean type)
    {
        this.type = new ByNameRuntimeBindingType(type);
    }

    public Object unmarshal(UnmarshalContext context)
    {
        final Object inter = type.createIntermediary(context);
        deserializeAttributes(inter, context);
        deserializeContents(inter, context);
        return type.getFinalObjectFromIntermediary(inter, context);
    }

    public Object unmarshalSimpleType(CharSequence lexicalValue,
                                      UnmarshalContext context)
    {
        throw new UnsupportedOperationException();
    }

    //TODO: cleanup this code.  We are doing extra work for assertion checking
    private void deserializeContents(Object inter, UnmarshalContext context)
    {
        final XMLStreamReader xmlStream = context.getXmlStream();
        assert xmlStream.isStartElement();
        final QName ourStartName = xmlStream.getName();
        try {
            //move past our current start element
            xmlStream.next();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }

        while (context.advanceToNextStartElement()) {
            assert xmlStream.isStartElement();

            RuntimeBindingProperty prop = findMatchingElementProperty(context);
            if (prop == null) {
                context.skipElement();
            } else {
                //TODO: implement first one wins?, this is last one wins
                fillElementProp(prop, context, inter);
            }

        }

        assert xmlStream.isEndElement();
        final QName ourEndName = xmlStream.getName();
        assert ourStartName.equals(ourEndName) :
            "expected=" + ourStartName + " got=" + ourEndName;

        try {
            if (xmlStream.hasNext()) xmlStream.next();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    //debugging only method
    private void dumpState(UnmarshalContext context, String str)
    {
        System.out.println(str + "=" +
                           XmlStreamUtils.printEvent(context.getXmlStream()) +
                           " THIS="+this);
    }


    private static void fillElementProp(RuntimeBindingProperty prop,
                                        UnmarshalContext context,
                                        Object inter)
    {
        final TypeUnmarshaller um = prop.getTypeUnmarshaller(context);
        assert um != null;

        final Object prop_val = um.unmarshal(context);
        prop.fill(inter, prop_val);
    }


    private static void fillAttributeProp(RuntimeBindingProperty prop,
                                          CharSequence lexical,
                                          UnmarshalContext context,
                                          Object inter)
    {
        final TypeUnmarshaller um = prop.getTypeUnmarshaller(context);
        assert um != null;

        final Object prop_val = um.unmarshalSimpleType(lexical, context);
        prop.fill(inter, prop_val);
    }

    private void deserializeAttributes(Object inter, UnmarshalContext context)
    {
        final int cnt = context.getAttributeCount();
        for (int att_idx = 0; att_idx < cnt; att_idx++) {
            RuntimeBindingProperty prop =
                findMatchingAttributeProperty(att_idx, context);
            if (prop != null) {
                String att_val = context.getAttributeValue(att_idx);
                fillAttributeProp(prop, att_val, context, inter);
            }
        }
    }

    private RuntimeBindingProperty findMatchingElementProperty(UnmarshalContext context)
    {
        String uri = context.getNamespaceURI();
        String lname = context.getLocalName();
        return type.getMatchingElementProperty(uri, lname);
    }

    private RuntimeBindingProperty findMatchingAttributeProperty(int att_idx,
                                                                 UnmarshalContext context)
    {
        String uri = context.getAttributeNamespaceURI(att_idx);
        String lname = context.getAttributeLocalName(att_idx);

        return type.getMatchingAttributeProperty(uri, lname);
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
        type.initialize(typeTable, bindingLoader);
    }


}
