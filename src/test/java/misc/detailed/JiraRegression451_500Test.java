/*
 *   Copyright 2009 The Apache Software Foundation
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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JiraRegression451_500Test extends JiraTestBase {
    /*
     * [XMLBEANS-487]: Entity replacement in wrong place when expansion
     * coincides with buffer growth
     */
    @Test
    void test_jira_xmlbeans487() throws IOException, XmlException {
        XmlObject dok = XmlObject.Factory.parse(new File(JIRA_CASES + "xmlbeans_487.xml"));

        XmlOptions XML_OPTIONS = new XmlOptions().setSaveOuter().setSaveNamespacesFirst().setSaveAggressiveNamespaces();
        int INITIAL_READ = 28;

        Reader reader = dok.newReader(XML_OPTIONS);

        char[] buffer = new char[30000];
        String part1 = new String(buffer, 0, reader.read(buffer, 0, INITIAL_READ));
        String part2 = new String(buffer, 0, reader.read(buffer, 0, buffer.length));

        String totalResult = part1 + part2;

        assertEquals(dok.xmlText(XML_OPTIONS), totalResult, "Should be identical");
    }
}
