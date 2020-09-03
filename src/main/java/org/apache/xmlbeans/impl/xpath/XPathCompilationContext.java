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

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.common.XMLChar;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.apache.xmlbeans.impl.xpath.XPath._NS_BOUNDARY;

class XPathCompilationContext {
    private String _expr;

    private boolean _sawDeepDot;  // Saw one overall
    private boolean _lastDeepDot;

    private final String _currentNodeVar;

    // private Map _namespaces;
    protected final Map<String, String> _namespaces = new HashMap<>();
    private final Map<String, String> _externalNamespaces;

    private int _offset;
    private int _line;
    private int _column;

    XPathCompilationContext(Map<String, String> namespaces, String currentNodeVar) {
        // TODO: checkme
        // assert (_currentNodeVar == null || _currentNodeVar.startsWith("$"));

        _currentNodeVar = (currentNodeVar == null) ? "$this" : currentNodeVar;

        _externalNamespaces = (namespaces == null) ? new HashMap<>() : namespaces;
    }

    XPath compile(String expr) throws XPath.XPathCompileException {
        _offset = 0;
        _line = 1;
        _column = 1;
        _expr = expr;

        return tokenizeXPath();
    }

    int currChar() {
        return currChar(0);
    }

    int currChar(int offset) {
        return
            _offset + offset >= _expr.length()
                ? -1
                : _expr.charAt(_offset + offset);
    }

    void advance() {
        if (_offset < _expr.length()) {
            char ch = _expr.charAt(_offset);

            _offset++;
            _column++;

            if (ch == '\r' || ch == '\n') {
                _line++;
                _column = 1;

                if (_offset + 1 < _expr.length()) {
                    char nextCh = _expr.charAt(_offset + 1);

                    if ((nextCh == '\r' || nextCh == '\n') && ch != nextCh) {
                        _offset++;
                    }
                }
            }
        }
    }

    void advance(int count) {
        assert count >= 0;

        while (count-- > 0) {
            advance();
        }
    }

    boolean isWhitespace() {
        return isWhitespace(0);
    }

