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

package org.apache.xmlbeans.impl.xpath;

import javax.xml.namespace.QName;

class XPathStep {

    final boolean _attr;
    final boolean _deep;

    int _flags;

    final QName _name;

    XPathStep _next, _prev;

    boolean _hasBacktrack;
    XPathStep _backtrack;

    XPathStep(boolean deep, boolean attr, QName name) {
        _name = name;

        _deep = deep;
        _attr = attr;

        int flags = 0;

        if (_deep || !_attr) {
            flags |= XPathExecutionContext.DESCEND;
        }

        if (_attr) {
            flags |= XPathExecutionContext.ATTRS;
        }

        _flags = flags;
    }

    boolean isWild() {
        return _name.getLocalPart().length() == 0;
    }

    boolean match(QName name) {
        String local = _name.getLocalPart();
        String nameLocal = name.getLocalPart();

        int localLength = local.length();

        // match any name to _name when _name is empty ""@""
        if (localLength == 0) {
            String uri = _name.getNamespaceURI();
            return uri.isEmpty() || uri.equals(name.getNamespaceURI());
        }

        if (localLength != nameLocal.length()) {
            return false;
        }

        String uri = _name.getNamespaceURI();
        String nameUri = name.getNamespaceURI();

        if (uri.length() != nameUri.length()) {
            return false;
        }

        return local.equals(nameLocal) && uri.equals(nameUri);
    }
}

