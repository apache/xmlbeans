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

import org.apache.xmlbeans.BindingContext;
import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.Unmarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import java.util.Collection;

/**
 * Main entry point into marshalling framework.
 * Use the BindingContextFactory to create one
 */
final class BindingContextImpl implements BindingContext
{
    private final BindingLoader bindingLoader;
    private final RuntimeTypeFactory runtimeTypeFactory;
    private final RuntimeBindingTypeTable typeTable;
    private final SchemaTypeLoaderProvider schemaTypeLoaderProvider;


    /* package protected -- use the factory */
    BindingContextImpl(BindingLoader bindingLoader,
                       SchemaTypeLoaderProvider provider)
    {
        this.bindingLoader = bindingLoader;
        runtimeTypeFactory = new RuntimeTypeFactory();
        this.typeTable =
            RuntimeBindingTypeTable.createTable(runtimeTypeFactory);
        this.schemaTypeLoaderProvider  = provider;
    }


    public Unmarshaller createUnmarshaller()
        throws XmlException
    {
        return new UnmarshallerImpl(bindingLoader, typeTable,
                                    schemaTypeLoaderProvider);
    }

    public Unmarshaller createUnmarshaller(XmlOptions options)
        throws XmlException
    {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }

        return createUnmarshaller();
    }

    public Marshaller createMarshaller()
        throws XmlException
    {
        return new MarshallerImpl(bindingLoader, typeTable, runtimeTypeFactory);
    }


    public Marshaller createMarshaller(XmlOptions options)
        throws XmlException
    {
        if (options == null) {
            throw new IllegalArgumentException("options must not be null");
        }

        return createMarshaller();
    }

    static Collection extractErrorHandler(XmlOptions options)
    {
        if (options != null) {
            Collection underlying = (Collection)options.get(XmlOptions.ERROR_LISTENER);
            if (underlying != null)
                return underlying;
        }

        return FailFastErrorHandler.getInstance();
    }


}
