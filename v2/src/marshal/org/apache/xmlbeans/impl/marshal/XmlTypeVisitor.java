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

abstract class XmlTypeVisitor
{
    private final Object parentObject;
    private final RuntimeBindingProperty bindingProperty;
    protected final PullMarshalResult marshalResult;

    XmlTypeVisitor(Object obj,
                   RuntimeBindingProperty property,
                   PullMarshalResult result)
        throws XmlException
    {
        this.parentObject = obj;
        this.bindingProperty = property;
        marshalResult = result;

    }


    protected final Object getParentObject()
    {
        return parentObject;
    }

    protected final RuntimeBindingProperty getBindingProperty()
    {
        return bindingProperty;
    }

    static final int START = 1;
    static final int CONTENT = 2;
    static final int CHARS = 3;
    static final int END = 4;

    protected abstract int getState();

    /**
     *
     * @return  next state
     */
    protected abstract int advance()
        throws XmlException;

    public abstract XmlTypeVisitor getCurrentChild()
        throws XmlException;

    protected abstract QName getName();

    protected abstract String getLocalPart();

    protected abstract String getNamespaceURI();

    protected abstract String getPrefix();


    //guaranteed to be called before any getAttribute* or getNamespace* method
    protected void initAttributes()
        throws XmlException
    {
    }

    protected abstract CharSequence getCharData();

    public String toString()
    {
        return this.getClass().getName() +
            " prop=" + bindingProperty.getName() +
            " type=" + bindingProperty.getRuntimeBindingType().getBindingType();
    }


}
