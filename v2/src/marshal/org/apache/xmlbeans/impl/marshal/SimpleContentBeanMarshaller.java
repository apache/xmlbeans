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

final class SimpleContentBeanMarshaller implements TypeMarshaller
{
    private final SimpleContentRuntimeBindingType simpleContentType;

    public SimpleContentBeanMarshaller(SimpleContentRuntimeBindingType rtt)
        throws XmlException
    {
        simpleContentType = rtt;
        final RuntimeBindingType content_rtt =
            rtt.getSimpleContentProperty().getRuntimeBindingType();
        final TypeMarshaller marshaller = content_rtt.getMarshaller();
        if (marshaller == null) {
            String e = "failed to find marshaller for " +
                content_rtt.getBindingType();
            throw new AssertionError(e);
        }
    }

    //non simple types can throw a runtime exception
    public CharSequence print(Object value, MarshalResult result)
        throws XmlException
    {
        final RuntimeBindingProperty simple_content_prop =
            simpleContentType.getSimpleContentProperty();

        final Object simple_content_val =
            simple_content_prop.getValue(value, result);

        final RuntimeBindingType actual_prop_rtt =
            result.determineRuntimeBindingType(simple_content_prop.getRuntimeBindingType(),
                                               simple_content_val);

        return actual_prop_rtt.getMarshaller().print(simple_content_val, result);
    }
}
