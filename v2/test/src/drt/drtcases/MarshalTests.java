package drtcases;

import com.mytest.MyClass;
import com.mytest.MySubClass;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.BindingContext;
import org.apache.xmlbeans.BindingContextFactory;
import org.apache.xmlbeans.MarshalContext;
import org.apache.xmlbeans.Marshaller;
import org.apache.xmlbeans.UnmarshalContext;
import org.apache.xmlbeans.Unmarshaller;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.impl.common.XmlReaderToWriter;
import org.apache.xmlbeans.impl.common.XmlStreamUtils;
import org.apache.xmlbeans.impl.tool.PrettyPrinter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;


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
    public void testAStream()
        throws Exception
    {
        String doc = "<a x='y'>food</a>";
        StringReader sr = new StringReader(doc);
        final XMLStreamReader reader =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);

        dumpReader(reader);
    }

    public void testManySimpleTypesUnmarshall()
        throws Exception
    {
        testSimpleTypeUnmarshal(Boolean.TRUE, "boolean");
        testSimpleTypeUnmarshal(new Byte((byte)125), "byte");
        testSimpleTypeUnmarshal(new Short((short)5543), "short");
        testSimpleTypeUnmarshal(new Integer(55434535), "int");
        testSimpleTypeUnmarshal(new Long(554345354445555555L), "long");
        testSimpleTypeUnmarshal(new BigInteger("55434535443332323245555555"), "integer");
        testSimpleTypeUnmarshal(new BigDecimal("43434343342.233434342"), "decimal");
        testSimpleTypeUnmarshal(new Float(54.5423f), "float");
        testSimpleTypeUnmarshal(new Double(23432.43234), "double");
        testSimpleTypeUnmarshal("random string", "string");

        Calendar c = Calendar.getInstance();

        testSimpleTypeUnmarshal(c, "2002-03-06T08:04:39.265Z", "dateTime");

    }


    public void testManySimpleTypesMarshall()
        throws Exception
    {
//        testSimpleTypeMarshal(Boolean.TRUE, "boolean");
        testSimpleTypeMarshal(new Byte((byte)125), "byte");
        testSimpleTypeMarshal(new Short((short)5543), "short");
        testSimpleTypeMarshal(new Integer(55434535), "int");
        testSimpleTypeMarshal(new Long(554345354445555555L), "long");
        testSimpleTypeMarshal(new BigInteger("55434535443332323245555555"), "integer");
        testSimpleTypeMarshal(new BigDecimal("43434343342.233434342"), "decimal");
        testSimpleTypeMarshal(new Float(5555.5555f), "float");
        testSimpleTypeMarshal(new Double(1231.444), "double");
        testSimpleTypeMarshal("some text here", "string");
        testSimpleTypeMarshal("       ", "string");
    }



    //only works for values where .toString() is equivalent to marshalling
    public void testSimpleTypeUnmarshal(Object expected, String xsd_type)
        throws Exception
    {
        testSimpleTypeUnmarshal(expected, expected.toString(), xsd_type);
    }

    public void testSimpleTypeUnmarshal(Object expected,
                                        String lexval,
                                        String xsd_type)
        throws Exception
    {
        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext();

        Unmarshaller unmarshaller1 = bindingContext.createUnmarshaller();
        Unmarshaller unmarshaller = unmarshaller1;

        String xmldoc = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'" +
            " xmlns:xs='http://www.w3.org/2001/XMLSchema' xsi:type='xs:" +
            xsd_type + "' >" + lexval + "</a>";

        StringReader stringReader = new StringReader(xmldoc);
        XMLStreamReader xrdr =
            XMLInputFactory.newInstance().createXMLStreamReader(stringReader);

        UnmarshalContext umctx =
            bindingContext.createUnmarshallContext(new ArrayList(), xrdr);
        Object obj = unmarshaller.unmarshal(umctx);


        //special case date/time tests.
        //we really need more robust testing here.
        if (expected instanceof Calendar) {
            XmlCalendar got = (XmlCalendar)obj;
            String got_lex = got.toString();
            Assert.assertEquals(lexval, got_lex);
        } else {
            Assert.assertEquals(expected, obj);
        }

        Assert.assertTrue(!umctx.hasErrors());


//        System.out.println("OK for " + expected);
    }





    //only works for values where .toString() is equivalent to marshalling
    public void testSimpleTypeMarshal(Object orig, String xsd_type)
        throws Exception
    {
        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext();

        Marshaller m = bindingContext.createMarshaller();

        EmptyNamespaceContext namespaceContext = new EmptyNamespaceContext();
        MarshalContext ctx =
            bindingContext.createMarshallContext(new ArrayList(),
                                                 namespaceContext);

        final XMLStreamReader reader =
            m.marshallType(orig,
//                           new QName("uri", "lname"),
                           new QName("lname"),
                           new QName("http://www.w3.org/2001/XMLSchema", xsd_type),
                           orig.getClass().getName(),
                           ctx);


        System.out.println("==================OBJ: " + orig);
        dumpReader(reader);

        Assert.assertTrue(!ctx.hasErrors());
    }


    public void testByNameMarshal()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
        MySubClass sub = new MySubClass();
        sub.setBigInt(new BigInteger("123431234321234321234321234212341234"));
        myelt.setMyClass(sub);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});
        myelt.setMyClassArray(new MyClass[]{sub, new MyClass(), sub});

        final File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);

        Marshaller m = bindingContext.createMarshaller();

        EmptyNamespaceContext namespaceContext = new EmptyNamespaceContext();
        MarshalContext ctx =
            bindingContext.createMarshallContext(new ArrayList(), namespaceContext);

        final XMLStreamReader reader =
            m.marshallType(mc, new QName("java:com.mytest", "load"),
                           new QName("java:com.mytest", "MyClass"),
                           mc.getClass().getName(),
                           ctx);

        System.out.println("=======IN-OBJ: " + mc);

        dumpReader(reader);
        Assert.assertTrue(!ctx.hasErrors());
    }


    public void testByNameMarshalViaWriter()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
        myelt.setMyClass(null);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});


        final File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);

        Marshaller m = bindingContext.createMarshaller();

        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        MarshalContext ctx =
            bindingContext.createMarshallContext(new ArrayList(), w.getNamespaceContext());

         m.marshallType(w, mc, new QName("java:com.mytest", "load"),
                           new QName("java:com.mytest", "MyClass"),
                           mc.getClass().getName(),
                           ctx);

        System.out.println("=======IN-OBJ: " + mc);
        System.out.println("=======OUT-XML: " + PrettyPrinter.indent(sw.getBuffer().toString()));
        Assert.assertTrue(!ctx.hasErrors());
    }

    public void testByNameDocMarshalViaWriter()
        throws Exception
    {
        com.mytest.MyClass mc = new com.mytest.MyClass();
        mc.setMyatt("attval");
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(99999.777f);
        myelt.setMyFloat(5555.4444f);
//        myelt.setMyClass(new com.mytest.MyClass());
        myelt.setMyClass(null);
        mc.setMyelt(myelt);

        myelt.setStringArray(new String[]{"one", "two", "three"});


        final File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);

        Marshaller m = bindingContext.createMarshaller();

        StringWriter sw = new StringWriter();
        XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

        MarshalContext ctx =
            bindingContext.createMarshallContext(new ArrayList(),
                                                 w.getNamespaceContext());

        m.marshall(w, mc, ctx);


        //now unmarshall from String and compare objects...
        StringReader sr = new StringReader(sw.getBuffer().toString());
        XMLStreamReader rdr =
            XMLInputFactory.newInstance().createXMLStreamReader(sr);
        Unmarshaller um = bindingContext.createUnmarshaller();
        Object out_obj =
            um.unmarshal(bindingContext.createUnmarshallContext(new ArrayList(), rdr));
        Assert.assertEquals(mc, out_obj);
        Assert.assertTrue(!ctx.hasErrors());
    }


    public void testRoundtripPerf()
        throws Exception
    {
        //crank up these numbers to see real perf testing
        //the test still has some value aside from perf
        //in that it can test large stack depths.
        final int trials = 1;
        final int depth = 5;
        final int boolean_array_size = 5;

        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();

        com.mytest.MyClass curr = top_obj;

        boolean[] bools = createRandomBooleanArray(rnd, boolean_array_size);

        for (int i = 0; i < depth; i++) {
            com.mytest.YourClass myelt = new com.mytest.YourClass();
            myelt.setAttrib(rnd.nextFloat());
            myelt.setMyFloat(rnd.nextFloat());
            myelt.setBooleanArray(bools);
            final com.mytest.MyClass my_c = new com.mytest.MyClass();
            myelt.setMyClass(my_c);
            curr.setMyelt(myelt);
            curr.setMyatt("STR" + rnd.nextInt());
            curr = my_c;
        }

        //System.out.println("top_obj = " + top_obj);

        final File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);

        Marshaller m = bindingContext.createMarshaller();

        EmptyNamespaceContext namespaceContext = new EmptyNamespaceContext();
        final ArrayList errors = new ArrayList();


        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");
        final QName elem_name = new QName("java:com.mytest", "load");
        final String class_name = top_obj.getClass().getName();

        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();

        Object out_obj = null;
        final long before_millis = System.currentTimeMillis();
        for (int i = 0; i < trials; i++) {
            errors.clear();

            MarshalContext ctx =
                bindingContext.createMarshallContext(new ArrayList(),
                                                     namespaceContext);

            final XMLStreamReader reader =
                m.marshallType(top_obj, elem_name,
                               schemaType,
                               class_name,
                               ctx);


//            //DEBUG!!!
//            if (System.currentTimeMillis() > 1) {
//                dumpReader(reader);
//                return;
//            }

            UnmarshalContext umctx = bindingContext.createUnmarshallContext(new ArrayList(), reader);
            out_obj = unmarshaller.unmarshalType(schemaType, javaType, umctx);
        }
        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
