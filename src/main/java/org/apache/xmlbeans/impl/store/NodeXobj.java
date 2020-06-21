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

abstract class NodeXobj extends Xobj implements DomImpl.Dom, Node, NodeList {
    NodeXobj(Locale l, int kind, int domType) {
        super(l, kind, domType);
    }

    DomImpl.Dom getDom() {
        return this;
    }

    //
    //
    //

    public int getLength() {
        return DomImpl._childNodes_getLength(this);
    }

    public Node item(int i) {
        return DomImpl._childNodes_item(this, i);
    }

    public Node appendChild(Node newChild) {
        return DomImpl._node_appendChild(this, newChild);
    }

    public Node cloneNode(boolean deep) {
        return DomImpl._node_cloneNode(this, deep);
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public NodeList getChildNodes() {
        return this;
    }

    public Node getParentNode() {
        return DomImpl._node_getParentNode(this);
    }

    public Node removeChild(Node oldChild) {
        return DomImpl._node_removeChild(this, oldChild);
    }

    public Node getFirstChild() {
        return DomImpl._node_getFirstChild(this);
    }

    public Node getLastChild() {
        return DomImpl._node_getLastChild(this);
    }

    public String getLocalName() {
        return DomImpl._node_getLocalName(this);
    }

    public String getNamespaceURI() {
        return DomImpl._node_getNamespaceURI(this);
    }

    public Node getNextSibling() {
        return DomImpl._node_getNextSibling(this);
    }

    public String getNodeName() {
        return DomImpl._node_getNodeName(this);
    }

    public short getNodeType() {
        return DomImpl._node_getNodeType(this);
    }

    public String getNodeValue() {
        return DomImpl._node_getNodeValue(this);
    }

    public Document getOwnerDocument() {
        return DomImpl._node_getOwnerDocument(this);
    }

    public String getPrefix() {
        return DomImpl._node_getPrefix(this);
    }

    public Node getPreviousSibling() {
        return DomImpl._node_getPreviousSibling(this);
    }

    public boolean hasAttributes() {
        return DomImpl._node_hasAttributes(this);
    }

    public boolean hasChildNodes() {
        return DomImpl._node_hasChildNodes(this);
    }

    public Node insertBefore(Node newChild, Node refChild) {
        return DomImpl._node_insertBefore(this, newChild, refChild);
    }

    public boolean isSupported(String feature, String version) {
        return DomImpl._node_isSupported(this, feature, version);
    }

    public void normalize() {
        DomImpl._node_normalize(this);
    }

    public Node replaceChild(Node newChild, Node oldChild) {
        return DomImpl._node_replaceChild(this, newChild, oldChild);
    }

    public void setNodeValue(String nodeValue) {
        DomImpl._node_setNodeValue(this, nodeValue);
    }

    public void setPrefix(String prefix) {
        DomImpl._node_setPrefix(this, prefix);
    }

    public boolean nodeCanHavePrefixUri() {
        return false;
    }

    // DOM Level 3
    public Object getUserData(String key) {
        return DomImpl._node_getUserData(this, key);
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return DomImpl._node_setUserData(this, key, data, handler);
    }

    public Object getFeature(String feature, String version) {
        return DomImpl._node_getFeature(this, feature, version);
    }

    public boolean isEqualNode(Node arg) {
        return DomImpl._node_isEqualNode(this, arg);
    }

    public boolean isSameNode(Node arg) {
        return DomImpl._node_isSameNode(this, arg);
    }

    public String lookupNamespaceURI(String prefix) {
        return DomImpl._node_lookupNamespaceURI(this, prefix);
    }

    public String lookupPrefix(String namespaceURI) {
        return DomImpl._node_lookupPrefix(this, namespaceURI);
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        return DomImpl._node_isDefaultNamespace(this, namespaceURI);
    }

    public void setTextContent(String textContent) {
        DomImpl._node_setTextContent(this, textContent);
    }

    public String getTextContent() {
        return DomImpl._node_getTextContent(this);
    }

    public short compareDocumentPosition(Node other) {
        return DomImpl._node_compareDocumentPosition(this, other);
    }

    public String getBaseURI() {
        return DomImpl._node_getBaseURI(this);
    }
}

