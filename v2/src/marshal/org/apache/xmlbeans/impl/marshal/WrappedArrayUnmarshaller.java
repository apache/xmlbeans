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

import javax.xml.namespace.QName;

public class WrappedArrayUnmarshaller
    implements TypeUnmarshaller
{
    private final WrappedArrayRuntimeBindingType type;


    public WrappedArrayUnmarshaller(WrappedArrayRuntimeBindingType rtt)
    {
        type = rtt;
    }

    public Object unmarshal(UnmarshalResult result)
        throws XmlException
    {
        final Object inter = type.createIntermediary();
        deserializeContents(inter, result);
        return type.getFinalObjectFromIntermediary(inter);
    }

    public void unmarshal(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this=" + this);
    }

    //TODO: cleanup this code.  We are doing extra work for assertion checking
    //also might consider consolidating the common code with the ByNameUnmarshaller
    private void deserializeContents(Object inter,
                                     UnmarshalResult context)
        throws XmlException
    {
        assert context.isStartElement();
        final String ourStartUri = context.getNamespaceURI();
        final String ourStartLocalName = context.getLocalName();
        context.next();

        while (context.advanceToNextStartElement()) {
            assert context.isStartElement();

            if (matchesItemElement(context)) {
                type.getElementProperty().extractAndFillElementProp(context, inter);
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

    private boolean matchesItemElement(UnmarshalResult context)
    {
        final QName el_name = type.getElementProperty().getName();
        return UnmarshalResult.doesElementMatch(el_name,
                                                context.getLocalName(),
                                                context.getNamespaceURI());
    }


    public Object unmarshalAttribute(UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("not used");
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("not used");
    }

    
    public void unmarshalAttribute(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("not used");
    }

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
    }
}
