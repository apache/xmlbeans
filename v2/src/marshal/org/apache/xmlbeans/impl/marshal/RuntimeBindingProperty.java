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


interface RuntimeBindingProperty
{
    RuntimeBindingType getRuntimeBindingType();

    RuntimeBindingType getActualRuntimeType(Object property_value,
                                            MarshalResult result)
        throws XmlException;

    QName getName();

    TypeUnmarshaller getTypeUnmarshaller(UnmarshalResult context)
        throws XmlException;

    void fill(Object inter, Object prop_obj)
        throws XmlException;

    //non simple type props can throw an exception
    CharSequence getLexical(Object parent, MarshalResult result)
        throws XmlException;

    Object getValue(Object parentObject, MarshalResult result)
        throws XmlException;

    boolean isSet(Object parentObject, MarshalResult result)
        throws XmlException;

    boolean isMultiple();

    boolean isNillable();

    /**
     * returns null if this property has no default
     *
     * @return
     */
    String getLexicalDefault();

}
