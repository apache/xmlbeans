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

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlLocale;
import org.apache.xmlbeans.impl.store.DomImpl.Dom;
import org.apache.xmlbeans.impl.values.TypeStore;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.apache.xmlbeans.impl.values.TypeStoreUserFactory;
import org.apache.xmlbeans.impl.values.TypeStoreVisitor;
import org.apache.xmlbeans.impl.xpath.XPathFactory;

import javax.xml.namespace.QName;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.xmlbeans.impl.store.Cur.*;

// DOM Level 3

abstract class Xobj implements TypeStore {

    Xobj(Locale l, int kind, int domType) {
        assert kind == ROOT || kind == ELEM || kind == ATTR || kind == COMMENT || kind == PROCINST;

        _locale = l;
        _bits = (domType << 4) + kind;
    }

    final int kind() {
        return _bits & 0xF;
    }

    final int domType() {
        return (_bits & 0xF0) >> 4;
    }

    final boolean isRoot() {
        return kind() == ROOT;
    }

    final boolean isAttr() {
        return kind() == ATTR;
    }

    final boolean isElem() {
        return kind() == ELEM;
    }

    final boolean isProcinst() {
        return kind() == PROCINST;
    }

    final boolean isComment() {
        return kind() == COMMENT;
    }

    final boolean isContainer() {
        return Cur.kindIsContainer(kind());
    }

    final boolean isUserNode() {
        int k = kind();
        return k == ELEM || k == ROOT || (k == ATTR && !isXmlns());
    }

    final boolean isNormalAttr() {
        return isAttr() && !Locale.isXmlns(_name);
    }

    final boolean isXmlns() {
        return isAttr() && Locale.isXmlns(_name);
    }

    final int cchAfter() {
        return _cchAfter;
    }

    final int posAfter() {
        return 2 + _cchValue;
    }

    final int posMax() {
        return 2 + _cchValue + _cchAfter;
    }

    final String getXmlnsPrefix() {
        return Locale.xmlnsPrefix(_name);
    }

    final String getXmlnsUri() {
        return getValueAsString();
    }

    final boolean hasTextEnsureOccupancy() {
        ensureOccupancy();
        return hasTextNoEnsureOccupancy();
    }

    final boolean hasTextNoEnsureOccupancy() {
        if (_cchValue > 0) {
            return true;
        }

        Xobj lastAttr = lastAttr();

        return lastAttr != null && lastAttr._cchAfter > 0;
    }

    final boolean hasAttrs() {
        return _firstChild != null && _firstChild.isAttr();
    }

    final boolean hasChildren() {
        return _lastChild != null && !_lastChild.isAttr();
    }


    /**
     * this method is to speed up DomImpl
     * when underlying obj is an Xobj
     *
     * @return 0 or 1 dom children; val 2 indicates that DomImpl needs to
     * compute the result itself
     */
    final protected int getDomZeroOneChildren() {
        if (_firstChild == null &&
            _srcValue == null &&
            _charNodesValue == null) {
            return 0;
        }

        if (_lastChild != null &&
            _lastChild.isAttr() &&
            _lastChild._charNodesAfter == null &&
            _lastChild._srcAfter == null &&
            _srcValue == null &&
            _charNodesValue == null
        ) {
            return 0;
        }

        if (_firstChild == _lastChild &&
            _firstChild != null &&
            !_firstChild.isAttr() &&
            _srcValue == null &&
            _charNodesValue == null &&
            _firstChild._srcAfter == null
        ) {
            return 1;
        }

        if (_firstChild == null &&
            _srcValue != null &&
            (_charNodesValue == null ||
             (_charNodesValue._next == null &&
              _charNodesValue._cch == _cchValue))
        ) {
            return 1;
        }
        //single elem after an attr
        Xobj lastAttr = lastAttr();
        Xobj node = lastAttr == null ?
            null : lastAttr._nextSibling;
        if (lastAttr != null &&
            lastAttr._srcAfter == null &&
            node != null &&
            node._srcAfter == null &&
            node._nextSibling == null) {
            return 1;
        }

        return 2;
    }

    /**
     * can one use the _firstChild pointer to retrieve
     * the first DOM child
     */
    final protected boolean isFirstChildPtrDomUsable() {
        if (_firstChild == null &&
            _srcValue == null &&
            _charNodesValue == null) {
            return true;
        }

        if (_firstChild != null &&
            !_firstChild.isAttr() &&
            _srcValue == null &&
            _charNodesValue == null) {
            assert (_firstChild instanceof NodeXobj) :
                "wrong node type";
            return true;
        }
        return false;
    }

    /**
     * can one use the _nextSibling pointer to retrieve
     * the next DOM sibling
     */
    final protected boolean isNextSiblingPtrDomUsable() {
        if (_charNodesAfter == null &&
            _srcAfter == null) {
            assert (_nextSibling == null ||
                    _nextSibling instanceof NodeXobj) :
                "wrong node type";
            return true;
        }
        return false;
    }

    /**
     * can one use the _charNodesValue pointer to retrieve
     * the next DOM sibling
     */
    final protected boolean isExistingCharNodesValueUsable() {
        if (_srcValue == null) {
            return false;
        }
        return _charNodesValue != null && _charNodesValue._next == null
               && _charNodesValue._cch == _cchValue;
    }

    final protected boolean isCharNodesValueUsable() {
        return isExistingCharNodesValueUsable() ||
               (_charNodesValue =
                   Cur.updateCharNodes(_locale, this,
                       _charNodesValue, _cchValue)) != null;
    }

    /**
     * can one use the _charNodesAfter pointer to retrieve
     * the next DOM sibling
     */
    final protected boolean isCharNodesAfterUsable() {
        if (_srcAfter == null) {
            return false;
        }
        if (_charNodesAfter != null && _charNodesAfter._next == null
            && _charNodesAfter._cch == this._cchAfter) {
            return true;
        }
        return (_charNodesAfter =
            Cur.updateCharNodes(_locale, this,
                _charNodesAfter, _cchAfter)) != null;
    }


    final Xobj lastAttr() {
        if (_firstChild == null || !_firstChild.isAttr()) {
            return null;
        }

        Xobj lastAttr = _firstChild;

        while (lastAttr._nextSibling != null && lastAttr._nextSibling.isAttr()) {
            lastAttr = lastAttr._nextSibling;
        }

        return lastAttr;
    }

    abstract Dom getDom();

    abstract Xobj newNode(Locale l);

    final int cchLeft(int p) {
        if (isRoot() && p == 0) {
            return 0;
        }

        Xobj x = getDenormal(p);

        p = posTemp();
        int pa = x.posAfter();

        return p - (p < pa ? 1 : pa);
    }

    final int cchRight(int p) {
        assert p < posMax();

        if (p <= 0) {
            return 0;
        }

        int pa = posAfter();

        return p < pa ? pa - p - 1 : posMax() - p;
    }

    //
    // Dom interface
    //

    public final Locale locale() {
        return _locale;
    }

    public final int nodeType() {
        return domType();
    }

