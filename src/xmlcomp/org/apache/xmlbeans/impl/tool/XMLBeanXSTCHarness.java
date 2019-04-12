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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;

import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.io.PrintWriter;
import java.io.StringWriter;

public class XMLBeanXSTCHarness implements XSTCTester.Harness
{
    public void runTestCase(XSTCTester.TestCaseResult result)
    {
        XSTCTester.TestCase testCase = result.getTestCase();
        
        // System.out.println("Running case " + testCase.getDescription());
        
        try
        {
            Collection errors = new ArrayList();
            boolean schemaValid = true;
            boolean instanceValid = true;
            
            if (testCase.getSchemaFile() == null)
                return;
            
            // step 1, load schema file etc.
            SchemaTypeLoader loader = null;
            try {
                SchemaTypeSystemCompiler.Parameters params = new SchemaTypeSystemCompiler.Parameters();
                XmlOptions options = new XmlOptions().setErrorListener(errors).setLoadLineNumbers();
                XmlObject schema = XmlObject.Factory.parse(testCase.getSchemaFile(), options);
                if (testCase.getResourceFile() != null) {
                    params.setInputXmls(schema, XmlObject.Factory.parse(testCase.getResourceFile(), options));
                } else {
                    params.setInputXmls(schema);
                }
                params.setLinkTo(XmlBeans.getBuiltinTypeSystem());
                params.setOptions(options);
                params.setClassesDir(new File("build/xstctest/"));

                SchemaTypeSystem system = XmlBeans.compileXmlBeans(params);
                loader = XmlBeans.typeLoaderUnion(system, XmlBeans.getBuiltinTypeSystem());
            }
            catch (Exception e)
            {
                schemaValid = false;
                if (!(e instanceof XmlException) || errors.isEmpty())
                {
                    result.setCrash(true);
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    result.addSvMessages(Collections.singleton(sw.toString()));
                }
            }
            
            result.addSvMessages(errors);
            result.setSvActual(schemaValid);
            errors.clear();
            
            if (loader == null)
                return;
            
            if (testCase.getInstanceFile() == null)
                return;
            
            // step 2, load instance file and validate
            try
            {
                XmlObject instance = loader.parse(testCase.getInstanceFile(), null, new XmlOptions().setErrorListener(errors).setLoadLineNumbers());
                if (!instance.validate(new XmlOptions().setErrorListener(errors)))
                    instanceValid = false;
            }
            catch (Exception e)
            {
                instanceValid = false;
                if (!(e instanceof XmlException) || errors.isEmpty())
                {
                    result.setCrash(true);
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    result.addIvMessages(Collections.singleton(sw.toString()));
                }
            }
            result.addIvMessages(errors);
            result.setIvActual(instanceValid);
                    
        }
        finally
        {
            // System.out.println(result.succeeded() ? "Success.": "Failure.");
        }
    }
}
