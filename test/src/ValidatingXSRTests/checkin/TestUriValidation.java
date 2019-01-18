package ValidatingXSRTests.checkin;

import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Cezar Andrei (cezar dot andrei at gmail dot com)
 * Date: Jul 23, 2009
 */
public class TestUriValidation {
    @Test
    public void testLexAnyUriValid()
    {
        String[] validURIs = {
            "http://www.ics.uci.edu/pub/ietf/uri/#Related",
            "http://www.ics.uci.edu/pub/ietf/uri/?query=abc#Related",
            "http://a/b/c/d;p?q",
            "g:h",
            "./g",
            "g/",
            "/g",
            "//g",
            "?y",
            "g?y",
            "#s",
            "g#s",
            "g?y#s",
            ";x",
            "g;x",
            "g;x?y#s",
            ".",
            "./",
            "..",
            "../",
            "../g",
            "../..",
            "../../",
            "../../g",
            "http:// www   .ics.uci.edu   /pub/ietf/uri  /#Related",
            "http:// www   .ics.uci.edu   /pub/iet%20%20f/uri  /#Related",
            "http:\\example.com\\examples",
            "http:\\\\example.com\\\\examples",
        };

        for (int i = 0; i < validURIs.length; i++)
        {
            try
            {
                XsTypeConverter.lexAnyURI(validURIs[i]);
            }
            catch (RuntimeException e)
            {
                System.out.println("URI should be valid: " + validURIs[i] + "  " + e.getCause().getCause().getMessage());
                fail("URI should be valid: " + validURIs[i]);
            }
        }
    }

    @Test
    public void testLexAnyUriInvalid()
    {
        // From XQTS cvshead June 2009
        String[] invalidURIs = {
            "http:\\\\invalid>URI\\someURI",        // K2-SeqExprCast-207: Construct an xs:anyURI from an invalid string. However, in F&O 17.1.1, it is said that "For xs:anyURI, the extent to which an implementation validates the lexical form of xs:anyURI is implementation dependent.".
            "http://www.example.com/file%GF.html",  // K2-SeqExprCast-210: '%' is not a disallowed character and therefore it's not encoded before being considered for RFC 2396 validness.
            "foo://",                               // K2-SeqExprCast-421: Pass an invalid anyURI.
            "foo:",                                 // K2-SeqExprCast-421-2: Pass an invalid anyURI.
            "%gg",                                  // K2-SeqExprCast-422: Pass an invalid anyURI(#2).
            ":/cut.jpg",                            // K2-SeqExprCast-423: no scheme
            ":/images/cut.png",                     // K2-SeqExprCast-424: An URI without scheme, combined with a relative directory.
            ":/",                                   // K2-SeqExprCast-505: ':/' is an invalid URI, no scheme.
            "http:%%",                              // fn-resolve-uri-4: Evaluation of resolve-uri function with an invalid URI value for second argument.
            ":",                                    // fn-resolve-uri-3: Evaluation of resolve-uri function with an invalid URI value for first argument.
            "###Rel",
            "##",
            "????###",
            "###????"
        };

        for (int i = 0; i < invalidURIs.length; i++)
        {
            try
            {
                XsTypeConverter.lexAnyURI(invalidURIs[i]);
                System.out.println("URI should be invalid: " + invalidURIs[i] );
                fail("URI should be invalid: " + invalidURIs[i]);
            }
            catch (InvalidLexicalValueException e)
            {
                assertTrue("URI should be invalid: " + invalidURIs[i] + "  " + e.getCause().getCause().getMessage(), true);
            }
        }
    }
}
