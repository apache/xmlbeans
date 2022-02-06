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
package compile.scomp.som.common;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.tool.Diff;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static common.Common.*;
import static org.junit.jupiter.api.Assertions.*;

public class SomTestBase {
    public static String casesRootDir = XBEAN_CASE_ROOT + P + "compile" + P + "som" + P;
    public static String somOutputRootDir = OUTPUTROOT + P + "som" + P;
    public static long runid;

    public SchemaTypeSystem builtin;
    public List<XmlError> errors;
    public XmlOptions options;

    public static final String anyType = "anyType";

    public static void inspectSOM(SchemaTypeSystem schematypesys,
                                  int expectedGlobalElems,
                                  int expectedGlobalAttrs,
                                  int expectedGlobalTypes,
                                  int expectedAttrGroups) {
        schematypesys.resolve();
        // dummy call
        schematypesys.isNamespaceDefined("TestNameSpace");
        assertNotNull(schematypesys.getName());

        // # of global attributes
        assertEquals(expectedGlobalAttrs, schematypesys.globalAttributes().length, "Incorrect Number of Global Attributes in STS " + schematypesys.getName());
        for (SchemaGlobalAttribute sga : schematypesys.globalAttributes()) {
            assertNotNull(sga.getName());
            assertNotNull(sga.getType());
        }

        // # of global elements
        assertEquals(expectedGlobalElems, schematypesys.globalElements().length, "Incorrect Number of Global Elements in STS " + schematypesys.getName());
        for (SchemaGlobalElement sge : schematypesys.globalElements()) {
            assertNotNull(sge.getName());
            assertNotNull(sge.getType());
        }

        // # of global Types
        assertEquals(expectedGlobalTypes, schematypesys.globalTypes().length, "Incorrect Number of Global Types in STS " + schematypesys.getName());
        for (SchemaType st : schematypesys.globalTypes()) {
            assertNotNull(st.getName());
        }

        // # of attribute Groups
        assertEquals(expectedAttrGroups, schematypesys.attributeGroups().length, "Incorrect Number of Attribute Groups in STS " + schematypesys.getName());
        for (SchemaAttributeGroup sag : schematypesys.attributeGroups()) {
            assertNotNull(sag.getName());
            assertNotNull(sag.getTypeSystem());
        }

        assertNotNull(schematypesys.documentTypes(), "Invalid Model Groups Collection returned in STS ");
        for (SchemaModelGroup smg : schematypesys.modelGroups()) {
            assertNotNull(smg.getName());
            assertNotNull(smg.getTypeSystem());
        }

        assertNotNull(schematypesys.annotations(), "Invalid Annotations Collection returned in STS ");
        for (SchemaAnnotation sa : schematypesys.annotations()) {
            assertTrue(Stream.of(sa.getApplicationInformation()).allMatch(Objects::nonNull));
            assertTrue(Stream.of(sa.getUserInformation()).allMatch(Objects::nonNull));
        }

        assertNotNull(schematypesys.attributeTypes(), "Invalid Attribute Types Collection returned in STS ");
        for (SchemaType st : schematypesys.attributeTypes()) {
            assertTrue(st.isAnonymousType() || st.getName() != null);
            assertNotNull(st.getTypeSystem());
        }

        assertNotNull(schematypesys.documentTypes(), "Invalid Document Types Collection returned in STS ");
        for (SchemaType st : schematypesys.documentTypes()) {
            assertTrue(st.isAnonymousType() || st.getName() != null);
            assertNotNull(st.getTypeSystem());
        }

        // walk through the Schema Types of this STS in detail
        for (SchemaType schema : schematypesys.globalTypes()) {
            assertNotNull(schema.getName());

            schema.isCompiled();

            assertNotNull(schema.getContentType());
            assertNotNull(schema.getName());
            // assertNotNull(schema.getDocumentElementName());
            // assertNotNull(schema.getAnnotation());
            // assertNotNull(schema.getFullJavaName());
            // assertNotNull(schema.getFullJavaImplName());
            // assertNotNull(schema.getJavaClass());
            // assertNotNull(schema.getSourceName());


            // get Elements and Attributes
            for (SchemaProperty schemaProperty : schema.getProperties()) {
                assertNotNull(schemaProperty.getName());
            }

            // other api's to look for
            for (SchemaProperty schemaProperty : schema.getDerivedProperties()) {
                assertNotNull(schemaProperty.getName());
            }

            // TODO anonymus types
            //schema.getAnonymousTypes();
        }
    }

    public boolean lookForAttributeInSTS(SchemaTypeSystem tgtSTS,
                                         String sAttrLocalName) {
        // The QName for the find is constructed using the local name since the schemas have no namespace
        SchemaGlobalAttribute sga = tgtSTS.findAttribute(new QName(sAttrLocalName));
        return sga != null;
    }

    public boolean lookForElemInSTS(SchemaTypeSystem tgtSTS, String sElemLocalName) {
        // The QName for the find is constructed using the local name since the schemas have no namespace
        return tgtSTS.findElement(new QName(sElemLocalName)) != null;
    }

    public boolean lookForIdentityConstraint(SchemaTypeSystem sts, String ConstraintLocalName) {
        return sts.findIdentityConstraintRef(new QName(ConstraintLocalName)) != null;
    }

    public boolean checkPSOMSave(SchemaTypeSystem tgtSTS) {
        String outDirName = tgtSTS.getName().split("org.apache.xmlbeans.metadata.system.")[1];
        String outDirNameWithPath = somOutputRootDir + P + runid + P + outDirName;

        // call the save
        try {
            tgtSTS.saveToDirectory(new File(outDirNameWithPath));
        } catch (IllegalStateException ise) {
            // uncomment to see the stack trace
            // ise.printStackTrace();
            return false;
        }
        return true;

    }

