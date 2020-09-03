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

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.store.Cur;
import org.apache.xmlbeans.impl.xpath.Path;
import org.apache.xmlbeans.impl.xpath.XPath;
import org.apache.xmlbeans.impl.xpath.XPathEngine;
import org.apache.xmlbeans.impl.xpath.XPathFactory;

//
// XmlBeans store specific implementation of compiled path
//

public class XmlbeansXPath implements Path {
    private final String _pathKey;
    private final String _currentVar;
    private final XPath _compiledPath;

    public XmlbeansXPath(String pathExpr, String currentVar, XPath xpath) {
        _pathKey = pathExpr;

        _currentVar = currentVar;
        _compiledPath = xpath;
    }

    public XPathEngine execute(Cur c, XmlOptions options) {
        options = XmlOptions.maskNull(options);

        // The builtin XPath engine works only on containers.  Delegate to
        // saxon otherwise.  Also, if the path had a //. at the end, the
        // simple xpath engine can't do the generate case, it only handles
        // attrs and elements.
        if (!c.isContainer() || _compiledPath.sawDeepDot()) {
            Path xpe = XPathFactory.getCompiledPathSaxon(_pathKey, _currentVar, null);
            return xpe.execute(c, options);
        } else {
            return new XmlbeansXPathEngine(_compiledPath, c);
        }
    }

}
