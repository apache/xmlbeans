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

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;

import javax.xml.namespace.QName;

final class Soap11UnmarshalResult
    extends SoapUnmarshalResult
{

    Soap11UnmarshalResult(BindingLoader loader,
                          RuntimeBindingTypeTable typeTable,
                          RefObjectTable refObjectTable,
                          XmlOptions options)
    {
        super(loader, typeTable, refObjectTable, options);
    }

    protected String getReferencedIdFromAttributeValue(String attval)
    {
        final String idstr = Soap11Constants.extractIdFromRef(attval);
        if (idstr == null) {
            throw new InvalidLexicalValueException("invalid reference",
                                                   getLocation());
        }
        return idstr;
    }

    protected String getIdFromAttributeValue(String attval)
    {
        return attval;
    }

    protected QName getIdAttributeName()
    {
        return Soap11Constants.ID_NAME;
    }

    protected QName getRefAttributeName()
    {
        return Soap11Constants.REF_NAME;
    }
}
