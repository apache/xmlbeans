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
package compile.scomp.som.detailed;

import compile.scomp.som.common.SomTestBase;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


public class PartialSOMDetailedTest extends SomTestBase {

    // inherited methods
    @BeforeEach
    public void setUp() throws Exception {
        // initialize the built in schema type
        builtin = XmlBeans.getBuiltinTypeSystem();

        // populate the XmlOptions
        if (errors == null) {
            errors = new ArrayList<>();
        }
        if (options == null) {
            options = (new XmlOptions()).setErrorListener(errors);
            options.setCompileDownloadUrls();
            options.setCompilePartialTypesystem();
            options.setLoadLineNumbers();
        }

        // initialize the runid to be used for generating output files for the PSOM walk thro's
        runid = new Date().getTime();

        // clean the output from the previous run
        // delete directories created by checkPSOMSave() and output text file created by inspectPSOM()
        deleteDirRecursive(new File(somOutputRootDir));
    }

    @AfterEach
    public void tearDown() {
        errors.clear();
    }

    @Test
    void testAddDataTypesList() throws IOException, XmlException {
        System.out.println("Inside test case testAddDataTypesList()");

        // Step 1 : create a PSOM from an incomplete/invalid xsd (datatypes.xsd) with unresolved references to various types
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // recovearble errors should exist
        assertTrue(printRecoveredErrors(), "No Recovered Errors for Invalid Schema");

        // Walk thro the SOM (pass #Elems, #Attr, #Types, #AttrGroups)
        inspectSOM(baseSTS, 12, 1, 4, 1);

        // Test for saving of the PSOM - should not be able to save
        assertFalse(checkPSOMSave(baseSTS), "Partial SOM " + baseSTS.getName() + "Save successful - should fail!");

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS), "Validation against instance Success - should fail ");

        // validate unresolved types - the ListType should resolve to 'anyType'
        assertEquals(anyType, getElementType(baseSTS, "testListTypeElem"), "Unresolved List Type should be 'anyType'");
    }

    @Test
    void testDeleteReusableGroups() throws IOException, XmlException {
        System.out.println("Inside test case testDeleteSubstitutionGroups()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("reusable_grps_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 5, 1);

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "SOM " + baseSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS), "Validation against instance failed");

        // verify named model groups
        assertTrue(getAttributeGroup(baseSTS, "AttributeGroup"), "Attribute Group 'AttributeGroup' should exist");
        assertTrue(getModelGroup(baseSTS, "NamedModelGroup"), "Model Group 'NamedModelGroup' should exist");

        // step2: load an invalid PSOM by deleting the ModelGroup and AttributeGroup definitions commented
        SchemaTypeSystem modifiedSTS = createNewSTS("reusable_grps.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // Recovered Errors, Test for saving of the SOM
        printRecoveredErrors();
        assertFalse(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save Success - should fail!");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 5, 0);

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS), "Validation against instance failed");

        // named model groups
        assertFalse(getAttributeGroup(modifiedSTS, "AttributeGroup"), "Attribute Group 'AttributeGroup' should not exist");
        assertFalse(getModelGroup(modifiedSTS, "NamedModelGroup"), "Model Group 'NamedModelGroup' should not exist");

        // step 3: create a PSOM with the original xsd
        SchemaTypeSystem finalSTS = createNewSTS("groups_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS", sBaseSourceName);
        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(finalSTS), "SOM " + finalSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), finalSTS), "Validation against instance failed");

        // verify named model groups types
        assertTrue(getAttributeGroup(baseSTS, "AttributeGroup"), "Attribute Group 'AttributeGroup' should exist");
        assertTrue(getModelGroup(baseSTS, "NamedModelGroup"), "Model Group 'NamedModelGroup' should exist");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testModifyDataTypesList() throws IOException, XmlException {
        System.out.println("Inside test case testModifyDataTypes()");

        // 1. remove one of the constituent types for the union and test to see if union is anySimpleType

        // Step 1: read in a clean XSD datatypes_added.xsd, to create a base schema with no unresolved components
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 1, 15, 1);

        // Recovered Errors, Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "SOM " + baseSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS), "Validation against instance failed");

        // check types before modify
        assertEquals("attachmentTypes", getElementType(baseSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'attachmentTypes'");
        assertEquals("attchmentExtensionListTypes", getElementType(baseSTS, "testListTypeElem"), "Unresolved List Type should be 'attchmentExtensionListTypes'");
        assertEquals("union.attachmentUnionType", getElementType(baseSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'union.attachmentUnionType");


        //Step 2 : modify types from the schema - should result in STS with unresolved refs
        SchemaTypeSystem modifiedSTS = createNewSTS("datatypes_modified.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // test the PSOM created :walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 13, 1, 13, 1);

        // Test for saving of the PSOM - should not be able to save
        assertFalse(checkPSOMSave(modifiedSTS), "PSOM " + modifiedSTS.getName() + " Save should fail");

        // validate unresolved types
        // list and union types are of type "anyType" and not "anySimpleType
        // https://stackoverflow.com/questions/37801268/what-are-the-restrictions-of-xsdanysimpletype-on-xsdanytype-and-where-are-the
        assertEquals(anyType, getElementType(modifiedSTS, "testAtomicTypeElem"), "Unresolved Simple Type - Atomic should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "testListTypeElem"), "Unresolved List Type should be 'anyType'");
//        assertEquals("Unresolved Simple Type - Union should be 'anySimpleType'",
//            anySimpleType,
//            getElementType(modifiedSTS, "testUnionTypeElem"));

        // validate against an xml valid instance - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_simple_types_invalid.xml"), modifiedSTS), "Validation against instance should Failed ");

        // step 3: reload the original STS
        SchemaTypeSystem finalSTS = createNewSTS("datatypes_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);
        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // walk the SOM
        inspectSOM(finalSTS, 13, 1, 15, 1);

        // validate successful save
        assertTrue(checkPSOMSave(finalSTS), "SOM " + finalSTS.getName() + "Save failed"); // should be able to save as its a valid SOM

        // validate instance - should validate
        assertTrue(validateInstance(getTestCaseFile("instance_simple_types_valid.xml"), finalSTS), "Validation against instance Failed ");

        // check types after modify
        assertEquals("attachmentTypes", getElementType(finalSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'attachmentTypes'");
        assertEquals("attchmentExtensionListTypes", getElementType(finalSTS, "testListTypeElem"), "Unresolved List Type should be 'attchmentExtensionListTypes'");
        assertEquals("union.attachmentUnionType", getElementType(finalSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'union.attachmentUnionType");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testDeleteDerivedTypes() throws IOException, XmlException {
        System.out.println("Inside test case testDeleteDerivedTypes()");

        // Step 1: read in a clean XSD derived_types_added.xsd with base and derived types to create a base schema with no unresolved components
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("derived_types_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "Valid SOM " + baseSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), baseSTS), "Validation against instance failed");

        // check types before deletion of base types
        assertEquals("RestrictionSimpleContentBaseType", getElementType(baseSTS, "RestrictionSimpleContentBaseTypeElem"), "Elem Type  should be 'RestrictionSimpleContentBaseType'");

        // Step 2: create invalid PSOM with base type removed
        SchemaTypeSystem modifiedSTS = createNewSTS("derived_types.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // recovearble errors
        assertTrue(printRecoveredErrors(), "No Recovered Errors for Invalid PSOM");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 13, 0, 9, 0);

        // Recovered Errors, Test for saving of the SOM
        assertFalse(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save Success - should fail!");

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), modifiedSTS), "Validation against instance failed");

        // check types - base should be 'anyType'
        // Restriction Complex Content Base type commented
        assertEquals(anyType, getElementType(modifiedSTS, "RestrictionBaseComplexContentTypeElem"), "Elem Type  should be 'anyType'");


    }

    @Test
    void testModifyReusableGroups() throws IOException, XmlException {
        System.out.println("Inside test case testModifyReusableGroups()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "SOM " + baseSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS), "Validation against instance failed");

        // verify named model groups
        assertTrue(getModelGroup(baseSTS, "NamedModelGroup"), "Model Group 'NamedModelGroup' should exist ");
        assertTrue(getAttributeGroup(baseSTS, "AttributeGroup"), "Attribute Group 'AttributeGroup' should exist");

        // step2: load a modified xsd with type of head elem in subs grp changed
        SchemaTypeSystem modifiedSTS = createNewSTS("reusable_grps_modified.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // Recovered Errors, Test for saving of the SOM  , invalid since grp definitions are commented out
        printRecoveredErrors();
        assertFalse(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save Success - should fail!");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 5, 1);

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS), "Validation against instance failed");

        // verify named model groups
        assertEquals("ModelGrpType", getElementType(modifiedSTS, "ModelGrpTypeElem"), "Elem Type  should be 'ModelGrpType'");
        assertTrue(getAttributeGroup(modifiedSTS, "AttributeGroup"), "Elem Type  should be 'AttributeGroup'");

        // step3 : reload the original xsd
        SchemaTypeSystem finalSTS = createNewSTS("groups_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);
        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(finalSTS), "SOM " + finalSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), finalSTS), "Validation against instance failed");

        // the tests - Walk thro the valid SOM
        inspectSOM(finalSTS, 7, 0, 5, 2);

        // verify named model groups
        assertEquals("ModelGrpType", getElementType(finalSTS, "ModelGrpTypeElem"), "Elem Type  should be 'ModelGrpType'");
        assertTrue(getAttributeGroup(finalSTS, "AttributeGroup"), "Elem Type  should be 'AttributeGroup'");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testModifyDerivedTypes() throws IOException, XmlException {
        System.out.println("Inside test case testModifyDerivedTypes()");

        // Step 1: read in a clean XSD derived_types_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("derived_types_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "Valid SOM " + baseSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), baseSTS), "Validation against instance failed");

        // check types before deletion of base types
        assertEquals("ExtensionBaseType", getElementType(baseSTS, "ExtensionBaseTypeElem"), "Elem Type  should be 'ExtensionBaseType' (base)");
        assertEquals("ExtensionDerivedComplexContentType", getElementType(baseSTS, "ExtensionDerivedComplexContentTypeElem"), "Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)");

        assertEquals("ExtensionBaseMixedContentType", getElementType(baseSTS, "ExtensionBaseMixedContentTypElem"), "Elem Type  should be 'ExtensionBaseMixedContentType' (base)");
        assertEquals("ExtensionDerivedMixedContentType", getElementType(baseSTS, "ExtensionDerivedMixedContentTypeElem"), "Elem Type  should be 'ExtensionDerivedMixedContentType' (derived)");

        assertEquals("RestrictionSimpleContentBaseType", getElementType(baseSTS, "RestrictionSimpleContentBaseTypeElem"), "Elem Type  should be 'RestrictionSimpleContentBaseType'");
        assertEquals("RestrictionSimpleContentDerivedType", getElementType(baseSTS, "RestrictionSimpleContentDerivedTypeElem"), "Elem Type  should be 'RestrictionSimpleContentDerivedType'");

        assertEquals("RestrictionBaseComplexContentType", getElementType(baseSTS, "RestrictionBaseComplexContentTypeElem"), "Elem Type  should be 'RestrictionBaseComplexContentType'");
        assertEquals("RestrictionDerivedComplexContentType", getElementType(baseSTS, "RestrictionDerivedComplexContentTypeElem"), "Elem Type  should be 'RestrictionDerivedComplexContentType'");

        assertEquals("RestrictionBaseMixedContentType", getElementType(baseSTS, "RestrictionBaseMixedContentTypeElem"), "Elem Type  should be 'RestrictionBaseMixedContentType'");
        assertEquals("RestrictionDerivedMixedContentType", getElementType(baseSTS, "RestrictionDerivedMixedContentTypeElem"), "Elem Type  should be 'RestrictionDerivedMixedContentType'");

        assertEquals("RestrictionBaseEmptyContentType", getElementType(baseSTS, "RestrictionBaseEmptyContentTypeElem"), "Elem Type  should be 'RestrictionBaseEmptyContentType'");
        assertEquals("RestrictionDerivedEmptyContentType", getElementType(baseSTS, "RestrictionDerivedEmptyContentTypeElem"), "Elem Type  should be 'RestrictionDerivedEmptyContentType'");


        // step 2 : change the base types now : derived_types_modified.xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("derived_types_modifed.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // no recovearble errors   just added another type
        assertFalse(printRecoveredErrors(), "valid PSOM");

        // the tests - Walk thro the valid SOM
        //inspectSOM(modifiedSTS, 13, 0, 14, 0);
        inspectSOM(modifiedSTS, 13, 0, 17, 0);

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), modifiedSTS), "Validation against instance success - should fail");

        // now validate instance with new base type - this should go thro
        // TODO resolve     this validation
        //Assert.assertTrue("Validation against instance failed",
        //        validateInstance(getTestCaseFile("instance_derived_types_modify.xml"), modifiedSTS));

    }

    @Test
    void testNameSpacesImportFile() throws IOException, XmlException {
        System.out.println("Inside test case testNameSpacesImportFile()");

        // Step 1: read in an xsd that imports from another xsd file providing file name only
        // The source name is not specified as this confuses the dereferecing of the location for the schemaLocation Attribute
        // The absolute rul specified in tbe basename (if specified) would also work.

        //String sBaseSourceName = "file:/D:/SVNNEW/xmlbeans/trunk/test/cases/xbean/compile/som/";
        SchemaTypeSystem baseSTS = createNewSTS("namespaces_import_fileonly.xsd_",
            null,
            "BaseSchemaTS",
            null);

        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors   this should not be a partial Schema
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");
    }

    @Test
    void testNameSpacesWithInclude() throws IOException, XmlException {
        System.out.println("Inside test case testNameSpacesWithInclude()");

        // Step 1: read in an xsd that includes another namespace in xsd file namespaces2.xsd
        //String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("namespaces_include.xsd_",
            null,
            "BaseSchemaTS",
            null);

        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors - this should not be a partial Schema
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "Valid SOM " + baseSTS.getName() + "Save failed!");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 2, 0, 1, 0);


    }

    @Test
    @Disabled
    public void testNameSpacesImportFileWithPath() throws IOException, XmlException {
        System.out.println("Inside test case testNameSpacesImportFileWithPath()");

        //Step 1: read in an xsd that does not have any imports
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("namespaces_noimports.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);

        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors - this should not be a partial Schema
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "Valid SOM " + baseSTS.getName() + "Save failed!");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 1, 0, 0, 0);

        // step 2 : read in an xsd that imports a namespace from another xsd file providing the complete file path for the imported xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("namespaces_import_filepath.xsd",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // no recovearble errors   just added another type
        assertFalse(printRecoveredErrors(), "valid PSOM");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 2, 0, 1, 0);

    }
}




