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

import javax.xml.namespace.QName;

public class SoapArrayUnmarshaller
    implements TypeUnmarshaller
{
    private final SoapArrayRuntimeBindingType type;


    public SoapArrayUnmarshaller(SoapArrayRuntimeBindingType rtt)
    {
        type = rtt;
    }

    public Object unmarshal(UnmarshalResult result)
        throws XmlException
    {
        final Object inter = type.createIntermediary();
        deserializeContents(inter, result);
        return type.getFinalObjectFromIntermediary(inter);
    }

    public void unmarshal(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this=" + this);
    }

    public void unmarshalIntoIntermediary(Object intermediary,
                                          UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("UNIMP!!");
    }

    //TODO: cleanup this code.  We are doing extra work for assertion checking
    //also might consider consolidating the common code with the ByNameUnmarshaller
    private void deserializeContents(Object inter,
                                     UnmarshalResult context)
        throws XmlException
    {
        throw new AssertionError("UNIMP");
    }


    public Object unmarshalAttribute(UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("not used");
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("not used");
    }


    public void unmarshalAttribute(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new AssertionError("not used");
    }

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
    }
}