    public final QName getQName() {
        return _name;
    }

    public final Cur tempCur() {
        Cur c = _locale.tempCur();
        c.moveTo(this);
        return c;
    }

    public void dump(PrintStream o, Object ref) {
        Cur.dump(o, this, ref);
    }

    public void dump(PrintStream o) {
        Cur.dump(o, this, this);
    }

    public void dump() {
        dump(System.out);
    }

    //
    //
    //

    final Cur getEmbedded() {
        _locale.embedCurs();

        return _embedded;
    }

    // Incoming p must be at text (implicitly denormalized)

    final boolean inChars(int p, Xobj xIn, int pIn, int cch, boolean includeEnd) {
        assert p > 0 && p < posMax() && p != posAfter() - 1 && cch > 0;
        assert xIn.isNormal(pIn);

        // No need to denormalize "in" if the right hand side is excluded.  Denormalizing deals
        // with the case where p is END_POS.

        int offset;

        if (includeEnd) {
            // Can't denormalize at the beginning of the document

            if (xIn.isRoot() && pIn == 0) {
                return false;
            }

            xIn = xIn.getDenormal(pIn);
            pIn = xIn.posTemp();

            offset = 1;
        } else {
            offset = 0;
        }

        return xIn == this && pIn >= p && pIn < p + cch + offset;
    }

    // Is x/p just after the end of this

    final boolean isJustAfterEnd(Xobj x, int p) {
        assert x.isNormal(p);

        // Get denormalize at the beginning of the doc

        if (x.isRoot() && p == 0) {
            return false;
        }

        return
            x == this
                ? p == posAfter()
                : x.getDenormal(p) == this && x.posTemp() == posAfter();
    }

    final boolean isInSameTree(Xobj x) {
        if (_locale != x._locale) {
            return false;
        }

        for (Xobj y = this; ; y = y._parent) {
            if (y == x) {
                return true;
            }

            if (y._parent == null) {
                for (; ; x = x._parent) {
                    if (x == this) {
                        return true;
                    }

                    if (x._parent == null) {
                        return x == y;
                    }
                }
            }
        }
    }

    final boolean contains(Cur c) {
        assert c.isNormal();

        return contains(c._xobj, c._pos);
    }

    final boolean contains(Xobj x, int p) {
        assert x.isNormal(p);

        if (this == x) {
            return p == END_POS || (p > 0 && p < posAfter());
        }

        if (_firstChild == null) {
            return false;
        }

        for (; x != null; x = x._parent) {
            if (x == this) {
                return true;
            }
        }

        return false;
    }

    final Bookmark setBookmark(int p, Object key, Object value) {
        assert isNormal(p);

        for (Bookmark b = _bookmarks; b != null; b = b._next) {
            if (p == b._pos && key == b._key) {
                if (value == null) {
                    _bookmarks = b.listRemove(_bookmarks);
                    return null;
                }

                b._value = value;

                return b;
            }
        }

        if (value == null) {
            return null;
        }

        Bookmark b = new Bookmark();

        b._xobj = this;
        b._pos = p;
        b._key = key;
        b._value = value;

        _bookmarks = b.listInsert(_bookmarks);

        return b;
    }

    final boolean hasBookmark(Object key, int pos) {
        for (Bookmark b = _bookmarks; b != null; b = b._next) {
            if (b._pos == pos && key == b._key) {
                //System.out.println("hasCDataBookmark  pos: " + pos + " xobj: " + getQName() + " b._pos: " + _bookmarks._pos);
                return true;
            }
        }

        return false;
    }

    final Xobj findXmlnsForPrefix(String prefix) {
        assert isContainer() && prefix != null;

        for (Xobj c = this; c != null; c = c._parent) {
            for (Xobj a = c.firstAttr(); a != null; a = a.nextAttr()) {
                if (a.isXmlns() && a.getXmlnsPrefix().equals(prefix)) {
                    return a;
                }
            }
        }

        return null;
    }

    final boolean removeAttr(QName name) {
        assert isContainer();

        Xobj a = getAttr(name);

        if (a == null) {
            return false;
        }

        Cur c = a.tempCur();

        for (; ; ) {
            c.moveNode(null);

            a = getAttr(name);

            if (a == null) {
                break;
            }

            c.moveTo(a);
        }

        c.release();

        return true;
    }

    final Xobj setAttr(QName name, String value) {
        assert isContainer();

        Cur c = tempCur();

        if (c.toAttr(name)) {
            c.removeFollowingAttrs();
        } else {
            c.next();
            c.createAttr(name);
        }

        c.setValue(value);

        Xobj a = c._xobj;

        c.release();

        return a;
    }

    final void setName(QName newName) {
        assert isAttr() || isElem() || isProcinst();
        assert newName != null;

        if (!_name.equals(newName) || !_name.getPrefix().equals(newName.getPrefix())) {
// TODO - this is not a structural change .... perhaps should not issue a change here?
            _locale.notifyChange();

            QName oldName = _name;

            _name = newName;
            if (this instanceof NamedNodeXobj) {
                NamedNodeXobj me = (NamedNodeXobj) this;
                me._canHavePrefixUri = true;
            }

            if (!isProcinst()) {
                Xobj disconnectFromHere = this;

                if (isAttr() && _parent != null) {
                    if (oldName.equals(Locale._xsiType) || newName.equals(Locale._xsiType)) {
                        disconnectFromHere = _parent;
                    }

                    if (oldName.equals(Locale._xsiNil) || newName.equals(Locale._xsiNil)) {
                        _parent.invalidateNil();
                    }
                }

                disconnectFromHere.disconnectNonRootUsers();
            }

            _locale._versionAll++;
            _locale._versionSansText++;
        }
    }

    final Xobj ensureParent() {
        assert _parent != null || (!isRoot() && cchAfter() == 0);
        return _parent == null ? new DocumentFragXobj(_locale).appendXobj(this) : _parent;
    }

    final Xobj firstAttr() {
        return _firstChild == null || !_firstChild.isAttr() ? null : _firstChild;
    }

    final Xobj nextAttr() {
        if (_firstChild != null && _firstChild.isAttr()) {
            return _firstChild;
        }

        if (_nextSibling != null && _nextSibling.isAttr()) {
            return _nextSibling;
        }

        return null;
    }

    final boolean isValid() {
        return !isVacant() || (_cchValue == 0 && _user != null);
    }

    final int posTemp() {
        return _locale._posTemp;
    }

    final Xobj getNormal(int p) {
        assert p == END_POS || (p >= 0 && p <= posMax());

        Xobj x = this;

        if (p == x.posMax()) {
            if (x._nextSibling != null) {
                x = x._nextSibling;
                p = 0;
            } else {
                x = x.ensureParent();
                p = END_POS;
            }
        } else if (p == x.posAfter() - 1) {
            p = END_POS;
        }

        _locale._posTemp = p;

        return x;
    }

    // Can't denormalize a position at the very beginning of the document.  No where to go to the
    // left!

