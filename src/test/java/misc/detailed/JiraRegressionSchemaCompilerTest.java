/*   Copyright 2006 The Apache Software Foundation
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
 *   limitations under the License.
 */
package misc.detailed;

import misc.common.JiraTestBase;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.tool.Parameters;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JiraRegressionSchemaCompilerTest extends JiraTestBase {
    private List<XmlError> _testCompile(File[] xsdFiles, String outputDirName) {
        List<XmlError> errors = new ArrayList<>();
        Parameters params = new Parameters();
        params.setXsdFiles(xsdFiles);
        params.setErrorListener(errors);
        params.setSrcDir(new File(schemaCompOutputDirPath + outputDirName + P + "src"));
        params.setClassesDir(new File(schemaCompOutputDirPath + outputDirName + P + "classes"));
        params.setQuiet(true);
        SchemaCompiler.compile(params);
        return errors;
    }

    private boolean findErrMsg(List<XmlError> errors, String msg) {
        boolean errFound = errors.stream().anyMatch(e -> e.getSeverity() == XmlError.SEVERITY_ERROR && msg.equals(e.getMessage()));
        errors.clear();
        return errFound;
    }

    @Test
    void test_jira_xmlbeans236() {
        File[] xsdFiles = {new File(scompTestFilesRoot + "xmlbeans_236.xsd_")};
        String outputDirName = "xmlbeans_236";
        List<XmlError> errors = _testCompile(xsdFiles, outputDirName);
        assertFalse(hasSevereError(errors),"test_jira_xmlbeans236(): failure when executing scomp");
    }

    @Test
    void test_jira_xmlbeans239() {
        /* complexType with complexContent extending base type with
           simpleContent is valid */
        File[] xsdFiles = {new File(scompTestFilesRoot + "xmlbeans_239a.xsd_")};
        String outputDirName = "xmlbeans_239";
        List<XmlError> errors = _testCompile(xsdFiles, outputDirName);
        assertFalse(hasSevereError(errors),"test_jira_xmlbeans239(): failure when executing scomp");

        /* complexType with complexContent extending simpleType is not valid */
        xsdFiles = new File[]{new File(scompTestFilesRoot + "xmlbeans_239b.xsd_")};
        errors = _testCompile(xsdFiles, outputDirName);
        String msg = "Type 'dtSTRING@http://www.test.bmecat.org' is being used as the base type for a complexContent definition. To do this the base type must be a complex type.";
        assertTrue(findErrMsg(errors, msg));

        /* complexType with complexContent extending base type with
           simpleContent cannot add particles */
        xsdFiles = new File[]{new File(scompTestFilesRoot + "xmlbeans_239c.xsd_")};
        errors = _testCompile(xsdFiles, outputDirName);
        msg = "This type extends a base type 'dtMLSTRING@http://www.test.bmecat.org' which has simpleContent. In that case this type cannot add particles.";
        assertTrue(findErrMsg(errors, msg));
    }

    @Test
    void test_jira_xmlbeans251() {
        File[] xsdFiles = {new File(scompTestFilesRoot + "xmlbeans_251.xsd_")};
        String outputDirName = "xmlbeans_251";
        List<XmlError> errors = _testCompile(xsdFiles, outputDirName);
        assertFalse(hasSevereError(errors), "test_jira_xmlbeans251(): failure when executing scomp");
    }
}
