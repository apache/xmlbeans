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

import org.apache.xmlbeans.impl.soap.Name;
import org.apache.xmlbeans.impl.soap.SOAPElement;
import org.apache.xmlbeans.impl.soap.SOAPException;

import javax.xml.namespace.QName;
import java.util.Iterator;

class SoapElementXobj extends ElementXobj implements SOAPElement, org.apache.xmlbeans.impl.soap.Node {
    SoapElementXobj(Locale l, QName name) {
        super(l, name);
    }

    Xobj newNode(Locale l) {
        return new SoapElementXobj(l, _name);
    }

    public void detachNode() {
        DomImpl._soapNode_detachNode(this);
    }

    public void recycleNode() {
        DomImpl._soapNode_recycleNode(this);
    }

    public String getValue() {
        return DomImpl._soapNode_getValue(this);
    }

    public void setValue(String value) {
        DomImpl._soapNode_setValue(this, value);
    }

    public SOAPElement getParentElement() {
        return DomImpl._soapNode_getParentElement(this);
    }

    public void setParentElement(SOAPElement p) {
        DomImpl._soapNode_setParentElement(this, p);
    }

    public void removeContents() {
        DomImpl._soapElement_removeContents(this);
    }

    public String getEncodingStyle() {
        return DomImpl._soapElement_getEncodingStyle(this);
    }

    public void setEncodingStyle(String encodingStyle) {
        DomImpl._soapElement_setEncodingStyle(this, encodingStyle);
    }

    public boolean removeNamespaceDeclaration(String prefix) {
        return DomImpl._soapElement_removeNamespaceDeclaration(this, prefix);
    }

    public Iterator getAllAttributes() {
        return DomImpl._soapElement_getAllAttributes(this);
    }

    public Iterator getChildElements() {
        return DomImpl._soapElement_getChildElements(this);
    }

    public Iterator getNamespacePrefixes() {
        return DomImpl._soapElement_getNamespacePrefixes(this);
    }

    public SOAPElement addAttribute(Name name, String value) throws SOAPException {
        return DomImpl._soapElement_addAttribute(this, name, value);
    }

    public SOAPElement addChildElement(SOAPElement oldChild) throws SOAPException {
        return DomImpl._soapElement_addChildElement(this, oldChild);
    }

    public SOAPElement addChildElement(Name name) throws SOAPException {
        return DomImpl._soapElement_addChildElement(this, name);
    }

    public SOAPElement addChildElement(String localName) throws SOAPException {
        return DomImpl._soapElement_addChildElement(this, localName);
    }

    public SOAPElement addChildElement(String localName, String prefix) throws SOAPException {
        return DomImpl._soapElement_addChildElement(this, localName, prefix);
    }

    public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException {
        return DomImpl._soapElement_addChildElement(this, localName, prefix, uri);
    }

    public SOAPElement addNamespaceDeclaration(String prefix, String uri) {
        return DomImpl._soapElement_addNamespaceDeclaration(this, prefix, uri);
    }

    public SOAPElement addTextNode(String data) {
        return DomImpl._soapElement_addTextNode(this, data);
    }

    public String getAttributeValue(Name name) {
        return DomImpl._soapElement_getAttributeValue(this, name);
    }

    public Iterator getChildElements(Name name) {
        return DomImpl._soapElement_getChildElements(this, name);
    }

    public Name getElementName() {
        return DomImpl._soapElement_getElementName(this);
    }

    public String getNamespaceURI(String prefix) {
        return DomImpl._soapElement_getNamespaceURI(this, prefix);
    }

    public Iterator getVisibleNamespacePrefixes() {
        return DomImpl._soapElement_getVisibleNamespacePrefixes(this);
    }

    public boolean removeAttribute(Name name) {
        return DomImpl._soapElement_removeAttribute(this, name);
    }
}