    final Xobj getDenormal(int p) {
        assert !isRoot() || p == END_POS || p > 0;

        Xobj x = this;

        if (p == 0) {
            if (x._prevSibling == null) {
                x = x.ensureParent();
                p = x.posAfter() - 1;
            } else {
                x = x._prevSibling;
                p = x.posMax();
            }
        } else if (p == END_POS) {
            if (x._lastChild == null) {
                p = x.posAfter() - 1;
            } else {
                x = x._lastChild;
                p = x.posMax();
            }
        }

        _locale._posTemp = p;

        return x;
    }

    final boolean isNormal(int p) {
        if (!isValid()) {
            return false;
        }

        if (p == END_POS || p == 0) {
            return true;
        }

        if (p < 0 || p >= posMax()) {
            return false;
        }

        if (p >= posAfter()) {
            if (isRoot()) {
                return false;
            }

            if (_nextSibling != null && _nextSibling.isAttr()) {
                return false;
            }

            if (_parent == null || !_parent.isContainer()) {
                return false;
            }
        }

        return p != posAfter() - 1;
    }

    final Xobj walk(Xobj root, boolean walkChildren) {
        if (_firstChild != null && walkChildren) {
            return _firstChild;
        }

        for (Xobj x = this; x != root; x = x._parent) {
            if (x._nextSibling != null) {
                return x._nextSibling;
            }
        }

        return null;
    }

    final void removeXobj() {
        if (_parent != null) {
            if (_parent._firstChild == this) {
                _parent._firstChild = _nextSibling;
            }

            if (_parent._lastChild == this) {
                _parent._lastChild = _prevSibling;
            }

            if (_prevSibling != null) {
                _prevSibling._nextSibling = _nextSibling;
            }

            if (_nextSibling != null) {
                _nextSibling._prevSibling = _prevSibling;
            }

            _parent = null;
            _prevSibling = null;
            _nextSibling = null;
        }

    }

    final void insertXobj(Xobj s) {
        assert _locale == s._locale;
        assert !s.isRoot() && !isRoot();
        assert s._parent == null;
        assert s._prevSibling == null;
        assert s._nextSibling == null;

        ensureParent();

        s._parent = _parent;
        s._prevSibling = _prevSibling;
        s._nextSibling = this;

        if (_prevSibling != null) {
            _prevSibling._nextSibling = s;
        } else {
            _parent._firstChild = s;
        }

        _prevSibling = s;

    }

    final Xobj appendXobj(Xobj c) {
        assert _locale == c._locale;
        assert !c.isRoot();
        assert c._parent == null;
        assert c._prevSibling == null;
        assert c._nextSibling == null;
        assert _lastChild == null || _firstChild != null;

        c._parent = this;
        c._prevSibling = _lastChild;

        if (_lastChild == null) {
            _firstChild = c;
        } else {
            _lastChild._nextSibling = c;
        }

        _lastChild = c;

        return this;
    }

    final void removeXobjs(Xobj first, Xobj last) {
        assert last._locale == first._locale;
        assert first._parent == this;
        assert last._parent == this;

        if (_firstChild == first) {
            _firstChild = last._nextSibling;
        }

        if (_lastChild == last) {
            _lastChild = first._prevSibling;
        }

        if (first._prevSibling != null) {
            first._prevSibling._nextSibling = last._nextSibling;
        }

        if (last._nextSibling != null) {
            last._nextSibling._prevSibling = first._prevSibling;
        }

        // Leave the children linked together

        first._prevSibling = null;
        last._nextSibling = null;

        for (; first != null; first = first._nextSibling) {
            first._parent = null;
        }
    }

    final void insertXobjs(Xobj first, Xobj last) {
        assert _locale == first._locale;
        assert last._locale == first._locale;
        assert first._parent == null && last._parent == null;
        assert first._prevSibling == null;
        assert last._nextSibling == null;

        first._prevSibling = _prevSibling;
        last._nextSibling = this;

        if (_prevSibling != null) {
            _prevSibling._nextSibling = first;
        } else {
            _parent._firstChild = first;
        }

        _prevSibling = last;

        for (; first != this; first = first._nextSibling) {
            first._parent = _parent;
        }
    }

    final void appendXobjs(Xobj first, Xobj last) {
        assert _locale == first._locale;
        assert last._locale == first._locale;
        assert first._parent == null && last._parent == null;
        assert first._prevSibling == null;
        assert last._nextSibling == null;
        assert !first.isRoot();

        first._prevSibling = _lastChild;

        if (_lastChild == null) {
            _firstChild = first;
        } else {
            _lastChild._nextSibling = first;
        }

        _lastChild = last;

        for (; first != null; first = first._nextSibling) {
            first._parent = this;
        }
    }

    // Potential attr is going to be moved/removed, invalidate parent if it is a special attr

    final void invalidateSpecialAttr(Xobj newParent) {
        if (isAttr()) {
            if (_name.equals(Locale._xsiType)) {
                if (_parent != null) {
                    _parent.disconnectNonRootUsers();
                }

                if (newParent != null) {
                    newParent.disconnectNonRootUsers();
                }
            }

            if (_name.equals(Locale._xsiNil)) {
                if (_parent != null) {
                    _parent.invalidateNil();
                }

                if (newParent != null) {
                    newParent.invalidateNil();
                }
            }
        }
    }

    // Move or remove chars.  Incoming p is denormalized.  Incoming xTo and pTo are denormalized.
    // Option to move curs with text.  Option to perform invalidations.
    //
    // Important note: this fcn must operate under the assumption that the tree may be in an
    // invalid state.  Most likely, there may be text on two different nodes which should belong
    // on the same node.  Assertion of cursor normalization usually detects this problem.  Any of
    // the fcns it calls must also deal with these invalid conditions.  Try not to call so many
    // fcns from here.

