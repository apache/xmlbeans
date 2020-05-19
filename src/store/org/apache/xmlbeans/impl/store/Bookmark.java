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

import org.apache.xmlbeans.XmlCursor;

class Bookmark implements XmlCursor.XmlMark {
    boolean isOnList(Bookmark head) {
        for (; head != null; head = head._next)
            if (head == this)
                return true;

        return false;
    }

    Bookmark listInsert(Bookmark head) {
        assert _next == null && _prev == null;

        if (head == null)
            head = _prev = this;
        else {
            _prev = head._prev;
            head._prev = head._prev._next = this;
        }

        return head;
    }

    Bookmark listRemove(Bookmark head) {
        assert _prev != null && isOnList(head);

        if (_prev == this) {
            head = null;
        } else {
            if (head == this) {
                head = _next;
            } else {
                _prev._next = _next;
            }

            if (_next == null) {
                if (head != null) {
                    head._prev = _prev;
                }
            } else {
                _next._prev = _prev;
                _next = null;
            }
        }

        _prev = null;
        assert _next == null;

        return head;
    }

    void moveTo(Xobj x, int p) {
        assert isOnList(_xobj._bookmarks);

        if (_xobj != x) {
            _xobj._bookmarks = listRemove(_xobj._bookmarks);
            x._bookmarks = listInsert(x._bookmarks);

            _xobj = x;
        }

        _pos = p;
    }

    //
    // XmlCursor.XmlMark method
    //

    public XmlCursor createCursor() {
        if (_xobj == null) {
            throw new IllegalStateException(
                "Attempting to create a cursor on a bookmark that " +
                "has been cleared or replaced.");
        }

        return Cursor.newCursor(_xobj, _pos);
    }

    //
    //
    //

    Xobj _xobj;
    int _pos;

    Bookmark _next;
    Bookmark _prev;

    Object _key;
    Object _value;
}
