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

import org.apache.xmlbeans.impl.soap.SOAPEnvelope;
import org.apache.xmlbeans.impl.soap.SOAPPart;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import java.io.PrintStream;
import java.util.Iterator;

class SoapPartDom extends SOAPPart implements DomImpl.Dom, Document, NodeList {
    SoapPartDom(SoapPartDocXobj docXobj) {
        _docXobj = docXobj;
    }

    public int nodeType() {
        return DomImpl.DOCUMENT;
    }

    public Locale locale() {
        return _docXobj._locale;
    }

    public Cur tempCur() {
        return _docXobj.tempCur();
    }

    public QName getQName() {
        return _docXobj._name;
    }

    public void dump() {
        dump(System.out);
    }

    public void dump(PrintStream o) {
        _docXobj.dump(o);
    }

    public void dump(PrintStream o, Object ref) {
        _docXobj.dump(o, ref);
    }

    public String name() {
        return "#document";
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

    public Node adoptNode(Node source) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public String getDocumentURI() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public DOMConfiguration getDomConfig() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public String getInputEncoding() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public boolean getStrictErrorChecking() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public String getXmlEncoding() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public boolean getXmlStandalone() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public String getXmlVersion() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void normalizeDocument() {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public Node renameNode(Node n, String namespaceURI, String qualifiedName) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void setDocumentURI(String documentURI) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void setXmlStandalone(boolean xmlStandalone) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public void setXmlVersion(String xmlVersion) {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    public Attr createAttribute(String name) {
        return DomImpl._document_createAttribute(this, name);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName) {
        return DomImpl._document_createAttributeNS(this, namespaceURI, qualifiedName);
    }

    public CDATASection createCDATASection(String data) {
        return DomImpl._document_createCDATASection(this, data);
    }

    public Comment createComment(String data) {
        return DomImpl._document_createComment(this, data);
    }

    public DocumentFragment createDocumentFragment() {
        return DomImpl._document_createDocumentFragment(this);
    }

    public Element createElement(String tagName) {
        return DomImpl._document_createElement(this, tagName);
    }

    public Element createElementNS(String namespaceURI, String qualifiedName) {
        return DomImpl._document_createElementNS(this, namespaceURI, qualifiedName);
    }

    public EntityReference createEntityReference(String name) {
        return DomImpl._document_createEntityReference(this, name);
    }

    public ProcessingInstruction createProcessingInstruction(String target, String data) {
        return DomImpl._document_createProcessingInstruction(this, target, data);
    }

    public Text createTextNode(String data) {
        return DomImpl._document_createTextNode(this, data);
    }

    public DocumentType getDoctype() {
        return DomImpl._document_getDoctype(this);
    }

    public Element getDocumentElement() {
        return DomImpl._document_getDocumentElement(this);
    }

    public Element getElementById(String elementId) {
        return DomImpl._document_getElementById(this, elementId);
    }

    public NodeList getElementsByTagName(String tagname) {
        return DomImpl._document_getElementsByTagName(this, tagname);
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return DomImpl._document_getElementsByTagNameNS(this, namespaceURI, localName);
    }

    public DOMImplementation getImplementation() {
        return DomImpl._document_getImplementation(this);
    }

    public Node importNode(Node importedNode, boolean deep) {
        return DomImpl._document_importNode(this, importedNode, deep);
    }

    public int getLength() {
        return DomImpl._childNodes_getLength(this);
    }

    public Node item(int i) {
        return DomImpl._childNodes_item(this, i);
    }

    public void removeAllMimeHeaders() {
        DomImpl._soapPart_removeAllMimeHeaders(this);
    }

    public void removeMimeHeader(String name) {
        DomImpl._soapPart_removeMimeHeader(this, name);
    }

    public Iterator getAllMimeHeaders() {
        return DomImpl._soapPart_getAllMimeHeaders(this);
    }

    public SOAPEnvelope getEnvelope() {
        return DomImpl._soapPart_getEnvelope(this);
    }

    public Source getContent() {
        return DomImpl._soapPart_getContent(this);
    }

    public void setContent(Source source) {
        DomImpl._soapPart_setContent(this, source);
    }

    public String[] getMimeHeader(String name) {
        return DomImpl._soapPart_getMimeHeader(this, name);
    }

    public void addMimeHeader(String name, String value) {
        DomImpl._soapPart_addMimeHeader(this, name, value);
    }

    public void setMimeHeader(String name, String value) {
        DomImpl._soapPart_setMimeHeader(this, name, value);
    }

    public Iterator getMatchingMimeHeaders(String[] names) {
        return DomImpl._soapPart_getMatchingMimeHeaders(this, names);
    }

    public Iterator getNonMatchingMimeHeaders(String[] names) {
        return DomImpl._soapPart_getNonMatchingMimeHeaders(this, names);
    }

    public boolean nodeCanHavePrefixUri() {
        return true;
    }

    SoapPartDocXobj _docXobj;
}

