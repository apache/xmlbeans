/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 3, 2003
 */
package drtcases;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.apache.xmlbeans.impl.binding.BindingFile;
import org.apache.xmlbeans.impl.binding.BindingType;
import org.apache.xmlbeans.impl.binding.ByNameBean;
import org.apache.xmlbeans.impl.binding.JavaName;
import org.apache.xmlbeans.impl.binding.XmlName;
import org.apache.xmlbeans.impl.binding.QNameProperty;
import org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument;

import javax.xml.namespace.QName;

public class BindingTests extends TestCase
{
    public BindingTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(BindingTests.class); }

    public void testBindingFile() throws Exception
    {
        BindingFile bf = new BindingFile();
        
        ByNameBean bnb = new ByNameBean(bf, JavaName.forString("com.mytest.MyClass"), XmlName.forString("t=my-type@http://www.mytest.com/"), false);
        bf.addBindingType(bnb, true, true);
        ByNameBean bnb2 = new ByNameBean(bf, JavaName.forString("com.mytest.YourClass"), XmlName.forString("t=your-type@http://www.mytest.com/"), false);
        bf.addBindingType(bnb2, true, true);
        
        // bnb
                        
        QNameProperty prop = new QNameProperty(bf);
        prop.setQName(new QName("http://www.mytest.com/", "myelt"));
        prop.setSetterName("setMyelt");
        prop.setGetterName("getMyelt");
        prop.setBindingType(bnb2);
        bnb.addProperty(prop);
        
        prop = new QNameProperty(bf);
        prop.setQName(new QName("http://www.mytest.com/", "myelt2"));
        prop.setSetterName("setMyelt2");
        prop.setGetterName("getMyelt2");
        prop.setBindingType(bnb);
        bnb.addProperty(prop);
        
        prop = new QNameProperty(bf);
        prop.setQName(new QName("http://www.mytest.com/", "myatt"));
        prop.setSetterName("setMyatt");
        prop.setGetterName("getMyatt");
        prop.setBindingType(bnb); // need to do simples
        bnb.addProperty(prop);
        
        // now bnb2
        
        prop = new QNameProperty(bf);
        prop.setQName(new QName("http://www.mytest.com/", "yourelt"));
        prop.setSetterName("setYourelt");
        prop.setGetterName("getYourelt");
        prop.setBindingType(bnb2);
        bnb2.addProperty(prop);
        
        prop = new QNameProperty(bf);
        prop.setQName(new QName("http://www.mytest.com/", "yourelt2"));
        prop.setSetterName("setYourelt2");
        prop.setGetterName("getYourelt2");
        prop.setBindingType(bnb);
        bnb2.addProperty(prop);
        
        // now serialize
        BindingConfigDocument doc = bf.write();
        System.out.println(doc.toString());
        
        // now load
        BindingFile bfc = BindingFile.forDoc(doc);
        ByNameBean bnbc = (ByNameBean)bfc.getBindingType(JavaName.forString("com.mytest.MyClass"), XmlName.forString("t=my-type@http://www.mytest.com/"));
        ByNameBean bnb2c = (ByNameBean)bfc.getBindingType(JavaName.forString("com.mytest.YourClass"), XmlName.forString("t=your-type@http://www.mytest.com/"));
        
        // check bnb
        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt"));
        Assert.assertEquals("setMyelt", prop.getSetterName());
        Assert.assertEquals("getMyelt", prop.getGetterName());
        Assert.assertEquals(bnb2c, prop.getBindingType());

        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt2"));
        Assert.assertEquals("setMyelt2", prop.getSetterName());
        Assert.assertEquals("getMyelt2", prop.getGetterName());
        Assert.assertEquals(bnbc, prop.getBindingType());
        
        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myatt"));
        Assert.assertEquals("setMyatt", prop.getSetterName());
        Assert.assertEquals("getMyatt", prop.getGetterName());
        Assert.assertEquals(bnbc, prop.getBindingType());
        
        // check bnb2
        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt"));
        Assert.assertEquals("setYourelt", prop.getSetterName());
        Assert.assertEquals("getYourelt", prop.getGetterName());
        Assert.assertEquals(bnb2c, prop.getBindingType());

        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt2"));
        Assert.assertEquals("setYourelt2", prop.getSetterName());
        Assert.assertEquals("getYourelt2", prop.getGetterName());
        Assert.assertEquals(bnbc, prop.getBindingType());
    }
}
