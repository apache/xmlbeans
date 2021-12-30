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

import compile.scomp.common.CompileTestBase;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.tool.Diff;
import org.junit.Assert;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SomTestBase extends CompileTestBase {
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
                                  int expectedAttrGroups) throws IOException {
        // System.outs written to a log file in the build\test\output\som directory, one file per run
        // ex. SOM_INSPECTION_RESULT_1107129259405.txt

        File outDir = new File(somOutputRootDir);
        assertTrue(outDir.exists() || outDir.mkdir());

        // check if file exists already
        String logFileName = somOutputRootDir + P + "SOM_INSPECTION_RESULT_" + runid + ".txt";
        File outfile = new File(logFileName);

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outfile,true), UTF_8))) {

            out.println("\n Call to inspectPSOM .. .. .. ..");
            out.println("\n\n =======================================================");
            out.println("Now Inspecting SOM for STS : " + schematypesys.getName());
            out.println("=======================================================");
            out.println("Input Params : #elems (" + expectedGlobalElems + "), #attr (" + expectedGlobalAttrs
                        + "), #types (" + expectedGlobalTypes + "), #attr groups (" + expectedAttrGroups + ")");
            out.println("-------------------------------------------------------");

            out.println("New STUFF -------------------------------------------------------");
            schematypesys.resolve();
            if (schematypesys.isNamespaceDefined("TestNameSpace")) {
                out.println("Name Space 'TestNameSpace' for this STS is define ..");
            } else {
                out.println("No Name Space 'TestNameSpace' for this STS is NOT ndefine ..");
            }
            out.println("End New STUFF -------------------------------------------------------");

            // walk thro the SOM here
            out.println("----- Loader Name      :" + schematypesys.getName());

            // # of global attributes
            out.println("----- # Global Attributes :" + schematypesys.globalAttributes().length);
            assertEquals("Incorrect Number of Global Attributes in STS " + schematypesys.getName(), expectedGlobalAttrs, schematypesys.globalAttributes().length);
            for (SchemaGlobalAttribute sga : schematypesys.globalAttributes()) {
                out.println("\t------> Attr Name  :" + sga.getName());
                out.println("\t------> Attr Type  :" + sga.getType());
            }

            // # of global elements
            out.println("----- # Global Elements :" + schematypesys.globalElements().length);
            assertEquals("Incorrect Number of Global Elements in STS " + schematypesys.getName(), expectedGlobalElems, schematypesys.globalElements().length);
            for (SchemaGlobalElement sge : schematypesys.globalElements()) {
                out.println("\t------> Elem Name :" + sge.getName());
                out.println("\t------> Elem Type :" + sge.getType());
            }

            // # of global Types
            out.println("----- # Global Types :" + schematypesys.globalTypes().length);
            assertEquals("Incorrect Number of Global Types in STS " + schematypesys.getName(), expectedGlobalTypes, schematypesys.globalTypes().length);
            for (SchemaType st : schematypesys.globalTypes()) {
                out.println("\t------> TypeName:" + st.getName());
            }

            // # of attribute Groups
            out.println("----- # of Attribute Groups :" + schematypesys.attributeGroups().length);
            assertEquals("Incorrect Number of Attribute Groups in STS " + schematypesys.getName(), expectedAttrGroups, schematypesys.attributeGroups().length);
            for (SchemaAttributeGroup sag : schematypesys.attributeGroups()) {
                out.println("\t------> Attr Group Name :" + sag.getName());
                out.println("\t------> Attr STS   :" + sag.getTypeSystem());
            }

            out.println("----- # of Model Groups :" + schematypesys.modelGroups().length);
            Assert.assertNotNull("Invalid Model Groups Collection returned in STS ", schematypesys.documentTypes());
            for (SchemaModelGroup smg : schematypesys.modelGroups()) {
                out.println("\t------> Model Group Name:" + smg.getName());
                out.println("\t------> Model Group STS :" + smg.getTypeSystem());
            }

            out.println("----- # of Schema Annotations :" + schematypesys.annotations().length);
            Assert.assertNotNull("Invalid Annotations Collection returned in STS ", schematypesys.annotations());
            for (SchemaAnnotation sa : schematypesys.annotations()) {
                out.println("\t------> Annotation Application Info Array :" + Arrays.toString(sa.getApplicationInformation()));
                out.println("\t------> Annotation User Info Array :" + Arrays.toString(sa.getUserInformation()));
            }

            out.println("----- # of Attribute Types :" + schematypesys.attributeTypes().length);
            Assert.assertNotNull("Invalid Attribute Types Collection returned in STS ", schematypesys.attributeTypes());

            for (SchemaType st : schematypesys.attributeTypes()) {
                out.println("\t------> Attr Type Name :" + st.getName());
                out.println("\t------> Attr STS :" + st.getTypeSystem());
            }

            out.println("----- # of Document Types :" + schematypesys.documentTypes().length);
            Assert.assertNotNull("Invalid Document Types Collection returned in STS ", schematypesys.documentTypes());
            for (SchemaType st : schematypesys.documentTypes()) {
                out.println("\t------> Doc Type Name :" + st.getName());
                out.println("\t------> Doc Type STS  :" + st.getTypeSystem());
            }

            // walk through the Schema Types of this STS in detail
            out.println("\t=======================================================");
            out.println("\tWalking thro Global Schema TYpes for STS : " + schematypesys.getName());
            out.println("\t=======================================================");
            for (SchemaType schema : schematypesys.globalTypes()) {
                out.println("\n\t Schema Type :" + schema.getName());
                out.println("\t=======================================================");

                out.println("\t----Acessing New Schema Type ......");
                if (schema.isCompiled()) {
                    out.println("\t----This Schema has been successfully compiled");
                } else {
                    out.println("\t----This Schema has NOT compiled successfully yet");
                }

                out.println("\t----Content Type: " + schema.getContentType());
                out.println("\t----Name: " + schema.getName());
                out.println("\t----Doc Elem Name : " + schema.getDocumentElementName());
                out.println("\t----Annotation (class) : " + schema.getAnnotation());
                out.println("\t----Java Name : " + schema.getFullJavaName());
                out.println("\t----Java Imp Name : " + schema.getFullJavaImplName());
                out.println("\t----Java Class Name : " + schema.getJavaClass());
                out.println("\t----XSD src File Name : " + schema.getSourceName());


                // get Elements and Attributes
                out.println("\t Elements & Attributes for Schema Type :" + schema.getName());
                out.println("\t=======================================================");
                SchemaProperty[] spropsArr = schema.getProperties();
                for (SchemaProperty schemaProperty : spropsArr) {
                    out.println("\t:::-> Each prop name : " + schemaProperty.getName());
                }
                out.println("\t=======================================================");

                // other api's to look for
                SchemaProperty[] sderviedpropArr = schema.getDerivedProperties();
                for (SchemaProperty schemaProperty : sderviedpropArr) {
                    out.println("\t+++-> Each derived prop name : " + schemaProperty.getName());
                }

                // TODO anonymus types
                //schema.getAnonymousTypes();

            }
            out.println("-------------------------------------------------------");

            out.println("Output for SchemaTypeSystem " + schematypesys.getName());
        }
    }

    public boolean lookForAttributeInSTS(SchemaTypeSystem tgtSTS,
                                         String sAttrLocalName) {
        // The QName for the find is constructed using the local name since the schemas have no namespace
        SchemaGlobalAttribute sga = tgtSTS.findAttribute(new QName(sAttrLocalName));
        return sga != null;
    }

    public boolean lookForElemInSTS(SchemaTypeSystem tgtSTS,
                                    String sElemLocalName) {
        // The QName for the find is constructed using the local name since the schemas have no namespace
        SchemaGlobalElement sge = tgtSTS.findElement(new QName(sElemLocalName));

        return sge != null;
    }

    public boolean lookForIdentityConstraint(SchemaTypeSystem sts,
                                             String ConstraintLocalName) {

        SchemaIdentityConstraint.Ref icref = sts.findIdentityConstraintRef(new QName(ConstraintLocalName));
        return icref != null;
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

        List<String> diff = new ArrayList<>();
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
        Assert.assertNotNull("Schema File " + sFileWithPath + " Loading failed", schemaFile);
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
        Assert.assertNotNull("Xml Object creation failed", xsdModifiedObj);
        XmlObject[] xobjArr = new XmlObject[]{xsdModifiedObj};

        SchemaTypeSystem returnSTS = XmlBeans.compileXmlBeans(sSTSName, baseSchema, xobjArr, null, builtin, null, options);
        Assert.assertNotNull("Schema Type System created is Null.", returnSTS);

        // validate the XmlObject created
        assertTrue("Return Value for Validate()", xsdModifiedObj.validate());
        return returnSTS;
    }

    // deletes contents of specified directory, does not delete the specified directory
    public void deleteDirRecursive(File dirToClean) {
        if (dirToClean.exists() && dirToClean.isDirectory()) {
            for (File file : Objects.requireNonNull(dirToClean.listFiles())) {
                if (file.isDirectory()) {
                    deleteDirRecursive(file);
                    assertTrue("Output Directory " + file + " Deletion Failed ", file.delete());
                } else if (file.isFile()) {
                    assertTrue("Output File " + file + " Deletion Failed ", file.delete());
                }
            }
        }
    }
}
