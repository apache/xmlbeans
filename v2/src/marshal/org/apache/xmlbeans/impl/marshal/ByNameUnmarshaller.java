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

final class ByNameUnmarshaller
    extends AttributeUnmarshaller
{
    private final ByNameRuntimeBindingType byNameType;

    public ByNameUnmarshaller(ByNameRuntimeBindingType type)
    {
        super(type);
        this.byNameType = type;
    }

    //TODO: cleanup this code.  We are doing extra work for assertion checking
    protected void deserializeContents(Object inter, UnmarshalResult context)
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
                prop.extractAndFillElementProp(context, inter);
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


    private RuntimeBindingProperty findMatchingElementProperty(UnmarshalResult context)
    {
        String uri = context.getNamespaceURI();
        String lname = context.getLocalName();
        return byNameType.getMatchingElementProperty(uri, lname);
    }


}
