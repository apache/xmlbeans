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
import java.util.Collection;

final class CharacterVisitor
    extends XmlTypeVisitor
{
    private final CharSequence chars;

    CharacterVisitor(RuntimeBindingProperty property,
                     Object parentObject,
                     MarshalResult result)
        throws XmlException
    {
        super(parentObject, property, result);
        assert (!(parentObject instanceof Collection));

        //we are getting the lexical value here because in certain cases
        //this action could end up modifying the namespace context.
        //(qname, type substitution).
        if (parentObject == null) {
            //REVIEW: should this be a special subclass for nil types?
            //Any use of this value should cause an npe later on.
            chars = null;
        } else {
            chars = grabChars();
        }
    }

    protected int getState()
    {
        return CHARS;
    }

    protected int advance()
        throws XmlException
    {
        return CHARS;
    }

    public XmlTypeVisitor getCurrentChild()
        throws XmlException
    {
        throw new AssertionError("no children");
    }

    protected QName getName()
    {
        throw new AssertionError("no name on " + this);
    }

    protected String getLocalPart()
    {
        throw new AssertionError("no name on " + this);
    }

    protected String getNamespaceURI()
    {
        throw new AssertionError("no name on " + this);
    }

    protected String getPrefix()
    {
        throw new AssertionError("no name on " + this);
    }

    protected CharSequence getCharData()
    {
        return chars;
    }

    private CharSequence grabChars()
        throws XmlException
    {
        final Object parent = getParentObject();
        assert parent != null : "bad visitor: this=" + this;
        return getBindingProperty().getLexical(parent, marshalResult);
    }

}
