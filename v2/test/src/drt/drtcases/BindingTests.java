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
import org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument;

import javax.xml.namespace.QName;

public class BindingTests extends TestCase
{
    public BindingTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(BindingTests.class); }

    public void testBindingFile() throws Exception
    {
        BindingFile bf = new BindingFile();
        BuiltinBindingLoader builtins = new BuiltinBindingLoader();
        
        // some complex types
        ByNameBean bnb = new ByNameBean(JavaName.forString("com.mytest.MyClass"), XmlName.forString("t=my-type@http://www.mytest.com/"), false);
        bf.addBindingType(bnb, true, true);
        ByNameBean bnb2 = new ByNameBean(JavaName.forString("com.mytest.YourClass"), XmlName.forString("t=your-type@http://www.mytest.com/"), false);
        bf.addBindingType(bnb2, true, true);
        
        // a custom simple type
        SimpleBindingType sbt = new SimpleBindingType(JavaName.forString("java.lang.String"), XmlName.forString("t=custom-string@http://www.mytest.com/"), false);
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
        System.out.println(doc.toString());
        
        // now load
        BindingFile bfc = BindingFile.forDoc(doc);
        BindingLoader lc = PathBindingLoader.forPath(new BindingLoader[] {builtins, bfc});
        ByNameBean bnbc = (ByNameBean)bfc.getBindingType(JavaName.forString("com.mytest.MyClass"), XmlName.forString("t=my-type@http://www.mytest.com/"));
        ByNameBean bnb2c = (ByNameBean)bfc.getBindingType(JavaName.forString("com.mytest.YourClass"), XmlName.forString("t=your-type@http://www.mytest.com/"));
        SimpleBindingType sbtc = (SimpleBindingType)bfc.getBindingType(JavaName.forString("java.lang.String"), XmlName.forString("t=custom-string@http://www.mytest.com/"));

        // check loading xsd:float
        QName qn = new QName("http://www.w3.org/2001/XMLSchema", "float");
        XmlName xn = XmlName.forTypeNamed(qn);
        XmlName xns = XmlName.forString("t=float@http://www.w3.org/2001/XMLSchema");
        Assert.assertEquals(xn, xns);
        Assert.assertEquals(xn.hashCode(), xns.hashCode());
        BindingType btype = lc.getBindingTypeForXmlPojo(xn);
        Assert.assertNotNull(btype);

        // check bnb
        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt"));
        Assert.assertEquals("setMyelt", prop.getSetterName());
        Assert.assertEquals("getMyelt", prop.getGetterName());
        Assert.assertEquals(bnb2c, prop.getBindingType(lc));

        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt2"));
        Assert.assertEquals("setMyelt2", prop.getSetterName());
        Assert.assertEquals("getMyelt2", prop.getGetterName());
        Assert.assertEquals(bnbc, prop.getBindingType(lc));
        
        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myatt"));
        Assert.assertEquals("setMyatt", prop.getSetterName());
        Assert.assertEquals("getMyatt", prop.getGetterName());
        Assert.assertEquals(sbtc, prop.getBindingType(lc));
        
        // check bnb2
        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt"));
        Assert.assertEquals("setYourelt", prop.getSetterName());
        Assert.assertEquals("getYourelt", prop.getGetterName());
        Assert.assertEquals(bnb2c, prop.getBindingType(lc));

        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt2"));
        Assert.assertEquals("setYourelt2", prop.getSetterName());
        Assert.assertEquals("getYourelt2", prop.getGetterName());
        Assert.assertEquals(bnbc, prop.getBindingType(lc));
        
        // check sbtc
        Assert.assertEquals(XmlName.forString("t=string@http://www.w3.org/2001/XMLSchema"), sbtc.getAsIfXmlType());
    }
}
