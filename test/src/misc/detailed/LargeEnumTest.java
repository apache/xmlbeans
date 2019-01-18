package misc.detailed;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlToken;
import org.junit.Test;
import xmlbeans307.*;

import java.util.ArrayList;

import static org.junit.Assert.*;

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
    public void testEnumCount_closeToMax() throws Exception {
        SchemaType mType = MaxAllowedEnumType.type;
        assertNotNull("Enumeration SchemaType was null", mType.getEnumerationValues());
        assertEquals("EnumerationValue was not 3665 as expected was" + mType.getEnumerationValues().length, 3665, mType.getEnumerationValues().length);

        SchemaType mElem = MaxAllowedElementDocument.type;
        assertNull("Enumeration SchemaType was null", mElem.getEnumerationValues());

        // Test that the Java type associated to this is an enum type
        assertNotNull("This type does not correspond to a Java enumeration", mType.getStringEnumEntries());
    }

    @Test
    public void testEnumCount_greaterThanMax() throws Exception {
        // TODO: verify if any xpath/xquery issues 
        SchemaType mType = MoreThanAllowedEnumType.type;

        assertNotNull("Enumeration should be null as type should be base type " + mType.getEnumerationValues(),
                mType.getEnumerationValues());
        assertEquals("EnumerationValue was not 3678 as expected was " + mType.getEnumerationValues().length, 3678, mType.getEnumerationValues().length);
        System.out.println("GET BASE TYPE: " + mType.getBaseType());
        System.out.println("GET BASE TYPE: " + mType.getPrimitiveType());
        assertEquals("type should have been base type, was " + mType.getBaseType(), mType.getBaseType().getBuiltinTypeCode(), XmlToken.type.getBuiltinTypeCode());

        SchemaType mElem = GlobalMoreThanElementDocument.type;
        assertNull("Enumeration SchemaType was null", mElem.getBaseEnumType());

        // Test that the Java type associated to this is not an enum type
        assertNull("This type corresponds to a Java enumeration, even though it has too many enumeration values",
            mType.getStringEnumEntries());
    }

    @Test
    public void testEnumCount_validate_invalid_enum() throws Exception {
        MoreThanAllowedEnumType mType = MoreThanAllowedEnumType.Factory.newInstance();

        //This value dos not exist in the enumeration set
        mType.setStringValue("12345AAA");
        ArrayList errors = new ArrayList();
        XmlOptions options = (new XmlOptions()).setErrorListener(errors);
        mType.validate(options);
        XmlError[] xErr = new XmlError[errors.size()];
        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
            xErr[i] = (XmlError)errors.get(i);
        }

        assertEquals("NO Expected Errors after validating enumType after set", 1, errors.size());
        assertEquals("Expected ERROR CODE was not as expected", 0, xErr[0].getErrorCode().compareTo("cvc-enumeration-valid"));
        // string value '12345AAA' is not a valid enumeration value for MoreThanAllowedEnumType in
    }

    @Test
    public void test_MoreEnum_Operations() throws Exception {
        MoreThanAllowedEnumType mType = MoreThanAllowedEnumType.Factory.newInstance();

        mType.setStringValue("AAA");
        ArrayList errors = new ArrayList();
        XmlOptions options = (new XmlOptions()).setErrorListener(errors);
        mType.validate(options);

        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
        }
        assertEquals("There were errors validating enumType after set", 0, errors.size());

        GlobalMoreThanElementDocument mDoc = GlobalMoreThanElementDocument.Factory.newInstance();
        mDoc.setGlobalMoreThanElement("AAA");
        errors = null;
        errors = new ArrayList();
        options = (new XmlOptions()).setErrorListener(errors);
        mDoc.validate(options);

        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
        }

        assertEquals("There were errors validating enumDoc after set", 0, errors.size());

        MoreThanAllowedComplexType mcType = MoreThanAllowedComplexType.Factory.newInstance();
        mcType.setComplexTypeMoreThanEnum("AAA");
        mcType.setSimpleString("This should work");
        errors = null;
        errors = new ArrayList();
        mcType.validate(options);
        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
        }

        assertEquals("There were errors validating complxType after set", 0, errors.size());
    }


}
