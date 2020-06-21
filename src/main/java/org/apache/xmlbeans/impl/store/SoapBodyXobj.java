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

import org.apache.xmlbeans.impl.soap.*;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;

class SoapBodyXobj extends SoapElementXobj implements SOAPBody {
    SoapBodyXobj(Locale l, QName name) {
        super(l, name);
    }

    Xobj newNode(Locale l) {
        return new SoapBodyXobj(l, _name);
    }

    public boolean hasFault() {
        return DomImpl.soapBody_hasFault(this);
    }

    public SOAPFault addFault() throws SOAPException {
        return DomImpl.soapBody_addFault(this);
    }

    public SOAPFault getFault() {
        return DomImpl.soapBody_getFault(this);
    }

    public SOAPBodyElement addBodyElement(Name name) {
        return DomImpl.soapBody_addBodyElement(this, name);
    }

    public SOAPBodyElement addDocument(Document document) {
        return DomImpl.soapBody_addDocument(this, document);
    }

    public SOAPFault addFault(Name name, String s) throws SOAPException {
        return DomImpl.soapBody_addFault(this, name, s);
    }

    public SOAPFault addFault(Name faultCode, String faultString, java.util.Locale locale) throws SOAPException {
        return DomImpl.soapBody_addFault(this, faultCode, faultString, locale);
    }
}
