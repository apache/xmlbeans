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

package org.apache.xmlbeans;

/**
 * Same as {@link XmlOptions} but adhering to JavaBean conventions
 *
 * @deprecated use XmlOptions instead
 */
@Deprecated
public class XmlOptionsBean extends XmlOptions {
    /**
     * Construct a new blank XmlOptions.
     */
    public XmlOptionsBean() {
    }

    /**
     * Construct a new XmlOptions, copying the options.
     *
     * @param other the source <code>XmlOptions</code> object
     */
    public XmlOptionsBean(XmlOptions other) {
        super(other);
    }
}
