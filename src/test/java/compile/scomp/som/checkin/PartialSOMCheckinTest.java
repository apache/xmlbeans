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

package compile.scomp.som.checkin;

import compile.scomp.som.common.SomTestBase;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PartialSOMCheckinTest extends SomTestBase {
    @BeforeEach
    public void setUp() {
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
    void testAddAttributeAndElements() throws Exception {
        System.out.println("Inside test case testAddAttributeAndElements()");

        // Step 1 : create a Schema Type System with the base 'bad' xsd and create the Schema Type System (STS) for it
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("elemattr.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);

        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 1, 1, 1, 0);

        // test for recoverable errors
        assertTrue(printRecoveredErrors(), "No Recovered Errors for Invalid Schema");

        // Test for saving of the PSOM - should not be able to save
        assertFalse(checkPSOMSave(baseSTS), "Partial SOM " + baseSTS.getName() + "Save successful - should fail!");

        // instance validation - should fail
        List<XmlError> errList = new ArrayList<>();
        assertFalse(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), baseSTS, errList), "Validation against instance Success - should fail ");
        assertEquals(3, errList.size());
        assertTrue(errList.get(0).toString().contains("instance_elemattr_valid.xml:20:36: error: cvc-complex-type.3.2.1: Attribute not allowed (no wildcards allowed): testAttributeComplex in element TestRoot"));

        // additional validation
        assertFalse(lookForAttributeInSTS(baseSTS,
            "testAttributeComplex"), "Attribute found but not expected - 'testAttributeComplex'");
        assertFalse(lookForElemInSTS(baseSTS,
            "ComplexTypeElem"), "Element found but not expected 'ComplexTypeElem'");
        assertFalse(lookForElemInSTS(baseSTS, "SimpleTypeElem"), "Element found but not expected  'SimpleTypeElem'");


        // Step 2: create a Schema Type System with the new xsd file that has additions to this schema
        SchemaTypeSystem modifiedSTS = createNewSTS("elemattr_added.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);

        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 3, 2, 1, 0);

        // Test for successful saving of the PSOM
        assertTrue(checkPSOMSave(modifiedSTS), "Valid Partial SOM " + modifiedSTS.getName() + "Save failed");

        // Look for  added attribute(s)/Element(s) by name in the STS
        assertTrue(lookForAttributeInSTS(modifiedSTS,
            "testAttributeComplex"), "Attribute expected, not found 'testAttributeComplex'");
        assertTrue(lookForElemInSTS(modifiedSTS,
            "ComplexTypeElem"), "Element expected, not found 'ComplexTypeElem'");
        assertTrue(lookForElemInSTS(modifiedSTS,
            "SimpleTypeElem"), "Element expected, not found 'SimpleTypeElem'");

        // validate against an xml instance
        assertTrue(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), modifiedSTS), "Validation against instance failed ");

        // Step 3: now creat the Schema Type System with the original XSD again
        SchemaTypeSystem finalSTS = createNewSTS("elemattr.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);

        assertNotNull(finalSTS, "Schema Type System created is Null.");

        //walk the SOM
        inspectSOM(finalSTS, 1, 1, 1, 0);

        // Test for saving of the PSOM - should not be able to save
        assertFalse(checkPSOMSave(finalSTS), "Partial SOM " + finalSTS.getName() + "Save successful - should fail!");

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_elemattr.xml"), finalSTS), "Validation against instance Success - should fail ");

    }

    @Test
    void testModifyAttributeAndElements() throws Exception {
        System.out.println("Inside test case testModifyAttributeAndElements()");

        // Step 1 : create a Schema Type System with the base 'good' xsd and create the Schema Type System (STS) for it
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("elemattr_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);

        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 3, 2, 1, 0);

        // validate successful save
        assertTrue(checkPSOMSave(baseSTS), "Valid SOM " + baseSTS.getName() + "Save failed ");

        // validate against instance successfully
        assertTrue(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), baseSTS), "Validation against instance Failed ");

        // Step 2: create a Schema Type System with the new xsd file with modifications to existing schema
        SchemaTypeSystem modifiedSTS = createNewSTS("elemattr_modified.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);

        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // test for recoverable errors
        assertTrue(printRecoveredErrors(), "No Recovered Errors for Invalid Schema");

        // test the PSOM created
        inspectSOM(modifiedSTS, 2, 2, 1, 0); // walk thro the PSOM, look for # of elements,attributes,types & attribute groups

        // Look for a modified attribute(s)/elements by name in the STS
        assertTrue(lookForAttributeInSTS(modifiedSTS, "testAttributeComplex"), "Attribute expected, not found 'testAttributeComplex'");
        assertTrue(lookForElemInSTS(modifiedSTS, "ComplexTypeElem"), "Element expected, not found 'ComplexTypeElem'");
        assertFalse(lookForElemInSTS(modifiedSTS, "SimpleTypeElem"), "Element expected, not found 'SimpleTypeElem'");

        // Test for saving of the PSOM - should not be able to save
        assertFalse(checkPSOMSave(modifiedSTS), "Partial SOM " + modifiedSTS.getName() + " Save successful- should fail");

        // validate against an xml instance - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), modifiedSTS), "Validation against instance Success - should Fail");

        // Step 3: now creat the Schema Type System with the original XSD again
        SchemaTypeSystem finalSTS = createNewSTS("elemattr_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);

        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // walk the PSOM
        inspectSOM(finalSTS, 3, 2, 1, 0);

        // should be able to save as its a valid SOM
        assertTrue(checkPSOMSave(finalSTS), "Partial SOM " + finalSTS.getName() + "Save failed for complete SOM");

        // validate against instance successfully
        assertTrue(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), finalSTS), "Validation against instance Failed ");

        // compare this to the original schema here - the root dir names used to save the PSOMs are the same as the STS names
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testDeleteAttributeAndElements() throws Exception {
        System.out.println("Inside test case testDeleteAttributeAndElements()");

        // Step 1 : create a Schema Type System with the base 'good' xsd and create the Schema Type System (STS) for it
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("elemattr_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);

        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 3, 2, 1, 0);

        // validate successful save
        assertTrue(checkPSOMSave(baseSTS), "Valid SOM " + baseSTS.getName() + "Save failed ");

        // validate against instance successfully
        assertTrue(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), baseSTS), "Validation against instance Failed ");

        // Step 2: create a Schema Type System with the new xsd file that has deletions
        SchemaTypeSystem modifiedSTS = createNewSTS("elemattr.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);

        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // test for recoverable errors
        assertTrue(printRecoveredErrors(), "No Recovered Errors for Invalid Schema");

        // test the PSOM created
        inspectSOM(modifiedSTS, 1, 1, 1, 0); // walk thro the PSOM, look for # of elements,attributes,types & attribute groups

        // Test for saving of the PSOM - should not be able to save
        assertFalse(checkPSOMSave(modifiedSTS), "Invalid PSOM " + modifiedSTS.getName() + " Save successful - Should fail");

        // verify types
        assertFalse(lookForAttributeInSTS(modifiedSTS, "testAttributeComplex"), "Attribute found but not expected - 'testAttributeComplex'");
        assertFalse(lookForElemInSTS(modifiedSTS, "ComplexTypeElem"), "Element found but not expected 'ComplexTypeElem'");
        assertFalse(lookForElemInSTS(modifiedSTS, "SimpleTypeElem"), "Element found but not expected  'SimpleTypeElem'");

        // validate against an xml instance - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), modifiedSTS), "Validation against success - should Fail ");

        // Step 3: now creat the Schema Type System with the original XSD again
        SchemaTypeSystem finalSTS = createNewSTS("elemattr_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);

        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // walk the SOM
        inspectSOM(finalSTS, 3, 2, 1, 0);

        // should be able to save as its a valid SOM
        assertTrue(checkPSOMSave(finalSTS), "Partial SOM " + finalSTS.getName() + "Save failed for complete SOM");

        // validate against instance
        assertTrue(validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), finalSTS), "Validation against instance Failed ");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));

    }

    @Test
    void testAddDataTypes() throws Exception {
        System.out.println("Inside test case testAddDataTypes()");
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

        // additional validation - check to see if the unresolved references to types are 'anyType'
        // validate unresolved types
        assertEquals(anyType, getElementType(baseSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'anyType'");
        assertEquals(anyType, getElementType(baseSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'anyType'");

        // moved to detailed PSOMDetailedTest class
        //Assert.assertEquals("Unresolved List Type should be 'anySimpleType'", anySimpleType, getElementType(baseSTS, "testListTypeElem"));
        assertEquals(anyType, getElementType(baseSTS, "testComplexTypeSimpleContentElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(baseSTS, "testComplexTypeElementOnlyContentElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(baseSTS, "testComplexTypeMixedElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(baseSTS, "testComplexTypeEmptyElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(baseSTS, "testChoiceGroupElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(baseSTS, "testAllGroupElem"), "Unresolved Complex Type should be 'anyType'");

        // Step 2 : create an incremental PSOM that is valid by loading datatypes_added.xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("datatypes_added.xsd_", baseSTS, "ModifiedSchemaTS", sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // no errors expected to be recovered - should be a valid SOM
        assertFalse(printRecoveredErrors(), "Valid Schema Type System, Errors recovered");

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 13, 1, 15, 1);

        // test successful save
        assertTrue(checkPSOMSave(modifiedSTS), "Valid SOM " + modifiedSTS.getName() + " Save failed");

        // validate against an xml valid instance - should succeed
        assertTrue(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), modifiedSTS), "Validation against instance Failed ");

        // validate against an xml invalid instance - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_simple_types_invalid.xml"), modifiedSTS), "Validation against instance Failed ");

        // additional validation - check to see if all types are resolved to their respective types
        assertEquals("attachmentTypes", getElementType(modifiedSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'attachmentTypes'");
        assertEquals("union.attachmentUnionType", getElementType(modifiedSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'union.attachmentUnionType'");
        assertEquals("attchmentExtensionListTypes", getElementType(modifiedSTS, "testListTypeElem"), "Unresolved List Type should be 'attchmentExtensionListTypes'");
        assertEquals("headerType", getElementType(modifiedSTS, "testComplexTypeSimpleContentElem"), "Unresolved Complex Type should be 'headerType'");
        assertEquals("mailsType", getElementType(modifiedSTS, "testComplexTypeElementOnlyContentElem"), "Unresolved Complex Type should be 'mailsType'");
        assertEquals("mixedContentType", getElementType(modifiedSTS, "testComplexTypeMixedElem"), "Unresolved Complex Type should be 'mixedContentType'");
        assertEquals("emptyContentType", getElementType(modifiedSTS, "testComplexTypeEmptyElem"), "Unresolved Complex Type should be 'emptyContentType'");
        assertEquals("choiceGroupType", getElementType(modifiedSTS, "testChoiceGroupElem"), "Unresolved Complex Type should be 'choiceGroupType'");
        assertEquals("allGroupType", getElementType(modifiedSTS, "testAllGroupElem"), "Unresolved Complex Type should be 'allGroupType'");


        // Step 3 : create an incremental STS with the file in step 1
        SchemaTypeSystem finalSTS = createNewSTS("datatypes.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);

        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(finalSTS, 12, 1, 4, 1);

        // test save failure
        assertFalse(checkPSOMSave(finalSTS), "Partial SOM " + finalSTS.getName() + "Save Success ");

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), finalSTS), "Validation against instance Success - should fail ");

    }

    @Test
    void testDeleteDataTypes() throws Exception {
        System.out.println("Inside test case testDeleteDataTypes()");

        // Step 1: read a clean XSD file to get a valid SOM
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);

        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 13, 1, 15, 1);

        // Recovered Errors, Test for saving of the PSOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "SOM " + baseSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS), "Validation against instance failed");

        // additional validation - check to see if all types are resolved to their respective types
        assertEquals("attachmentTypes", getElementType(baseSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'attachmentTypes'");
        assertEquals("union.attachmentUnionType", getElementType(baseSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'union.attachmentUnionType'");
        assertEquals("attchmentExtensionListTypes", getElementType(baseSTS, "testListTypeElem"), "Unresolved List Type should be 'attchmentExtensionListTypes'");
        assertEquals("headerType", getElementType(baseSTS, "testComplexTypeSimpleContentElem"), "Unresolved Complex Type should be 'headerType'");
        assertEquals("mailsType", getElementType(baseSTS, "testComplexTypeElementOnlyContentElem"), "Unresolved Complex Type should be 'mailsType'");
        assertEquals("mixedContentType", getElementType(baseSTS, "testComplexTypeMixedElem"), "Unresolved Complex Type should be 'mixedContentType'");
        assertEquals("emptyContentType", getElementType(baseSTS, "testComplexTypeEmptyElem"), "Unresolved Complex Type should be 'emptyContentType'");
        assertEquals("choiceGroupType", getElementType(baseSTS, "testChoiceGroupElem"), "Unresolved Complex Type should be 'choiceGroupType'");
        assertEquals("allGroupType", getElementType(baseSTS, "testAllGroupElem"), "Unresolved Complex Type should be 'allGroupType'");


        //Step 2 : delete/remove types from the schema - should result in STS with unresolved refs
        SchemaTypeSystem modifiedSTS = createNewSTS("datatypes.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // PSOM - recovered errors are expected
        assertTrue(printRecoveredErrors(), "Valid Schema Type System, Errors recovered");

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 12, 1, 4, 1);

        // Test for saving of the PSOM - should not be able to save
        assertFalse(checkPSOMSave(modifiedSTS), "PSOM " + modifiedSTS.getName() + " Save should fail");

        // validate unresolved types
        assertEquals(anyType, getElementType(modifiedSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'anyType'");
        // moved to detailed PSOMDetailedTest class
        // Assert.assertEquals("Unresolved List Type should be 'anySimpleType'", anySimpleType, getElementType(modifiedSTS, "testListTypeElem"));
        assertEquals(anyType, getElementType(modifiedSTS, "testComplexTypeSimpleContentElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "testComplexTypeElementOnlyContentElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "testComplexTypeMixedElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "testComplexTypeEmptyElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "testChoiceGroupElem"), "Unresolved Complex Type should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "testAllGroupElem"), "Unresolved Complex Type should be 'anyType'");

        // validate against an xml valid instance - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), modifiedSTS), "Validation against instance should Failed ");

        // Step 3 : reaload the xsd in Step 1
        SchemaTypeSystem finalSTS = createNewSTS("datatypes_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);

        // should be able to save as its a valid SOM
        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // walk the PSOM
        inspectSOM(finalSTS, 13, 1, 15, 1);

        // should be able to save as its a valid SOM
        assertTrue(checkPSOMSave(finalSTS), "SOM " + finalSTS.getName() + "Save failed");

        // instance validation - should be fine
        assertTrue(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), finalSTS), "Validation against instance Failed ");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));

        // additional validation - check to see if all types are resolved to their respective types
        assertEquals("attachmentTypes", getElementType(baseSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'attachmentTypes'");
        assertEquals("union.attachmentUnionType", getElementType(baseSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'union.attachmentUnionType'");
        assertEquals("attchmentExtensionListTypes", getElementType(baseSTS, "testListTypeElem"), "Unresolved List Type should be 'attchmentExtensionListTypes'");
        assertEquals("headerType", getElementType(baseSTS, "testComplexTypeSimpleContentElem"), "Unresolved Complex Type should be 'headerType'");
        assertEquals("mailsType", getElementType(baseSTS, "testComplexTypeElementOnlyContentElem"), "Unresolved Complex Type should be 'mailsType'");
        assertEquals("mixedContentType", getElementType(baseSTS, "testComplexTypeMixedElem"), "Unresolved Complex Type should be 'mixedContentType'");
        assertEquals("emptyContentType", getElementType(baseSTS, "testComplexTypeEmptyElem"), "Unresolved Complex Type should be 'emptyContentType'");
        assertEquals("choiceGroupType", getElementType(baseSTS, "testChoiceGroupElem"), "Unresolved Complex Type should be 'choiceGroupType'");
        assertEquals("allGroupType", getElementType(baseSTS, "testAllGroupElem"), "Unresolved Complex Type should be 'allGroupType'");

    }

    @Test
    void testModifyDataTypes() throws Exception {
        System.out.println("Inside test case testModifyDataTypes()");

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
        //remove one of the constituent types for the union and test to see if union is anySimpleType
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
        assertEquals(anyType, getElementType(modifiedSTS, "testAtomicTypeElem"), "Unresolved Simple Type - Atomic should be 'anyType'");

        // moved to detailed PSOMDetailedTest class
        //Assert.assertEquals("Unresolved List Type should be 'anySimpleType'", anySimpleType, getElementType(modifiedSTS, "testListTypeElem"));
        //Assert.assertEquals("Unresolved Simple Type - Union should be 'anySimpleType'", anySimpleType, getElementType(modifiedSTS, "testUnionTypeElem"));

        // validate against an xml valid instance - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), modifiedSTS), "Validation against instance should Failed ");

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
        assertTrue(validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), finalSTS), "Validation against instance Failed ");

        // check types after modify
        assertEquals("attachmentTypes", getElementType(finalSTS, "testAtomicTypeElem"), "Unresolved Simple Type should be 'attachmentTypes'");
        assertEquals("attchmentExtensionListTypes", getElementType(finalSTS, "testListTypeElem"), "Unresolved List Type should be 'attchmentExtensionListTypes'");
        assertEquals("union.attachmentUnionType", getElementType(finalSTS, "testUnionTypeElem"), "Unresolved Simple Type should be 'union.attachmentUnionType");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testDeleteDerivedTypes() throws Exception {
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
        assertEquals(anyType, getElementType(modifiedSTS, "ExtensionBaseTypeElem"), "Elem Type  should be 'anyType' (base)");
        assertEquals("ExtensionDerivedComplexContentType", getElementType(modifiedSTS, "ExtensionDerivedComplexContentTypeElem"), "Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)");

        assertEquals(anyType, getElementType(modifiedSTS, "ExtensionBaseMixedContentTypElem"), "Elem Type  should be 'anyType' (base)");
        assertEquals("ExtensionDerivedMixedContentType", getElementType(modifiedSTS, "ExtensionDerivedMixedContentTypeElem"), "Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)");

        // Restriction Simple Content Base type commented does not result in recoverable SOM
        // moved to Detailed Test
        //Assert.assertEquals("Elem Type  should be 'anyType'",
        //        anyType,
        //        getElementType(modifiedSTS, "RestrictionSimpleContentBaseTypeElem"));
        //

        assertEquals("RestrictionSimpleContentDerivedType", getElementType(modifiedSTS, "RestrictionSimpleContentDerivedTypeElem"), "Elem Type  should be 'RestrictionSimpleContentDerivedType'");

        assertEquals(anyType, getElementType(modifiedSTS, "RestrictionBaseComplexContentTypeElem"), "Elem Type  should be 'anyType'");
        assertEquals("RestrictionDerivedComplexContentType", getElementType(modifiedSTS, "RestrictionDerivedComplexContentTypeElem"), "Elem Type  should be 'RestrictionDerivedComplexContentType'");

        assertEquals(anyType, getElementType(modifiedSTS, "RestrictionBaseMixedContentTypeElem"), "Elem Type  should be 'anyType'");
        assertEquals("RestrictionDerivedMixedContentType", getElementType(modifiedSTS, "RestrictionDerivedMixedContentTypeElem"), "Elem Type  should be 'RestrictionDerivedMixedContentType'");

        assertEquals(anyType, getElementType(modifiedSTS, "RestrictionBaseEmptyContentTypeElem"), "Elem Type  should be 'anyType'");
        assertEquals("RestrictionDerivedEmptyContentType", getElementType(modifiedSTS, "RestrictionDerivedEmptyContentTypeElem"), "Elem Type  should be 'RestrictionDerivedEmptyContentType'");


        // step 3: reload the original STS
        SchemaTypeSystem finalSTS = createNewSTS("derived_types_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);
        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(finalSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(finalSTS), "SOM " + finalSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), finalSTS), "Validation against instance failed");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testAddDerivedTypes() throws Exception {
        System.out.println("Inside test case testAddDerivedTypes()");

        // Step 1: start with invalid SOM - one that has derived types but the base types are not defined
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("derived_types.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        assertTrue(printRecoveredErrors(), "No Recovered Errors for Invalid PSOM");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 0, 9, 0);

        // Recovered Errors, Test for saving of the SOM
        assertFalse(checkPSOMSave(baseSTS), "SOM " + baseSTS.getName() + "Save Success - should fail!");

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), baseSTS), "Validation against instance failed");

        // check types - base should be 'anyType'
        assertEquals(anyType, getElementType(baseSTS, "ExtensionBaseTypeElem"), "Elem Type  should be 'anyType' (base)");
        assertEquals("ExtensionDerivedComplexContentType", getElementType(baseSTS, "ExtensionDerivedComplexContentTypeElem"), "Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)");

        assertEquals(anyType, getElementType(baseSTS, "ExtensionBaseMixedContentTypElem"), "Elem Type  should be 'anyType' (base)");
        assertEquals("ExtensionDerivedMixedContentType", getElementType(baseSTS, "ExtensionDerivedMixedContentTypeElem"), "Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)");

        // Step 2: create valid PSOM now  from xsd with base types defined
        SchemaTypeSystem modifiedSTS = createNewSTS("derived_types_added.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), modifiedSTS), "Validation against instance failed");

        // check types before deletion of base types
        assertEquals("ExtensionBaseType", getElementType(modifiedSTS, "ExtensionBaseTypeElem"), "Elem Type  should be 'ExtensionBaseType' (base)");
        assertEquals("ExtensionDerivedComplexContentType", getElementType(modifiedSTS, "ExtensionDerivedComplexContentTypeElem"), "Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)");
        assertEquals("ExtensionBaseMixedContentType", getElementType(modifiedSTS, "ExtensionBaseMixedContentTypElem"), "Elem Type  should be 'ExtensionBaseMixedContentType' (base)");
        assertEquals("ExtensionDerivedMixedContentType", getElementType(modifiedSTS, "ExtensionDerivedMixedContentTypeElem"), "Elem Type  should be 'ExtensionDerivedMixedContentType' (derived)");

    }

    // moved to PSOMDetaiedTest
    //public void testDeleteReusableGroups() throws Exception
    //{}
    //public void testModifyReusableGroups() throws Exception
    @Test
    void testAddReusableGroups() throws Exception {
        System.out.println("Inside test case testAddReusableGroups()");

        // Step 1: read in invalid XSD groups.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // recovearble errors
        assertTrue(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 4, 1);

        // Recovered Errors, Test for saving of the SOM - should fail
        assertFalse(checkPSOMSave(baseSTS), "Partial SOM " + baseSTS.getName() + "Save successful - should failed!");

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS), "Validation against instance failed");

        // verify types
        // named model groups
        assertEquals("ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"), "Elem Type  should be 'ModelGrpType'");
        assertTrue(getAttributeGroup(baseSTS, "AttributeGroup"), "Elem Type  should be 'AttributeGroup'");

        // Step 2: create a SOM with valid xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("groups_added.xsd_",
            baseSTS,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS), "Validation against instance failed");

        // verify named model groups
        assertEquals("ModelGrpType", getElementType(modifiedSTS, "ModelGrpTypeElem"), "Elem Type  should be 'ModelGrpType'");
        assertTrue(getAttributeGroup(modifiedSTS, "AttributeGroup"), "Elem Type  should be 'AttributeGroup'");


    }

    @Test
    void testAddSubstitutionGroups() throws Exception {
        System.out.println("Inside test case testAddSubstitutionGroups()");

        // step1: load an invalid PSOM by with incomplete/missing Subst Grp head elem definition
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // errors recovered
        assertTrue(printRecoveredErrors(), "No Recovered Errors for recovered PSOM");

        // Recovered Errors, Test for saving of the SOM
        assertFalse(checkPSOMSave(baseSTS), "SOM " + baseSTS.getName() + "Save Success - should fail!");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 4, 1);

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS), "Validation against instance failed");

        // verify types
        assertEquals(anyType, getElementType(baseSTS, "SubGrpHeadElem"), "Elem Type  should be 'anyType'");
        assertEquals(anyType, getElementType(baseSTS, "SubGrpMemberElem1"), "Elem Type  should be 'anyType' (Member of Sub. Group)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(baseSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

        // named model groups
        assertEquals("ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"), "Elem Type  should be 'ModelGrpType'");
        assertTrue(getAttributeGroup(baseSTS, "AttributeGroup"), "Elem Type  should be 'AttributeGroup'");

        // Step 2: create a valid SOM and add to these
        SchemaTypeSystem modifiedSTS = createNewSTS("groups_added.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS), "Validation against instance failed");

        // verify types - substitution groups
        assertEquals("SubGrpHeadElemType", getElementType(modifiedSTS, "SubGrpHeadElem"), "Elem Type  should be 'SubGrpHeadElemType' (base)");
        assertEquals("SubGrpHeadElemType", getElementType(modifiedSTS, "SubGrpMemberElem1"), "Elem Type  should be 'SubGrpHeadElemType' (derived)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(modifiedSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

        // named model groups - moved to check in test
        //Assert.assertEquals("Elem Type  should be 'ModelGrpType'", "ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"));
        //Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup", getAttributeGroup(baseSTS,"AttributeGroup"));
    }

    @Test
    void testDeleteSubstitutionGroups() throws Exception {
        System.out.println("Inside test case testDeleteSubstitutionGroups()");

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

        // verify types - substitution groups
        assertEquals("SubGrpHeadElemType", getElementType(baseSTS, "SubGrpHeadElem"), "Elem Type  should be 'SubGrpHeadElemType' (base)");
        assertEquals("SubGrpHeadElemType", getElementType(baseSTS, "SubGrpMemberElem1"), "Elem Type  should be 'SubGrpHeadElemType' (derived)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(baseSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

        // named model groups
        //Assert.assertEquals("Elem Type  should be 'ModelGrpType'", "ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"));
        //Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup", getAttributeGroup(baseSTS,"AttributeGroup"));

        // step2: load an invalid PSOM by deleting the Subst Grp head elem definition
        SchemaTypeSystem modifiedSTS = createNewSTS("groups.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        assertTrue(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // Recovered Errors, Test for saving of the SOM
        assertFalse(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save Success - should fail!");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 4, 1);

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS), "Validation against instance failed");

        // verify types
        assertEquals(anyType, getElementType(modifiedSTS, "SubGrpHeadElem"), "Elem Type  should be 'anyType'");
        assertEquals(anyType, getElementType(modifiedSTS, "SubGrpMemberElem1"), "Elem Type  should be 'anyType' (Member of Sub. Group)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(modifiedSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

        // named model groups
        assertEquals("ModelGrpType", getElementType(modifiedSTS, "ModelGrpTypeElem"), "Elem Type  should be 'ModelGrpType'");
        assertTrue(getAttributeGroup(modifiedSTS, "AttributeGroup"), "Elem Type  should be 'AttributeGroup'");

        // step 3: create a PSOM with the original xsd
        SchemaTypeSystem finalSTS = createNewSTS("groups_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);
        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(finalSTS), "SOM " + finalSTS.getName() + "Save failed!");

        // instance validation - should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), finalSTS), "Validation against instance failed");

        // verify types
        assertEquals("SubGrpHeadElemType", getElementType(finalSTS, "SubGrpHeadElem"), "Elem Type  should be 'SubGrpHeadElemType' (base)");
        assertEquals("SubGrpHeadElemType", getElementType(finalSTS, "SubGrpMemberElem1"), "Elem Type  should be 'SubGrpHeadElemType' (derived)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(finalSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

        // named model groups
        //Assert.assertEquals("Elem Type  should be 'ModelGrpType'", "ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"));
        //Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup", getAttributeGroup(baseSTS,"AttributeGroup"));

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testModifySubstitutionGroups() throws Exception {
        System.out.println("Inside test case testModifySubstitutionGroups()");

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

        // verify types
        assertEquals("SubGrpHeadElemType", getElementType(baseSTS, "SubGrpHeadElem"), "Elem Type  should be 'SubGrpHeadElemType' (base)");
        assertEquals("SubGrpHeadElemType", getElementType(baseSTS, "SubGrpMemberElem1"), "Elem Type  should be 'SubGrpHeadElemType' (derived)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(baseSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

        // step2: load a modified xsd with type of head elem in subs grp changed
        SchemaTypeSystem modifiedSTS = createNewSTS("groups_modified.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // Recovered Errors, Test for saving of the SOM    - still a valid PSOM
        assertTrue(checkPSOMSave(modifiedSTS), "SOM " + modifiedSTS.getName() + "Save Success - should fail!");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 5, 0, 3, 0);

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS), "Validation against instance failed");

        // verify types
        assertEquals("SubGrpHeadElemType2", getElementType(modifiedSTS, "SubGrpHeadElem"), "Elem Type  should be 'SubGrpHeadElemType2'");
        assertEquals("SubGrpHeadElemType2", getElementType(modifiedSTS, "SubGrpMemberElem1"), "Elem Type  should be 'SubGrpHeadElemType2' (derived)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(modifiedSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

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

        // verify types
        assertEquals("SubGrpHeadElemType", getElementType(finalSTS, "SubGrpHeadElem"), "Elem Type  should be 'SubGrpHeadElemType' (base)");
        assertEquals("SubGrpHeadElemType", getElementType(finalSTS, "SubGrpMemberElem1"), "Elem Type  should be 'SubGrpHeadElemType' (derived)");
        assertEquals("ExtensionSubGrpHeadElemType", getElementType(finalSTS, "SubGrpMemberElem2"), "Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

    @Test
    void testModifyIdConstraints() throws Exception {
        System.out.println("Inside test case testModifyIdConstraints()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("constraints_added.xsd_",
            null,
            "BaseSchemaTS",
            sBaseSourceName);
        assertNotNull(baseSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 5, 0, 2, 0);

        assertTrue(lookForIdentityConstraint(baseSTS, "uniqueConstraint"), "Constraint 'uniqueConstraint' should be found");
        assertTrue(lookForIdentityConstraint(baseSTS, "keyConstraint"), "Constraint 'keyConstraint' should be found");
        assertTrue(lookForIdentityConstraint(baseSTS, "KeyRefConstraint"), "Constraint 'KeyRefConstraint' should be found");

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(baseSTS), "SOM " + baseSTS.getName() + "Save failed!");

        // instance validation against valid instance- should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_constraints_valid.xml"), baseSTS), "Validation against instance failed");

        // validation against instance which violates the Constraints - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_constraints_invalid.xml"), baseSTS), "Validation against invalid should fail");

        // Step 2: create an incremental PSOM with the constraint commented out
        // Note: Partial SOMs cannot be created for Unique/Key constraints. They generate valid complete SOMs.
        // The xsd includes these but the invalid SOM in this case is from a keyref definition referring to a
        // non existant key

        SchemaTypeSystem modifiedSTS = createNewSTS("constraints.xsd_",
            baseSTS,
            "ModifiedSchemaTS",
            sBaseSourceName);
        assertNotNull(modifiedSTS, "Schema Type System created is Null.");

        // recovearble errors
        assertTrue(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // Recovered Errors, Test for saving of the SOM
        assertFalse(checkPSOMSave(modifiedSTS), "valid PSOM " + modifiedSTS.getName() + "Save failed !");

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 5, 0, 2, 0);

        // instance validation - should fail
        assertFalse(validateInstance(getTestCaseFile("instance_constraints_valid.xml"), modifiedSTS), "Validation against instance failed");

        // Invalid instance validation - should fail bcos of Unique constraint definition missing
        assertFalse(validateInstance(getTestCaseFile("instance_constraints_invalid.xml"), modifiedSTS), "Validation against instance failed");

        assertFalse(lookForIdentityConstraint(modifiedSTS, "KeyConstraint"), "KeyRef 'KeyRefConstraint' should not be resolved");

        // Step 3 : recreate SOM in first step and compare it
        SchemaTypeSystem finalSTS = createNewSTS("constraints_added.xsd_",
            modifiedSTS,
            "FinalSchemaTS",
            sBaseSourceName);
        assertNotNull(finalSTS, "Schema Type System created is Null.");

        // there should be NO recovearble errors
        assertFalse(printRecoveredErrors(), "Recovered Errors for Valid Schema");

        // the tests - Walk thro the valid SOM
        inspectSOM(finalSTS, 5, 0, 2, 0);

        // Test for saving of the SOM - should go thro
        assertTrue(checkPSOMSave(finalSTS), "SOM " + finalSTS.getName() + "Save failed!");

        // instance validation against valid instance- should be ok
        assertTrue(validateInstance(getTestCaseFile("instance_constraints_valid.xml"), finalSTS), "Validation against instance failed");

        // compare this to the original schema here
        assertTrue(compareSavedSOMs("BaseSchemaTS", "FinalSchemaTS"));
    }

}




