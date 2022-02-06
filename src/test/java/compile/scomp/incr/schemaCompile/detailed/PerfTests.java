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

package compile.scomp.incr.schemaCompile.detailed;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static compile.scomp.common.CompileTestBase.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PerfTests {

    private static final File outincr = xbeanOutput(INCR_PATH);
    private static final File out = xbeanOutput(OUT_PATH);
    private static final File OBJ_FILE_1 = new File(out, "obj1.xsd");
    private static final File OBJ_FILE_2 = new File(outincr, "obj2.xsd");

    private static final String XSD1 =
        getSchemaTop("baz") +
       "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
       "<xs:complexType name=\"aType\">" +
       "<xs:choice>" +
       "<xs:element name=\"a\" type=\"xs:string\" />" +
       "<xs:element name=\"b\" type=\"xs:string\" />" +
       "</xs:choice>" +
       "</xs:complexType>" +
       getSchemaBottom();

    private static final String XSD2 =
        getSchemaTop("baz") +
        "<xs:redefine schemaLocation=\"" + OBJ_FILE_1.toURI() + "\">" +
        "<xs:complexType name=\"aType\">" +
        "<xs:sequence>" +
        "<xs:element name=\"a\" type=\"xs:string\" />" +
        "<xs:element name=\"b\" type=\"xs:string\" />" +
        "</xs:sequence>" +
        "</xs:complexType>" +
        "</xs:redefine>" +
        getSchemaBottom();


    private final List<XmlError> errors = new ArrayList<>();
    private final XmlOptions xm = new XmlOptions();

    public PerfTests() {
        xm.setErrorListener(errors);
        xm.setSavePrettyPrint();
    }

    @BeforeEach
    public void setUp() throws IOException {
        clearOutputDirs();
        errors.clear();
    }

    @Test
    @Disabled("works in standalone, doesn't work in Jenkins")
    public void test_perf_choice2seqchange() throws Exception {

        Files.write(OBJ_FILE_1.toPath(), XSD1.getBytes(StandardCharsets.UTF_8), CREATE, TRUNCATE_EXISTING);
        Files.write(OBJ_FILE_2.toPath(), XSD2.getBytes(StandardCharsets.UTF_8), CREATE, TRUNCATE_EXISTING);

        XmlObject obj1 = XmlObject.Factory.parse(OBJ_FILE_1);
        XmlObject obj2 = XmlObject.Factory.parse(OBJ_FILE_2);
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};

        long initBase = System.currentTimeMillis();
        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        long endBase = System.currentTimeMillis();

        long initIncr = System.currentTimeMillis();
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);
        long endIncr = System.currentTimeMillis();

        long initTime = endBase - initBase;
        long incrTime = endIncr - initIncr;
        long diffTime = initTime - incrTime;

        assertTrue(diffTime > 0, "Perf Time Increased: " + diffTime);

        QName[] baseTypes = {new QName("http://baz", "elName")};
        QName[] incrTypes = {new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }
}
