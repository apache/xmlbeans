package xmlobject.xmlloader.detailed;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import tools.util.JarUtil;

/**
 * @owner: ykadiysk
 * Date: Jul 12, 2004
 * Time: 10:40:56 AM
 */
public class JapaneseTextTest extends TestCase {

    public void testEucJp() throws Exception{
        loadFile("pr-xml-euc-jp.xml");
    }
    public void testIso2022Jp()throws Exception{
           loadFile("pr-xml-iso-2022-jp.xml");
    }
     public void testLittleEndian()throws Exception{
            loadFile("pr-xml-little-endian.xml");
     }
     public void testShift_jis()throws Exception{
          loadFile("pr-xml-shift_jis.xml");
     }
     public void testUtf8()throws Exception{
          loadFile("pr-xml-utf-8.xml");
     }
     public void testUtf16()throws Exception{
          loadFile("pr-xml-utf-16.xml");
     }

    public void testWeeklyEucJp()throws Exception{
        loadFile("weekly-euc-jp.xml");
    }
    public void testWeeklyIso2022Jp()throws Exception{
        loadFile("weekly-iso-2022-jp.xml");
    }
     public void testWeeklyLittleEndian()throws Exception{
         loadFile("weekly-little-endian.xml");
     }
     public void testWeeklyShift_jis()throws Exception{
         loadFile("weekly-shift_jis.xml");
     }
     public void testWeeklyUtf8()throws Exception{
         loadFile("weekly-utf-8.xml");
     }
     public void testWeeklyUtf16()throws Exception{
         loadFile("weekly-utf-16.xml");
     }


    public void loadFile(String file) throws Exception{

        XmlObject.Factory.parse(JarUtil.getResourceFromJarasStream("xbean/xmlobject/japanese/"+file));

    }



}
