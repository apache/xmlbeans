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

import common.Common;
import noNamespace.TestCase;
import noNamespace.TestSuiteDocument;
import noNamespace.TestSuiteDocument.TestSuite.TestGroup;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Too many XMLBeans unrelated errors")
public class NISTTest {

    private static ZipFile zf;

    public static Stream<Arguments> files() throws IOException, XmlException {
        zf = new ZipFile(Common.getCaseLocation()+"/xbean/xmlcursor/xquery/xmlQuery.zip");
        ZipEntry e = zf.getEntry("testSuite/NIST/files/catalog.xml");

        try (InputStream is = zf.getInputStream(e)) {
            TestSuiteDocument doc = TestSuiteDocument.Factory.parse(is);

            List<Arguments> files = new ArrayList<>();
            for (TestGroup xg : doc.getTestSuite().getTestGroupArray()) {
                String groupName = xg.getName();
                for (TestCase tc : xg.getTestCaseArray()) {
                    String testName = tc.getName();
                    files.add(Arguments.of(groupName, testName, tc));

                    // NIST BUG: folder is called testSuite but appears as testsuite in desc. file
                    String filePath = tc.getFilePath()
                        .replaceAll("testsuite", "testSuite")
                        .replace((char) 92, '/');
                    tc.setFilePath(filePath);
                }
            }
            return files.stream();
        }
    }

    @ParameterizedTest(name = "{index}: {0} {1}")
    @MethodSource("files")
    void bla(String groupName, String testName, TestCase testCase) throws Exception {
        //bad comment syntax in suite
        String query = getString(testCase.getFilePath() + testCase.getName() + ".xq")
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
        ZipEntry queryFile = zf.getEntry(zipFile);
        try (InputStream is = zf.getInputStream(queryFile)) {
            return new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8);
        }
    }
}
