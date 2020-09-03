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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XPathExecutionContext {

    private XPath _xpath;
    private final ArrayList<QName> _stack;
    private PathContext[] _paths;

    public XPathExecutionContext() {
        _stack = new ArrayList<>();
    }

    public static final int HIT = 0x1;
    public static final int DESCEND = 0x2;
    public static final int ATTRS = 0x4;

    public final void init(XPath xpath) {
        if (_xpath != xpath) {
            _xpath = xpath;

            _paths = new PathContext[xpath._selector._paths.length];

            Arrays.setAll(_paths, i -> new PathContext());
        }

        _stack.clear();

        for (int i = 0; i < _paths.length; i++) {
            _paths[i].init(xpath._selector._paths[i]);
        }
    }

    public final int start() {
        int result = 0;

        for (PathContext path : _paths) {
            result |= path.start();
        }

        return result;
    }

    public final int element(QName name) {
        assert name != null;

        _stack.add(name);

        int result = 0;

        for (PathContext path : _paths) {
            result |= path.element(name);
        }

        return result;
    }

    public final boolean attr(QName name) {
        boolean hit = false;

        for (PathContext path : _paths) {
            hit = hit | path.attr(name);
        }

        return hit;
    }

    public final void end() {
        _stack.remove(_stack.size() - 1);

        for (PathContext path : _paths) {
            path.end();
        }
    }

    private final class PathContext {

        private XPathStep _curr;
        private final List<XPathStep> _prev = new ArrayList<>();

        void init(XPathStep steps) {
            _curr = steps;
            _prev.clear();
        }

        private QName top(int i) {
            return XPathExecutionContext.this._stack.get(_stack.size() - 1 - i);
        }

        // goes back to the begining of the sequence since last // wildcard
        private void backtrack() {
            assert _curr != null;

            if (_curr._hasBacktrack) {   // _backtrack seems to be a pointer to the step that follows a // wildcard
                // ex: for .//b/c/d steps c and d should backtrack to b in case there isn't a match
                _curr = _curr._backtrack;
                return;
            }

            assert !_curr._deep;

            _curr = _curr._prev;

            search:
            for (; !_curr._deep; _curr = _curr._prev) {
                int t = 0;

                for (XPathStep s = _curr; !s._deep; s = s._prev) {
                    if (!s.match(top(t++))) {
                        continue search;
                    }
                }

                break;
            }
        }

        int start() {
            assert _curr != null;
            assert _curr._prev == null;

            if (_curr._name != null) {
                return _curr._flags;
            }

            // If the steps consist on only a terminator, then the path can
            // only be '.'.  In this case, we get a hit, but there is
            // nothing else to match.  No need to backtrack.

            _curr = null;

            return HIT;
        }

        int element(QName name) {
            //System.out.println("  Path.element: " + name);
            _prev.add(_curr);

            if (_curr == null) {
                return 0;
            }

            assert _curr._name != null;

            if (!_curr._attr && _curr.match(name)) {
                if ((_curr = _curr._next)._name != null) {
                    return _curr._flags;
                }

                backtrack();

                //System.out.println("    element - HIT " + _curr._flags);
                return _curr == null ? HIT : HIT | _curr._flags;
            }

            for (; ; ) {
                backtrack();

                if (_curr == null) {
                    return 0;
                }

                if (_curr.match(name)) {
                    _curr = _curr._next;
                    break;
                }

                if (_curr._deep) {
                    break;
                }
            }

            return _curr._flags;
        }

        boolean attr(QName name) {
            return _curr != null && _curr._attr && _curr.match(name);
        }

        void end() {
            //System.out.println("  Path.end ");
            _curr = (XPathStep) _prev.remove(_prev.size() - 1);
        }
    }
}
