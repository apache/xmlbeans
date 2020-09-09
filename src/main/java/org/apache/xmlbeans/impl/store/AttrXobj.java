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

package org.apache.xmlbeans.impl.store;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import javax.xml.namespace.QName;

import static org.apache.xmlbeans.impl.store.Cur.ATTR;

class AttrXobj extends NamedNodeXobj implements Attr {
    AttrXobj(Locale l, QName name) {
        super(l, ATTR, DomImpl.ATTR);
        _name = name;
    }

    Xobj newNode(Locale l) {
        return new AttrXobj(l, _name);
    }

    //
    public Node getNextSibling() {
        return null;
    }
    //

    public String getName() {
        return DomImpl._node_getNodeName(this);
    }

    public Element getOwnerElement() {
        return DomImpl._attr_getOwnerElement(this);
    }

    public boolean getSpecified() {
        return DomImpl._attr_getSpecified(this);
    }

    public String getValue() {
        return DomImpl._node_getNodeValue(this);
    }

    public void setValue(String value) {
        DomImpl._node_setNodeValue(this, value);
    }

    // DOM Level 3
    public TypeInfo getSchemaTypeInfo() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public boolean isId() {
        return false;
    }
}