    final void removeCharsHelper(
        int p, int cchRemove, Xobj xTo, int pTo, boolean moveCurs, boolean invalidate) {
        assert p > 0 && p < posMax() && p != posAfter() - 1;
        assert cchRemove > 0;
        assert cchRight(p) >= cchRemove;
        assert !moveCurs || xTo != null;

        // Here I check the span of text to be removed for cursors.  If xTo/pTo is not specified,
        // then the caller wants these cursors to collapse to be after the text being removed.  If
        // the caller specifies moveCurs, then the caller has arranged for the text being removed
        // to have been copied to xTp/pTo and wants the cursors to be moved there as well.
        // Note that I call nextChars here.  I do this because trying to shift the cursor to the
        // end of the text to be removed with a moveTo could cause the improper placement of the
        // cursor just before an end tag, instead of placing it just before the first child.  Also,
        // I adjust all positions of curs after the text to be removed to account for the removal.

        for (Cur c = getEmbedded(); c != null; ) {
            Cur next = c._next;

            // Here I test to see if the Cur c is in the range of chars to be removed.  Normally
            // I would call inChars, but it can't handle the invalidity of the tree, so I heve
            // inlined the inChars logic here (includeEnd is false, makes it much simpler).
            // Note that I also call moveToNoCheck because the destination may have afterText
            // and no parent which will cause normaliztion checks in MoveTo to fail.  I don't think
            // that nextChars will be called under such circumstnaces.

            assert c._xobj == this;

            if (c._pos >= p && c._pos < p + cchRemove) {
                if (moveCurs) {
                    c.moveToNoCheck(xTo, pTo + c._pos - p);
                } else {
                    c.nextChars(cchRemove - c._pos + p);
                }
            }

            // If c is still on this Xobj and it's to the right of the chars to remove, adjust
            // it to adapt to the removal of the cars.  I don't have to worry about END_POS
            // here, just curs in text.

            if (c._xobj == this && c._pos >= p + cchRemove) {
                c._pos -= cchRemove;
            }

            c = next;
        }

        // Here I move bookmarks in this text to the span of text at xTo/pTo.  The text at this/p
        // is going away, but a caller of this fcn who specifies xTo/pTo has copied the text to
        // xTo/pTo.  The caller has to make sure that if xTo/pTo is not specified, then there are
        // no bookmarks in the span of text to be removed.

        for (Bookmark b = _bookmarks; b != null; ) {
            // Similarly, as above, I can't call inChars here

            assert b._xobj == this;

            if (b._pos >= p && b._pos < p + cchRemove) {
                assert xTo != null;
                b.moveTo(xTo, pTo + b._pos - p);
            }

            if (b._xobj == this && b._pos >= p + cchRemove) {
                b._pos -= cchRemove;
            }

            b = b._next;
        }

        // Now, remove the actual chars

        int pa = posAfter();
        CharUtil cu = _locale.getCharUtil();

        if (p < pa) {
            _srcValue = cu.removeChars(p - 1, cchRemove, _srcValue, _offValue, _cchValue);
            _offValue = cu._offSrc;
            _cchValue = cu._cchSrc;

            if (invalidate) {
                invalidateUser();
                invalidateSpecialAttr(null);
            }
        } else {
            _srcAfter = cu.removeChars(p - pa, cchRemove, _srcAfter, _offAfter, _cchAfter);
            _offAfter = cu._offSrc;
            _cchAfter = cu._cchSrc;

            if (invalidate && _parent != null) {
                _parent.invalidateUser();
            }
        }
    }

    // Insert chars into this xobj.  Incoming p is denormalized.  Update bookmarks and cursors.
    // This fcn does not deal with occupation of the value, this needs to be handled by the
    // caller.

    final void insertCharsHelper(int p, Object src, int off, int cch, boolean invalidate) {
        assert p > 0;
        assert p >= posAfter() || isOccupied();

        int pa = posAfter();

        // Here I shuffle bookmarks and cursors affected by the insertion of the new text.  Because
        // getting the embedded cursors is non-trivial, I avoid getting them if I don't need to.
        // Basically, I need to know if p is before any text in the node as a whole.  If it is,
        // then there may be cursors/marks I need to shift right.

        if (p - (p < pa ? 1 : 2) < _cchValue + _cchAfter) {
            for (Cur c = getEmbedded(); c != null; c = c._next) {
                if (c._pos >= p) {
                    c._pos += cch;
                }
            }

            for (Bookmark b = _bookmarks; b != null; b = b._next) {
                if (b._pos >= p) {
                    b._pos += cch;
                }
            }
        }

        // Now, stuff the new characters in!  Also invalidate the proper container and if the
        // value of an attribute is changing, check for special attr invalidation.  Note that
        // I do not assume that inserting after text will have a parent.  There are use cases
        // from moveNodesContents which excersize this.

        CharUtil cu = _locale.getCharUtil();

        if (p < pa) {
            _srcValue = cu.insertChars(p - 1, _srcValue, _offValue, _cchValue, src, off, cch);
            _offValue = cu._offSrc;
            _cchValue = cu._cchSrc;

            if (invalidate) {
                invalidateUser();
                invalidateSpecialAttr(null);
            }
        } else {
            _srcAfter = cu.insertChars(p - pa, _srcAfter, _offAfter, _cchAfter, src, off, cch);
            _offAfter = cu._offSrc;
            _cchAfter = cu._cchSrc;

            if (invalidate && _parent != null) {
                _parent.invalidateUser();
            }
        }
    }

    Xobj copyNode(Locale toLocale) {
        Xobj newParent = null;
        Xobj copy = null;

        for (Xobj x = this; ; ) {
            x.ensureOccupancy();

            Xobj newX = x.newNode(toLocale);

            newX._srcValue = x._srcValue;
            newX._offValue = x._offValue;
            newX._cchValue = x._cchValue;

            newX._srcAfter = x._srcAfter;
            newX._offAfter = x._offAfter;
            newX._cchAfter = x._cchAfter;

            for (Bookmark b = x._bookmarks; b != null; b = b._next) {
                if (x.hasBookmark(CDataBookmark.CDATA_BOOKMARK.getKey(), b._pos)) {
                    newX.setBookmark(b._pos, CDataBookmark.CDATA_BOOKMARK.getKey(), CDataBookmark.CDATA_BOOKMARK);
                }
            }
            // TODO - strange to have charNode stuff inside here .....
            // newX._charNodesValue = CharNode.copyNodes( x._charNodesValue, newX._srcValue );
            // newX._charNodesAfter = CharNode.copyNodes( x._charNodesAfter, newX._srcAfter );

            if (newParent == null) {
                copy = newX;
            } else {
                newParent.appendXobj(newX);
            }

            // Walk to the next in-order xobj.  Record the current (y) to compute newParent

            Xobj y = x;

            if ((x = x.walk(this, true)) == null) {
                break;
            }

            if (y == x._parent) {
                newParent = newX;
            } else {
                for (; y._parent != x._parent; y = y._parent) {
                    newParent = newParent._parent;
                }
            }
        }

        copy._srcAfter = null;
        copy._offAfter = 0;
        copy._cchAfter = 0;

        return copy;
    }

    // Rturns all the chars, even if there is text intermixed with children

    String getCharsAsString(int p, int cch, int wsr) {
        if (cchRight(p) == 0) {
            return "";
        }

        Object src = getChars(p, cch);

        if (wsr == WS_PRESERVE) {
            return CharUtil.getString(src, _locale._offSrc, _locale._cchSrc);
        }

        Locale.ScrubBuffer scrub = Locale.getScrubBuffer(wsr);

        scrub.scrub(src, _locale._offSrc, _locale._cchSrc);

        return scrub.getResultAsString();
    }

    String getCharsAfterAsString(int off, int cch) {
        int offset = off + _cchValue + 2;
        if (offset == posMax()) {
            offset = -1;
        }
        return getCharsAsString(offset, cch, WS_PRESERVE);
    }

    String getCharsValueAsString(int off, int cch) {
        return getCharsAsString(off + 1, cch, WS_PRESERVE);
    }

