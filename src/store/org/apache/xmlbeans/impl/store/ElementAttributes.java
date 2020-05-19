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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

final class ElementAttributes implements NamedNodeMap {
    ElementAttributes(ElementXobj elementXobj) {
        _elementXobj = elementXobj;
    }

    public int getLength() {
        return DomImpl._attributes_getLength(_elementXobj);
    }

    public Node getNamedItem(String name) {
        return DomImpl._attributes_getNamedItem(_elementXobj, name);
    }

    public Node getNamedItemNS(String namespaceURI, String localName) {
        return DomImpl._attributes_getNamedItemNS(_elementXobj, namespaceURI, localName);
    }

    public Node item(int index) {
        return DomImpl._attributes_item(_elementXobj, index);
    }

    public Node removeNamedItem(String name) {
        return DomImpl._attributes_removeNamedItem(_elementXobj, name);
    }

    public Node removeNamedItemNS(String namespaceURI, String localName) {
        return DomImpl._attributes_removeNamedItemNS(_elementXobj, namespaceURI, localName);
    }

    public Node setNamedItem(Node arg) {
        return DomImpl._attributes_setNamedItem(_elementXobj, arg);
    }

    public Node setNamedItemNS(Node arg) {
        return DomImpl._attributes_setNamedItemNS(_elementXobj, arg);
    }

    private ElementXobj _elementXobj;
}

