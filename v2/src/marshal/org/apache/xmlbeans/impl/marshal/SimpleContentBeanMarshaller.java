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
import org.apache.xmlbeans.impl.binding.bts.BindingType;

final class SimpleContentBeanMarshaller implements TypeMarshaller
{
    private final SimpleContentRuntimeBindingType simpleContentType;
    private final TypeMarshaller contentMarshaller;

    public SimpleContentBeanMarshaller(SimpleContentRuntimeBindingType rtt,
                                       RuntimeBindingTypeTable table,
                                       BindingLoader loader)
        throws XmlException
    {
        simpleContentType = rtt;
        final BindingType content_binding_type =
            rtt.getSimpleContentProperty().getRuntimeBindingType().getBindingType();
        final TypeMarshaller marshaller =
            table.lookupMarshaller(content_binding_type, loader);
        if (marshaller == null) {
            String e = "failed to find marshaller for " + content_binding_type;
            throw new AssertionError(e);
        }
        contentMarshaller = marshaller;
    }

    //non simple types can throw a runtime exception
    public CharSequence print(Object value, MarshalResult result)
        throws XmlException
    {
        final RuntimeBindingProperty simple_content_prop =
            simpleContentType.getSimpleContentProperty();

        final Object simple_content_val =
            simple_content_prop.getValue(value, result);

        final RuntimeBindingType prop_rtt =
            simple_content_prop.getRuntimeBindingType();

        final RuntimeBindingType actualRuntimeType =
            MarshalResult.findActualRuntimeType(simple_content_val,
                                                prop_rtt, result);

        TypeMarshaller content_marshaller;
        if (actualRuntimeType == prop_rtt) {
            content_marshaller = contentMarshaller;
        } else {
            RuntimeBindingTypeTable table = result.getTypeTable();
            BindingLoader loader = result.getBindingLoader();
            content_marshaller =
                table.lookupMarshaller(prop_rtt.getBindingType(), loader);
            if (content_marshaller == null) {
                String msg = "unable to find marshaller for " +
                    prop_rtt.getBindingType() + ".  Using default marshaller";
                result.addWarning(msg);
                content_marshaller = contentMarshaller;
            }
        }
        return content_marshaller.print(simple_content_val, result);
    }
}