    String getValueAsString(int wsr) {
        if (!hasChildren()) {
            Object src = getFirstChars();

            if (wsr == WS_PRESERVE) {
                String s = CharUtil.getString(src, _locale._offSrc, _locale._cchSrc);

                // Cache string to be able to use it later again

                int cch = s.length();

                if (cch > 0) {
                    Xobj lastAttr = lastAttr();

                    assert (lastAttr == null ? _cchValue : lastAttr._cchAfter) == cch;

                    if (lastAttr != null) {
                        lastAttr._srcAfter = s;
                        lastAttr._offAfter = 0;
                    } else {
                        _srcValue = s;
                        _offValue = 0;
                    }
                }

                return s;
            }

            Locale.ScrubBuffer scrub = Locale.getScrubBuffer(wsr);

            scrub.scrub(src, _locale._offSrc, _locale._cchSrc);

            return scrub.getResultAsString();
        }

        Locale.ScrubBuffer scrub = Locale.getScrubBuffer(wsr);

        Cur c = tempCur();

        c.push();

        for (c.next(); !c.isAtEndOfLastPush(); ) {
            if (c.isText()) {
                scrub.scrub(c.getChars(-1), c._offSrc, c._cchSrc);
            }

            if (c.isComment() || c.isProcinst()) {
                c.skip();
            } else {
                c.next();
            }
        }

        String s = scrub.getResultAsString();

        c.release();

        return s;
    }

    String getValueAsString() {
        return getValueAsString(WS_PRESERVE);
    }

    // Returns just chars just after the begin tag ... does not get all the text if there are
    // children

    Object getFirstChars() {
        ensureOccupancy();

        if (_cchValue > 0) {
            return getChars(1, -1);
        }

        Xobj lastAttr = lastAttr();

        if (lastAttr == null || lastAttr._cchAfter <= 0) {
            _locale._offSrc = 0;
            _locale._cchSrc = 0;

            return null;
        }

        return lastAttr.getChars(lastAttr.posAfter(), -1);
    }

    Object getChars(int pos, int cch, Cur c) {
        Object src = getChars(pos, cch);

        c._offSrc = _locale._offSrc;
        c._cchSrc = _locale._cchSrc;

        return src;
    }

    // These return the remainder of the char triple that getChars starts

    Object getChars(int pos, int cch) {
        assert isNormal(pos);

        int cchRight = cchRight(pos);

        if (cch < 0 || cch > cchRight) {
            cch = cchRight;
        }

        if (cch == 0) {
            _locale._offSrc = 0;
            _locale._cchSrc = 0;

            return null;
        }

        return getCharsHelper(pos, cch);
    }

    // Assumes that there are chars to return, does not assume normal x/p

    Object getCharsHelper(int pos, int cch) {
        assert cch > 0 && cchRight(pos) >= cch;

        int pa = posAfter();

        Object src;

        if (pos >= pa) {
            src = _srcAfter;
            _locale._offSrc = _offAfter + pos - pa;
        } else {
            src = _srcValue;
            _locale._offSrc = _offValue + pos - 1;
        }

        _locale._cchSrc = cch;

        return src;
    }

    //
    //
    //

    final void setBit(int mask) {
        _bits |= mask;
    }

    final void clearBit(int mask) {
        _bits &= ~mask;
    }

    final boolean bitIsSet(int mask) {
        return (_bits & mask) != 0;
    }

    final boolean bitIsClear(int mask) {
        return (_bits & mask) == 0;
    }

    static final int VACANT = 0x100;
    static final int STABLE_USER = 0x200;
    static final int INHIBIT_DISCONNECT = 0x400;

    final boolean isVacant() {
        return bitIsSet(VACANT);
    }

    final boolean isOccupied() {
        return bitIsClear(VACANT);
    }

    final boolean inhibitDisconnect() {
        return bitIsSet(INHIBIT_DISCONNECT);
    }

    final boolean isStableUser() {
        return bitIsSet(STABLE_USER);
    }

    void invalidateNil() {
        if (_user != null) {
            _user.invalidate_nilvalue();
        }
    }

    void setStableType(SchemaType type) {
        setStableUser(((TypeStoreUserFactory) type).createTypeStoreUser());
    }

    void setStableUser(TypeStoreUser user) {
        disconnectNonRootUsers();
        disconnectUser();

        assert _user == null;

        _user = user;

        _user.attach_store(this);

        setBit(STABLE_USER);
    }

    void disconnectUser() {
        if (_user != null && !inhibitDisconnect()) {
            ensureOccupancy();
            _user.disconnect_store();
            _user = null;
        }
    }

    // If a node does not have a user, then I don't need to walk its descendents.  NOte that
    // the doconnect happens in document order.  This may be a problem ... not sure ... May want
    // to disconnect in a bottom up manner.

    void disconnectNonRootUsers() {
        Xobj next;

        for (Xobj x = this; x != null; x = next) {
            next = x.walk(this, x._user != null);

            if (!x.isRoot()) {
                x.disconnectUser();
            }
        }
    }

    void disconnectChildrenUsers() {
        Xobj next;

        for (Xobj x = walk(this, _user == null); x != null; x = next) {
            next = x.walk(this, x._user != null);

            x.disconnectUser();
        }
    }

    /**
     * Given a prefix, returns the namespace corresponding to
     * the prefix at this location, or null if there is no mapping
     * for this prefix.
     * <p>
     * prefix="" indicates the absence of a prefix.  A return value
     * of "" indicates the no-namespace, and should not be confused
     * with a return value of null, which indicates an illegal
     * state, where there is no mapping for the given prefix.
     * <p>
     * If the the default namespace is not explicitly mapped in the xml,
     * the xml spec says that it should be mapped to the no-namespace.
     * When the 'defaultAlwaysMapped' parameter is true, the default namepsace
     * will return the no-namespace even if it is not explicity
     * mapped, otherwise the default namespace will return null.
     * <p>
     * This function intercepts the built-in prefixes "xml" and
     * "xmlns" and returns their well-known namespace URIs.
     *
     * @param prefix              The prefix to look up.
     * @param defaultAlwaysMapped If true, return the no-namespace for the default namespace if not set.
     * @return The mapped namespace URI ("" if no-namespace), or null if no mapping.
     */

    final String namespaceForPrefix(String prefix, boolean defaultAlwaysMapped) {
        if (prefix == null) {
            prefix = "";
        }

        // handle built-in prefixes

        if (prefix.equals("xml")) {
            return Locale._xml1998Uri;
        }

        if (prefix.equals("xmlns")) {
            return Locale._xmlnsUri;
        }

        for (Xobj x = this; x != null; x = x._parent) {
            for (Xobj a = x._firstChild; a != null && a.isAttr(); a = a._nextSibling) {
                if (a.isXmlns() && a.getXmlnsPrefix().equals(prefix)) {
                    return a.getXmlnsUri();
                }
            }
        }

        return defaultAlwaysMapped && prefix.isEmpty() ? "" : null;
    }

