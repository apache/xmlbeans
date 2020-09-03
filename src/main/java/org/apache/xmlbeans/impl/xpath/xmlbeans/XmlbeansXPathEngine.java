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

package org.apache.xmlbeans.impl.xpath.xmlbeans;

import org.apache.xmlbeans.impl.store.Cur;
import org.apache.xmlbeans.impl.store.Locale;
import org.apache.xmlbeans.impl.xpath.XPath;
import org.apache.xmlbeans.impl.xpath.XPathEngine;
import org.apache.xmlbeans.impl.xpath.XPathExecutionContext;

import java.util.ConcurrentModificationException;

class XmlbeansXPathEngine extends XPathExecutionContext implements XPathEngine {
    XmlbeansXPathEngine(XPath xpath, Cur c) {
        assert c.isContainer();

        _version = c.getLocale().version();
        _cur = c.weakCur(this);

        _cur.push();

        init(xpath);

        int ret = start();

        if ((ret & HIT) != 0) {
            c.addToSelection();
        }

        doAttrs(ret, c);

        if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement(_cur)) {
            release();
        }
    }

    private void advance(Cur c) {
        assert _cur != null;

        if (_cur.isFinish()) {
            if (_cur.isAtEndOfLastPush()) {
                release();
            } else {
                end();
                _cur.next();
            }
        } else if (_cur.isElem()) {
            int ret = element(_cur.getName());

            if ((ret & HIT) != 0) {
                c.addToSelection(_cur);
            }

            doAttrs(ret, c);

            if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement(_cur)) {
                end();
                _cur.skip();
            }
        } else {
            do {
                _cur.next();
            }
            while (!_cur.isContainerOrFinish());
        }
    }

    private void doAttrs(int ret, Cur c) {
        assert _cur.isContainer();

        if ((ret & ATTRS) != 0) {
            if (_cur.toFirstAttr()) {
                do {
                    if (attr(_cur.getName())) {
                        c.addToSelection(_cur);
                    }
                }
                while (_cur.toNextAttr());

                _cur.toParent();
            }
        }
    }

    public boolean next(Cur c) {
        if (_cur != null && _version != _cur.getLocale().version()) {
            throw new ConcurrentModificationException("Document changed during select");
        }

        int startCount = c.selectionCount();

        while (_cur != null) {
            advance(c);

            if (startCount != c.selectionCount()) {
                return true;
            }
        }

        return false;
    }

    public void release() {
        if (_cur != null) {
            _cur.release();
            _cur = null;
        }
    }

    private final long _version;
    private Cur _cur;
}