//        System.out.println(" perf_out_obj = " + top_obj);
        Assert.assertEquals(top_obj, out_obj);
        System.out.println("milliseconds: " + diff + " trials: " + trials);
        System.out.println("milliseconds PER trial: " + (diff / (double)trials));
    }

    private boolean[] createRandomBooleanArray(Random rnd, int size)
    {
        boolean[] a = new boolean[size];
        for(int i = 0 ; i < size ; i++) {
            a[i] = rnd.nextBoolean();
        }
        return a;
    }


    public void testJavaToSchemaToJava()
        throws Exception
    {
        Random rnd = new Random();

        com.mytest.MyClass top_obj = new com.mytest.MyClass();
        com.mytest.YourClass myelt = new com.mytest.YourClass();
        myelt.setAttrib(rnd.nextFloat());
        myelt.setMyFloat(rnd.nextFloat());
        final com.mytest.MyClass my_c = new com.mytest.MyClass();
//        myelt.setMyClass(my_c);
        myelt.setMyClass(null);
        top_obj.setMyelt(myelt);
//        curr.setMyatt("STR" + rnd.nextInt());
        top_obj.setMyatt(null);
//        top_obj.setMyatt("someVALUE");


        System.out.println("top_obj = " + top_obj);

        File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);

        Marshaller m = bindingContext.createMarshaller();

        EmptyNamespaceContext namespaceContext = new EmptyNamespaceContext();
        final ArrayList errors = new ArrayList();


        //TODO: remove hard coded values
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");
        final QName elem_name = new QName("java:com.mytest", "load");
        final String class_name = top_obj.getClass().getName();

        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();

        Object out_obj = null;
        errors.clear();

        MarshalContext ctx =
            bindingContext.createMarshallContext(new ArrayList(),
                                                 namespaceContext);

        final XMLStreamReader reader =
            m.marshallType(top_obj, elem_name,
                           schemaType,
                           class_name,
                           ctx);

        UnmarshalContext umctx =
            bindingContext.createUnmarshallContext(new ArrayList(), reader);
        out_obj = unmarshaller.unmarshalType(schemaType, javaType, umctx);
        System.out.println(" out_obj = " + top_obj);
        Assert.assertEquals(top_obj, out_obj);
        Assert.assertTrue(!ctx.hasErrors());
        Assert.assertTrue(!umctx.hasErrors());
    }

    private static void dumpReader(final XMLStreamReader reader)
        throws XMLStreamException, XmlException, IOException
    {
        final boolean write_doc = true;
        if (write_doc) {
            StringWriter sw = new StringWriter();
            XMLStreamWriter xsw =
                XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

            //NOTE: next two lines depend on the 173_ri to even compile
            XmlReaderToWriter.writeAll(reader, xsw);

            xsw.close();

            final String xmldoc = sw.getBuffer().toString();
            System.out.println("DOC:");
            System.out.println(PrettyPrinter.indent(xmldoc));
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
        File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);

        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();

        Assert.assertNotNull(unmarshaller);

        File doc = TestEnv.xbeanCase("marshal/doc2.xml");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        UnmarshalContext um_ctx =
            bindingContext.createUnmarshallContext(new ArrayList(), xrdr);
        Object obj = unmarshaller.unmarshal(um_ctx);

        System.out.println("obj = " + obj);
        Assert.assertTrue(!um_ctx.hasErrors());

    }


    //temp hacked up test for dd stuff