    boolean isWhitespace(int offset) {
        int ch = currChar(offset);
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    boolean isNCNameStart() {
        return currChar() != -1 && XMLChar.isNCNameStart(currChar());
    }

    boolean isNCName() {
        return currChar() != -1 && XMLChar.isNCName(currChar());
    }

    boolean startsWith(String s, int offset) {
        if (_offset + offset >= _expr.length()) {
            return false;
        }

        return _expr.startsWith(s, _offset + offset);
    }

    private XPath.XPathCompileException newError(String msg) {
        XmlError err =
            XmlError.forLocation(
                msg, XmlError.SEVERITY_ERROR, null,
                _line, _column, _offset);

        return new XPath.XPathCompileException(err);
    }

    String lookupPrefix(String prefix) throws XPath.XPathCompileException {
        if (_namespaces.containsKey(prefix)) {
            return _namespaces.get(prefix);
        }

        if (_externalNamespaces.containsKey(prefix)) {
            return _externalNamespaces.get(prefix);
        }

        switch (prefix != null ? prefix : "") {
            case "xml":
                return "http://www.w3.org/XML/1998/namespace";

            case "xs":
                return "http://www.w3.org/2001/XMLSchema";

            case "xsi":
                return "http://www.w3.org/2001/XMLSchema-instance";

            case "fn":
                return "http://www.w3.org/2002/11/xquery-functions";

            case "xdt":
                return "http://www.w3.org/2003/11/xpath-datatypes";

            case "local":
                return "http://www.w3.org/2003/11/xquery-local-functions";
        }

        throw newError("Undefined prefix: " + prefix);
    }

    private boolean parseWhitespace() {
        boolean sawSpace = false;

        while (isWhitespace()) {
            advance();
            sawSpace = true;
        }

        return sawSpace;
    }

    //
    // Tokenizing will consume whitespace followed by the tokens, separated
    // by whitespace.  The whitespace following the last token is not
    // consumed.
    //
    private boolean tokenize(String... tokens) {
        int offset = 0;

        for (String s : tokens) {
            assert (s != null && !s.isEmpty());

            while (isWhitespace(offset)) {
                offset++;
            }

            if (!startsWith(s, offset)) {
                return false;
            }

            offset += s.length();
        }

        advance(offset);

        return true;
    }


    private String tokenizeNCName() throws XPath.XPathCompileException {
        parseWhitespace();

        if (!isNCNameStart()) {
            throw newError("Expected non-colonized name");
        }

        StringBuilder sb = new StringBuilder();

        sb.append((char) currChar());

        for (advance(); isNCName(); advance()) {
            sb.append((char) currChar());
        }

        return sb.toString();
    }

    private QName getAnyQName() {
        return new QName("", "");
    }

    private QName tokenizeQName() throws XPath.XPathCompileException {
        if (tokenize("*")) {
            return getAnyQName();
        }

        String ncName = tokenizeNCName();

        if (!tokenize(":")) {
            return new QName(lookupPrefix(""), ncName);
        }

        return
            new QName(
                lookupPrefix(ncName),
                tokenize("*") ? "" : tokenizeNCName());
    }

    private String tokenizeQuotedUri() throws XPath.XPathCompileException {
        char quote;

        if (tokenize("\"")) {
            quote = '"';
        } else if (tokenize("'")) {
            quote = '\'';
        } else {
            throw newError("Expected quote (\" or ')");
        }

        StringBuilder sb = new StringBuilder();

        for (; ; ) {
            if (currChar() == -1) {
                throw newError("Path terminated in URI literal");
            }

            if (currChar() == quote) {
                advance();

                if (currChar() != quote) {
                    break;
                }
            }

            sb.append((char) currChar());

            advance();
        }

        return sb.toString();
    }

    private XPathStep addStep(boolean deep, boolean attr, QName name, XPathStep steps) {
        XPathStep step = new XPathStep(deep, attr, name);

        if (steps == null) {
            return step;
        }

        XPathStep s = steps;

        while (steps._next != null) {
            steps = steps._next;
        }

        steps._next = step;
        step._prev = steps;

        return s;
    }

    private XPathStep tokenizeSteps() throws XPath.XPathCompileException {
        if (tokenize("/")) {
            throw newError("Absolute paths unsupported");
        }

        boolean deep;

        if (tokenize("$", _currentNodeVar, "//") || tokenize(".", "//")) {
            deep = true;
        } else if (tokenize("$", _currentNodeVar, "/") || tokenize(".", "/")) {
            deep = false;
        } else if (tokenize("$", _currentNodeVar) || tokenize(".")) {
            return addStep(false, false, null, null);
        } else {
            deep = false;
        }

        XPathStep steps = null;

        // Compile the steps removing /. and mergind //. with the next step

        boolean deepDot = false;

        for (; ; ) {
            if (tokenize("attribute", "::") || tokenize("@")) {
                steps = addStep(deep, true, tokenizeQName(), steps);
                break;
            }

            QName name;

            if (tokenize(".")) {
                deepDot = deepDot || deep;
            } else {
                tokenize("child", "::");
                name = tokenizeQName();
                steps = addStep(deep, false, name, steps);
                deep = false; // only this step needs to be deep
                // other folowing steps will be deep only if they are preceded by // wildcard
            }

            if (tokenize("//")) {
                deep = true;
                deepDot = false;
            } else if (tokenize("/")) {
                if (deepDot) {
                    deep = true;
                }
            } else {
                break;
            }
        }

        // If there was a //. at the end of th path, then we need to make
        // two paths, one with * at the end and another with @* at the end.

        if ((_lastDeepDot = deepDot)) {
            _lastDeepDot = true;
            steps = addStep(true, false, getAnyQName(), steps);
        }

        // Add sentinal step (_name == null)

        return addStep(false, false, null, steps);
    }

    private void computeBacktrack(XPathStep steps) {
        //
        // Compute static backtrack information
        //
        // Note that I use the fact that _hasBacktrack is initialized to
        // false and _backtrack to null in the following code.
        //

        XPathStep s, t;

        for (s = steps; s != null; s = t) {
            // Compute the segment from [ s, t )

            for (t = s._next; t != null && !t._deep; ) {
                t = t._next;
            }

            // If the segment is NOT rooted at //, then the backtrack is
            // null for the entire segment, including possible attr and/or
            // sentinal

            if (!s._deep) {
                for (XPathStep u = s; u != t; u = u._next) {
                    u._hasBacktrack = true;
                }

                continue;
            }

            // Compute the sequence [ s, u ) of length n which contain no
            // wild steps.

            int n = 0;
            XPathStep u = s;

            while (u != t && u._name != null && !u.isWild() && !u._attr) {
                n++;
                u = u._next;
            }

            // Now, apply KMP to [ s, u ) for fast backtracking

            QName[] pattern = new QName[n + 1];
            int[] kmp = new int[n + 1];

            XPathStep v = s;

            for (int i = 0; i < n; i++) {
                pattern[i] = v._name;
                v = v._next;
            }

            pattern[n] = getAnyQName();

            int i = 0;
            int j = kmp[0] = -1;

            while (i < n) {
                while (j > -1 && !pattern[i].equals(pattern[j])) {
                    j = kmp[j];
                }

                i++;
                j++;
                kmp[i] = (pattern[i].equals(pattern[j])) ? kmp[j] : j;
            }

            i = 0;

            for (v = s; v != u; v = v._next) {
                v._hasBacktrack = true;
                v._backtrack = s;

                for (j = kmp[i]; j > 0; j--) {
                    v._backtrack = v._backtrack._next;
                }

                i++;
            }

            // Compute the success backtrack and stuff it into an attr and
            // sentinal if they exist for this segment

            v = s;

            if (n > 1) {
                for (j = kmp[n - 1]; j > 0; j--) {
                    v = v._next;
                }
            }

            if (u != t && u._attr) {
                u._hasBacktrack = true;
                u._backtrack = v;
                u = u._next;
            }

            if (u != t && u._name == null) {
                u._hasBacktrack = true;
                u._backtrack = v;
            }

            // The first part of a deep segment always backtracks to itself

            assert s._deep;

            s._hasBacktrack = true;
            s._backtrack = s;
        }
    }

    private void tokenizePath(ArrayList<XPathStep> paths)
        throws XPath.XPathCompileException {
        _lastDeepDot = false;

        XPathStep steps = tokenizeSteps();

        computeBacktrack(steps);

        paths.add(steps);

        // If the last path ended in //., that path will match all
        // elements, here I make a path which matches all attributes.

        if (_lastDeepDot) {
            _sawDeepDot = true;

            XPathStep s = null;

            for (XPathStep t = steps; t != null; t = t._next) {
                boolean attr = (t._next != null && t._next._next == null) || t._attr;
                s = addStep(t._deep, attr, t._name, s);
            }

            computeBacktrack(s);

            paths.add(s);
        }
    }

    private XPath.Selector tokenizeSelector() throws XPath.XPathCompileException {
        ArrayList<XPathStep> paths = new ArrayList<>();

        tokenizePath(paths);

        while (tokenize("|")) {
            tokenizePath(paths);
        }

        return new XPath.Selector(paths.toArray(new XPathStep[0]));
    }

    private XPath tokenizeXPath() throws XPath.XPathCompileException {
        for (; ; ) {
            if (tokenize("declare", "namespace")) {
                if (!parseWhitespace()) {
                    throw newError("Expected prefix after 'declare namespace'");
                }

                String prefix = tokenizeNCName();

                if (!tokenize("=")) {
                    throw newError("Expected '='");
                }

                String uri = tokenizeQuotedUri();

                if (_namespaces.containsKey(prefix)) {
                    throw newError("Redefinition of namespace prefix: " + prefix);
                }

                _namespaces.put(prefix, uri);

                //return these to saxon:? Is it an error to pass external NS
                //that conflicts? or should we just override it?
                if (_externalNamespaces.containsKey(prefix)) {
                    throw newError("Redefinition of namespace prefix: " + prefix);
                }
                _externalNamespaces.put(prefix, uri);

                if (!tokenize(";")) {
//			            throw newError( "Namespace declaration must end with ;" );
                }

                _externalNamespaces.put(_NS_BOUNDARY, Integer.toString(_offset));

                continue;
            }

            if (tokenize("declare", "default", "element", "namespace")) {
                String uri = tokenizeQuotedUri();

                if (_namespaces.containsKey("")) {
                    throw newError("Redefinition of default element namespace");
                }

                _namespaces.put("", uri);

                //return these to saxon:? Is it an error to pass external NS
                //that conflicts? or should we just override it?
                if (_externalNamespaces.containsKey(XPath._DEFAULT_ELT_NS)) {
                    throw newError("Redefinition of default element namespace : ");
                }
                _externalNamespaces.put(XPath._DEFAULT_ELT_NS, uri);

                if (!tokenize(";")) {
                    throw newError("Default Namespace declaration must end with ;");
                }
                //the boundary is the last ; in the prolog...
                _externalNamespaces.put(_NS_BOUNDARY, Integer.toString(_offset));

                continue;
            }

            break;
        }

        // Add the default prefix mapping if it has not been redefined

        if (!_namespaces.containsKey("")) {
            _namespaces.put("", "");
        }

        XPath.Selector selector = tokenizeSelector();

        parseWhitespace();

        if (currChar() != -1) {
            throw newError("Unexpected char '" + (char) currChar() + "'");
        }

        return new XPath(selector, _sawDeepDot);
    }

    //split of prolog decls that are not standard XPath syntax
    //but work in v1
    private void processNonXpathDecls() {

    }

}

