/*
 *   Copyright 2004 The Apache Software Foundation
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

package misc.detailed;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/* Test class loading using XmlBeans.getContextLoader() after changes to SystemCache.java (r240333)
*  Now a custom implementation of the SystemCache can be provided
*/
public class SystemCacheClassloadersTest {

    @Test
    public void testSystemCacheAndThreadLocal()
    {
        Thread testThread = new SystemCacheThread("SchemTypeLoader Test Thread");

        try {
            testThread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            try {
                testThread.join();
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

    }

    public static class SystemCacheThread extends Thread
    {
        private String name;

        SystemCacheThread(String threadName)
        {
            super();
            name = threadName;
        }

        /**
         */
        public void run()
        {
            System.out.println("Run Method of thread " + name);

            try {

                // test classloading from 2 different scomp jars using the default impl of SystemCache
                testDefaultSystemCacheClassLoading();

            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }

        void testDefaultSystemCacheClassLoading()
        {
            try {
                // create classloaders here
                String xbean_home = System.getProperty("xbean.rootdir");
                if (xbean_home == null) {
                    xbean_home = new File(".").getAbsolutePath();
                }

                String[] domPaths = {
                    "build/classes",
                    "build/test-syscache/2/classes",
                    "build/test-syscache/2/generated-resources"
                };

                List<URL> domUrls = new ArrayList<URL>();
                for (String p : domPaths) {
                    domUrls.add(new File(xbean_home, p).toURI().toURL());
                }

                String[] miscPaths = {
                    "build/classes",
                    "build/test-syscache/1/classes",
                    "build/test-syscache/1/generated-resources"
                };

                List<URL> miscUrls = new ArrayList<URL>();
                for (String p : miscPaths) {
                    miscUrls.add(new File(xbean_home, p).toURI().toURL());
                }


                URLClassLoader domCL = new URLClassLoader(domUrls.toArray(new URL[0]));
                URLClassLoader miscCL = new URLClassLoader(miscUrls.toArray(new URL[0]));

                // define the Qnames of types to look for in the compiled xbeans after switching the class loaders
                QName domTypeQName = new QName("http://xbean/misc/SyscacheTests2", "elementT");
                QName miscPersonTypeQName = new QName("http://xbean/misc/SyscacheTests1", "personType", "test");

                setContextClassLoader(domCL);
                //System.out.println("Testing elementT Type From dom tests complexTypeTest.xsd");
                SchemaTypeLoader initialDomLoader = XmlBeans.getContextTypeLoader();
                SchemaType domSchemaType = initialDomLoader.findType(domTypeQName);
                assertNotNull(domSchemaType);
                assertEquals("Invalid Type!", domSchemaType.getFullJavaImplName(), "xbean.misc.syscacheTests2.impl.ElementTImpl");

                // -ve test, look for the person type from cases\misc\syscachetest.xsd
                SchemaType personTypeFromMiscTests = initialDomLoader.findType(miscPersonTypeQName);
                assertNull(personTypeFromMiscTests);

                // switch the SchemaTypeLoader
                setContextClassLoader(miscCL);
                //System.out.println("Testing Person Type From misc syscachetests.xsd");
                SchemaTypeLoader initialMiscSchemaLoader = XmlBeans.getContextTypeLoader();
                SchemaType miscPersonType = initialMiscSchemaLoader.findType(miscPersonTypeQName);
                assertNotNull(miscPersonType);
                assertEquals("Invalid Type!", miscPersonType.getFullJavaImplName(), "xbean.misc.syscacheTests1.impl.PersonTypeImpl");

                // -ve test
                SchemaType personTypeFromMisc = initialMiscSchemaLoader.findType(domTypeQName);
                assertNull(personTypeFromMisc);

                // reload the original loader
                setContextClassLoader(domCL);
                SchemaTypeLoader secondDomLoader = XmlBeans.getContextTypeLoader();
                assertNotNull(secondDomLoader.findType(domTypeQName));
                assertSame("SchemaTypeLoaders expected to be equal", initialDomLoader, secondDomLoader);

                setContextClassLoader(miscCL);
                SchemaTypeLoader secondMiscLoader = XmlBeans.getContextTypeLoader();
                assertSame("SchemaTypeLoaders expected to be equal", initialMiscSchemaLoader, secondMiscLoader);

            }
            catch (Throwable t) {
                t.printStackTrace();
            }

        }


    }

}
