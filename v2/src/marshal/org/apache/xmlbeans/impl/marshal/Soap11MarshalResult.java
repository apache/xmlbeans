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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

final class Soap11MarshalResult
    extends PullSoapMarshalResult
{

    private static final QName ID_NAME = new QName("id");

    //TODO: REVIEW: consider ways to reduce the number of parameters here
    Soap11MarshalResult(BindingLoader loader,
                        RuntimeBindingTypeTable tbl,
                        NamespaceContext root_nsctx,
                        RuntimeBindingProperty property,
                        Object obj,
                        XmlOptions options,
                        ObjectRefTable object_ref_table,
                        boolean writingMultiRefdObjs)
        throws XmlException
    {
        super(loader, tbl, root_nsctx, property, obj,
              options, object_ref_table, writingMultiRefdObjs);
    }

    protected XmlTypeVisitor createRefdObjectVisitor(RuntimeBindingProperty property,
                                                     Object obj,
                                                     int id)
        throws XmlException
    {
        return new Soap11RefdObjectVisitor(property, obj, this, id);
    }

    protected QName getIdAttributeName()
    {
        return ID_NAME;
    }


}
