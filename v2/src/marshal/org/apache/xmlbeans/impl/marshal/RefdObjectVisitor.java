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

abstract class RefdObjectVisitor
    extends NamedXmlTypeVisitor
{
    protected final int id;

    private int state = START;


    public RefdObjectVisitor(RuntimeBindingProperty property,
                             Object obj,
                             PullMarshalResult result,
                             int id)
        throws XmlException
    {
        super(obj, property, result);
        assert obj != null;  //cannot have multiple refs to null
        assert id >= 0 ;
        this.id = id;
    }

    protected int getState()
    {
        return state;
    }

    protected int advance()
        throws XmlException
    {
        if (state == START) {
            return (state = END);
        } else {
            throw new AssertionError("invalid state: " + state);
        }
    }

    protected void initAttributes()
        throws XmlException
    {
        marshalResult.fillAndAddAttribute(getRefQName(), getRefValue());
    }

    protected abstract QName getRefQName();

    protected abstract String getRefValue();

    public XmlTypeVisitor getCurrentChild()
        throws XmlException
    {
        throw new AssertionError("no children");
    }

    protected CharSequence getCharData()
    {
        throw new AssertionError("no char data");
    }
}
