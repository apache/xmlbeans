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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.NamespaceContext;

final class LiteralMarshalResult
    extends MarshalResult
{

    //TODO: REVIEW: consider ways to reduce the number of parameters here
    LiteralMarshalResult(BindingLoader loader,
                      RuntimeBindingTypeTable tbl,
                      NamespaceContext root_nsctx,
                      RuntimeBindingProperty property,
                      Object obj,
                      XmlOptions options)
        throws XmlException
    {
        super(loader, tbl, root_nsctx, property, obj, options);
    }
}
