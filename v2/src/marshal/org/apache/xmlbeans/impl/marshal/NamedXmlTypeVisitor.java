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
import org.apache.xmlbeans.impl.util.XsTypeConverter;

import javax.xml.namespace.QName;

//named means we have an actual start/end element versus just characters

abstract class NamedXmlTypeVisitor
    extends XmlTypeVisitor
{
    private final String prefix;

    private final RuntimeBindingType actualRuntimeBindingType;

    protected static final String TRUE_LEX =
        XsTypeConverter.printBoolean(true).intern();

    NamedXmlTypeVisitor(Object parentObject,
                        RuntimeBindingProperty property,
                        PullMarshalResult result)
        throws XmlException
    {
        super(parentObject, property, result);

        actualRuntimeBindingType =
            property.getActualRuntimeType(parentObject, result);

        final String uri = getBindingProperty().getName().getNamespaceURI();
        if (uri.length() > 0) {
            prefix = marshalResult.ensurePrefix(uri);
        } else {
            prefix = null;
        }
    }

    protected QName getName()
    {
        final QName name = getBindingProperty().getName();
        return new QName(name.getNamespaceURI(), name.getLocalPart(), prefix);
    }

    protected String getLocalPart()
    {
        return getBindingProperty().getName().getLocalPart();
    }

    protected String getNamespaceURI()
    {
        return getBindingProperty().getName().getNamespaceURI();
    }

    protected String getPrefix()
    {
        return prefix;
    }

    protected final RuntimeBindingType getActualRuntimeBindingType()
    {
        return actualRuntimeBindingType;
    }

    protected final boolean needsXsiType()
    {
        return (actualRuntimeBindingType !=
            getBindingProperty().getRuntimeBindingType());
    }

}
