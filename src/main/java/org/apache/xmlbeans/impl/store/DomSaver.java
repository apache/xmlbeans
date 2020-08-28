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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.List;

final class DomSaver extends Saver {
    private Cur _nodeCur;
    private SchemaType _type;
    private final SchemaTypeLoader _stl;
    private final XmlOptions _options;
    private final boolean _isFrag;


    DomSaver(Cur c, boolean isFrag, XmlOptions options) {
        super(c, options);

        if (c.isUserNode()) {
            _type = c.getUser().get_schema_type();
        }

        _stl = c._locale._schemaTypeLoader;
        _options = options;
        _isFrag = isFrag;
    }

    Node saveDom() {
        Locale l = Locale.getLocale(_stl, _options);

        l.enter();

        try {
            _nodeCur = l.getCur();  // Not weak or temp

            // Build the tree

            //noinspection StatementWithEmptyBody
            while (process());

            // Set the type

            while (!_nodeCur.isRoot()) {
                _nodeCur.toParent();
            }

            if (_type != null) {
                _nodeCur.setType(_type);
            }

            Node node = (Node) _nodeCur.getDom();

            _nodeCur.release();

            _nodeCur = null;

            return node;
        } finally {
            l.exit();
        }
    }

    @Override
    protected boolean emitElement(SaveCur c, List<QName> attrNames, List<String> attrValues) {
        // If there was text or comments before the frag element, I will loose them -- oh well
        // Also, I will lose any attributes and namesapces on the fragment -- DOM can
        // have attrs in fragments

        if (Locale.isFragmentQName(c.getName())) {
            _nodeCur.moveTo(null, Cur.NO_POS);
        }

        ensureDoc();

        _nodeCur.createElement(getQualifiedName(c, c.getName()));
        _nodeCur.next();

        for (iterateMappings(); hasMapping(); nextMapping()) {
            _nodeCur.createAttr(_nodeCur._locale.createXmlns(mappingPrefix()));
            _nodeCur.next();
            _nodeCur.insertString(mappingUri());
            _nodeCur.toParent();
            _nodeCur.skipWithAttrs();
        }

        for (int i = 0; i < attrNames.size(); i++) {
            _nodeCur.createAttr(getQualifiedName(c, (QName) attrNames.get(i)));
            _nodeCur.next();
            _nodeCur.insertString((String) attrValues.get(i));
            _nodeCur.toParent();
            _nodeCur.skipWithAttrs();
        }

        return false;
    }

    protected void emitFinish(SaveCur c) {
        if (!Locale.isFragmentQName(c.getName())) {
            assert _nodeCur.isEnd();
            _nodeCur.next();
        }
    }

    protected void emitText(SaveCur c) {
        ensureDoc();

        Object src = c.getChars();

        if (c._cchSrc > 0) {
            _nodeCur.insertChars(src, c._offSrc, c._cchSrc);
            _nodeCur.next();
        }
    }

    protected void emitComment(SaveCur c) {
        ensureDoc();

        _nodeCur.createComment();
        emitTextValue(c);
        _nodeCur.skip();
    }

    protected void emitProcinst(SaveCur c) {
        ensureDoc();

        _nodeCur.createProcinst(c.getName().getLocalPart());
        emitTextValue(c);
        _nodeCur.skip();
    }

    protected void emitDocType(String docTypeName, String publicId, String systemId) {
        ensureDoc();

        XmlDocumentProperties props = Locale.getDocProps(_nodeCur, true);
        props.setDoctypeName(docTypeName);
        props.setDoctypePublicId(publicId);
        props.setDoctypeSystemId(systemId);
    }

    protected void emitStartDoc(SaveCur c) {
        ensureDoc();
    }

    protected void emitEndDoc(SaveCur c) {
    }

    private QName getQualifiedName(SaveCur c, QName name) {
        String uri = name.getNamespaceURI();

        String prefix = uri.length() > 0 ? getUriMapping(uri) : "";

        if (prefix.equals(name.getPrefix())) {
            return name;
        }

        return _nodeCur._locale.makeQName(uri, name.getLocalPart(), prefix);
    }

    private void emitTextValue(SaveCur c) {
        c.push();
        c.next();

        if (c.isText()) {
            _nodeCur.next();
            _nodeCur.insertChars(c.getChars(), c._offSrc, c._cchSrc);
            _nodeCur.toParent();
        }

        c.pop();
    }

    private void ensureDoc() {
        if (!_nodeCur.isPositioned()) {
            if (_isFrag) {
                _nodeCur.createDomDocFragRoot();
            } else {
                _nodeCur.createDomDocumentRoot();
            }

            _nodeCur.next();
        }
    }

}

