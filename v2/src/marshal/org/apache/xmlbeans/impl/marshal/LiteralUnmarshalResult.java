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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

final class LiteralUnmarshalResult
    extends UnmarshalResult
{
    private final SchemaTypeLoaderProvider schemaTypeLoaderProvider;


    LiteralUnmarshalResult(BindingLoader bindingLoader,
                           RuntimeBindingTypeTable typeTable,
                           SchemaTypeLoaderProvider provider,
                           XmlOptions options)
    {

        super(bindingLoader, typeTable, options);
        this.schemaTypeLoaderProvider = provider;

    }

    protected XMLStreamReader getValidatingStream(XMLStreamReader reader)
        throws XmlException
    {
        if (isValidating()) {
            ValidatingXMLStreamReader vr = new ValidatingXMLStreamReader();
            final SchemaTypeLoader schemaTypeLoader =
                schemaTypeLoaderProvider.getSchemaTypeLoader();
            if (schemaTypeLoader == null) {
                final String msg = "null schema type loader from " +
                    "schemaTypeLoaderProvider " + schemaTypeLoaderProvider;
                throw new XmlException(msg);
            }
            vr.init(reader, false, null, schemaTypeLoader, options, errors);
            return vr;
        } else {
            return reader;
        }
    }

    protected XMLStreamReader getValidatingStream(QName schemaType,
                                                  XMLStreamReader reader)
        throws XmlException
    {
        if (isValidating()) {
            ValidatingXMLStreamReader vr = new ValidatingXMLStreamReader();
            SchemaTypeLoader schemaTypeLoader =
                schemaTypeLoaderProvider.getSchemaTypeLoader();
            SchemaType schema_type = schemaTypeLoader.findType(schemaType);
            if (schema_type == null) {
                String e = "unable to locate definition of type " +
                    schemaType + " in supplied schema type system";
                throw new XmlException(e);
            }
            vr.init(reader, false, schema_type, schemaTypeLoader, options, errors);
            return vr;
        } else {
            return reader;
        }
    }


    private boolean isValidating()
    {
        if (options == null) return false;

        return options.hasOption(XmlOptions.UNMARSHAL_VALIDATE);
    }

    void extractAndFillElementProp(final RuntimeBindingProperty prop,
                                   Object inter)
        throws XmlException
    {
        try {
            final RuntimeBindingType actual_rtt =
                this.determineActualRuntimeType(prop.getRuntimeBindingType());

            final Object this_val =
                unmarshalElementProperty(prop, inter, actual_rtt);

            prop.fill(inter, this_val);
        }
        catch (InvalidLexicalValueException ilve) {
            //unlike attributes, the error has been added to the this
            //already via BaseSimpleTypeConveter...
        }
    }

    //TODO: move to RuntimeBindingProperty?
    protected Object unmarshalElementProperty(RuntimeBindingProperty prop,
                                              Object inter,
                                              RuntimeBindingType actual_rtt)
        throws XmlException
    {
        final String lexical_default = prop.getLexicalDefault();
        if (lexical_default != null) {
            setNextElementDefault(lexical_default);
        }

        final Object this_val;
        if (prop.hasFactory()) {
            final Object prop_inter =
                prop.createIntermediary(inter, actual_rtt, this);
            actual_rtt.getUnmarshaller().unmarshalIntoIntermediary(prop_inter, this);
            this_val = actual_rtt.getFinalObjectFromIntermediary(prop_inter, this);
        } else {
            TypeUnmarshaller um = getUnmarshaller(actual_rtt);
            this_val = um.unmarshal(this);
        }
        return this_val;
    }
}
