package org.apache.xmlbeans.test.jam;

import junit.framework.TestCase;
import org.apache.xmlbeans.impl.jam.*;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class JamTests extends TestCase {

  // ========================================================================
  // Constants

  private static final String
          DUMMY = "org.apache.xmlbeans.test.jam.dummyclasses";

  // ========================================================================
  // Variables

  private JService mService = null;
  private JClassLoader mLoader = null;

  // ========================================================================
  // Constructors
  
  public JamTests() {
    super("JamTests");
  }

  public JamTests(String casename) {
    super(casename);
  }

  // ========================================================================
  // TestCase implementation

  public void setUp() throws IOException {
    JServiceFactory jsf = JServiceFactory.getInstance();
    JServiceParams params = jsf.createServiceParams();
    params.includeSourceFiles(new File("dummyclasses"),"**/*.java");
    mService = jsf.createService(params);
    mLoader = mService.getClassLoader();
  }

  // ========================================================================
  // Test methods

  public void testInterfaceIsAssignableFrom()
    throws ClassNotFoundException 
  {
    JClass fooImpl = mLoader.loadClass(DUMMY+".FooImpl");
    JClass foo = mLoader.loadClass(DUMMY+".Foo");
    assertTrue("Foo should be assignableFrom FooImpl",
               foo.isAssignableFrom(fooImpl));
    assertTrue("FooImpl should not be assignableFrom Foo",
               !fooImpl.isAssignableFrom(foo));
  }

  public void testClassIsAssignableFrom() 
    throws ClassNotFoundException 
  {
    JClass fooImpl = mLoader.loadClass(DUMMY+".FooImpl");
    JClass base = mLoader.loadClass(DUMMY+".Base");
    assertTrue("Base should be assignableFrom FooImpl",
               base.isAssignableFrom(fooImpl));
    assertTrue("FooImpl should not be assignableFrom Base",
               !fooImpl.isAssignableFrom(base));
  }

  public void testClassIsAssignableFromDifferentClassLoaders() 
    throws ClassNotFoundException 
  {
    JClass baz = mLoader.loadClass(DUMMY+".Baz");
    JClass runnable = mLoader.loadClass("java.lang.Runnable");
    assertTrue("Runnable should be assignableFrom Baz",
               runnable.isAssignableFrom(baz));
    assertTrue("Baz should not be assignableFrom Runnable",
               !baz.isAssignableFrom(runnable));
  }


  public void testAnnotationsAndInheritance() {
    JClass ejb = mLoader.loadClass(DUMMY+".ejb.TraderEJB");
    JClass ienv = ejb.getInterfaces()[0];
    JMethod ejbBuy = ejb.getMethods()[0];
    JMethod ienvBuy = ienv.getMethods()[0];


    String INTER_ANN = "ejbgen:remote-method@transaction-attribute";
    String INTER_ANN_VALUE = "NotSupported";
    String CLASS_ANN = "ejbgen:remote-method@isolation-level";
    String CLASS_ANN_VALUE = "Serializable";

    verifyAnnotationAbsent(ejbBuy,INTER_ANN);
    verifyAnnotationAbsent(ienvBuy,CLASS_ANN);

    verifyAnnotation(ienvBuy,INTER_ANN,INTER_ANN_VALUE);
    verifyAnnotation(ejbBuy,CLASS_ANN,CLASS_ANN_VALUE);
  }



  // ========================================================================
  // Private methods

  private void verifyAnnotation(JElement j, String ann, String val) {
    JAnnotation a = j.getAnnotation(ann);
    assertTrue(j.getParent().getQualifiedName()+" '"+j.getQualifiedName()+"' is missing expected annotation '"+ann+"'",
                a != null);
    assertTrue(j.getQualifiedName()+"  annotation '"+ann+"' does not equal "+
               val,val.equals(a.getStringValue().trim()));
  }

  private void verifyAnnotationAbsent(JElement j, String ann) {
    JAnnotation a = j.getAnnotation(ann);
    assertTrue("'"+j.getQualifiedName()+"' expected to NOT have annotation '"+ann+"'",
                a == null);
  }



}
