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

abstract class AttributeUnmarshaller implements TypeUnmarshaller
{
    private final AttributeRuntimeBindingType type;

    public AttributeUnmarshaller(AttributeRuntimeBindingType type)
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

    protected abstract void deserializeContents(Object inter,
                                                UnmarshalResult context)
        throws XmlException;


    protected void deserializeAttributes(Object inter, UnmarshalResult context)
        throws XmlException
    {
        while (context.hasMoreAttributes()) {
            RuntimeBindingProperty prop = findMatchingAttributeProperty(context);

            if (prop != null) {
                UnmarshalResult.fillAttributeProp(prop, context, inter);
            }

            context.advanceAttribute();
        }

        type.fillDefaultAttributes(inter, context);
    }

    protected RuntimeBindingProperty findMatchingAttributeProperty(UnmarshalResult context)
    {
        String uri = context.getCurrentAttributeNamespaceURI();
        String lname = context.getCurrentAttributeLocalName();

        return type.getMatchingAttributeProperty(uri, lname, context);
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
    }


}
