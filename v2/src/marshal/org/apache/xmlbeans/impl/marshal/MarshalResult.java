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
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.Collection;


abstract class MarshalResult
{
    //per binding context constants
    protected final BindingLoader bindingLoader;
    protected final RuntimeBindingTypeTable typeTable;

    //state fields
    private final Collection errors;
    private int prefixCnt = 0;

    private static final String NSPREFIX = "n";


    //TODO: REVIEW: consider ways to reduce the number of parameters here
    MarshalResult(BindingLoader loader,
                  RuntimeBindingTypeTable tbl,
                  XmlOptions options)
        throws XmlException
    {
        bindingLoader = loader;
        typeTable = tbl;
        errors = BindingContextImpl.extractErrorHandler(options);
    }

    protected void resetPrefixCount()
    {
        prefixCnt = 0;
    }

    public abstract NamespaceContext getNamespaceContext();

    protected String ensurePrefix(String uri)
        throws XmlException
    {
        assert uri != null;  //QName's should use "" for no namespace
        assert (uri.length() > 0);

        String prefix = getNamespaceContext().getPrefix(uri);
        if (prefix == null) {
            prefix = bindNextPrefix(uri);
        }
        assert prefix != null;
        return prefix;
    }

    private String bindNextPrefix(final String uri)
        throws XmlException
    {
        String prefix = findNextPrefix(uri);
        bindNamespace(prefix, uri);
        return prefix;
    }

    protected final String findNextPrefix(final String uri)
    {
        assert uri != null;
        String testuri;
        String prefix;
        do {
            prefix = NSPREFIX + (++prefixCnt);
            testuri = getNamespaceContext().getNamespaceURI(prefix);
        } while (testuri != null);
        assert prefix != null;
        return prefix;
    }

    protected abstract void bindNamespace(String prefix, String uri)
        throws XmlException;


    void addXsiNilAttribute()
        throws XmlException
    {
        addAttribute(MarshalStreamUtils.XSI_NS,
                     MarshalStreamUtils.XSI_NIL_ATTR,
                     ensurePrefix(MarshalStreamUtils.XSI_NS),
                     NamedXmlTypeVisitor.TRUE_LEX);
    }

    final void addXsiTypeAttribute(RuntimeBindingType rtt)
        throws XmlException
    {
        final QName schema_type = rtt.getSchemaTypeName();
        final String type_uri = schema_type.getNamespaceURI();

        //TODO: what about types from a schema with no targetNamespace??
        assert type_uri != null;
        assert type_uri.length() > 0;

        final String aval =
            XsTypeConverter.getQNameString(type_uri,
                                           schema_type.getLocalPart(),
                                           ensurePrefix(type_uri));

        addAttribute(MarshalStreamUtils.XSI_NS,
                     MarshalStreamUtils.XSI_TYPE_ATTR,
                     ensurePrefix(MarshalStreamUtils.XSI_NS),
                     aval);
    }


    void fillAndAddAttribute(QName qname_without_prefix,
                             String value)
        throws XmlException
    {
        final String uri = qname_without_prefix.getNamespaceURI();
        if (uri.length() == 0) {
            addAttribute(qname_without_prefix.getLocalPart(), value);
        } else {
            addAttribute(uri, qname_without_prefix.getLocalPart(),
                         ensurePrefix(uri), value);
        }
    }


    protected abstract void addAttribute(String lname,
                                         String value)
        throws XmlException;

    protected abstract void addAttribute(String uri,
                                         String lname,
                                         String prefix,
                                         String value)
        throws XmlException;


    RuntimeBindingType determineRuntimeBindingType(RuntimeBindingType expected,
                                                   Object instance)
        throws XmlException
    {
        if (instance == null || !expected.canHaveSubtype()) {
            return expected;
        }

        final Class instance_class = instance.getClass();
        if (instance_class.equals(expected.getJavaType())) {
            return expected;
        }

        final BindingTypeName type_name = expected.getBindingType().getName();
        //TODO: avoid expensive comparison here by having RuntimeBindingType
        //cache stuff to make it faster.
        if (!type_name.getJavaName().isNameForClass(instance_class)) {
            //NOTE: lookupBindingType will go up the type heirarchy
            final BindingType actual_type =
                MarshallerImpl.lookupBindingType(instance_class,
                                                 type_name.getJavaName(),
                                                 type_name.getXmlName(),
                                                 bindingLoader);
            if (actual_type != null) {
                return typeTable.createRuntimeType(actual_type, bindingLoader);
            }
            //else go with original type and hope for the best...
        }
        return expected;
    }


    Collection getErrorCollection()
    {
        return errors;
    }

}
