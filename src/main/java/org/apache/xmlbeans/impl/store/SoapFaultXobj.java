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

import org.apache.xmlbeans.impl.soap.Detail;
import org.apache.xmlbeans.impl.soap.Name;
import org.apache.xmlbeans.impl.soap.SOAPException;
import org.apache.xmlbeans.impl.soap.SOAPFault;

import javax.xml.namespace.QName;

class SoapFaultXobj extends SoapBodyElementXobj implements SOAPFault {
    SoapFaultXobj(Locale l, QName name) {
        super(l, name);
    }

    Xobj newNode(Locale l) {
        return new SoapFaultXobj(l, _name);
    }

    public void setFaultString(String faultString) {
        DomImpl.soapFault_setFaultString(this, faultString);
    }

    public void setFaultString(String faultString, java.util.Locale locale) {
        DomImpl.soapFault_setFaultString(this, faultString, locale);
    }

    public void setFaultCode(Name faultCodeName) throws SOAPException {
        DomImpl.soapFault_setFaultCode(this, faultCodeName);
    }

    public void setFaultActor(String faultActorString) {
        DomImpl.soapFault_setFaultActor(this, faultActorString);
    }

    public String getFaultActor() {
        return DomImpl.soapFault_getFaultActor(this);
    }

    public String getFaultCode() {
        return DomImpl.soapFault_getFaultCode(this);
    }

    public void setFaultCode(String faultCode) throws SOAPException {
        DomImpl.soapFault_setFaultCode(this, faultCode);
    }

    public java.util.Locale getFaultStringLocale() {
        return DomImpl.soapFault_getFaultStringLocale(this);
    }

    public Name getFaultCodeAsName() {
        return DomImpl.soapFault_getFaultCodeAsName(this);
    }

    public String getFaultString() {
        return DomImpl.soapFault_getFaultString(this);
    }

    public Detail addDetail() throws SOAPException {
        return DomImpl.soapFault_addDetail(this);
    }

    public Detail getDetail() {
        return DomImpl.soapFault_getDetail(this);
    }
}
