package drtcases;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.marshal.BindingContext;
import org.apache.xmlbeans.impl.marshal.BindingContextFactory;
import org.apache.xmlbeans.impl.marshal.MarshalContext;
import org.apache.xmlbeans.impl.marshal.Marshaller;
import org.apache.xmlbeans.impl.marshal.UnmarshalContext;
import org.apache.xmlbeans.impl.marshal.Unmarshaller;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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

    //does not test any xmlbeans code, but rather a quick sanity check
    //of the current jsr 173 impl
    public void testAStream() throws XMLStreamException
    {
        String doc = "<a>foo</a>";
        StringReader sr = new StringReader(doc);
        final XMLStreamReader reader =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);

        dumpReader(reader);
    }

    public void testManySimpleTypesUnmarshall()
        throws Exception
    {
        testSimpleTypeUnmarshal(new Long(554345354445555555L), "long");
        testSimpleTypeUnmarshal(new Float(54.5423f), "float");
        testSimpleTypeUnmarshal("random string", "string");
    }


    //only works for values where .toString() is equivalent to marshalling
    public void testSimpleTypeUnmarshal(Object expected, String xsd_type)
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


    public void testManySimpleTypesMarshall()
        throws Exception
    {
        testSimpleTypeMarshal(new Long(554345354445555555L), "long");
        testSimpleTypeMarshal("some text here", "string");
        testSimpleTypeMarshal("       ", "string");
    }


    //only works for values where .toString() is equivalent to marshalling
    public void testSimpleTypeMarshal(Object orig, String xsd_type)
        throws Exception
    {
        BindingFile bf = new BindingFile();
        BindingConfigDocument bindingConfigDocument = bf.write();

        BindingContext bindingContext =
            BindingContextFactory.createBindingContext(bindingConfigDocument);

        Marshaller m = bindingContext.createMarshaller();

        EmptyNamespaceContext namespaceContext = new EmptyNamespaceContext();
        final ArrayList errors = new ArrayList();
        MarshalContext ctx =
            bindingContext.createMarshallContext(namespaceContext, errors);

        final XMLStreamReader reader =
            m.marshallType(orig,
//                           new QName("uri", "lname"),
                           new QName("lname"),
                           new QName("http://www.w3.org/2001/XMLSchema", xsd_type),
                           orig.getClass().getName(),
                           ctx);


        System.out.println("==================OBJ: " + orig);
        dumpReader(reader);
    }


    public void testByNameMarshal()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(432.3432f);
        myelt.setMyFloat(5555.4444f);
        myelt.setMyClass(new com.mytest.MyClass());
        mc.setMyelt(myelt);


        BindingConfigDocument bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.createBindingContext(bcdoc);

        Marshaller m = bindingContext.createMarshaller();

        EmptyNamespaceContext namespaceContext = new EmptyNamespaceContext();
        final ArrayList errors = new ArrayList();
        MarshalContext ctx =
            bindingContext.createMarshallContext(namespaceContext, errors);

        final XMLStreamReader reader =
            m.marshallType(mc, new QName("java:com.mytest", "load"),
                           new QName("java:com.mytest", "MyClass"),
                           mc.getClass().getName(),
                           ctx);

        System.out.println("=======IN-OBJ: " + mc);

