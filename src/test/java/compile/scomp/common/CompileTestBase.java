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
package compile.scomp.common;

import common.Common;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.tool.Diff;
import org.apache.xmlbeans.impl.tool.SchemaCodeGenerator;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * TODO: modify for deprecation warnings
 */
public class CompileTestBase extends Common {

    private static final String outputDir = "compile" + P + "scomp" + P + "incr";
    public static final String OUT_PATH = outputDir + P + "out";
    public static final String SANITY_PATH = outputDir + P + "sanity";
    public static final String INCR_PATH = outputDir + P + "outincr";


    //schemas to use
    public static final String FOR_XSD =
        "<xs:schema attributeFormDefault=\"unqualified\" " +
        "elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://baz\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
        "<xs:element name=\"elName\" type=\"bas:aType\" " +
        "xmlns:bas=\"http://baz\"/> <xs:complexType name=\"aType\"> " +
        "<xs:simpleContent> " +
        "<xs:extension base=\"xs:string\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
        "<xs:attribute type=\"xs:string\" name=\"attrName\"/> " +
        "</xs:extension> " +
        "</xs:simpleContent> " +
        "</xs:complexType> " +
        "</xs:schema>";

    public static final String INCR_XSD =
        "<xs:schema attributeFormDefault=\"unqualified\" " +
        "elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://bar\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
        "<xs:element name=\"elName\" type=\"bas:aType\" " +
        "xmlns:bas=\"http://baz\"/> <xs:complexType name=\"aType\"> " +
        "<xs:simpleContent> " +
        "<xs:extension base=\"xs:string\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
        "<xs:attribute type=\"xs:string\" name=\"attrName\"/> " +
        "</xs:extension> " +
        "</xs:simpleContent> " +
        "</xs:complexType> " +
        "</xs:schema>";

    public static final String ERR_XSD =
        "<xs:schema attributeFormDefault=\"unqualified\" " +
        "elementFormDefault=\"qualified\" " +
        "targetNamespace=\"http://bar\" " +
        "xmlns:tnf=\"http://baz\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
        "<xs:element name=\"elErrName\" type=\"tnf:bType\" /> " +
        "</xs:schema>";

    public static File[] getClassPath() {
        String cp = System.getProperty("java.class.path");
        String[] cpList = cp.split(File.pathSeparator);
        File[] fList = new File[cpList.length];

        for (int i = 0; i < cpList.length; i++) {
            fList[i] = new File(cpList[i]);
        }
        return fList;
    }

    public void log(SchemaTypeSystem sts) {
        System.out.println("SchemaTypes: " + sts);
    }

    public void log(Object[] arr) {
        for (int i = 0; i < arr.length; i++)
            System.out.print(arr[i].toString());
    }


    /**
     * compares type systems and populates error list based on differences in files
     */
    public static void compareandPopErrors(File outDir, File outIncrDir, List<XmlError> errors) {
        // Compare the results
        String oldPropValue = System.getProperty("xmlbeans.diff.diffIndex");
        System.setProperty("xmlbeans.diff.diffIndex", "false");
        errors.clear();
        Diff.dirsAsTypeSystems(outDir, outIncrDir, errors);
        System.setProperty("xmlbeans.diff.diffIndex", oldPropValue == null ? "true" : oldPropValue);

    }

    public static void clearOutputDirs() {
        File outRoot = new File(OUTPUTROOT);
        if (outRoot.exists()) {
            deltree(outRoot);
        }
        xbeanOutput(OUT_PATH);
        xbeanOutput(SANITY_PATH);
        xbeanOutput(INCR_PATH);

    }

    /**
     * take the type system that gets created in compileSchemas()
     */
    public static SchemaTypeSystem incrCompileXsd(SchemaTypeSystem system, XmlObject[] schemas, File outDir, XmlOptions options)
    throws XmlException, IOException {
        SchemaTypeSystem builtin = XmlBeans.getBuiltinTypeSystem();
        SchemaTypeSystem sts = XmlBeans.compileXsd(system, schemas, builtin, options);
        assertNotNull("Compilation failed during Incremental Compile.", sts);
        SchemaCodeGenerator.saveTypeSystem(sts, outDir, null, null, null);
        return sts;

    }

    //original compile to get base type system
    public static SchemaTypeSystem compileSchemas(XmlObject[] schemas, File outDir, XmlOptions options)
    throws XmlException, IOException {
        SchemaTypeSystem system;
        SchemaTypeSystem builtin = XmlBeans.getBuiltinTypeSystem();
        system = XmlBeans.compileXsd(schemas, builtin, options);
        assertNotNull("Compilation failed during compile.", system);
        SchemaCodeGenerator.saveTypeSystem(system, outDir, null, null, null);
        return system;
    }

    public static void handleErrors(List<XmlError> errors) {
        if (errors.size() > 0) {
            StringWriter message = new StringWriter();
            for (XmlError error : errors) {
                message.write(error + "\n");
            }
            fail("\nDifferences encountered:\n" + message);
        }
    }

    public static void findElementbyQName(SchemaTypeSystem sts, QName[] lookup) {
        assertTrue("Element was expected but not found",
            Stream.of(lookup).map(sts::findElement).allMatch(Objects::nonNull));
    }

    public static String getSchemaTop(String tns) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xs:schema " +
                "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"http://" + tns + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >";
    }

    public static String getSchemaBottom() {
        return "</xs:schema>";
    }

    public static boolean findGlobalElement(SchemaGlobalElement[] sge, QName q) {
        return Stream.of(sge).map(SchemaGlobalElement::getName).anyMatch(sg ->
            sg.getLocalPart().equals(q.getLocalPart()) && sg.getNamespaceURI().equals(q.getNamespaceURI()));
    }

}
