package drtcases;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.marshal.BindingContext;
import org.apache.xmlbeans.impl.marshal.BindingContextFactory;
import org.apache.xmlbeans.impl.marshal.Unmarshaller;
import org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.io.IOException;

public class MarshalTests extends TestCase
{
  public MarshalTests(String name)
  {
    super(name);
  }

  public static Test suite()
  {
    return new TestSuite(MarshalTests.class);
  }

  public void testFloatUnmarshall()
    throws Exception
  {
    Unmarshaller unmarshaller = getSimpleUnmarshaller();

    final float val = 3.45f;

    String xmldoc = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
      " xmlns:xs='http://www.w3.org/2001/XMLSchema' xsi:type='xs:float' >" +
      val + "</a>";

    StringReader stringReader = new StringReader(xmldoc);
    XMLStreamReader xrdr =
      XMLInputFactory.newInstance().createXMLStreamReader(stringReader);

    Object obj = unmarshaller.unmarshal(xrdr);

    Float expected = new Float(val);
    Assert.assertEquals(expected, obj);
  }

  public void testManySimpleTypesUnmarshall()
    throws Exception
  {
    testSimpleTypeUnmarshall(new Long(554345354445555555L), "long");
    testSimpleTypeUnmarshall(new Float(54.5423f), "float");
    testSimpleTypeUnmarshall("random string", "string");
  }


  //only works for values where .toString() is equivalent to marshalling
  public void testSimpleTypeUnmarshall(Object expected, String xsd_type)
    throws Exception
  {
    Unmarshaller unmarshaller = getSimpleUnmarshaller();

    String xmldoc = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
      " xmlns:xs='http://www.w3.org/2001/XMLSchema' xsi:type='xs:" +
      xsd_type + "' >" + expected + "</a>";

    StringReader stringReader = new StringReader(xmldoc);
    XMLStreamReader xrdr =
      XMLInputFactory.newInstance().createXMLStreamReader(stringReader);

    Object obj = unmarshaller.unmarshal(xrdr);

    Assert.assertEquals(expected, obj);

    System.out.println("OK for " + expected);
  }

  private static Unmarshaller getSimpleUnmarshaller() throws IOException
  {
    BindingFile bf = new BindingFile();
    BindingConfigDocument bindingConfigDocument = bf.write();

    BindingContext bindingContext =
      BindingContextFactory.createBindingContext(bindingConfigDocument);

    Unmarshaller unmarshaller = bindingContext.createUnmarshaller();
    return unmarshaller;
  }

  public void testSimpleMarshall()
    throws Exception
  {
    BindingFile bf = new BindingFile();
    BindingConfigDocument bindingConfigDocument = bf.write();

    BindingContext bindingContext =
      BindingContextFactory.createBindingContext(bindingConfigDocument);


    final float val = 3.45f;
    final Float out_obj = new Float(val);

    System.out.println("obj = " + out_obj);

  }

}
