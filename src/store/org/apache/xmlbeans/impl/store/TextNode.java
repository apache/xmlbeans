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

import org.w3c.dom.Text;

class TextNode extends CharNode implements Text {
    TextNode(Locale l) {
        super(l);
    }

    public int nodeType() {
        return DomImpl.TEXT;
    }

    public String name() {
        return "#text";
    }

    public Text splitText(int offset) {
        return DomImpl._text_splitText(this, offset);
    }

    public String getWholeText() {
        return DomImpl._text_getWholeText(this);
    }

    public boolean isElementContentWhitespace() {
        return DomImpl._text_isElementContentWhitespace(this);
    }

    public Text replaceWholeText(String content) {
        return DomImpl._text_replaceWholeText(this, content);
    }
}
