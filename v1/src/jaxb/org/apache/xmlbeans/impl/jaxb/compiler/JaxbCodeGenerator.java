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
