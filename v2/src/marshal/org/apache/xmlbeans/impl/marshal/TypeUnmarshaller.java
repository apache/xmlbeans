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

/**
 * A TypeUnmarshaller knows how to unmarshal an xml
 * instance of a given BindingType
 *
 * All TypeUnmarshaller objects should be immutable.
 */
interface TypeUnmarshaller
{
    /**
     * Unmarshalls the current node in the xml into a java object.
     * The UnmarshalResult must be pointing at the start element of the node
     * to be unmarshalled (such that XmlStreamReader.isStarteElement()
     * returns true).  The unmarshal method must consume the entire contents
     * of that node including the matching end element.
     *
     * @param result  contains that state of the document unmarshal process
     * @return  Object representing the converted xml
     */
    Object unmarshal(UnmarshalResult result)
        throws XmlException;


    /**
     * Unmarshal into an existing object (i.e. fill the object's properties).
     *
     * This method is optional and TypeUnmarshaller's for immutable
     * types (e.g. java.lang.String) will throw UnsupportedOperationException
     *
     * @param object  Object who's properties will be filled.
     * @param result
     * @throws UnsupportedOperationException  if not appropriate for this type
     * @throws XmlException
     */
    void unmarshal(Object object, UnmarshalResult result)
        throws XmlException;

    /**
     * unmarshal the lexical value of an instance of xsd:anySimpleType.
     * This could be called on an attribute value or on element content.
     *
     * @param result
     * @return Object representing java value of lexical
     *
     * @exception UnsupportedOperationException if the
     *            <tt>unmarshalSimpleType</tt> operation is not supported
     *            by this TypeUnmarshaller.
     */
    Object unmarshalAttribute(UnmarshalResult result)
        throws XmlException;

    /**
     * unmarshal the lexical value of an instance of xsd:anySimpleType into an
     * existing object.  This could be called on an attribute value
     * or on element content.
     *
     * This method is optional and TypeUnmarshaller's for immutable
     * types (e.g. java.lang.String) will throw UnsupportedOperationException
     *
     * @param object  Object who's value will be filled.
     * @param result
     *
     * @exception UnsupportedOperationException if the
     *            <tt>unmarshalSimpleType</tt> operation is not supported
     *            by this TypeUnmarshaller.
     */
    void unmarshalAttribute(Object object, UnmarshalResult result)
        throws XmlException;


    /**
     * called once per object before first use.
     *
     * @param typeTable
     * @param bindingLoader
     */
    void initialize(RuntimeBindingTypeTable typeTable,
                    BindingLoader bindingLoader);
}
