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

package misc.detailed;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlToken;
import org.junit.jupiter.api.Test;
import xmlbeans307.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test was put together for:
 * http://issues.apache.org/jira/browse/XMLBEANS-307
 * XMLBeans scomp throws error "code too large"
 */
public class LargeEnumTest {
    /**
     * These are tests for a enumeration type
     */
    @Test
    void testEnumCount_closeToMax() throws Exception {
        SchemaType mType = MaxAllowedEnumType.type;
        assertNotNull(mType.getEnumerationValues(), "Enumeration SchemaType was null");
        assertEquals(3660, mType.getEnumerationValues().length, "EnumerationValue was not 3660 as expected was" + mType.getEnumerationValues().length);

        SchemaType mElem = MaxAllowedElementDocument.type;
        assertNull(mElem.getEnumerationValues(), "Enumeration SchemaType was null");

        // Test that the Java type associated to this is an enum type
        assertNotNull(mType.getStringEnumEntries(), "This type does not correspond to a Java enumeration");
    }

    @Test
    void testEnumCount_greaterThanMax() throws Exception {
        // TODO: verify if any xpath/xquery issues
        SchemaType mType = MoreThanAllowedEnumType.type;

        assertNotNull(mType.getEnumerationValues(), "Enumeration should be null as type should be base type " + mType.getEnumerationValues());
        assertEquals(3678, mType.getEnumerationValues().length, "EnumerationValue was not 3678 as expected was " + mType.getEnumerationValues().length);
        assertEquals(mType.getBaseType().getBuiltinTypeCode(), XmlToken.type.getBuiltinTypeCode(), "type should have been base type, was " + mType.getBaseType());

        SchemaType mElem = GlobalMoreThanElementDocument.type;
        assertNull(mElem.getBaseEnumType(), "Enumeration SchemaType was null");

        // Test that the Java type associated to this is not an enum type
        assertNull(mType.getStringEnumEntries(), "This type corresponds to a Java enumeration, even though it has too many enumeration values");
    }

    @Test
    void testEnumCount_validate_invalid_enum() throws Exception {
        MoreThanAllowedEnumType mType = MoreThanAllowedEnumType.Factory.newInstance();

        //This value dos not exist in the enumeration set
        mType.setStringValue("12345AAA");
        List<XmlError> errors = new ArrayList<>();
        XmlOptions options = (new XmlOptions()).setErrorListener(errors);
        mType.validate(options);

        assertEquals(1, errors.size(), "NO Expected Errors after validating enumType after set");
        assertEquals(0, errors.get(0).getErrorCode().compareTo("cvc-enumeration-valid"), "Expected ERROR CODE was not as expected");
        // string value '12345AAA' is not a valid enumeration value for MoreThanAllowedEnumType in
    }

    @Test
    void test_MoreEnum_Operations() throws Exception {
        MoreThanAllowedEnumType mType = MoreThanAllowedEnumType.Factory.newInstance();

        mType.setStringValue("AAA");
        List<XmlError> errors = new ArrayList<>();
        XmlOptions options = (new XmlOptions()).setErrorListener(errors);
        mType.validate(options);

        assertEquals(0, errors.size(), "There were errors validating enumType after set");

        GlobalMoreThanElementDocument mDoc = GlobalMoreThanElementDocument.Factory.newInstance();
        mDoc.setGlobalMoreThanElement("AAA");
        errors.clear();
        options = (new XmlOptions()).setErrorListener(errors);
        mDoc.validate(options);

        assertEquals(0, errors.size(), "There were errors validating enumDoc after set");

        MoreThanAllowedComplexType mcType = MoreThanAllowedComplexType.Factory.newInstance();
        mcType.setComplexTypeMoreThanEnum("AAA");
        mcType.setSimpleString("This should work");
        errors.clear();
        mcType.validate(options);

        assertEquals(0, errors.size(), "There were errors validating complxType after set");
    }


}
