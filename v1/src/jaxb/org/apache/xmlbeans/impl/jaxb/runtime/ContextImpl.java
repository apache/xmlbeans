/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package org.apache.xmlbeans.impl.jaxb.runtime;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.values.TypeStoreFactory;
import org.apache.xmlbeans.impl.values.TypeStoreUser;

import org.apache.xmlbeans.impl.jaxb.compiler.ElementInfo;
import org.apache.xmlbeans.impl.jaxb.compiler.TypeInfo;

import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument;
import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument.JaxbConfig;
import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument.JaxbConfig.GlobalElements;
import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument.JaxbConfig.GlobalTypes;
import org.apache.xmlbeans.impl.jaxb.config.GlobalElement;
import org.apache.xmlbeans.impl.jaxb.config.GlobalType;

public class ContextImpl extends JAXBContext implements TypeStoreFactory
{
    private SchemaTypeLoader _stl;
    private Map _globalElements;

    public javax.xml.bind.Unmarshaller createUnmarshaller() 
    {
        return new UnmarshallerImpl(_stl, this);
    }
    public javax.xml.bind.Marshaller createMarshaller() 
    {
        return new MarshallerImpl();
    }
    public javax.xml.bind.Validator createValidator() 
    {
        return new ValidatorImpl();
    }

    public TypeStoreUser createElementUser(SchemaType parentType, QName name, QName xsiType) 
    {
        if (parentType.isDocumentType())
        {
            ElementInfo ei = (ElementInfo)_globalElements.get(name);
            if (ei == null) return null;
            try {
                Constructor ctr = ei.getJavaImplConstructor(parentType.getTypeSystem().getClassLoader());
                return (TypeStoreUser)ctr.newInstance(null);
            }
            catch (Exception e)
            {
                return null;
            }
        }

        return null;
        
    }

    public TypeStoreUser createAttributeUser(SchemaType parentType, QName name) 
    {
        return null;
    }

    ContextImpl(SchemaTypeLoader stl, Map globalElements)
    {
        _stl = stl;
        _globalElements = globalElements;
    }

    public static JAXBContext createContext(String contextPath, ClassLoader classLoader)
        throws JAXBException
    {
        DatatypeConverter.setDatatypeConverter(
            DatatypeConverterImpl.instance);


        String[] pkgs = contextPath.split(":");

        SchemaTypeLoader[] stls = new SchemaTypeLoader[pkgs.length + 1];
        stls[0] = XmlBeans.getBuiltinTypeSystem();

        Map globalElements = new HashMap();

        for (int i = 0 ; i < pkgs.length ; i++)
        {
            try {
                String props = pkgs[i].replace('.', '/') + "/jaxb.properties";
                Properties p = new Properties();

                p.load(classLoader.getResourceAsStream(props));
                String indexClassName = p.getProperty("org.apache.xmlbeans.impl.jaxb.TypeSystemHolder");

                if (indexClassName == null)
                    throw new JAXBException("This package was not created with this Jaxb impl.");

                Field f = Class.forName(indexClassName, false, classLoader).getField("typeSystem");
                SchemaTypeSystem sts = (SchemaTypeSystem)f.get(null);
                stls[i + 1] = sts;

                // Now load the Jaxb Config info
                loadJaxbConfig(classLoader, sts, globalElements);
            }
            catch (IOException e)
            {
                throw new JAXBException("", e);
            }
            catch (ClassNotFoundException e)
            {
                throw new JAXBException("This package was not created with this jaxb impl.",  e);
            }
            catch (NoSuchFieldException e)
            {
                throw new JAXBException("This package was not created with this jaxb impl.",  e);
            }
            catch (IllegalAccessException e)
            {
                throw new JAXBException("This package was not created with this jaxb impl.",  e);
            }
            catch (XmlException e)
            {
                throw new JAXBException("This package was not created with this jaxb impl.",  e);
            }

        }

        return new ContextImpl(XmlBeans.typeLoaderUnion(stls), globalElements);

    }

    static void loadJaxbConfig(ClassLoader cl, SchemaTypeSystem sts, Map globalElements)
        throws IOException, XmlException
    {
        String filename = sts.getName().replace('.', '/') + "/JaxbConfig.xml";
        InputStream is = cl.getResourceAsStream(filename);

        JaxbConfig config = JaxbConfigDocument.Factory.parse(is).getJaxbConfig();
        GlobalElement[] elts = config.getGlobalElements().getGlobalElementArray();

        for (int i = 0 ; i < elts.length ; i++)
        {
            ElementInfo ei = new ElementInfo();
            ei.setFullJavaImplName(elts[i].getJavaImpl());
            globalElements.put(elts[i].getQname(), ei);
        }

    }
}