//        dumpReader(reader);

        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();
        //TODO: remove hard coded values
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");
        UnmarshalContext umctx = bindingContext.createUnmarshallContext(reader, errors);
        Object out = unmarshaller.unmarshallType(schemaType, javaType, umctx);
        System.out.println("======OUT-OBJ: " + out);


    }

    private static void dumpReader(final XMLStreamReader reader)
        throws XMLStreamException
    {
        final boolean write_doc = true;
        if (write_doc) {
            StringWriter sw = new StringWriter();
            XMLStreamWriter xsw =
                XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

            //NOTE: next two lines depend on the 173_ri to even compile
            com.bea.xml.stream.ReaderToWriter rtow =
                new com.bea.xml.stream.ReaderToWriter(xsw);
            rtow.writeAll(reader);
            rtow.write(reader); //make up for bug in ReaderToWriter for now

            xsw.close();

            System.out.println("doc = " + sw.getBuffer());

        } else {
            int i = 0;
            System.out.println((i++) + "\tSTATE: " + XmlStreamUtils.printEvent(reader));
            while (reader.hasNext()) {
                final int state = reader.next();
                System.out.println((i++) + "\tSTATE: " + XmlStreamUtils.printEvent(reader));
            }
        }
    }

    public void testByNameBeanUnmarshal()
        throws Exception
    {
        BindingConfigDocument bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.createBindingContext(bcdoc);

        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();

        Assert.assertNotNull(unmarshaller);

        //TODO: remove hard coded path
        File doc = new File("test/cases/marshal/doc2.xml");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        Object obj = unmarshaller.unmarshal(xrdr);

        System.out.println("obj = " + obj);
    }

    public void testByNameBeanUnmarshalType()
        throws Exception
    {
        BindingConfigDocument bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.createBindingContext(bcdoc);


        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();

        Assert.assertNotNull(unmarshaller);

        //TODO: remove hard coded values
        final File doc = new File("test/cases/marshal/doc.xml");
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        List errors = new ArrayList();

        UnmarshalContext ctx = bindingContext.createUnmarshallContext(xrdr, errors);

        //this is not very safe but it should work...
        while (!xrdr.isStartElement()) {
            xrdr.next();
        }

        Object obj = unmarshaller.unmarshallType(schemaType, javaType, ctx);


        System.out.println("+++++TYPE obj = " + obj);
    }

//    public void testPerfByNameBeanUnmarshall()
//        throws Exception
//    {
//        BindingConfigDocument bcdoc = getBindingConfigDocument();
//
//        BindingContext bindingContext =
//            BindingContextFactory.createBindingContext(bcdoc);
//
//        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();
//
//        Assert.assertNotNull(unmarshaller);
//
//        //TODO: remove hard coded path
//        File doc = new File("test/cases/marshal/doc.xml");
//        final FileReader fileReader = new FileReader(doc);
//        CharArrayWriter cw = new CharArrayWriter();
//
//        bufferedStreamCopy(fileReader, cw);
//        final char[] chars = cw.toCharArray();
//        final CharArrayReader cr = new CharArrayReader(chars);
//
//        final int trials = 5000;
//
//        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
//
//        final long before_millis = System.currentTimeMillis();
//        for (int i = 0; i < trials; i++) {
//            cr.reset();
//            XMLStreamReader xrdr =
//                xmlInputFactory.createXMLStreamReader(cr);
//
//            Object obj = unmarshaller.unmarshal(xrdr);
//
//            if ((i % 1000) == 0)
//                System.out.println("i=" + i + "\tobj = " + obj);
//        }
//        final long after_millis = System.currentTimeMillis();
//        final long diff = (after_millis - before_millis);
//        System.out.println("milliseconds: " + diff + " trials: " + trials);
//        System.out.println("milliseconds PER trial: " + (diff / (double)trials));
//    }

    protected static void bufferedStreamCopy(Reader in, Writer out)
        throws IOException
    {
        int charsRead;
        char[] buf = new char[1024];

        while ((charsRead = in.read(buf)) != -1) {
            out.write(buf, 0, charsRead);
        }
    }


    private BindingConfigDocument getBindingConfigDocument()
        throws IOException, XmlException
    {
        //TODO: remove hard coded path
        File loc = new File("test/cases/marshal/example_config.xml");
        BindingConfigDocument bcdoc = BindingConfigDocument.Factory.parse(loc);
        return bcdoc;
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

    private static Marshaller getSimpleMarshaller() throws IOException
    {
        BindingFile bf = new BindingFile();
        BindingConfigDocument bindingConfigDocument = bf.write();

        BindingContext bindingContext =
            BindingContextFactory.createBindingContext(bindingConfigDocument);

        Marshaller m = bindingContext.createMarshaller();
        return m;
    }

    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }

    private static class EmptyNamespaceContext
        implements NamespaceContext
    {
        public String getNamespaceURI(String s)
        {
            return null;
        }

        public String getPrefix(String s)
        {
            return null;
        }

        public Iterator getPrefixes(String s)
        {
            return null;
        }

    }

}
