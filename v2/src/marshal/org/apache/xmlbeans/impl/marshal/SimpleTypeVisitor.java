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

final class SimpleTypeVisitor
    extends SimpleContentVisitor
{
    private QName attributeName;
    private String xsiTypeAttVal;

    public SimpleTypeVisitor(RuntimeBindingProperty property, Object obj,
                             MarshalResult result)
        throws XmlException
    {
        super(property, obj, result);
    }


    protected void initAttributes()
        throws XmlException
    {
        if (getParentObject() == null) {
            attributeName = fillPrefix(MarshalStreamUtils.XSI_NIL_QNAME);
        } else if (needsXsiType()) {
            attributeName = fillPrefix(MarshalStreamUtils.XSI_TYPE_QNAME);

            final QName schema_type_name =
                getActualRuntimeBindingType().getSchemaTypeName();

            QName tn = fillPrefix(schema_type_name);
            xsiTypeAttVal = XsTypeConverter.getQNameString(tn.getNamespaceURI(),
                                                           tn.getLocalPart(),
                                                           tn.getPrefix());
        } else {
            attributeName = null;
        }
    }

    protected int getAttributeCount()
        throws XmlException
    {
        return attributeName == null ? 0 : 1;
    }

    protected String getAttributeValue(int idx)
    {
        assert attributeName != null;
        
        return xsiTypeAttVal==null ? NIL_ATT_VAL : xsiTypeAttVal;
    }

    protected QName getAttributeName(int idx)
    {
        assert attributeName != null;
        return attributeName;
    }

}
