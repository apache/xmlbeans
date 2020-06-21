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

import javax.xml.namespace.QName;

class SoapEnvelopeXobj extends SoapElementXobj implements SOAPEnvelope {
    SoapEnvelopeXobj(Locale l, QName name) {
        super(l, name);
    }

    Xobj newNode(Locale l) {
        return new SoapEnvelopeXobj(l, _name);
    }

    public SOAPBody addBody() throws SOAPException {
        return DomImpl._soapEnvelope_addBody(this);
    }

    public SOAPBody getBody() throws SOAPException {
        return DomImpl._soapEnvelope_getBody(this);
    }

    public SOAPHeader getHeader() throws SOAPException {
        return DomImpl._soapEnvelope_getHeader(this);
    }

    public SOAPHeader addHeader() throws SOAPException {
        return DomImpl._soapEnvelope_addHeader(this);
    }

    public Name createName(String localName) {
        return DomImpl._soapEnvelope_createName(this, localName);
    }

    public Name createName(String localName, String prefix, String namespaceURI) {
        return DomImpl._soapEnvelope_createName(this, localName, prefix, namespaceURI);
    }
}
