package xmlobject.xmlloader.detailed;

import junit.framework.TestCase;
import tools.util.JarUtil;

import java.io.InputStream;

import org.apache.xmlbeans.XmlObject;

/**
 * Date: Sep 13, 2004
 * Time: 9:51:38 AM
 *
 * @owner ykadiysk
 */
public class PiccoloParseTests extends TestCase{
    String filename="xbean/xmlobject/japanese/core_generated_wsdl_src.xml";
    String temp="xbean/xmlobject/japanese/UCS2Encoding.xml";

    public void testParseInputStream() throws Exception{
        InputStream is=JarUtil.getResourceFromJarasStream(filename);
        assertTrue (is != null );
        XmlObject obj=XmlObject.Factory.parse(is);
    }

    public void testParseString() throws Exception{
        String str=JarUtil.getResourceFromJar(filename);
        assertTrue (str != null );
        XmlObject obj=XmlObject.Factory.parse(str);
    }

    public void testParseInputStreamUCS2() throws Exception{
          InputStream is=JarUtil.getResourceFromJarasStream(temp);
          assertTrue (is != null );
          XmlObject obj=XmlObject.Factory.parse(is);
      }

}
