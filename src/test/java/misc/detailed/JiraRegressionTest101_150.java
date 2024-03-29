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

package misc.detailed;

import misc.common.JiraTestBase;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.Parameters;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 */
public class JiraRegressionTest101_150 extends JiraTestBase
{
    /**
     * [XMLBEANS-103]   XMLBeans - QName thread cache, cause memory leaks
     */
    @Test
    void test_jira_xmlbeans102a() throws Exception{
        // set the parameters similar to those in the bug
        Parameters params = new Parameters();
        params.setXsdFiles(new File(JIRA_CASES + "xmlbeans_102.xsd"));
        params.setOutputJar(new File(outputroot+P+"xmlbeans_102.jar"));
        File outputDir = new File(outputroot + P + "xmlbeans_102");
        outputDir.mkdirs();
        params.setClassesDir(outputDir);
        params.setSrcDir(outputDir);
        // compile schema
        SchemaCompiler.compile(params);
        // check for jar - compilation success
        if(!(new File(outputroot + P + "xmlbeans_102.jar").exists()) )
            throw new Exception("Jar File was not found");
        //cleanup
        deltree(outputroot);
    }

    /*
    * [XMLBEANS-102]: scomp - infinite loop during jar for specific xsd and params for netui_config.xsd
    */
    @Test
    void test_jira_xmlbeans102b() {
        Parameters params = new Parameters();
        params.setOutputJar(new File(schemaCompOutputDirPath + "jira102.jar"));
        params.setClassesDir(schemaCompClassesDir);

        params.setXsdFiles(new File(scompTestFilesRoot + "xmlbeans_102_netui-config.xsd_"));
        List<XmlError> errors = new ArrayList<>();
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);

        SchemaCompiler.compile(params);
        assertFalse(hasSevereError(errors));
    }

    /**
     * NPE while initializing a type system w/ a type that extends
     * an a complex type from a different type system
     */
    @Test
    void test_jira_xmlbeans105() throws Exception {
        //run untyped parse
        XmlObject obj = XmlObject.Factory.parse(new File(JIRA_CASES + "xmlbeans_105.xml"));

        //run Typed Parse
        jira.xmlbeans105.ResourceUnknownFaultDocument rud =
                jira.xmlbeans105.ResourceUnknownFaultDocument.Factory.parse(new File(JIRA_CASES + "xmlbeans_105.xml"));

        // / we know the instance is invalid
        // make sure the error message is what is expected
        rud.validate(xmOpts);
        assertEquals(1, errorList.size(), "More Errors than expected");
        assertEquals(0, ((XmlError) errorList.get(0)).getErrorCode().compareToIgnoreCase("cvc-complex-type.2.4a"), "Did not receive the expected error code: " + ((XmlError) errorList.get(0)).getErrorCode());

    }
}