    final String prefixForNamespace(String ns, String suggestion, boolean createIfMissing) {
        if (ns == null) {
            ns = "";
        }

        // special cases

        if (ns.equals(Locale._xml1998Uri)) {
            return "xml";
        }

        if (ns.equals(Locale._xmlnsUri)) {
            return "xmlns";
        }

        // Get the closest container for the spot we're on

        Xobj base = this;

        while (!base.isContainer()) {
            base = base.ensureParent();
        }

        // Special handling for the no-namespace case

        if (ns.isEmpty()) {
            // Search for a namespace decl which defines the default namespace

            Xobj a = base.findXmlnsForPrefix("");

            // If I did not find a default decl or the decl maps to the no namespace, then
            // the default namespace is mapped to ""

            if (a == null || a.getXmlnsUri().isEmpty()) {
                return "";
            }

            // At this point, I've found a default namespace which is *not* the no-namespace.
            // If I can't modify the document to mape the desired no-namespace, I must fail.

            if (!createIfMissing) {
                return null;
            }

            // Ok, I need to make the default namespace on the nearest container map to ""

            base.setAttr(_locale.createXmlns(null), "");

            return "";
        }

        // Look for an exisiting mapping for the desired uri which has a visible prefix

        for (Xobj c = base; c != null; c = c._parent) {
            for (Xobj a = c.firstAttr(); a != null; a = a.nextAttr()) {
                if (a.isXmlns() && a.getXmlnsUri().equals(ns)) {
                    if (base.findXmlnsForPrefix(a.getXmlnsPrefix()) == a) {
                        return a.getXmlnsPrefix();
                    }
                }
            }
        }

        // No exisiting xmlns I can use, need to create one.  See if I can first

        if (!createIfMissing) {
            return null;
        }

        // Sanitize the suggestion.

        if (suggestion != null &&
            (suggestion.isEmpty() || suggestion.toLowerCase(java.util.Locale.ROOT).startsWith("xml") ||
             base.findXmlnsForPrefix(suggestion) != null)) {
            suggestion = null;
        }

        // If no suggestion, make one up

        if (suggestion == null) {
            String prefixBase = QNameHelper.suggestPrefix(ns);

            suggestion = prefixBase;

            for (int i = 1; ; suggestion = prefixBase + i++) {
                if (base.findXmlnsForPrefix(suggestion) == null) {
                    break;
                }
            }
        }

        // Add a new namespace decl at the top elem if one exists, otherwise at root

        Xobj c = base;

        while (!c.isRoot() && !c.ensureParent().isRoot()) {
            c = c._parent;
        }

        base.setAttr(_locale.createXmlns(suggestion), ns);

        return suggestion;
    }

    final QName getValueAsQName() {
        assert !hasChildren();

        // TODO -
        // caching the QName value in this object would prevent one from having
        // to repeat all this string arithmatic over and over again.  Perhaps
        // when I make the store capable of handling strong simple types this
        // can be done ...

        String value = getValueAsString(WS_COLLAPSE);

        String prefix, localname;

        int firstcolon = value.indexOf(':');

        if (firstcolon >= 0) {
            prefix = value.substring(0, firstcolon);
            localname = value.substring(firstcolon + 1);
        } else {
            prefix = "";
            localname = value;
        }

        String uri = namespaceForPrefix(prefix, true);

        if (uri == null) {
            return null; // no prefix definition found - that's illegal
        }

        return new QName(uri, localname);
    }

    final Xobj getAttr(QName name) {
        for (Xobj x = _firstChild; x != null && x.isAttr(); x = x._nextSibling) {
            if (x._name.equals(name)) {
                return x;
            }
        }

        return null;
    }

    final QName getXsiTypeName() {
        assert isContainer();

        Xobj a = getAttr(Locale._xsiType);

        return a == null ? null : a.getValueAsQName();
    }

    final XmlObject getObject() {
        return isUserNode() ? (XmlObject) getUser() : null;
    }

    final TypeStoreUser getUser() {
        assert isUserNode();
        assert _user != null || (!isRoot() && !isStableUser());

        if (_user == null) {
            // BUGBUG - this is recursive

            TypeStoreUser parentUser =
                _parent == null
                    ? ((TypeStoreUserFactory) XmlBeans.NO_TYPE).createTypeStoreUser()
                    : _parent.getUser();

            _user =
                isElem()
                    ? parentUser.create_element_user(_name, getXsiTypeName())
                    : parentUser.create_attribute_user(_name);

            _user.attach_store(this);
        }

        return _user;
    }

    final void invalidateUser() {
        assert isValid();
        assert _user == null || isUserNode();

        if (_user != null) {
            _user.invalidate_value();
        }
    }

    final void ensureOccupancy() {
        assert isValid();

        if (isVacant()) {
            assert isUserNode();

            // In order to use Cur to set the value, I mark the
            // value as occupied and remove the user to prohibit
            // further user invalidations

            clearBit(VACANT);

            TypeStoreUser user = _user;
            _user = null;

            String value = user.build_text(this);


            long saveVersion = _locale._versionAll;
            long saveVersionSansText = _locale._versionSansText;


            setValue(value);
            assert saveVersionSansText == _locale._versionSansText;

            _locale._versionAll = saveVersion;


            assert _user == null;
            _user = user;
        }
    }

    private void setValue(String val) {
        assert CharUtil.isValid(val, 0, val.length());

        // Check for nothing to insert

        if (val.length() <= 0) {
            return;
        }

        _locale.notifyChange();
        Xobj lastAttr = lastAttr();
        int startPos = 1;
        Xobj charOwner = this;
        if (lastAttr != null) {
            charOwner = lastAttr;
            startPos = charOwner.posAfter();
        }
        charOwner.insertCharsHelper(startPos, val, 0, val.length(), true);
    }
    //
    // TypeStore
    //

    public SchemaTypeLoader get_schematypeloader() {
        return _locale._schemaTypeLoader;
    }

    public XmlLocale get_locale() {
        return _locale;
    }

    // TODO - remove this when I've replaced the old store
    public Object get_root_object() {
        return _locale;
    }

    public boolean is_attribute() {
        assert isValid();
        return isAttr();
    }

    public boolean validate_on_set() {
        assert isValid();
        return _locale._validateOnSet;
    }

    public void invalidate_text() {
        _locale.enter();

        try {
            assert isValid();

            if (isOccupied()) {
                if (hasTextNoEnsureOccupancy() || hasChildren()) {
                    TypeStoreUser user = _user;
                    _user = null;

                    Cur c = tempCur();
                    c.moveNodeContents(null, false);
                    c.release();

                    assert _user == null;
                    _user = user;
                }

                setBit(VACANT);
            }

            assert isValid();
        } finally {
            _locale.exit();
        }
    }

    public String fetch_text(int wsr) {
        _locale.enter();

        try {
            assert isValid() && isOccupied();

            return getValueAsString(wsr);
        } finally {
            _locale.exit();
        }
    }

    public XmlCursor new_cursor() {
        _locale.enter();

        try {
            Cur c = tempCur();
            XmlCursor xc = new Cursor(c);
            c.release();
            return xc;

        } finally {
            _locale.exit();
        }
    }

    public SchemaField get_schema_field() {
        assert isValid();

        if (isRoot()) {
            return null;
        }

        TypeStoreUser parentUser = ensureParent().getUser();

        if (isAttr()) {
            return parentUser.get_attribute_field(_name);
        }

        assert isElem();

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null) {
            return null;
        }

