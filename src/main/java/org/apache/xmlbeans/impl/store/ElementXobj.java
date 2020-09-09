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

import org.w3c.dom.*;

import javax.xml.namespace.QName;

import static org.apache.xmlbeans.impl.store.Cur.ELEM;

class ElementXobj extends NamedNodeXobj implements Element {
    ElementXobj(Locale l, QName name) {
        super(l, ELEM, DomImpl.ELEMENT);
        _name = name;
    }

    Xobj newNode(Locale l) {
        return new ElementXobj(l, _name);
    }

    //
    //
    //

    public NamedNodeMap getAttributes() {
        if (_attributes == null)
            _attributes = new ElementAttributes(this);

        return _attributes;
    }

    public String getAttribute(String name) {
        return DomImpl._element_getAttribute(this, name);
    }

    public Attr getAttributeNode(String name) {
        return DomImpl._element_getAttributeNode(this, name);
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return DomImpl._element_getAttributeNodeNS(this, namespaceURI, localName);
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        return DomImpl._element_getAttributeNS(this, namespaceURI, localName);
    }

    public NodeList getElementsByTagName(String name) {
        return DomImpl._element_getElementsByTagName(this, name);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return DomImpl._element_getElementsByTagNameNS(this, namespaceURI, localName);
    }

    public String getTagName() {
        return DomImpl._element_getTagName(this);
    }

    public boolean hasAttribute(String name) {
        return DomImpl._element_hasAttribute(this, name);
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return DomImpl._element_hasAttributeNS(this, namespaceURI, localName);
    }

    public void removeAttribute(String name) {
        DomImpl._element_removeAttribute(this, name);
    }

    public Attr removeAttributeNode(Attr oldAttr) {
        return DomImpl._element_removeAttributeNode(this, oldAttr);
    }

    public void removeAttributeNS(String namespaceURI, String localName) {
        DomImpl._element_removeAttributeNS(this, namespaceURI, localName);
    }

    public void setAttribute(String name, String value) {
        DomImpl._element_setAttribute(this, name, value);
    }

    public Attr setAttributeNode(Attr newAttr) {
        return DomImpl._element_setAttributeNode(this, newAttr);
    }

    public Attr setAttributeNodeNS(Attr newAttr) {
        return DomImpl._element_setAttributeNodeNS(this, newAttr);
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
        DomImpl._element_setAttributeNS(this, namespaceURI, qualifiedName, value);
    }

    // DOM Level 3
    public TypeInfo getSchemaTypeInfo() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void setIdAttribute(String name, boolean isId) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    private ElementAttributes _attributes;
}

