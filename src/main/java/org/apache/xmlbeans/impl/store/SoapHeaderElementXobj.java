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

import org.apache.xmlbeans.impl.soap.SOAPHeaderElement;

import javax.xml.namespace.QName;

class SoapHeaderElementXobj extends SoapElementXobj implements SOAPHeaderElement {
    SoapHeaderElementXobj(Locale l, QName name) {
        super(l, name);
    }

    Xobj newNode(Locale l) {
        return new SoapHeaderElementXobj(l, _name);
    }

    public void setMustUnderstand(boolean mustUnderstand) {
        DomImpl.soapHeaderElement_setMustUnderstand(this, mustUnderstand);
    }

    public boolean getMustUnderstand() {
        return DomImpl.soapHeaderElement_getMustUnderstand(this);
    }

    public void setActor(String actor) {
        DomImpl.soapHeaderElement_setActor(this, actor);
    }

    public String getActor() {
        return DomImpl.soapHeaderElement_getActor(this);
    }
}