//    public void testJ2EEByNameBeanUnmarshal()
//        throws Exception
//    {
//        File loc = new File("/tmp/j2ee14-binding.xml");
//        File bcdoc = loc;
//
//        BindingContext bindingContext =
//            BindingContextFactory.newInstance().createBindingContext(bcdoc);
//
//        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();
//
//        Assert.assertNotNull(unmarshaller);
//
//        File doc = new File("/tmp/j2ee14-instance.xml");
//
//        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
//        XMLStreamReader xrdr =
//            xmlInputFactory.createXMLStreamReader(new FileReader(doc));
//
//        UnmarshalContext um_ctx =
//            bindingContext.createUnmarshallContext(new ArrayList(), xrdr);
//        Object obj = unmarshaller.unmarshal(um_ctx);
//
//        System.out.println("obj = " + obj);
//        Assert.assertTrue(!um_ctx.hasErrors());
//    }

    public void testByNameBeanUnmarshalType()
        throws Exception
    {
        File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);


        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();

        Assert.assertNotNull(unmarshaller);

        final File doc = TestEnv.xbeanCase("marshal/doc.xml");
        final String javaType = "com.mytest.MyClass";
        final QName schemaType = new QName("java:com.mytest", "MyClass");

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xrdr =
            xmlInputFactory.createXMLStreamReader(new FileReader(doc));

        UnmarshalContext ctx =
            bindingContext.createUnmarshallContext(new ArrayList(), xrdr);

        //this is not very safe but it should work...
        while (!xrdr.isStartElement()) {
            xrdr.next();
        }

        Object obj = unmarshaller.unmarshalType(schemaType, javaType, ctx);
        final Collection errors = ctx.getErrors();
        for (Iterator itr = errors.iterator(); itr.hasNext();) {
            System.out.println("ERROR: " + itr.next());
        }
        System.out.println("+++++TYPE obj = " + obj);

        Assert.assertTrue(errors.isEmpty());
        Assert.assertTrue(!ctx.hasErrors());
    }

    public void DISABLED_testPerfByNameBeanUnmarshall()
        throws Exception
    {
        File bcdoc = getBindingConfigDocument();

        BindingContext bindingContext =
            BindingContextFactory.newInstance().createBindingContext(bcdoc);

        Unmarshaller unmarshaller = bindingContext.createUnmarshaller();

        Assert.assertNotNull(unmarshaller);

        //File doc = TestEnv.xbeanCase("marshal/doc2.xml");
        File doc = TestEnv.xbeanCase("marshal/bigdoc.xml");
        final FileReader fileReader = new FileReader(doc);
        CharArrayWriter cw = new CharArrayWriter();

        bufferedStreamCopy(fileReader, cw);
        final char[] chars = cw.toCharArray();
        final CharArrayReader cr = new CharArrayReader(chars);

        final int trials = 50000;

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        final long before_millis = System.currentTimeMillis();
        for (int i = 0; i < trials; i++) {
            cr.reset();
            XMLStreamReader xrdr =
                xmlInputFactory.createXMLStreamReader(cr);

            Object obj = unmarshaller.unmarshal(null);

            if ((i % 1000) == 0) {
                String s = obj.toString().substring(0, 70);
                System.out.println("i=" + i + "\tobj = " + s + "...");
            }
        }
        final long after_millis = System.currentTimeMillis();
        final long diff = (after_millis - before_millis);
        System.out.println("milliseconds: " + diff + " trials: " + trials);
        System.out.println("milliseconds PER trial: " + (diff / (double)trials));
    }

    protected static void bufferedStreamCopy(Reader in, Writer out)
        throws IOException
    {
        int charsRead;
        char[] buf = new char[1024];

        while ((charsRead = in.read(buf)) != -1) {
            out.write(buf, 0, charsRead);
        }
    }


    private File getBindingConfigDocument()
    {
        File loc = TestEnv.xbeanCase("marshal/example_config.xml");
        return loc;
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
