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

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import static org.apache.xmlbeans.impl.store.Cur.PROCINST;

class ProcInstXobj extends NodeXobj implements ProcessingInstruction {
    ProcInstXobj(Locale l, String target) {
        super(l, PROCINST, DomImpl.PROCINST);
        _name = _locale.makeQName(null, target);
    }

    Xobj newNode(Locale l) {
        return new ProcInstXobj(l, _name.getLocalPart());
    }

    public int getLength() {
        return 0;
    }

    public Node getFirstChild() {
        return null;
    }

    public String getData() {
        return DomImpl._processingInstruction_getData(this);
    }

    public String getTarget() {
        return DomImpl._processingInstruction_getTarget(this);
    }

    public void setData(String data) {
        DomImpl._processingInstruction_setData(this, data);
    }
}

