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
