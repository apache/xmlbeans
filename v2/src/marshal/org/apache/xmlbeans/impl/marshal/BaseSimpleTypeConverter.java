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
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;

/**
 * Basic XmlStreamReader based impl that can handle converting
 * simple types of the form <a>4.54</a>.
 */
abstract class BaseSimpleTypeConverter
    implements TypeConverter
{

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader)
    {
    }

    public final Object unmarshal(UnmarshalResult context)
        throws XmlException
    {
        try {
            return getObject(context);
        }
        catch (InvalidLexicalValueException ilve) {
            context.addError(ilve.getMessage(), ilve.getLocation());
            throw ilve;
        }
        finally {
            //Note that this assertion can be trigger by invalid xml input,
            //so we've disabled it.
            //assert context.isEndElement();

            if (context.hasNext()) context.next();
        }
    }

    public void unmarshal(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this="+this);
    }

    //subclass should override this where appropriate
    public void unmarshalAttribute(Object object, UnmarshalResult result)
        throws XmlException
    {
        throw new UnsupportedOperationException("not supported: this=" + this);
    }


    protected abstract Object getObject(UnmarshalResult context)
        throws XmlException;



}
