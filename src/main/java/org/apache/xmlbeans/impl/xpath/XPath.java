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
import org.apache.xmlbeans.XmlException;

import java.util.Map;


public class XPath {
    public static final String _NS_BOUNDARY = "$xmlbeans!ns_boundary";
    public static final String _DEFAULT_ELT_NS = "$xmlbeans!default_uri";
    final Selector _selector;
    private final boolean _sawDeepDot;

    public static class XPathCompileException extends XmlException {
        XPathCompileException(XmlError err) {
            super(err.toString(), null, err);
        }
    }

    public static XPath compileXPath(String xpath) throws XPathCompileException {
        return compileXPath(xpath, "$this", null);
    }

    public static XPath compileXPath(String xpath, String currentNodeVar)
    throws XPathCompileException {
        return compileXPath(xpath, currentNodeVar, null);
    }

    public static XPath compileXPath(String xpath, Map<String, String> namespaces)
    throws XPathCompileException {
        return compileXPath(xpath, "$this", namespaces);
    }

    public static XPath compileXPath(String xpath, String currentNodeVar, Map<String, String> namespaces)
    throws XPathCompileException {
        return new XPathCompilationContext(namespaces, currentNodeVar).compile(xpath);
    }

    static final class Selector {
        Selector(XPathStep[] paths) {
            _paths = paths;
        }

        final XPathStep[] _paths;
    }

    XPath(Selector selector, boolean sawDeepDot) {
        _selector = selector;
        _sawDeepDot = sawDeepDot;
    }

    public boolean sawDeepDot() {
        return _sawDeepDot;
    }

}