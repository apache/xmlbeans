/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 3, 2003
 */
package drtcases;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.PathBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.compile.Schema2Java;
import org.apache.xmlbeans.impl.binding.compile.SchemaSourceSet;
import org.apache.xmlbeans.impl.binding.compile.SimpleSourceSet;
import org.apache.xmlbeans.impl.binding.joust.SourceJavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.WriterFactory;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.binding.tylar.TylarWriter;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.w3.x2001.xmlSchema.SchemaDocument;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.Iterator;

public class BindingTests extends TestCase
{
    public BindingTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(BindingTests.class); }
    
    public static boolean verbose = false;
    
    public void testJAXRPCBinding() throws Exception
    {
        // bind
        File typesonlyfile = TestEnv.xbeanCase("schema/typesonly/typesonly.xsd");
        SchemaSourceSet input = SimpleSourceSet.forXsdFile(typesonlyfile, null);
        Schema2Java compiler = new Schema2Java(input);

        final JavaOutputStream joust = createJoust();
        BindingFile bindingFile = null;
        TylarWriter twriter = new TylarWriter() {
          public JavaOutputStream getJavaOutputStream() { return joust; }
          public void writeBindingFile(BindingFile bf) throws IOException {
            if (verbose) bf.write().save(System.out);
          }
          public void writeSchema(SchemaDocument xsd, String filepath) {}
          public void close() {}
        };
        //FIXME this is kinda dumb, just emulating current behavior.
        //real test should create a tylar on disk  -pcal
        compiler.bind(twriter);

        // now compile
        // SimpleSchemaToJavaResultCompiler.Params params = new SimpleSchemaToJavaResultCompiler.Params();
        // File theJar = TestEnv.xbeanOutput("schema/binding/typesonly.jar");
        // params.setOutputJar(theJar);
        // to test later
        //SimpleSchemaToJavaResultCompiler.compile(result, params);
    }

  //creates a JavaOutputStream that either spits out to System.out
  //or swallows the source output entirely.  This is temporary.  -pcal
  private JavaOutputStream createJoust() {
    final PrintWriter sourceOut;
    if (verbose) {
      sourceOut = new PrintWriter(System.out);
    } else {
      sourceOut = new PrintWriter(new Writer() { //null writer
        public void write(char cbuf[], int off, int len) {}
        public void close() {};
        public void flush() {};
      });
    }
    return new SourceJavaOutputStream
            (new WriterFactory() {
              public Writer createWriter(String x, String y) {
                sourceOut.println("=======================");
                return sourceOut;
              }
            });
  }



    public void testBindingFile() throws Exception
    {
        BindingFile bf = new BindingFile();
        BindingLoader builtins = BuiltinBindingLoader.getInstance();

        // some complex types
        ByNameBean bnb = new ByNameBean(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.MyClass"), XmlTypeName.forString("t=my-type@http://www.mytest.com/")));
        bf.addBindingType(bnb, true, true);
        ByNameBean bnb2 = new ByNameBean(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.YourClass"), XmlTypeName.forString("t=your-type@http://www.mytest.com/")));
        bf.addBindingType(bnb2, true, true);

        // a custom simple type
        SimpleBindingType sbt = new SimpleBindingType(BindingTypeName.forPair(JavaTypeName.forString("java.lang.String"), XmlTypeName.forString("t=custom-string@http://www.mytest.com/")));
        bf.addBindingType(sbt, false, true); // note not from-java-default for String


        // bnb

        QNameProperty prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "myelt"));
        prop.setSetterName("setMyelt");
        prop.setGetterName("getMyelt");
        prop.setBindingType(bnb2);
        bnb.addProperty(prop);

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "myelt2"));
        prop.setSetterName("setMyelt2");
        prop.setGetterName("getMyelt2");
        prop.setBindingType(bnb);
        bnb.addProperty(prop);

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "myatt"));
        prop.setSetterName("setMyatt");
        prop.setGetterName("getMyatt");
        prop.setBindingType(sbt);
        bnb.addProperty(prop);

        // now bnb2

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "yourelt"));
        prop.setSetterName("setYourelt");
        prop.setGetterName("getYourelt");
        prop.setBindingType(bnb2);
        bnb2.addProperty(prop);

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "yourelt2"));
        prop.setSetterName("setYourelt2");
        prop.setGetterName("getYourelt2");
        prop.setBindingType(bnb);
        bnb2.addProperty(prop);

        // sbt
        sbt.setAsIfXmlType(XmlTypeName.forString("t=string@http://www.w3.org/2001/XMLSchema"));

        // now serialize
        BindingConfigDocument doc = bf.write();
        if (verbose)
            System.out.println(doc.toString());

        // now load
        BindingFile bfc = BindingFile.forDoc(doc);
        BindingLoader lc = PathBindingLoader.forPath(new BindingLoader[] {builtins, bfc});
        ByNameBean bnbc = (ByNameBean)bfc.getBindingType(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.MyClass"), XmlTypeName.forString("t=my-type@http://www.mytest.com/")));
        ByNameBean bnb2c = (ByNameBean)bfc.getBindingType(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.YourClass"), XmlTypeName.forString("t=your-type@http://www.mytest.com/")));
        SimpleBindingType sbtc = (SimpleBindingType)bfc.getBindingType(BindingTypeName.forPair(JavaTypeName.forString("java.lang.String"), XmlTypeName.forString("t=custom-string@http://www.mytest.com/")));

        // check loading xsd:float
        {
            QName qn = new QName("http://www.w3.org/2001/XMLSchema", "float");
            XmlTypeName xn = XmlTypeName.forTypeNamed(qn);
            XmlTypeName xns = XmlTypeName.forString("t=float@http://www.w3.org/2001/XMLSchema");
            Assert.assertEquals(xn, xns);
            Assert.assertEquals(xn.hashCode(), xns.hashCode());
            BindingType btype = lc.getBindingType(lc.lookupPojoFor(xn));
            Assert.assertNotNull(btype);
        }

        // check loading xsd:string
        {
            QName qn = new QName("http://www.w3.org/2001/XMLSchema", "string");
            XmlTypeName xn = XmlTypeName.forTypeNamed(qn);
            XmlTypeName xns = XmlTypeName.forString("t=string@http://www.w3.org/2001/XMLSchema");
            Assert.assertEquals(xn, xns);
            Assert.assertEquals(xn.hashCode(), xns.hashCode());
            BindingType btype = lc.getBindingType(lc.lookupPojoFor(xn));
            Assert.assertNotNull(btype);
        }

        // check bnb
        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt"));
        Assert.assertEquals("setMyelt", prop.getSetterName());
        Assert.assertEquals("getMyelt", prop.getGetterName());
        Assert.assertEquals(bnb2c, lc.getBindingType(prop.getTypeName()));

        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt2"));
        Assert.assertEquals("setMyelt2", prop.getSetterName());
        Assert.assertEquals("getMyelt2", prop.getGetterName());
        Assert.assertEquals(bnbc, lc.getBindingType(prop.getTypeName()));

        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myatt"));
        Assert.assertEquals("setMyatt", prop.getSetterName());
        Assert.assertEquals("getMyatt", prop.getGetterName());
        Assert.assertEquals(sbtc, lc.getBindingType(prop.getTypeName()));

        // check bnb2
        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt"));
        Assert.assertEquals("setYourelt", prop.getSetterName());
        Assert.assertEquals("getYourelt", prop.getGetterName());
        Assert.assertEquals(bnb2c, lc.getBindingType(prop.getTypeName()));

        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt2"));
        Assert.assertEquals("setYourelt2", prop.getSetterName());
        Assert.assertEquals("getYourelt2", prop.getGetterName());
        Assert.assertEquals(bnbc, lc.getBindingType(prop.getTypeName()));

        // check sbtc
        Assert.assertEquals(XmlTypeName.forString("t=string@http://www.w3.org/2001/XMLSchema"), sbtc.getAsIfXmlType());
    }
}
