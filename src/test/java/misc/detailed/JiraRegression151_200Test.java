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

import misc.common.JiraTestBase;
import net.eads.space.scoexml.test.TestExponentDocument;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.tool.Parameters;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class JiraRegression151_200Test extends JiraTestBase
{

    /**
     * [XMLBEANS-175]   Validation of decimal in exponential representation fails
     */
    @Test
    void test_jira_xmlbeans175() {

        TestExponentDocument.TestExponent exponent = TestExponentDocument.TestExponent.Factory.newInstance();
        exponent.setDecimal(new BigDecimal("1E1"));

        List<XmlError> errors = new ArrayList<>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(errors);
        exponent.validate(validationOptions);

        for (XmlError error : errors) {
            System.out.println("Validation Error:" + error);
        }

        // fails, IMHO should not!
        assertEquals(0, errors.size());
        /* note: the following uses JDK 1.5 API, not supported in 1.4
        // workaround
        exponent.setDecimal(new BigDecimal(new BigDecimal("1E1").toPlainString()));
        errors.removeAll(errors);
        exponent.validate(validationOptions);
        assertEquals(0, errors.size());
        */
    }

    /**
     * [XMLBEANS-179]   Saving xml with '&' and '<' characters in attribute values throws an ArrayIndexOutOfBoundsException
     */
    @Test
    void test_jira_xmlbeans179() throws Exception{
        String xmlWithIssues = "<Net id=\"dbid:66754220\" name=\"3&lt;.3V\" type=\"POWER\"/>";

        XmlObject xobj = XmlObject.Factory.parse(xmlWithIssues);
        File outFile = new File(schemaCompOutputDirPath + P + "jira_xmlbeans179.xml");
        assertNotNull(outFile);

        if(outFile.exists()) {
            outFile.delete();
        }

        xobj.save(outFile);
    }

    /*
    * [XMLBEANS-184]: NPE when running scomp without nopvr option
    */
    @Test
    void test_jira_xmlbeans184() throws Exception {
        List<XmlError> errors = new ArrayList<>();

        // compile with nopvr, goes thro fine
        Parameters params = new Parameters();
        params.setXsdFiles(new File(scompTestFilesRoot + "xmlbeans_184_vdx_data_V1.04.xsd_"));
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);
        params.setNoPvr(true);

        SchemaCompiler.compile(params);
        assertFalse(hasSevereError(errors));

        // now compile without the pvr option and NPE is thrown
        params.setNoPvr(false);
        SchemaCompiler.compile(params);

        assertFalse(hasSevereError(errors));
    }


}