    public boolean compareSavedSOMs(String outDirSchemaOne, String outDirSchemaTwo) {
        System.out.println("Comparing Schemas....");

        String runDir = somOutputRootDir + P + runid + P;
        File sts1 = new File(somOutputRootDir + P + runid + P + outDirSchemaOne);
        if (!sts1.exists() && (!sts1.isDirectory())) {
            System.out.println("Schema Type System save dir specified (" + runDir + outDirSchemaOne + ") does not exist!");
            return false;
        }

        File sts2 = new File(somOutputRootDir + P + runid + P + outDirSchemaTwo);
        if (!sts2.exists() && (!sts2.isDirectory())) {
            System.out.println("Schema Type System save dir specified (" + runDir + outDirSchemaTwo + ") does not exist!");
            return false;
        }

        List<XmlError> diff = new ArrayList<>();
        Diff.filesAsXsb(sts1, sts2, diff);
        if (diff.isEmpty()) {
            return true;
        } else {
            for (Object o : diff) {
                System.out.println("Difference found : " + o);
            }
            return false;
        }
    }

    public boolean printRecoveredErrors() {
        // check list of errors and print them
        boolean errFound = false;
        for (XmlError eacherr : errors) {
            int errSeverity = eacherr.getSeverity();
            if (errSeverity == XmlError.SEVERITY_ERROR) {
                System.out.println("Schema invalid: partial schema type system recovered");
                System.out.println("Err Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                errFound = true;
            } else if (errSeverity == XmlError.SEVERITY_WARNING) {
                System.out.println("Warning Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
            } else if (errSeverity == XmlError.SEVERITY_INFO) {
                System.out.println("Info Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
            }
        }
        errors.clear();
        return errFound;
    }



    public boolean validateInstance(File instancefile, SchemaTypeSystem sts) throws IOException, XmlException {
        return validateInstance(instancefile, sts, new ArrayList<>());
    }

    public boolean validateInstance(File instancefile, SchemaTypeSystem sts, List<XmlError> errList) throws IOException, XmlException {
        XmlOptions loadOpt = new XmlOptions();
        loadOpt.setLoadLineNumbers();
        XmlObject instancedoc = sts.parse(instancefile, null, loadOpt);

        errList.clear();
        XmlOptions instanceValOptions = new XmlOptions();
        instanceValOptions.setErrorListener(errList);
        instanceValOptions.setLoadLineNumbers();

        return instancedoc.validate(instanceValOptions);
    }

    public File getTestCaseFile(String sFileName) {
        String sFileWithPath = casesRootDir + P + sFileName;
        //System.out.println("getTestCaseFile() Opening File : " + sFileWithPath);
        File schemaFile = new File(sFileWithPath);
        assertNotNull(schemaFile, "Schema File " + sFileWithPath + " Loading failed");
        return (schemaFile);
    }

    // returns the Local Part of the type QName for the specified Elem
    public String getElementType(SchemaTypeSystem sts,
                                 String sElementLocalName) {

        SchemaGlobalElement elem = sts.findElement(new QName(sElementLocalName));
        if (elem != null) {
            return elem.getType().getName().getLocalPart();
        }
        return "ElemNotFound";
    }

    public boolean getAttributeGroup(SchemaTypeSystem sts,
                                     String sAttrGrpLocalName) {
        SchemaAttributeGroup attrGp = sts.findAttributeGroup(new QName(sAttrGrpLocalName));
        return attrGp != null;
    }

    public boolean getModelGroup(SchemaTypeSystem sts,
                                 String sModelGrpLocalName) {
        SchemaModelGroup.Ref modelGp = sts.findModelGroupRef(new QName(sModelGrpLocalName));
        return modelGp != null;
    }

    public SchemaTypeSystem createNewSTS(String xsdFileName,
                                         SchemaTypeSystem baseSchema,
                                         String sSTSName, String sBaseSourceName) throws IOException, XmlException {
        File xsdModified = getTestCaseFile(xsdFileName);
        XmlObject xsdModifiedObj = XmlObject.Factory.parse(xsdModified);
        System.out.println("Source Name for STS: " + xsdModifiedObj.documentProperties().getSourceName());

        // If null is passed for the basename, the basename is not set. Modified for namespace testcases.
        // If a source name is specified, deferencing of location for schemaLocation attribute gets messed up.
        if (sBaseSourceName != null) {
            xsdModifiedObj.documentProperties().setSourceName(sBaseSourceName);
        }
        assertNotNull(xsdModifiedObj, "Xml Object creation failed");
        XmlObject[] xobjArr = new XmlObject[]{xsdModifiedObj};

        SchemaTypeSystem returnSTS = XmlBeans.compileXmlBeans(sSTSName, baseSchema, xobjArr, null, builtin, null, options);
        assertNotNull(returnSTS, "Schema Type System created is Null.");

        // validate the XmlObject created
        assertTrue(xsdModifiedObj.validate(), "Return Value for Validate()");
        return returnSTS;
    }

    // deletes contents of specified directory, does not delete the specified directory
    public void deleteDirRecursive(File dirToClean) {
        if (dirToClean.exists() && dirToClean.isDirectory()) {
            for (File file : Objects.requireNonNull(dirToClean.listFiles())) {
                if (file.isDirectory()) {
                    deleteDirRecursive(file);
                    assertTrue(file.delete(), "Output Directory " + file + " Deletion Failed ");
                } else if (file.isFile()) {
                    assertTrue(file.delete(), "Output File " + file + " Deletion Failed ");
                }
            }
        }
    }
}
