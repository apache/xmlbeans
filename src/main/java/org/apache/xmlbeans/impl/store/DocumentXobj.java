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

import static org.apache.xmlbeans.impl.store.Cur.ROOT;

class DocumentXobj extends NodeXobj implements Document {
    DocumentXobj(Locale l) {
        super(l, ROOT, DomImpl.DOCUMENT);
    }

    Xobj newNode(Locale l) {
        return new DocumentXobj(l);
    }

    //
    //
    //

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
        if (_idToElement == null) {
            return null;
        }
        Xobj o = (Xobj) _idToElement.get(elementId);
        if (o == null) {
            return null;
        }
        if (!isInSameTree(o)) {
            _idToElement.remove(elementId);
        }
        return (Element) o;
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

    // DOM Level 3
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

    protected void addIdElement(String idVal, DomImpl.Dom e) {
        if (_idToElement == null) {
            _idToElement = new java.util.Hashtable<>();
        }
        _idToElement.put(idVal, e);
    }

    void removeIdElement(String idVal) {
        if (_idToElement != null) {
            _idToElement.remove(idVal);
        }
    }

    private java.util.Hashtable<String,DomImpl.Dom> _idToElement;
}
