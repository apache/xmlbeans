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

import javax.xml.namespace.QName;

class Soap11RefdObjectVisitor
    extends RefdObjectVisitor
{

    private static final QName HREF_NAME = new QName("href");
    private static final String REF_PREFIX = '#' + SoapMarshalResult.ID_PREFIX;

    public Soap11RefdObjectVisitor(RuntimeBindingProperty property,
                                   Object obj,
                                   PullMarshalResult result,
                                   int id)
        throws XmlException
    {
        super(property, obj, result, id);
    }

    protected QName getRefQName()
    {
        return HREF_NAME;
    }

    protected String getRefValue()
    {
        return REF_PREFIX + Integer.toString(id);
    }
}
