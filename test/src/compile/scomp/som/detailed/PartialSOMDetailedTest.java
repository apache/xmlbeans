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
import junit.framework.Assert;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;


/**
 *
 *
 */
public class PartialSOMDetailedTest extends SomTestBase
{

    public PartialSOMDetailedTest(String name)
    {
        super(name);
    }

    // inherited methods
    public void setUp() throws Exception
    {
        super.setUp();
        // initialize the built in schema type
        builtin = XmlBeans.getBuiltinTypeSystem();

        // populate the XmlOptions
        if (errors== null) {
            errors = new ArrayList();
        }
        if (options == null) {
            options = (new XmlOptions()).setErrorListener(errors);
            options.setCompileDownloadUrls();
            options.put("COMPILE_PARTIAL_TYPESYSTEM");
            options.setLoadLineNumbers();
        }

        // initialize the runid to be used for generating output files for the PSOM walk thro's
        runid = new Date().getTime();

        // clean the output from the previous run
        // delete directories created by checkPSOMSave() and output text file created by inspectPSOM()
        deleteDirRecursive(new File(somOutputRootDir));
    }

    public void tearDown() throws Exception
    {
        errors.clear();
        super.tearDown();
    }

    // TODO: all of add/del/modify
    public void testAddDataTypesList() throws Exception
    {
        System.out.println("Inside test case testAddDataTypesList()");

        // Step 1 : create a PSOM from an incomplete/invalid xsd (datatypes.xsd) with unresolved references to various types
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // recovearble errors should exist
        Assert.assertTrue("No Recovered Errors for Invalid Schema",
                printRecoveredErrors());

        // Walk thro the SOM (pass #Elems, #Attr, #Types, #AttrGroups)
        inspectSOM(baseSTS, 12, 1, 4, 1);

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("Partial SOM " + baseSTS.getName() + "Save successful - should fail!",
                checkPSOMSave(baseSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance Success - should fail ",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS));

        // validate unresolved types - the ListType should resolve to 'anySimpleType'
        Assert.assertEquals("Unresolved List Type should be 'anySimpleType'",
                anySimpleType,
                getElementType(baseSTS, "testListTypeElem"));
    }

    public void testDeleteReusableGroups() throws Exception
    {
        System.out.println("Inside test case testDeleteSubstitutionGroups()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups_added.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS));

        // verify named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(baseSTS, "ModelGrpTypeElem"));
        Assert.assertEquals("Elem Type  should be 'AttributeGroup'",
                "AttributeGroup",
                getAttributeGroup(baseSTS, "AttributeGroup"));

        // step2: load an invalid PSOM by deleting the ModelGroup and AttributeGroup definitions commented
        SchemaTypeSystem modifiedSTS = createNewSTS("reusable_grps.xsd",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // Recovered Errors, Test for saving of the SOM
        Assert.assertFalse("SOM " + modifiedSTS.getName() + "Save Success - should fail!",
                checkPSOMSave(modifiedSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 4, 1);

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS));

        // named model groups
        Assert.assertEquals("Elem Type  should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "ModelGrpTypeElem"));
        Assert.assertEquals("Elem Type  should be 'anyType'",
                anyType,
                getAttributeGroup(modifiedSTS, "AttributeGroup"));

        // step 3: create a PSOM with the original xsd
        SchemaTypeSystem finalSTS = createNewSTS("groups_added.xsd",
                modifiedSTS,
                "FinalSchemaTS", sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed!",
                checkPSOMSave(finalSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), finalSTS));

        // verify named model groups types
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(baseSTS, "ModelGrpTypeElem"));
        Assert.assertEquals("Elem Type  should be 'AttributeGroup'",
                "AttributeGroup",
                getAttributeGroup(baseSTS, "AttributeGroup"));

        // TODO compare this to the original schema here
    }

    public void testModifyDataTypesList() throws Exception
    {
        System.out.println("Inside test case testModifyDataTypes()");

        // 1. remove one of the constituent types for the union and test to see if union is anySimpleType

        // Step 1: read in a clean XSD datatypes_added.xsd, to create a base schema with no unresolved components
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes_added.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 1, 15, 1);

        // Recovered Errors, Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS));

        // check types before modify
        Assert.assertEquals("Unresolved Simple Type should be 'attachmentTypes'",
                "attachmentTypes",
                getElementType(baseSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'attchmentExtensionListTypes'",
                "attchmentExtensionListTypes",
                getElementType(baseSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'union.attachmentUnionType",
                "union.attachmentUnionType",
                getElementType(baseSTS, "testUnionTypeElem"));


        //Step 2 : modify types from the schema - should result in STS with unresolved refs
        SchemaTypeSystem modifiedSTS = createNewSTS("datatypes_modified.xsd",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // test the PSOM created :walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 13, 1, 13, 1);

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("PSOM " + modifiedSTS.getName() + " Save should fail",
                checkPSOMSave(modifiedSTS));

        // validate unresolved types
        Assert.assertEquals("Unresolved Simple Type - Atomic should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'anySimpleType'",
                anySimpleType,
                getElementType(modifiedSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Simple Type - Union should be 'anySimpleType'",
                anySimpleType,
                getElementType(modifiedSTS, "testUnionTypeElem"));

        // validate against an xml valid instance - should fail
        Assert.assertFalse("Validation against instance should Failed ",
                validateInstance(getTestCaseFile("instance_simple_types_valid.xml"), modifiedSTS));

        // step 3: reload the original STS
        SchemaTypeSystem finalSTS = createNewSTS("datatypes_added.xsd",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // walk the SOM
        inspectSOM(finalSTS, 13, 1, 15, 1);

        // validate successful save
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed",
                checkPSOMSave(finalSTS)); // should be able to save as its a valid SOM

        // validate instance - should validate
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_simple_types_valid.xml"), finalSTS));

        // check types after modify
        Assert.assertEquals("Unresolved Simple Type should be 'attachmentTypes'",
                "attachmentTypes",
                getElementType(finalSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'attchmentExtensionListTypes'",
                "attchmentExtensionListTypes",
                getElementType(finalSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'union.attachmentUnionType",
                "union.attachmentUnionType",
                getElementType(finalSTS, "testUnionTypeElem"));

        // TODO compare this to the original schema here
    }

    public void testDeleteDerivedTypes() throws Exception
    {
        System.out.println("Inside test case testDeleteDerivedTypes()");

        // Step 1: read in a clean XSD derived_types_added.xsd with base and derived types to create a base schema with no unresolved components
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("derived_types_added.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        Assert.assertTrue("Valid SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), baseSTS));

        // check types before deletion of base types
        Assert.assertEquals("Elem Type  should be 'RestrictionSimpleContentBaseType'",
                "RestrictionSimpleContentBaseType", getElementType(baseSTS, "RestrictionSimpleContentBaseTypeElem"));

        // Step 2: create invalid PSOM with base type removed
        SchemaTypeSystem modifiedSTS = createNewSTS("derived_types.xsd",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // recovearble errors
        Assert.assertTrue("No Recovered Errors for Invalid PSOM",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 13, 0, 9, 0);

        // Recovered Errors, Test for saving of the SOM
        Assert.assertEquals("SOM " + modifiedSTS.getName() + "Save Success - should fail!",
                false, checkPSOMSave(modifiedSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), modifiedSTS));

        // check types - base should be 'anyType'
        // Restriction Simple Content Base type commented does not result in recoverable SOM
        Assert.assertEquals("Elem Type  should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "RestrictionSimpleContentBaseTypeElem"));


    }

    public void testModifyReusableGroups() throws Exception
    {
        System.out.println("Inside test case testModifyReusableGroups()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups_added.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        Assert.assertEquals("SOM " + baseSTS.getName() + "Save failed!", true, checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertEquals("Validation against instance failed", true, validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS));

        // verify named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'", "ModelGrpType",
                getElementType(baseSTS, "ModelGrpTypeElem"));
        Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup",
                getAttributeGroup(baseSTS, "AttributeGroup"));

        // step2: load a modified xsd with type of head elem in subs grp changed
        SchemaTypeSystem modifiedSTS = createNewSTS("reusable_grps_modified.xsd",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // Recovered Errors, Test for saving of the SOM    - still a valid PSOM
        Assert.assertTrue("SOM " + modifiedSTS.getName() + "Save Success - should fail!",
                checkPSOMSave(modifiedSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 5, 2);

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS));

        // verify named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(modifiedSTS, "ModelGrpTypeElem"));
        Assert.assertEquals("Elem Type  should be 'AttributeGroup'",
                "AttributeGroup",
                getAttributeGroup(modifiedSTS, "AttributeGroup"));

        // step3 : reload the original xsd
        SchemaTypeSystem finalSTS = createNewSTS("groups_added.xsd",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed!",
                checkPSOMSave(finalSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), finalSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(finalSTS, 7, 0, 5, 2);

        // verify named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(finalSTS, "ModelGrpTypeElem"));
        Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup",
                getAttributeGroup(finalSTS, "AttributeGroup"));

        // TODO compare this to the original schema here
    }

    public void testModifyDerivedTypes() throws Exception
    {
        System.out.println("Inside test case testModifyDerivedTypes()");

        // Step 1: read in a clean XSD derived_types_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("derived_types_added.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        Assert.assertTrue("Valid SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), baseSTS));

        // check types before deletion of base types
        Assert.assertEquals("Elem Type  should be 'ExtensionBaseType' (base)",
                "ExtensionBaseType",
                getElementType(baseSTS, "ExtensionBaseTypeElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)",
                "ExtensionDerivedComplexContentType",
                getElementType(baseSTS, "ExtensionDerivedComplexContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'ExtensionBaseMixedContentType' (base)",
                "ExtensionBaseMixedContentType",
                getElementType(baseSTS, "ExtensionBaseMixedContentTypElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedMixedContentType' (derived)",
                "ExtensionDerivedMixedContentType",
                getElementType(baseSTS, "ExtensionDerivedMixedContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionSimpleContentBaseType'",
                "RestrictionSimpleContentBaseType",
                getElementType(baseSTS, "RestrictionSimpleContentBaseTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionSimpleContentDerivedType'",
                "RestrictionSimpleContentDerivedType",
                getElementType(baseSTS, "RestrictionSimpleContentDerivedTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionBaseComplexContentType'",
                "RestrictionBaseComplexContentType",
                getElementType(baseSTS, "RestrictionBaseComplexContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedComplexContentType'",
                "RestrictionDerivedComplexContentType",
                getElementType(baseSTS, "RestrictionDerivedComplexContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionBaseMixedContentType'",
                "RestrictionBaseMixedContentType",
                getElementType(baseSTS, "RestrictionBaseMixedContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedMixedContentType'",
                "RestrictionDerivedMixedContentType",
                getElementType(baseSTS, "RestrictionDerivedMixedContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionBaseEmptyContentType'",
                "RestrictionBaseEmptyContentType",
                getElementType(baseSTS, "RestrictionBaseEmptyContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedEmptyContentType'",
                "RestrictionDerivedEmptyContentType",
                getElementType(baseSTS, "RestrictionDerivedEmptyContentTypeElem"));


        // step 2 : change the base types now : derived_types_modified.xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("derived_types_modifed.xsd",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // no recovearble errors   just added another type
        Assert.assertFalse("valid PSOM",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        //inspectSOM(modifiedSTS, 13, 0, 14, 0);
        inspectSOM(modifiedSTS, 13, 0, 15, 0);

        // instance validation - should fail
        Assert.assertFalse("Validation against instance success - should fail",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), modifiedSTS));

        // now validate instance with new base type - this should go thro
        // TODO resolve     this validation
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_modify.xml"), modifiedSTS));

    }
    public void testNameSpacesImportFile() throws Exception
    {
        System.out.println("Inside test case testNameSpacesImportFile()");

        // Step 1: read in an xsd that imports from another xsd file providing file name only
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("namespaces_import_fileonly.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors   this should not be a partial Schema
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

    }

    public void testNameSpacesImportFileWithPath() throws Exception
    {
        System.out.println("Inside test case testNameSpacesImportFileWithPath()");

        //Step 1: read in an xsd that does not have any imports
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("namespaces_noimports.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors - this should not be a partial Schema
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("Valid SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 1, 0, 0, 0);

        // step 2 : read in an xsd that imports a namespace from another xsd file providing the complete file path for the imported xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("namespaces_import_filepath.xsd",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // no recovearble errors   just added another type
        Assert.assertFalse("valid PSOM",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 2, 0, 1, 0);

    }

    public void testNameSpacesWithInclude() throws Exception
    {
        System.out.println("Inside test case testNameSpacesWithInclude()");

        // Step 1: read in an xsd that includes another namespace in xsd file namespaces2.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("namespaces_include.xsd",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors - this should not be a partial Schema
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("Valid SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 2, 0, 1, 0);


    }

}




