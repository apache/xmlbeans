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
    private final CharacterVisitor charVisitor;

    private int state = START;

    public SimpleContentVisitor(RuntimeBindingProperty property, Object obj,
                                MarshalResult result)
        throws XmlException
    {
        super(obj, property, result);
        charVisitor = new CharacterVisitor(property, obj, result);
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
        return charVisitor;
    }

    protected CharSequence getCharData()
    {
        throw new AssertionError("not text");
    }


}
