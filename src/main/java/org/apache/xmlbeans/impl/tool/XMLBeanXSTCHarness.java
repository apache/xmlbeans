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

import org.apache.xmlbeans.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class XMLBeanXSTCHarness implements XSTCTester.Harness {
    public void runTestCase(XSTCTester.TestCaseResult result) {
        XSTCTester.TestCase testCase = result.getTestCase();

        Collection<XmlError> errors = new ArrayList<>();
        boolean schemaValid = true;
        boolean instanceValid = true;

        if (testCase.getSchemaFile() == null) {
            return;
        }

        // step 1, load schema file etc.
        SchemaTypeLoader loader = null;
        try {
            XmlObject schema = XmlObject.Factory.parse(testCase.getSchemaFile(), new XmlOptions().setErrorListener(errors).setLoadLineNumbers());
            XmlObject schema2 = null;
            if (testCase.getResourceFile() != null) {
                schema2 = XmlObject.Factory.parse(testCase.getResourceFile(), new XmlOptions().setErrorListener(errors).setLoadLineNumbers());
            }
            XmlObject[] schemas = schema2 == null ? new XmlObject[]{schema} : new XmlObject[]{schema, schema2};
            SchemaTypeSystem system = XmlBeans.compileXsd(schemas, XmlBeans.getBuiltinTypeSystem(), new XmlOptions().setErrorListener(errors));
            loader = XmlBeans.typeLoaderUnion(system, XmlBeans.getBuiltinTypeSystem());
        } catch (Exception e) {
            schemaValid = false;
            if (!(e instanceof XmlException) || errors.isEmpty()) {
                result.setCrash(true);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                result.addSvMessages(Collections.singleton(XmlError.forMessage(sw.toString())));
            }
        }

        result.addSvMessages(errors);
        result.setSvActual(schemaValid);
        errors.clear();

        if (loader == null) {
            return;
        }

        if (testCase.getInstanceFile() == null) {
            return;
        }

        // step 2, load instance file and validate
        try {
            XmlObject instance = loader.parse(testCase.getInstanceFile(), null, new XmlOptions().setErrorListener(errors).setLoadLineNumbers());
            if (!instance.validate(new XmlOptions().setErrorListener(errors))) {
                instanceValid = false;
            }
        } catch (Exception e) {
            instanceValid = false;
            if (!(e instanceof XmlException) || errors.isEmpty()) {
                result.setCrash(true);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                result.addIvMessages(Collections.singleton(XmlError.forMessage(sw.toString())));
            }
        }
        result.addIvMessages(errors);
        result.setIvActual(instanceValid);

    }
}
