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

import org.apache.xmlbeans.impl.soap.SOAPElement;

class SaajCdataNode extends CdataNode implements org.apache.xmlbeans.impl.soap.Text {
    public SaajCdataNode(Locale l) {
        super(l);
    }

    public boolean isComment() {
        return DomImpl._soapText_isComment(this);
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
}

