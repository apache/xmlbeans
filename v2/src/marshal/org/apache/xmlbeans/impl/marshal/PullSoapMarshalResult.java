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

abstract class PullSoapMarshalResult
    extends PullMarshalResult
{
    private final ObjectRefTable objectRefTable;
    private final boolean writingMultiRefdObjs;

    //TODO: REVIEW: consider ways to reduce the number of parameters here
    PullSoapMarshalResult(BindingLoader loader,
                          RuntimeBindingTypeTable tbl,
                          NamespaceContext root_nsctx,
                          RuntimeBindingProperty property,
                          Object obj,
                          XmlOptions options,
                          ObjectRefTable object_ref_table,
                          boolean writingMultiRefdObjs)
        throws XmlException
    {
        super(loader, tbl, root_nsctx, property, obj, options);

        this.objectRefTable = object_ref_table;
        this.writingMultiRefdObjs = writingMultiRefdObjs;
        if (writingMultiRefdObjs) topLevelIdInit(obj);
    }


    protected void reset(RuntimeBindingProperty property,
                         Object obj,
                         boolean writingMultiRefdObjs)
        throws XmlException
    {
        reset(property, obj);
        if (writingMultiRefdObjs) topLevelIdInit(obj);
    }

    private void topLevelIdInit(Object top_lvl_obj)
        throws XmlException
    {
        initAttributes();

        if (objectRefTable != null) {
            int id = objectRefTable.getId(top_lvl_obj);
            if (id >= 0) {
                fillAndAddAttribute(getIdAttributeName(),
                                    Soap11Constants.constructIdValueFromId(id));
            }
        }

    }


    //somewhat ugly way to "un-override" our createVisitor override, since
    //we don't want the special behavior when writing top lvl id parts.
    protected XmlTypeVisitor createInitialVisitor(RuntimeBindingProperty property,
                                                  Object obj)
        throws XmlException
    {
        if (writingMultiRefdObjs)
            return super.createVisitor(property, obj);
        else
            return createVisitor(property, obj);
    }


    protected XmlTypeVisitor createVisitor(RuntimeBindingProperty property,
                                           Object obj)
        throws XmlException
    {
        if (objectRefTable != null) {
            final int id = objectRefTable.getId(obj);
            if (id >= 0) {
                return createRefdObjectVisitor(property, obj, id);
            }
        }

        return super.createVisitor(property, obj);

    }

    protected abstract XmlTypeVisitor createRefdObjectVisitor(RuntimeBindingProperty property,
                                                              Object obj,
                                                              int id)
        throws XmlException;

    protected abstract QName getIdAttributeName();
}
