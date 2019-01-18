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
package xmlcursor.xquery.detailed;

import noNamespace.TestCase;
import noNamespace.TestSuiteDocument;
import noNamespace.TestSuiteDocument.TestSuite.TestGroup;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;

@Ignore("Too many XMLBeans unrelated errors")
@RunWith(Parameterized.class)
public class NISTTest {

    private static ZipFile zf;

    @Rule
    public final QueryFailed queryLog = new QueryFailed();

    @SuppressWarnings("DefaultAnnotationParam")
    @Parameterized.Parameter(value = 0)
    public String groupName;

    @Parameterized.Parameter(value = 1)
    public String testName;

    @Parameterized.Parameter(value = 2)
    public TestCase testCase;

    private String query;

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Iterable<Object[]> files() throws IOException, XmlException {
        zf = new ZipFile("test/cases/xbean/xmlcursor/xquery/xmlQuery.zip");

        ZipEntry e = zf.getEntry("testSuite/NIST/files/catalog.xml");
        InputStream is = zf.getInputStream(e);
        TestSuiteDocument doc = TestSuiteDocument.Factory.parse(is);

        List<Object[]> files = new ArrayList<Object[]>();
        for (TestGroup xg : doc.getTestSuite().getTestGroupArray()) {
            String groupName = xg.getName();
            for (TestCase tc : xg.getTestCaseArray()) {
                String testName = tc.getName();
                files.add(new Object[]{groupName, testName, tc});

                // NIST BUG: folder is called testSuite but appears as testsuite in desc. file
                String filePath = tc.getFilePath()
                    .replaceAll("testsuite", "testSuite")
                    .replace((char) 92, '/');
                tc.setFilePath(filePath);
            }
        }
        is.close();

        return files;
    }

    @Test
    public void bla() throws Exception {
        //bad comment syntax in suite
        query = getString(testCase.getFilePath()+testCase.getName()+".xq")
            .replace("{--", "(:")
            .replace("--}", ":)");

        XmlObject obj = XmlObject.Factory.parse("<xml-fragment/>");
        String inputFile = testCase.getInputFileArray(0).getStringValue();
        assertEquals("emptyDoc", inputFile);

        // String outputFile = testCase.getFilePath()+testCase.getOutputFileArray(0).getStringValue();
        // XmlObject[] expRes = { XmlObject.Factory.parse(getString(outputFile)) };

        XmlObject[] queryRes = obj.execQuery(query);
        // XPathCommon.compare(queryRes, expRes);
    }

    private static String getString(String zipFile) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipEntry queryFile = zf.getEntry(zipFile);
        InputStream is = zf.getInputStream(queryFile);
        byte[] buf = new byte[4096];
        for (int readBytes; (readBytes = is.read(buf)) > -1; ) {
            bos.write(buf, 0, readBytes);
        }
        is.close();
        return new String(bos.toByteArray(), Charset.forName("UTF-8"));
    }



    private class QueryFailed extends TestWatcher {
        @Override
        protected void failed(Throwable e, Description description) {
            System.out.println(
                "Description:\n"+
                testCase.getQuery().getDescription().getStringValue()+
                "\n\nQuery:\n"+
                query);
        }
    }
}
