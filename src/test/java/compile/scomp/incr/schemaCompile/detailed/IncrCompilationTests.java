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

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.util.FilerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static compile.scomp.common.CompileTestBase.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.junit.jupiter.api.Assertions.*;

public class IncrCompilationTests {

    private static final File outincr = xbeanOutput(INCR_PATH);
    private static final File out = xbeanOutput(OUT_PATH);
    private static final File OBJ_1 = new File(out, "obj1.xsd");
    private static final File OBJ_2 = new File(outincr, "obj2.xsd");


    private static final String schemaFilesRegeneration_schema1 =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xs:schema " +
        "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://openuri.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:element name=\"TestElement\" type=\"bas:aType\" xmlns:bas=\"http://openuri.org\" />" +
        "<xs:element name=\"NewTestElement\" type=\"bas:bType\" xmlns:bas=\"http://openuri.org\" />" +
        "<xs:complexType name=\"aType\">" +
        "<xs:simpleContent>" +
        "<xs:extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:attribute type=\"xs:string\" name=\"stringAttr\" />" +
        "</xs:extension>" +
        "</xs:simpleContent>" +
        "</xs:complexType>" +
        "<xs:complexType name=\"bType\">" +
        "<xs:simpleContent>" +
        "<xs:extension base=\"xs:integer\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "</xs:extension>" +
        "</xs:simpleContent>" +
        "</xs:complexType>" +
        "</xs:schema>";

    private static final String schemaFilesRegeneration_schema1_modified =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xs:schema " +
        "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://openuri.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:element name=\"TestElement\" type=\"bas:aType\" xmlns:bas=\"http://openuri.org\" />" +
        "<xs:complexType name=\"aType\">" +
        "<xs:simpleContent>" +
        "<xs:extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:attribute type=\"xs:token\" name=\"tokenAttr\" />" +
        "</xs:extension>" +
        "</xs:simpleContent>" +
        "</xs:complexType>" +
        "</xs:schema>";

    private static final String XSD_BASE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xs:schema " +
        "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://#namespace#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:element name=\"#elTypeName#\" type=\"bas:aType\" xmlns:bas=\"http://#namespace#\" />" +
        "<xs:complexType name=\"aType\">" +
        "<xs:simpleContent>" +
        "<xs:extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:attribute type=\"xs:#attrType#\" name=\"#attrTypeName#\" />" +
        "</xs:extension>" +
        "</xs:simpleContent>" +
        "</xs:complexType>" +
        "</xs:schema>";

    private static final String XSD_REDEF =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xs:schema " +
        "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://#namespace#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:redefine schemaLocation=\"" + OBJ_1.toURI() + "\">" +
        "<xs:complexType name=\"aType\">" +
        "<xs:simpleContent>" +
        "<xs:extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
        "<xs:attribute type=\"xs:#attrType#\" name=\"#attrTypeName#\" />" +
        "</xs:extension>" +
        "</xs:simpleContent>" +
        "</xs:complexType>" +
        "</xs:redefine>" +
        "</xs:schema>";

    private final SchemaTypeSystem builtin = XmlBeans.getBuiltinTypeSystem();
    private final List<XmlError> errors = new ArrayList<>();
    private final XmlOptions xm = new XmlOptions();



    public IncrCompilationTests() {
        xm.setErrorListener(errors);
        xm.setSavePrettyPrint();
    }

    @BeforeEach
    public void setUp() throws IOException {
        clearOutputDirs();
        errors.clear();
    }


