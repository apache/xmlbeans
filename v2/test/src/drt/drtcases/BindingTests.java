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
import org.apache.xmlbeans.impl.binding.bts.JavaName;
import org.apache.xmlbeans.impl.binding.bts.PathBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.XmlName;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.compile.JAXRPCSchemaBinder;
import org.apache.xmlbeans.impl.binding.compile.SchemaToJavaResult;
import org.apache.xmlbeans.impl.binding.compile.JavaCodeGenerator;
import org.apache.xmlbeans.impl.binding.compile.SchemaToJavaInput;
import org.apache.xmlbeans.impl.binding.compile.SimpleSchemaSourceSet;
import org.apache.xmlbeans.impl.binding.compile.SimpleSchemaToJavaResultCompiler;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;

import javax.xml.namespace.QName;
import java.io.File;
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
        SchemaToJavaInput input = SimpleSchemaSourceSet.forFile(typesonlyfile);
        SchemaToJavaResult result = JAXRPCSchemaBinder.bind(input);
        if (verbose)
        {
            result.getBindingFileGenerator().printBindingFile(System.out);
            JavaCodeGenerator javacode = result.getJavaCodeGenerator();
            for (Iterator i = javacode.getToplevelClasses().iterator(); i.hasNext(); )
            {
                String javaclass = (String)i.next();
                System.out.println("=======================");
                System.out.println(javaclass);
                System.out.println("=======================");
                javacode.printSourceCode(javaclass, System.out);
                System.out.flush();
            }
        }
        
        // now compile
        SimpleSchemaToJavaResultCompiler.Params params = new SimpleSchemaToJavaResultCompiler.Params();
        File theJar = TestEnv.xbeanOutput("schema/binding/typesonly.jar");
        params.setOutputJar(theJar);
        // to test later
        //SimpleSchemaToJavaResultCompiler.compile(result, params);
    }

    public void testBindingFile() throws Exception
    {
        BindingFile bf = new BindingFile();
        BindingLoader builtins = BuiltinBindingLoader.getInstance();

        // some complex types
        ByNameBean bnb = new ByNameBean(BindingTypeName.forPair(JavaName.forString("com.mytest.MyClass"), XmlName.forString("t=my-type@http://www.mytest.com/")));
        bf.addBindingType(bnb, true, true);
        ByNameBean bnb2 = new ByNameBean(BindingTypeName.forPair(JavaName.forString("com.mytest.YourClass"), XmlName.forString("t=your-type@http://www.mytest.com/")));
        bf.addBindingType(bnb2, true, true);

        // a custom simple type
        SimpleBindingType sbt = new SimpleBindingType(BindingTypeName.forPair(JavaName.forString("java.lang.String"), XmlName.forString("t=custom-string@http://www.mytest.com/")));
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
        sbt.setAsIfXmlType(XmlName.forString("t=string@http://www.w3.org/2001/XMLSchema"));

        // now serialize
        BindingConfigDocument doc = bf.write();
        if (verbose)
            System.out.println(doc.toString());

        // now load
        BindingFile bfc = BindingFile.forDoc(doc);
        BindingLoader lc = PathBindingLoader.forPath(new BindingLoader[] {builtins, bfc});
        ByNameBean bnbc = (ByNameBean)bfc.getBindingType(BindingTypeName.forPair(JavaName.forString("com.mytest.MyClass"), XmlName.forString("t=my-type@http://www.mytest.com/")));
        ByNameBean bnb2c = (ByNameBean)bfc.getBindingType(BindingTypeName.forPair(JavaName.forString("com.mytest.YourClass"), XmlName.forString("t=your-type@http://www.mytest.com/")));
        SimpleBindingType sbtc = (SimpleBindingType)bfc.getBindingType(BindingTypeName.forPair(JavaName.forString("java.lang.String"), XmlName.forString("t=custom-string@http://www.mytest.com/")));

        // check loading xsd:float
        {
            QName qn = new QName("http://www.w3.org/2001/XMLSchema", "float");
            XmlName xn = XmlName.forTypeNamed(qn);
            XmlName xns = XmlName.forString("t=float@http://www.w3.org/2001/XMLSchema");
            Assert.assertEquals(xn, xns);
            Assert.assertEquals(xn.hashCode(), xns.hashCode());
            BindingType btype = lc.getBindingType(lc.lookupPojoFor(xn));
            Assert.assertNotNull(btype);
        }

        // check loading xsd:string
        {
            QName qn = new QName("http://www.w3.org/2001/XMLSchema", "string");
            XmlName xn = XmlName.forTypeNamed(qn);
            XmlName xns = XmlName.forString("t=string@http://www.w3.org/2001/XMLSchema");
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
        Assert.assertEquals(XmlName.forString("t=string@http://www.w3.org/2001/XMLSchema"), sbtc.getAsIfXmlType());
    }
}
