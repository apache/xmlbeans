package xmlcursor.detailed;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import junit.framework.TestCase;

/**
 * Date: Nov 3, 2004
 * Time: 4:41:07 PM
 *
 * @owner ykadiysk
 */
public class CopyTest extends TestCase{

//this is per CR128353
    public void testCopyNamespaceMigration()throws XmlException{
        String s1="<X xmlns=\"foo\" xmlns:xsi=\"bar\"><zzz>123</zzz></X>";
       String s2="<Y> ... [some content] ... </Y>";
        XmlCursor xc1=XmlObject.Factory.parse(s1).newCursor();
        xc1.toFirstContentToken();
        xc1.toFirstChild();
        XmlCursor xc2=XmlObject.Factory.parse(s2).newCursor();
        assertEquals(XmlCursor.TokenType.START,xc2.toFirstContentToken());
        xc2.toNextToken();
        xc1.copyXml(xc2);
        xc2.toStartDoc();
        assertEquals("<Y>" +
                "<foo:zzz xmlns:foo=\"foo\" xmlns:xsi=\"bar\">123</foo:zzz>" +
                " ... [some content] ... </Y>",xc2.xmlText());
       xc1.dispose();
        xc2.dispose();
    }

}
