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

abstract class SimpleContentVisitor
    extends NamedXmlTypeVisitor
{
    private final CharSequence chars;

    private int state = START;

    public SimpleContentVisitor(RuntimeBindingProperty property,
                                Object obj,
                                PullMarshalResult result)
        throws XmlException
    {
        super(obj, property, result);

        //we are getting the lexical value here because in certain cases
        //this action could end up modifying the namespace context.
        //(qname, type substitution).
        if (obj == null) {
            //REVIEW: should this be a special subclass for nil types?
            //Any use of this value should cause an npe later on.
            chars = null;
        } else {
            chars = property.getLexical(obj, marshalResult);
        }
    }

    protected int getState()
    {
        return state;
    }

    protected int advance()
        throws XmlException
    {
        final int newstate;
        switch (state) {
            case START:
                if (getParentObject() == null) {
                    newstate = END;
                } else {
                    newstate = CHARS;
                }
                break;
            case CHARS:
                newstate = END;
                break;
            default:
                throw new AssertionError("invalid state: " + state);
        }
        state = newstate;
        return newstate;
    }

    public XmlTypeVisitor getCurrentChild()
        throws XmlException
    {
        assert state == CHARS;
        return this;
    }

    protected CharSequence getCharData()
    {
        assert state == CHARS;
        assert getParentObject() != null; // should have skipped this
        return chars;
    }


}
