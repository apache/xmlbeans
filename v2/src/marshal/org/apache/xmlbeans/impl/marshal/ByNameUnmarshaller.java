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
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.XmlError;

final class ByNameUnmarshaller implements TypeUnmarshaller
{
    private final ByNameRuntimeBindingType type;

    public ByNameUnmarshaller(ByNameBean type)
    {
        this.type = new ByNameRuntimeBindingType(type);
    }

    public Object unmarshal(UnmarshalContextImpl context)
    {
        final Object inter = type.createIntermediary(context);
        deserializeAttributes(inter, context);
        deserializeContents(inter, context);
        return type.getFinalObjectFromIntermediary(inter, context);
    }

    public Object unmarshalAttribute(UnmarshalContextImpl context)
    {
        throw new UnsupportedOperationException();
    }

    //TODO: cleanup this code.  We are doing extra work for assertion checking
    private void deserializeContents(Object inter, UnmarshalContextImpl context)
    {
        assert context.isStartElement();
        final String ourStartUri = context.getNamespaceURI();
        final String ourStartLocalName = context.getLocalName();
        context.next();

        while (context.advanceToNextStartElement()) {
            assert context.isStartElement();

            RuntimeBindingProperty prop = findMatchingElementProperty(context);
            if (prop == null) {
                context.skipElement();
            } else {
                //TODO: implement first one wins?, this is last one wins
                fillElementProp(prop, context, inter);
            }
        }

        assert context.isEndElement();
        final String ourEndUri = context.getNamespaceURI();
        final String ourEndLocalName = context.getLocalName();
        assert ourStartUri.equals(ourEndUri) :
            "expected=" + ourStartUri + " got=" + ourEndUri;
        assert ourStartLocalName.equals(ourEndLocalName) :
            "expected=" + ourStartLocalName + " got=" + ourEndLocalName;

        if (context.hasNext()) context.next();
    }


    private static void fillElementProp(RuntimeBindingProperty prop,
                                        UnmarshalContextImpl context,
                                        Object inter)
    {
        final TypeUnmarshaller um = prop.getTypeUnmarshaller(context);
        assert um != null;

        try {
            final Object prop_val = um.unmarshal(context);
            prop.fill(inter, prop_val);
        }
        catch (InvalidLexicalValueException ilve) {
            //error messages should have been added to the context by this
            //point, so we'll just skip calling any setters and keep on going
            assert !context.getErrors().isEmpty();
        }
    }


    private static void fillAttributeProp(RuntimeBindingProperty prop,
                                          UnmarshalContextImpl context,
                                          Object inter)
    {
        final TypeUnmarshaller um = prop.getTypeUnmarshaller(context);
        assert um != null;

        try {
            final Object prop_val = um.unmarshalAttribute(context);
            prop.fill(inter, prop_val);
        }
        catch (InvalidLexicalValueException ilve) {
            // (cezar) Todo: for Scott to take a look
            //error messages should have been added to the context by this
            //point, so we'll just skip calling any setters and keep on going
            //context.addError(ilve.getMessage());
            //assert !context.getErrors().isEmpty();
        }
    }

    private void deserializeAttributes(Object inter, UnmarshalContextImpl context)
    {
        while (context.hasMoreAttributes()) {
            RuntimeBindingProperty prop = findMatchingAttributeProperty(context);

            if (prop != null) {
                fillAttributeProp(prop, context, inter);
            }

            context.advanceAttribute();
        }
    }

    private RuntimeBindingProperty findMatchingAttributeProperty(UnmarshalContextImpl context)
    {
        String uri = context.getCurrentAttributeNamespaceURI();
        String lname = context.getCurrentAttributeLocalName();

        return type.getMatchingAttributeProperty(uri, lname);
    }

    private RuntimeBindingProperty findMatchingElementProperty(UnmarshalContextImpl context)
    {
        String uri = context.getNamespaceURI();
        String lname = context.getLocalName();
        return type.getMatchingElementProperty(uri, lname);
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
        type.initialize(typeTable, bindingLoader);
    }


}
