package scomp.derivation.restriction.facets.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import xbean.scomp.derivation.facets.facetRestriction.*;
import scomp.common.BaseCase;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.math.BigDecimal;

/**
 * Date: Nov 8, 2004
 * Time: 11:34:46 AM
 *
 * @owner ykadiysk
 */
public class FacetRestrictionTest extends BaseCase {

    public void testMinMaxInclusiveElt() throws Throwable {
        MinMaxInclusiveEltDocument doc =
                MinMaxInclusiveEltDocument.Factory.newInstance();
        doc.setMinMaxInclusiveElt(3);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID};

        doc.setMinMaxInclusiveElt(2);
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        clearErrors();
        errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        doc.setMinMaxInclusiveElt(10);
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

    }
    public void testMinMaxInclusiveDateElt() throws Throwable {
           MinMaxInclusiveDateEltDocument doc =
                   MinMaxInclusiveDateEltDocument.Factory.newInstance();
           TimeZone tz = TimeZone.getDefault();
           Calendar c = new GregorianCalendar(tz);
           c.set(2003, 11, 22);
           doc.setMinMaxInclusiveDateElt(c);
           try {
               assertTrue(doc.validate(validateOptions));
           } catch (Throwable t) {
               showErrors();
               throw t;
           }
           c = new GregorianCalendar(2003, 11, 24);
           doc.setMinMaxInclusiveDateElt(c);
           String[] errExpected = new String[]{
               XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
           assertTrue(!doc.validate(validateOptions));
           assertTrue(compareErrorCodes(errExpected));
      


       }
    public void testMinMaxExclusiveElt() throws Throwable {
           MinMaxExclusiveEltDocument doc =
                   MinMaxExclusiveEltDocument.Factory.newInstance();
           String[] errExpected = new String[]{
               XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID};

           doc.setMinMaxExclusiveElt(3);
           assertTrue(!doc.validate(validateOptions));
           assertTrue(compareErrorCodes(errExpected));
           clearErrors();

           doc.setMinMaxExclusiveElt(2);
           try {
               assertTrue(doc.validate(validateOptions));
           } catch (Throwable t) {
               showErrors();
               throw t;
           }
           doc.setMinMaxExclusiveElt(10);
           try {
               assertTrue(doc.validate(validateOptions));
           } catch (Throwable t) {
               showErrors();
               throw t;
           }

       }
     public void testMinMaxExclusiveDateElt() throws Throwable {
        MinMaxExclusiveDateEltDocument doc = MinMaxExclusiveDateEltDocument.Factory.newInstance();
        Calendar c = new GregorianCalendar(2003, 11, 24);
        doc.setMinMaxExclusiveDateElt(c);
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID};
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));
          clearErrors();
        c = new GregorianCalendar(2003, 11, 25);
        doc.setMinMaxExclusiveDateElt(c);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }


    }

      public void testLengthElt() throws Throwable {
        LengthEltDocument doc = LengthEltDocument.Factory.newInstance();
        doc.setLengthElt("foobar");
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_LENGTH_VALID$STRING};

        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        doc.setLengthElt("fo");
        clearErrors();
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

        doc.setLengthElt("f");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testMinMaxLengthElt() throws Throwable {
          MinMaxLengthEltDocument doc = MinMaxLengthEltDocument.Factory.newInstance();
          String[] errExpected = new String[]{
              XmlErrorCodes.DATATYPE_MAX_LENGTH_VALID$STRING};

          doc.setMinMaxLengthElt("fooba");
          assertTrue(!doc.validate(validateOptions));
          assertTrue(compareErrorCodes(errExpected));

          doc.setMinMaxLengthElt("fo");
          errExpected = new String[]{
              XmlErrorCodes.DATATYPE_MIN_LENGTH_VALID$STRING};
          clearErrors();
          assertTrue(!doc.validate(validateOptions));
          assertTrue(compareErrorCodes(errExpected));

          doc.setMinMaxLengthElt("foo");
          try {
              assertTrue(doc.validate(validateOptions));
          } catch (Throwable t) {
              showErrors();
              throw t;
          }
          doc.setMinMaxLengthElt("foob");
          try {
              assertTrue(doc.validate(validateOptions));
          } catch (Throwable t) {
              showErrors();
              throw t;
          }

      }
    public void testDigitsElt() throws Throwable {
           DigitsEltDocument doc = DigitsEltDocument.Factory.newInstance();
           String[] errExpected = new String[]{
               XmlErrorCodes.DATATYPE_TOTAL_DIGITS_VALID};

           doc.setDigitsElt(new BigDecimal("122.2"));
           assertTrue(!doc.validate(validateOptions));
           assertTrue(compareErrorCodes(errExpected));

           doc.setDigitsElt(new BigDecimal("12.3"));
           try {
               assertTrue(doc.validate(validateOptions));
           } catch (Throwable t) {
               showErrors();
               throw t;
           }
           clearErrors();
           errExpected = new String[]{
               XmlErrorCodes.DATATYPE_FRACTION_DIGITS_VALID};
           doc.setDigitsElt(new BigDecimal("2.45"));
           assertTrue(!doc.validate(validateOptions));
           assertTrue(compareErrorCodes(errExpected));

       }

     public void testWSElt() throws Throwable {
        fail("do this test");
    }

   public void testEnumElt() throws Throwable {
        EnumEltDocument doc = EnumEltDocument.Factory.newInstance();
        doc.setEnumElt(EnumT.A);
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc = EnumEltDocument.Factory.parse("<EnumElt " +
                "xmlns=\"http://xbean/scomp/derivation/facets/FacetRestriction\">" +
                "b" +
                "</EnumElt>");
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_ENUM_VALID};

        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));

    }

    public void testPatternElt() throws Throwable {
        PatternEltDocument doc = PatternEltDocument.Factory.newInstance();
        doc.setPatternElt("aedafd");
        try {
            assertTrue(doc.validate(validateOptions));
        } catch (Throwable t) {
            showErrors();
            throw t;
        }
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};

        doc.setPatternElt("aedaedaed");
        assertTrue(!doc.validate(validateOptions));
        assertTrue(compareErrorCodes(errExpected));


    }
}
