/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.JavaName;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.XmlName;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

/**
 * Entry point for marshalling java objects to xml.
 */
class MarshallerImpl
    implements Marshaller
{
    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;


    /*package*/
    MarshallerImpl(BindingLoader bindingLoader,
                   RuntimeBindingTypeTable typeTable)
    {
        this.bindingLoader = bindingLoader;
        this.typeTable = typeTable;
    }

    public XMLStreamReader marshall(Object obj,
                                    NamespaceContext nscontext)
    {
        final JavaName jname = JavaName.forString(obj.getClass().getName());
        BindingTypeName root_elem_btype = bindingLoader.lookupElementFor(jname);
        if (root_elem_btype == null) {
            final String msg = "failed to find root " +
                "element corresponding to " + jname;
            throw new XmlRuntimeException(msg);
        }

        final XmlName elem = root_elem_btype.getXmlName();
        assert elem.getComponentType() == XmlName.ELEMENT;
        final QName elem_qn = elem.getQName();

        //get type for this element/object pair
        final BindingTypeName type_name = bindingLoader.lookupTypeFor(jname);
        final BindingType btype = bindingLoader.getBindingType(type_name);

        RuntimeGlobalProperty prop = new RuntimeGlobalProperty(btype, elem_qn);

        final ArrayList errors = new ArrayList();
        MarshalContext ctx = new MarshalContext(nscontext, bindingLoader,
                                                typeTable, errors);

        return new MarshalResult(prop, obj, ctx);
    }

    public XMLStreamReader marshallType(Object obj,
                                        QName elementName,
                                        QName schemaType,
                                        String javaType,
                                        MarshalContext context)
        throws XmlException
    {
        //TODO: assert that the passed in context has the same loader and typetable as we do.
        //TODO: REVIEW: should we move this method to the context?
        BindingType type = determineBindingType(obj, schemaType, javaType);
        RuntimeGlobalProperty prop = new RuntimeGlobalProperty(type, elementName);
        return new MarshalResult(prop, obj, context);
    }

    private BindingType determineBindingType(Object obj,
                                             QName schemaType,
                                             String javaType)
    {
        //TODO: consult object when needed for polymorphism
        BindingType binding_type = lookupBindingType(javaType, schemaType,
                                                     bindingLoader);

        return binding_type;
    }

    private static BindingType lookupBindingType(String javaType,
                                                 QName schemaType,
                                                 BindingLoader loader)
    {
        JavaName jname = JavaName.forString(javaType);
        XmlName xname = XmlName.forTypeNamed(schemaType);
        BindingTypeName btname = BindingTypeName.forPair(jname, xname);
        if (btname == null) {
            final String msg = "failed to find type corresponding to " + btname;
            throw new XmlRuntimeException(msg);
        }

        final BindingType binding_type = loader.getBindingType(btname);
        if (binding_type == null) {
            final String msg = "failed to load type " + btname;
            throw new XmlRuntimeException(msg);
        }
        return binding_type;
    }

}