        for (Xobj x = _parent._firstChild; ; x = x._nextSibling) {
            if (x.isElem()) {
                visitor.visit(x._name);

                if (x == this) {
                    return visitor.get_schema_field();
                }
            }
        }
    }

    public void validate(ValidatorListener eventSink) {
        _locale.enter();

        try {
            Cur c = tempCur();
            new Validate(c, eventSink);
            c.release();
        } finally {
            _locale.exit();
        }
    }

    public TypeStoreUser change_type(SchemaType type) {
        _locale.enter();

        try {
            Cur c = tempCur();
            c.setType(type, false);
            c.release();
        } finally {
            _locale.exit();
        }

        return getUser();
    }

    public TypeStoreUser substitute(QName name, SchemaType type) {
        _locale.enter();

        try {
            Cur c = tempCur();
            c.setSubstitution(name, type);
            c.release();
        } finally {
            _locale.exit();
        }

        return getUser();
    }

    public QName get_xsi_type() {
        return getXsiTypeName();
    }

    public void store_text(String text) {
        _locale.enter();

        TypeStoreUser user = _user;
        _user = null;

        try {
            Cur c = tempCur();

            c.moveNodeContents(null, false);

            if (text != null && !text.isEmpty()) {
                c.next();
                c.insertString(text);
            }

            c.release();
        } finally {
            assert _user == null;
            _user = user;

            _locale.exit();
        }
    }

    public int compute_flags() {
        if (isRoot()) {
            return 0;
        }

        TypeStoreUser parentUser = ensureParent().getUser();

        if (isAttr()) {
            return parentUser.get_attributeflags(_name);
        }

        int f = parentUser.get_elementflags(_name);

        if (f != -1) {
            return f;
        }

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null) {
            return 0;
        }

        for (Xobj x = _parent._firstChild; ; x = x._nextSibling) {
            if (x.isElem()) {
                visitor.visit(x._name);

                if (x == this) {
                    return visitor.get_elementflags();
                }
            }
        }
    }

    public String compute_default_text() {
        if (isRoot()) {
            return null;
        }

        TypeStoreUser parentUser = ensureParent().getUser();

        if (isAttr()) {
            return parentUser.get_default_attribute_text(_name);
        }

        String result = parentUser.get_default_element_text(_name);

        if (result != null) {
            return result;
        }

        TypeStoreVisitor visitor = parentUser.new_visitor();

        if (visitor == null) {
            return null;
        }

        for (Xobj x = _parent._firstChild; ; x = x._nextSibling) {
            if (x.isElem()) {
                visitor.visit(x._name);

                if (x == this) {
                    return visitor.get_default_text();
                }
            }
        }
    }

    public boolean find_nil() {
        if (isAttr()) {
            return false;
        }

        _locale.enter();

        try {
            Xobj a = getAttr(Locale._xsiNil);

            if (a == null) {
                return false;
            }

            String value = a.getValueAsString(WS_COLLAPSE);

            return value.equals("true") || value.equals("1");
        } finally {
            _locale.exit();
        }
    }

    public void invalidate_nil() {
        if (isAttr()) {
            return;
        }

        _locale.enter();

        try {
            if (!_user.build_nil()) {
                removeAttr(Locale._xsiNil);
            } else {
                setAttr(Locale._xsiNil, "true");
            }
        } finally {
            _locale.exit();
        }
    }

    public int count_elements(QName name) {
        return _locale.count(this, name, null);
    }

    public int count_elements(QNameSet names) {
        return _locale.count(this, null, names);
    }

    public TypeStoreUser find_element_user(QName name, int i) {
        for (Xobj x = _firstChild; x != null; x = x._nextSibling) {
            if (x.isElem() && x._name.equals(name) && --i < 0) {
                return x.getUser();
            }
        }

        return null;
    }

    public TypeStoreUser find_element_user(QNameSet names, int i) {
        for (Xobj x = _firstChild; x != null; x = x._nextSibling) {
            if (x.isElem() && names.contains(x._name) && --i < 0) {
                return x.getUser();
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends XmlObject> void find_all_element_users(QName name, List<T> fillMeUp) {
        for (Xobj x = _firstChild; x != null; x = x._nextSibling) {
            if (x.isElem() && x._name.equals(name)) {
                fillMeUp.add((T) x.getUser());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends XmlObject> void find_all_element_users(QNameSet names, List<T> fillMeUp) {
        for (Xobj x = _firstChild; x != null; x = x._nextSibling) {
            if (x.isElem() && names.contains(x._name)) {
                fillMeUp.add((T) x.getUser());
            }
        }
    }

    private static TypeStoreUser insertElement(QName name, Xobj x, int pos) {
        x._locale.enter();

        try {
            Cur c = x._locale.tempCur();
            c.moveTo(x, pos);
            c.createElement(name);
            TypeStoreUser user = c.getUser();
            c.release();
            return user;
        } finally {
            x._locale.exit();
        }
    }

    public TypeStoreUser insert_element_user(QName name, int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (!isContainer()) {
            throw new IllegalStateException();
        }

        Xobj x = _locale.findNthChildElem(this, name, null, i);

        if (x == null) {
            if (i > _locale.count(this, name, null) + 1) {
                throw new IndexOutOfBoundsException();
            }

            return add_element_user(name);
        }

        return insertElement(name, x, 0);
    }

    public TypeStoreUser insert_element_user(QNameSet names, QName name, int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (!isContainer()) {
            throw new IllegalStateException();
        }

        Xobj x = _locale.findNthChildElem(this, null, names, i);

        if (x == null) {
            if (i > _locale.count(this, null, names) + 1) {
                throw new IndexOutOfBoundsException();
            }

            return add_element_user(name);
        }

        return insertElement(name, x, 0);
    }

    public TypeStoreUser add_element_user(QName name) {
        if (!isContainer()) {
            throw new IllegalStateException();
        }

        QNameSet endSet = null;
        boolean gotEndSet = false;

        Xobj candidate = null;

        for (Xobj x = _lastChild; x != null; x = x._prevSibling) {
            if (x.isContainer()) {
                if (x._name.equals(name)) {
                    break;
                }

                if (!gotEndSet) {
                    endSet = _user.get_element_ending_delimiters(name);
                    gotEndSet = true;
                }

                if (endSet == null || endSet.contains(x._name)) {
                    candidate = x;
                }
            }
        }

        return
            candidate == null
                ? insertElement(name, this, END_POS)
                : insertElement(name, candidate, 0);
    }

    private static void removeElement(Xobj x) {
        if (x == null) {
            throw new IndexOutOfBoundsException();
        }

        x._locale.enter();

        try {
            Cur c = x.tempCur();
            c.moveNode(null);
            c.release();
        } finally {
            x._locale.exit();
        }
    }

    public void remove_element(QName name, int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (!isContainer()) {
            throw new IllegalStateException();
        }

        Xobj x;

        for (x = _firstChild; x != null; x = x._nextSibling) {
            if (x.isElem() && x._name.equals(name) && --i < 0) {
                break;
            }
        }

        removeElement(x);
    }

    public void remove_element(QNameSet names, int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (!isContainer()) {
            throw new IllegalStateException();
        }

        Xobj x;

        for (x = _firstChild; x != null; x = x._nextSibling) {
            if (x.isElem() && names.contains(x._name) && --i < 0) {
                break;
            }
        }

        removeElement(x);
    }

    public TypeStoreUser find_attribute_user(QName name) {
        Xobj a = getAttr(name);

        return a == null ? null : a.getUser();
    }

    public TypeStoreUser add_attribute_user(QName name) {
        if (getAttr(name) != null) {
            throw new IndexOutOfBoundsException();
        }

        _locale.enter();

        try {
            return setAttr(name, "").getUser();
        } finally {
            _locale.exit();
        }
    }

    public void remove_attribute(QName name) {
        _locale.enter();

        try {
            if (!removeAttr(name)) {
                throw new IndexOutOfBoundsException();
            }
        } finally {
            _locale.exit();
        }
    }

    public TypeStoreUser copy_contents_from(TypeStore source) {
        Xobj xSrc = (Xobj) source;

        if (xSrc == this) {
            return getUser();
        }

        _locale.enter();

        try {
            xSrc._locale.enter();

            Cur c = tempCur();

            try {
                Cur cSrc1 = xSrc.tempCur();
                Map<String, String> sourceNamespaces = Locale.getAllNamespaces(cSrc1, null);
                cSrc1.release();

                if (isAttr()) {
                    Cur cSrc = xSrc.tempCur();
                    String value = Locale.getTextValue(cSrc);
                    cSrc.release();

                    c.setValue(value);
                } else {
                    // Here I save away the user of this node so that it does not get whacked
                    // in the following operations.

                    disconnectChildrenUsers();

                    assert !inhibitDisconnect();

                    setBit(INHIBIT_DISCONNECT);

                    QName xsiType = isContainer() ? getXsiTypeName() : null;

                    Xobj copy = xSrc.copyNode(_locale);

                    Cur.moveNodeContents(this, null, true);

                    c.next();

                    Cur.moveNodeContents(copy, c, true);

                    c.moveTo(this);

                    if (xsiType != null) {
                        c.setXsiType(xsiType);
                    }

                    assert inhibitDisconnect();
                    clearBit(INHIBIT_DISCONNECT);
                }

                if (sourceNamespaces != null) {
                    if (!c.isContainer()) {
                        c.toParent();
                    }

                    Locale.applyNamespaces(c, sourceNamespaces);
                }

            } finally {
                c.release();

                xSrc._locale.exit();
            }
        } finally {
            _locale.exit();
        }

        return getUser();
    }

    public TypeStoreUser copy(SchemaTypeLoader stl, SchemaType type, XmlOptions options) {
        //do not use a user's Factory method for copying.
        //XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        options = XmlOptions.maskNull(options);
        SchemaType sType = options.getDocumentType();

        if (sType == null) {
            sType = type == null ? XmlObject.type : type;
        }

        Locale locale = this.locale();
        if (options.isCopyUseNewSynchronizationDomain()) {
            locale = Locale.getLocale(stl, options);
        }

        boolean isFragment = !sType.isDocumentType() && !(sType.isNoType() && (this instanceof DocumentXobj));
        Xobj destination = Cur.createDomDocumentRootXobj(locale, isFragment);


        locale.enter();
        try {
            Cur c = destination.tempCur();
            c.setType(type);
            c.release();
        } finally {
            locale.exit();
        }

        return destination.copy_contents_from(this);
    }

    public void array_setter(XmlObject[] sources, QName elementName) {
        _locale.enter();

        try {
            // TODO - this is the quick and dirty implementation, make this faster

            int m = sources.length;

            List<Xobj> copies = new ArrayList<>();
            List<SchemaType> types = new ArrayList<>();

            for (XmlObject source : sources) {
                // TODO - deal with null sources[ i ] here -- what to do?

                if (source == null) {
                    throw new IllegalArgumentException("Array element null");
                } else if (source.isImmutable()) {
                    copies.add(null);
                    types.add(null);
                } else {
                    Xobj x = ((Xobj) ((TypeStoreUser) source).get_store());

                    if (x._locale == _locale) {
                        copies.add(x.copyNode(_locale));
                    } else {
                        x._locale.enter();

                        try {
                            copies.add(x.copyNode(_locale));
                        } finally {
                            x._locale.exit();
                        }
                    }

                    types.add(source.schemaType());
                }
            }

            int n = count_elements(elementName);

            for (; n > m; n--) {
                remove_element(elementName, m);
            }

            for (; m > n; n++) {
                add_element_user(elementName);
            }

            assert m == n;

            List<XmlObject> elementsUser = new ArrayList<>();

            find_all_element_users(elementName, elementsUser);

            List<Xobj> elements = elementsUser.stream()
                .map(x -> (TypeStoreUser) x)
                .map(TypeStoreUser::get_store)
                .map(x -> (Xobj) x)
                .collect(Collectors.toList());

            assert elements.size() == n;

            Cur c = tempCur();

            for (int i = 0; i < n; i++) {
                Xobj x = elements.get(i);

                if (sources[i].isImmutable()) {
                    x.getObject().set(sources[i]);
                } else {
                    Cur.moveNodeContents(x, null, true);

                    c.moveTo(x);
                    c.next();

                    Cur.moveNodeContents(copies.get(i), c, true);

                    x.change_type(types.get(i));
                }
            }

            c.release();
        } finally {
            _locale.exit();
        }
    }

    public void visit_elements(TypeStoreVisitor visitor) {
        throw new RuntimeException("Not implemeneted");
    }

    public XmlObject[] exec_query(String queryExpr, XmlOptions options) {
        _locale.enter();

        try {
            Cur c = tempCur();

            XmlObject[] result = XPathFactory.objectExecQuery(c, queryExpr, options);

            c.release();

            return result;
        } finally {
            _locale.exit();
        }
    }

    public String find_prefix_for_nsuri(String nsuri, String suggested_prefix) {
        _locale.enter();

        try {
            return prefixForNamespace(nsuri, suggested_prefix, true);
        } finally {
            _locale.exit();
        }
    }

    public String getNamespaceForPrefix(String prefix) {
        return namespaceForPrefix(prefix, true);
    }

    Locale _locale;
    QName _name;

    Cur _embedded;

    Bookmark _bookmarks;

    int _bits;

    Xobj _parent;
    Xobj _nextSibling;
    Xobj _prevSibling;
    Xobj _firstChild;
    Xobj _lastChild;

    Object _srcValue, _srcAfter;
    int _offValue, _offAfter;
    int _cchValue, _cchAfter;

    // TODO - put this in a ptr off this node
    CharNode _charNodesValue;
    CharNode _charNodesAfter;

    // TODO - put this in a ptr off this node
    TypeStoreUser _user;
}
