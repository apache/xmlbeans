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

import org.apache.xmlbeans.BindingContext;
import org.apache.xmlbeans.MarshalContext;
import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.UnmarshalContext;
import org.apache.xmlbeans.Unmarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Main entry point into marshalling framework.
 * Use the BindingContextFactory to create one
 */
class BindingContextImpl
    implements BindingContext
{

    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;

    /* package protected -- use the factory */
    BindingContextImpl(BindingLoader bindingLoader,
                       RuntimeBindingTypeTable typeTable)
    {
        this.bindingLoader = bindingLoader;
        this.typeTable = typeTable;
    }


    public Unmarshaller createUnmarshaller()
        throws XmlException
    {
        return new UnmarshallerImpl(bindingLoader, typeTable);
    }

    public UnmarshalContext createUnmarshallContext(XMLStreamReader reader)
        throws XmlException

    {
        final ArrayList errors = new ArrayList();
        final UnmarshalContextImpl uc =
            new UnmarshalContextImpl(reader, bindingLoader, typeTable, errors);
        checkErrors(errors, "error creating UnmarshalContext");
        return uc;
    }

    public UnmarshalContext createUnmarshallContext()
        throws XmlException

    {
        final ArrayList errors = new ArrayList();
        final UnmarshalContextImpl unmarshalContext =
            new UnmarshalContextImpl(bindingLoader, typeTable, errors);
        checkErrors(errors, "error creating UnmarshalContext");
        return unmarshalContext;
    }


    public Marshaller createMarshaller()

        throws XmlException
    {
        return new MarshallerImpl(bindingLoader, typeTable);
    }

    public MarshalContext createMarshallContext(NamespaceContext namespaceContext)
        throws XmlException

    {
        final ArrayList errors = new ArrayList();
        final MarshalContextImpl mc = new MarshalContextImpl(namespaceContext,
                                                             bindingLoader,
                                                             typeTable, errors);
        checkErrors(errors, "error creating MarshalContext");
        return mc;
    }


    static void checkErrors(Collection errors, String err_msg)
        throws XmlException
    {
        if (errors.isEmpty()) return;
        throw new XmlException(err_msg, null, errors);
    }

}
