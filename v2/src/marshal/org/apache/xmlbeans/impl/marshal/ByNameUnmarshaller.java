/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;

final class ByNameUnmarshaller implements TypeUnmarshaller
{
    private final ByNameRuntimeBindingType type;

    public ByNameUnmarshaller(ByNameRuntimeBindingType type)
    {
        this.type = type;
    }

    public Object unmarshal(UnmarshalResult context)
        throws XmlException
    {
        final Object inter = type.createIntermediary(context);
        deserializeAttributes(inter, context);
        deserializeContents(inter, context);
        return type.getFinalObjectFromIntermediary(inter, context);
    }

    public Object unmarshalAttribute(UnmarshalResult context)
    {
        throw new UnsupportedOperationException("not an attribute: " +
                                                type.getSchemaTypeName());
    }

    //TODO: cleanup this code.  We are doing extra work for assertion checking
    private void deserializeContents(Object inter, UnmarshalResult context)
        throws XmlException
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
                                        UnmarshalResult context,
                                        Object inter)
        throws XmlException
    {
        final TypeUnmarshaller um = prop.getTypeUnmarshaller(context);
        assert um != null;

        try {
            final String lexical_default = prop.getLexicalDefault();
            if (lexical_default != null) {
                context.setNextElementDefault(lexical_default);
            }
            final Object prop_val = um.unmarshal(context);
            prop.fill(inter, prop_val);
        }
        catch (InvalidLexicalValueException ilve) {
            //unlike attributes, the error has been added to the context
            //already via BaseSimpleTypeConveter...
        }
    }


    private static void fillAttributeProp(RuntimeBindingProperty prop,
                                          UnmarshalResult context,
                                          Object inter)
        throws XmlException
    {
        final TypeUnmarshaller um = prop.getTypeUnmarshaller(context);
        assert um != null;

        try {
            final Object prop_val = um.unmarshalAttribute(context);
            prop.fill(inter, prop_val);
        }
        catch (InvalidLexicalValueException ilve) {
            //TODO: review error messages
            String msg = "invalid value for " + prop.getName() +
                ": " + ilve.getMessage();
            context.addError(msg, ilve.getLocation());
        }
    }

    private void deserializeAttributes(Object inter, UnmarshalResult context)
        throws XmlException
    {
        while (context.hasMoreAttributes()) {
            RuntimeBindingProperty prop = findMatchingAttributeProperty(context);

            if (prop != null) {
                fillAttributeProp(prop, context, inter);
            }

            context.advanceAttribute();
        }

        type.fillDefaultAttributes(inter, context);
    }

    private RuntimeBindingProperty findMatchingAttributeProperty(UnmarshalResult context)
    {
        String uri = context.getCurrentAttributeNamespaceURI();
        String lname = context.getCurrentAttributeLocalName();

        return type.getMatchingAttributeProperty(uri, lname, context);
    }

    private RuntimeBindingProperty findMatchingElementProperty(UnmarshalResult context)
    {
        String uri = context.getNamespaceURI();
        String lname = context.getLocalName();
        return type.getMatchingElementProperty(uri, lname);
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
        //type.initialize(typeTable, bindingLoader);
    }


}