    @Test
    void test_dupetype_diffns() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "attrName", "string"));
        XmlObject[] schemas = {obj1};
        SchemaTypeSystem base = compileSchemas(schemas, out, xm);

        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("bar", "elName", "attrName", "string"));
        XmlObject[] schemas2 = {obj2};
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        assertNotNull(base.findElement(new QName("http://baz", "elName")), "BASE: Baz elName was not found");
        // assertNotNull("INCR: Baz elName was not found", incr.findElement(new QName("http://baz", "elName")));
        assertNotNull(incr.findElement(new QName("http://bar", "elName")), "INCR: Bar elName was not found");

        // compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    @Disabled("incremental build / test doesn't provide new elements")
    public void test_dupens_difftypename() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getRedefSchema("baz", "elName2", "attrName2", "string"));
        XmlObject[] schemas = {obj1};
        XmlObject[] schemas2 = {obj2};

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName"),
                                        new QName("http://baz", "elName2")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        // compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    @Disabled("works in standalone, doesn't work in Jenkins")
    public void test_dupens_attrnamechange() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getRedefSchema("baz", "elName", "attrName2", "string"));
        XmlObject[] schemas = {obj1};
        XmlObject[] schemas2 = {obj2};

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        assertNotSame(base, incr);

        // compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    @Disabled("works in standalone, doesn't work in Jenkins")
    public void test_dupens_attrtypechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getRedefSchema("baz", "elName", "attrName", "int"));
        XmlObject[] schemas = {obj1};
        XmlObject[] schemas2 = {obj2};

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        assertNotSame(base, incr);

        // compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    @Disabled("works in standalone, doesn't work in Jenkins")
    public void test_dupens_eltypechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getRedefSchema("baz", "elName", "attrName", "string"));
        XmlObject[] schemas = {obj1};
        XmlObject[] schemas2 = {obj2};

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        // compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    void test_typechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "attrName2", "string"));
        XmlObject[] schemas = {obj1};
        XmlObject[] schemas2 = {obj2};

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        assertNotSame(base, incr);

        // compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    // test regeneration of generated java files by the Filer
    @Test
    void test_schemaFilesRegeneration_01() throws Exception {

        // incremental compile with the same file again. There should be no regeneration of src files
        XmlObject obj1 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1);
        XmlObject obj2 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1);

        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};

        // the source name should be set the same for incremental compile
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj1");

        // create a new filer here with the incrCompile flag value set to 'true'
        Filer filer = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem base = XmlBeans.compileXmlBeans("teststs",null,schemas,null,builtin,filer,xm);
        assertNotNull(base, "Compilation failed during Incremental Compile.");
        base.saveToDirectory(out);

        // get timestamps for first compile
        Map<String,Long> initialTimeStamps = new HashMap<>();
        recordTimeStamps(out, initialTimeStamps);

        // the incr compile - provide the same name for the STS as the initial compile
        // note: providing a null or different name results in regeneration of generated Interface java src files
        Map<String,Long> recompileTimeStamps = new HashMap<>();
        Filer filer2 = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem incr = XmlBeans.compileXmlBeans("teststs",base,schemas2,null,builtin,filer2,xm);
        assertNotNull(incr, "Compilation failed during Incremental Compile.");
        incr.saveToDirectory(out);
        recordTimeStamps(out, recompileTimeStamps);

        // compare generated source timestamps here
        assertEquals(initialTimeStamps.size(), recompileTimeStamps.size(), "Number of Files not equal for Incremental Schema Compilation using Filer");
        Set<String> keyset = initialTimeStamps.keySet();
        for (String eachFile : keyset) {
            assertEquals(initialTimeStamps.get(eachFile), recompileTimeStamps.get(eachFile), "Mismatch for File " + eachFile);
        }

        handleErrors(errors);
    }

    @Test
    void test_schemaFilesRegeneration_02() throws Exception {
        // incremental compile with the changes. Specific files should be regenerated
        XmlObject obj1 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1);
        XmlObject obj2 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1_modified);

        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};

        // the source name should be set the same for incremental compile
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj1");

        // create a new filer here with the incrCompile flag value set to 'true'
        Filer filer = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem base = XmlBeans.compileXmlBeans("test",null,schemas,null,builtin,filer,xm);
        assertNotNull(base, "Compilation failed during Incremental Compile.");
        base.saveToDirectory(out);

        // get timestamps for first compile
        HashMap<String,Long> initialTimeStamps = new HashMap<>();
        recordTimeStamps(out, initialTimeStamps);

        // the incr compile
        Map<String,Long> recompileTimeStamps = new HashMap<>();
        Filer filer2 = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem incr = XmlBeans.compileXmlBeans("test",base,schemas2,null,builtin,filer2,xm);
        assertNotNull(incr, "Compilation failed during Incremental Compile.");
        incr.saveToDirectory(out);
        recordTimeStamps(out, recompileTimeStamps);

        // compare generated source timestamps here
        assertEquals(initialTimeStamps.size(), recompileTimeStamps.size(), "Number of Files not equal for Incremental Schema Compilation using Filer");
        Set<String> keyset = initialTimeStamps.keySet();

        // Atype has been modified, BType has been removed
        String sep = System.getProperty("file.separator");
        String modifiedFileName = out.getCanonicalFile() + sep + "org" + sep + "openuri" + sep + "impl" + sep + "ATypeImpl.java";
        String modifiedFileName2 = out.getCanonicalFile() + sep + "org" + sep + "openuri" + sep + "AType.java";

        for (String eachFile : keyset) {
            if (eachFile.equalsIgnoreCase(modifiedFileName)) {
                assertNotSame(initialTimeStamps.get(eachFile), recompileTimeStamps.get(eachFile), "File Should have been regenerated by Filer but has the same timestamp");
                continue;
            }
            if (eachFile.equalsIgnoreCase(modifiedFileName2)) {
                assertNotSame(initialTimeStamps.get(eachFile), recompileTimeStamps.get(eachFile), "File Should have been regenerated by Filer but has the same timestamp");
                continue;
            }
            assertEquals(initialTimeStamps.get(eachFile), recompileTimeStamps.get(eachFile), "Mismatch for File " + eachFile);
        }

        handleErrors(errors);
    }

    static File getBaseSchema(String namespace, String elTypeName, String attrTypeName, String attrType) throws IOException {
        String xsd = XSD_BASE
            .replace("#namespace#", namespace)
            .replace("#elTypeName#", elTypeName)
            .replace("#attrTypeName#", attrTypeName)
            .replace("#attrType#", attrType)
        ;

        Files.write(OBJ_1.toPath(), xsd.getBytes(StandardCharsets.UTF_8), CREATE, TRUNCATE_EXISTING);

        return OBJ_1;
    }

    static File getRedefSchema(String namespace, String elTypeName, String attrTypeName, String attrType) throws IOException {
        String xsd = XSD_REDEF
            .replace("#namespace#", namespace)
            .replace("#attrTypeName#", attrTypeName)
            .replace("#attrType#", attrType)
            ;

        Files.write(OBJ_2.toPath(), xsd.getBytes(StandardCharsets.UTF_8), CREATE, TRUNCATE_EXISTING);

        return OBJ_2;
    }

    public boolean recordTimeStamps(File inputDir, Map<String,Long> timeStampResults) throws Exception {
        if (timeStampResults == null || inputDir == null || !inputDir.exists()) {
            return false;
        }

        if(inputDir.isFile()) {
            return true;
        }

        for (File file : Objects.requireNonNull(inputDir.listFiles())) {
            if (file.getName().endsWith(".java")) {
                timeStampResults.put(file.getCanonicalPath(), file.lastModified());
            }
            recordTimeStamps(file, timeStampResults);
        }

        return true;
    }
}
