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

package org.apache.xmlbeans.impl.jaxb.compiler;

import java.util.*;
import java.io.*;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;

import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument;
import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument.JaxbConfig;
import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument.JaxbConfig.GlobalElements;
import org.apache.xmlbeans.impl.jaxb.config.JaxbConfigDocument.JaxbConfig.GlobalTypes;
import org.apache.xmlbeans.impl.jaxb.config.GlobalElement;
import org.apache.xmlbeans.impl.jaxb.config.GlobalType;

public final class JaxbCodeGenerator
{
    public static boolean compile(SchemaTypeSystem sts, List sourcefiles, File sourcedir, File classesdir, XmlErrorWatcher errors)
    {
        Map componentData = new LinkedHashMap();
        Map packageData = new LinkedHashMap();
        boolean failure = false;

        if ( ! JaxbJavaizer.javaize(sts, componentData, packageData, errors) )
            return false;

        JaxbConfigDocument doc = JaxbConfigDocument.Factory.newInstance();
        JaxbConfig root = doc.addNewJaxbConfig();
        GlobalElements elts = root.addNewGlobalElements();
        GlobalTypes types = root.addNewGlobalTypes();

        for (Iterator it = packageData.values().iterator() ; it.hasNext() ; )
        {
            PackageInfo pi = (PackageInfo) it.next();

            Writer writer = null;
            try 
            {
                // First construct the jaxb.properties file for this package
                String filename = pi.getPackageName().replace('.', '/') + "/jaxb.properties";
                File sourcefile = new File(classesdir, filename);
                sourcefile.getParentFile().mkdirs();

                writer = new FileWriter(sourcefile);
                JaxbCodePrinter.printPackageProperties(writer, pi);
                writer.close();
                writer = null;
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
                failure = true;
            }
            finally {
                try { if (writer != null) writer.close(); } catch (IOException e) {}
            }

            // TODO: generate ObjectFactory class


            // Generate the interfaces & implementations for the schema components
            List allComponents = new ArrayList();
            allComponents.addAll(Arrays.asList(sts.globalElements()));
            allComponents.addAll(Arrays.asList(sts.globalTypes()));

            for (int i = 0, len = allComponents.size() ; i < len ; i++)
            {
                SchemaComponent sc = (SchemaComponent)allComponents.get(i);
                ComponentInfo ci = (ComponentInfo)componentData.get(sc);

                addComponentToConfig(sc, ci, root);

                assert ci != null;

                String filename = ci.getFullJavaIntfName().replace('.', '/') + ".java";

                // print the java interface

                try {
                    File sourcefile = new File(sourcedir,  filename);
                    sourcefile.getParentFile().mkdirs();

                    writer = new FileWriter(sourcefile);
                    JaxbCodePrinter.printJavaInterface(writer, sc, ci);
                    writer.close();
                    writer = null;
                    sourcefiles.add(sourcefile);
                }
                catch (IOException e)
                {
                    System.err.println("IO Error " + e);
                    failure = true;
                }
                finally {
                    try { if (writer != null) writer.close(); } catch (IOException e) {}
                }

                // print the java impl


                filename = ci.getFullJavaImplName().replace('.', '/') + ".java";
                try {
                    File sourcefile = new File(sourcedir,  filename);
                    sourcefile.getParentFile().mkdirs();

                    writer = new FileWriter(sourcefile);
                    JaxbCodePrinter.printJavaImpl(writer, sc, ci);
                    writer.close();
                    writer = null;
                    sourcefiles.add(sourcefile);
                }
                catch (IOException e)
                {
                    System.err.println("IO Error " + e);
                    failure = true;
                }
                finally {
                    try { if (writer != null) writer.close(); } catch (IOException e) {}
                }
            }

        }

        // Save out the jaxb config file

        OutputStream os = null;
        try 
        {
            // First construct the jaxb.properties file for this package
            String filename = sts.getName().replace('.', '/') + "/JaxbConfig.xml";
            File sourcefile = new File(classesdir, filename);
            sourcefile.getParentFile().mkdirs();

            os = new FileOutputStream(sourcefile);
            doc.save(os);
            os.close();
            os = null;
        }
        catch (IOException e)
        {
            System.err.println("IO Error " + e);
            failure = true;
        }
        finally {
            try { if (os != null) os.close(); } catch (IOException e) {}
        }

        return failure;

    }

    static void addComponentToConfig(SchemaComponent sc, ComponentInfo ci, JaxbConfig config)
    {
        switch (sc.getComponentType())
        {
            case SchemaComponent.ELEMENT:
                GlobalElement elt = config.getGlobalElements().addNewGlobalElement();
                elt.setQname(sc.getName());
                elt.setJavaImpl(ci.getFullJavaImplName());
                break;
            default:
                throw new IllegalStateException();
        }

    }

}
