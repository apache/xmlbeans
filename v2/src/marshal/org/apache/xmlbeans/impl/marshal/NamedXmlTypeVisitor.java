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

import javax.xml.namespace.QName;

abstract class NamedXmlTypeVisitor
    extends XmlTypeVisitor
{
    private final QName name;

    NamedXmlTypeVisitor(Object parentObject,
                        RuntimeBindingProperty property,
                        MarshalResult result)
    {
        super(parentObject, property, result);

        //TODO: optimize to avoid object creation
        name = addPrefixToName(getBindingProperty().getName());
    }

    protected QName getName()
    {
        return name;
    }

    private QName addPrefixToName(final QName pname)
    {
        final String uri = pname.getNamespaceURI();

        assert uri != null;  //QName's should use "" for no namespace

        if (uri.length() == 0) {
            return new QName(pname.getLocalPart());
        } else {
            String prefix = marshalResult.ensurePrefix(uri);
            return new QName(uri, pname.getLocalPart(), prefix);
        }
    }

}
