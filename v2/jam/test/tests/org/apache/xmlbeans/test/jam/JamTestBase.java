/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.test.jam;

import junit.framework.TestCase;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.annogen.AnnotationServiceFactory;
import org.apache.xmlbeans.impl.jam.annogen.AnnotationService;
import org.apache.xmlbeans.impl.jam.annogen.AnnotationServiceParams;
import org.apache.xmlbeans.impl.jam.xml.JamXmlUtils;
import org.w3c.dom.Document;

import javax.xml.stream.XMLStreamException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileReader;
import java.io.StringReader;
import java.io.InputStream;
import java.io.Writer;
import java.io.FileInputStream;
import java.util.*;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import org.apache.xmlbeans.test.jam.dummyclasses.jsr175.RFEAnnotation;
import org.apache.xmlbeans.test.jam.dummyclasses.jsr175.EmployeeAnnotation;
import org.apache.xmlbeans.test.jam.dummyclasses.jsr175.EmployeeGroupAnnotation;
import org.apache.xmlbeans.test.jam.dummyclasses.jsr175.AddressAnnotation;
import org.apache.xmlbeans.test.jam.dummyclasses.jsr175.impl.RFEAnnotationImpl;

/**
 * <p>Abstract base class for basic jam test cases.  These test cases work
 * against an abstract JamService - they don't care how the java types
 * were loaded.  Extending classes are responsible for implementing the
 * getService() method which should create the service from sources, or
 * classes, or whatever is appropriate.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class JamTestBase extends TestCase {

  // ========================================================================
  // Constants

  private static final boolean CONTINUE_ON_COMPARE_FAIL = true;
  private static final boolean WRITE_RESULT_ON_FAIL = true;

  private static final String WRITE_RESULT_PREFIX = "result-";

  protected static final String
          DUMMY = "org.apache.xmlbeans.test.jam.dummyclasses";

  protected static final String DUMMY_EJB = DUMMY+".ejb";

  private static final String OUTER_CLASS = DUMMY+".OuterClass";
  private static final String OUTER_CLASS_TOO = DUMMY+".OuterClassToo";
  private static final String INNER_A = OUTER_CLASS+"$InnerClassA";

  //this array must contain the names of all of the test classes under
  //dummyclasses
  private static final String[] ALL_CLASSES = {
    "DefaultPackageClass",
    "org.TopLevelPackageClass",
    DUMMY+".ejb.IEnv",
    DUMMY+".ejb.MyEjbException",
    DUMMY+".ejb.TraderEJB",
    DUMMY+".ejb.TradeResult",

    DUMMY+".jsr175.AnnotatedClass",
    DUMMY+".jsr175.NestedAnnotatedClass",
    DUMMY+".jsr175.AddressAnnotation",
    DUMMY+".jsr175.EmployeeAnnotation",
    DUMMY+".jsr175.EmployeeGroupAnnotation",
    DUMMY+".jsr175.Constants",

    DUMMY+".jsr175.RFEAnnotation",
    DUMMY+".jsr175.impl.RFEAnnotationImpl",

    DUMMY+".Base",
    DUMMY+".Baz",
    DUMMY+".Foo",
    DUMMY+".FooImpl",
    DUMMY+".HeavilyCommented",
    DUMMY+".ImportsGalore",
    DUMMY+".MyException",
    DUMMY+".MyGenericThing",
    DUMMY+".MyGenericThingSubclass",
    OUTER_CLASS,
    OUTER_CLASS_TOO,
    DUMMY+".MultilineTags",
    DUMMY+".ManyTags",
    DUMMY+".ValuesById"
  };


  // this needs to correspond to the methods on the FooImpl dummyclass
  private static final String[][] FOOIMPL_METHODS = {
    {"public",                   "int",      "getId",  null,   null},

    {"public",                   "void",     "setId",  "int id",null},

    {"private final static",     "void",     "setId2",  "double id",null},

    {"protected synchronized ",  "void",     "setId3",  "double id, double id2",null},

    {"protected abstract",       "void",     "setId4",  "double id, double id2, double id3",null},

    {"",             "java.lang.String[][]", "methodDealingWithArrays",  "int[] foo, java.lang.Object[] bar",null},

    {"protected abstract",       "void",     "iThrowExceptions",  "int p1, java.lang.String p2",
     "java.lang.IllegalArgumentException," +
          "java.lang.NoSuchMethodError," +
          DUMMY+".MyException,"+
          DUMMY+".ejb.MyEjbException,"+
          "java.net.MalformedURLException,"+
          "java.lang.OutOfMemoryError,"+
          "java.lang.NullPointerException"
    }
  };

  // this needs to correspond to the methods on the FooImpl dummyclass
  private static final String[] HEAVILY_COMMENTS = {
    "A simple comment.",
    "A comment which\n spans\n\n several\n\n\n lines."
  };
  /**
   * A comment which
   * spans
   *
   * several
   *
   *
   * lines.
   */

  private static final boolean VERBOSE = false;

  // ========================================================================
  // Variables

  private JamService mResult = null;
  private JamClassLoader mLoader = null;


  // ========================================================================
  // Constructors

  public JamTestBase() {
    super("JamTestBase");
    System.out.println("constructed JamTestBase");
  }

  public JamTestBase(String casename) {
    super(casename);
    System.out.println("constructed JamTestBase "+casename);    
  }

  // ========================================================================
  // Abstract methods

  /**
   * Called during setup() to get the JamService object to test against.
   */
  protected abstract JamService getResultToTest() throws Exception;

  //kind of a quick hack for now, should remove this and make sure that
  //even the classes case make the annotations available using a special
  //JStore
  protected abstract boolean isAnnotationsAvailable();


  //kind of a quick hack for now, should remove this and make sure that
  //even the classes case make the annotations available using a special
  //JStore
  protected abstract boolean is175AnnotationInstanceAvailable();


  //kind of a quick hack for now, should remove this and make sure that
  //even the classes case make the annotations available using a special
  //JStore
  protected abstract boolean isParameterNamesKnown();

  protected abstract boolean isCommentsAvailable();

  protected abstract boolean isImportsAvailable();

  protected abstract File getMasterDir();

  // ========================================================================
  // Utility methods

  /**
   * Returns the directory in which the sources for the dummyclasses live.
   */
  protected File[] getDummyclassesSourcepath() {
    return new File[] {new File("dummyclasses")};
  }

  /**
   * Returns the directory into which the dummyclasses have been compiled.
   */
  protected File[] getDummyclassesClassPath() {
    return new File[] {new File("../../build/jam/test/dummyclasses")};
  }

  // ========================================================================
  // TestCase implementation

  public void setUp() throws Exception {
    mResult = getResultToTest();
    mLoader = mResult.getClassLoader();
  }

  // ========================================================================
  // Test methods

  public void testAnnogen() {
    AnnotationServiceFactory asf = AnnotationServiceFactory.getInstance();
    AnnotationServiceParams asp = asf.createServiceParams();
    asp.appendPopulator(new TestProxyPopulator());
    AnnotationService as = asf.createService(asp);
    JClass c = mLoader.loadClass(DUMMY+".jsr175.AnnotatedClass");
    RFEAnnotationImpl ra = (RFEAnnotationImpl)
      as.getAnnotation(RFEAnnotationImpl.class,c);

    assertTrue("ra.id() == "+ra.id(),ra.id() == 4561413 + 1);
  }


  public void testGenerics() {
    JClass gts = mLoader.loadClass(DUMMY+".MyGenericThingSubclass");
    resolved(gts);
//    System.out.println("=========== "+gts.getSuperclass().getQualifiedName());
  }

  public void testArrayNames() {
    doOneArrayTest("int[]","[I");
    doOneArrayTest("boolean[][]","[[Z");
    doOneArrayTest("java.lang.Object[]","[Ljava.lang.Object;");    
    doOneArrayTest("java.lang.String[][][]","[[[Ljava.lang.String;");
  }

  private void doOneArrayTest(String decl, String fd) {
    JClass a = mLoader.loadClass(decl);
    JClass b = mLoader.loadClass(fd);
    assertTrue("didn't get same array class for int array", a == b);
    assertTrue("it isn't an array!", a.isArrayType());
    assertTrue("wrong name ", a.getQualifiedName().equals(decl));
    assertTrue("wrong fd ", a.getFieldDescriptor().equals(fd));
  }


  public void testInnerClasses() {
    // make sure we can load it by name this way
    resolved(mLoader.loadClass(INNER_A));
    JClass outer = resolved(mLoader.loadClass(OUTER_CLASS));
    JClass outerToo = resolved(mLoader.loadClass(OUTER_CLASS_TOO));
    assertTrue(outerToo.getQualifiedName().equals(OUTER_CLASS_TOO));
    JClass[] inners = outerToo.getClasses();
    assertTrue(OUTER_CLASS_TOO+" does not contain 3 inner classes",
               inners.length == 3);
    inners = outer.getClasses();
    assertTrue("first inner class name is '"+inners[0].getQualifiedName()+
               "', expecting '"+INNER_A,
               inners[0].getQualifiedName().equals(INNER_A));
    assertTrue("first inner class has unexpected inner classes!",
               inners[0].getClasses().length == 0);
  }


  public void testAllClassesAvailable() {
    JClass[] classes = mResult.getAllClasses();
    List classNames = new ArrayList(classes.length);
    for(int i=0; i<classes.length; i++) {
      resolved(classes[i]);
      classNames.add(classes[i].getQualifiedName());
      //System.out.println("-- "+classes[i].getQualifiedName());
    }
    List expected = Arrays.asList(ALL_CLASSES);
    {
      //System.out.println("found:");
      //for(int i=0; i<classNames.size(); i++) System.out.println(classNames.get(i).toString());
    }
    assertTrue("result does not contain all expected classes",
               classNames.containsAll(expected));
    assertTrue("result contains more than expected classes",
               expected.containsAll(classNames));
  }

  public void testAnnotationUrlValues() {
    if (!isAnnotationsAvailable()) return;
    JClass clazz = resolved(mLoader.loadClass(DUMMY+".ValuesById"));
    {
      final String ANN = "xsdgen:type@target_namespace";
      final String VAL = "http://www.yahoo.com";
      JAnnotationValue tns = clazz.getAnnotationValue(ANN);
      assertTrue("no "+ANN, tns !=  null);
      assertTrue(ANN+" does not equal "+VAL+", instead is '"+tns.asString(),
                 tns.asString().equals(VAL));
    }
    {
      final String ANN = "xsdgen:type@quoted_tns";
      final String VAL = "http://homestarrunner.com/sbemail58.html";
      JAnnotationValue tns = clazz.getAnnotationValue(ANN);
      assertTrue("no "+ANN, tns !=  null);
      assertTrue(ANN+" does not equal "+VAL+", instead is '"+tns.asString(),
                 tns.asString().equals(VAL));
    }
    {
      final String ANN = "someurl";
      final String VAL = "http://www.apache.org/foo";
      JAnnotationValue tns = clazz.getAnnotationValue(ANN);
      assertTrue("no "+ANN, tns !=  null);
      assertTrue(ANN+" does not equal "+VAL+", instead is '"+tns.asString(),
                 tns.asString().equals(VAL));
    }
  }


  public void testAnnotationValuesById() {
    if (!isAnnotationsAvailable()) return;
    JClass clazz = resolved(mLoader.loadClass(DUMMY+".ValuesById"));
//    assertTrue("value id foo has unexpected single-member value",
//               clazz.getAnnotationValue("foo") == null);
    {
      final String ANN = "bar@x";
      final String VAL = "hello";
      JAnnotationValue barx = clazz.getAnnotationValue(ANN);
      assertTrue("no "+ANN, barx !=  null);
      assertTrue(ANN+" does not equal "+VAL+", instead is '"+barx.asString(),
                 barx.asString().equals(VAL));
    }
    {
      final String ANN = "bar@y";
      final String VAL = "goodbye";
      JAnnotationValue bary = clazz.getAnnotationValue(ANN);
      assertTrue("no "+ANN, bary !=  null);
      assertTrue(ANN+" does not equal "+VAL+", instead is '"+bary.asString(),
                 bary.asString().equals(VAL));
    }
    {
      final String ANN = "baz";
      final String VAL = "I have no pairs.";
      JAnnotationValue val = clazz.getAnnotationValue(ANN);
      assertTrue("no "+ANN, val !=  null);
      assertTrue(ANN+" does not equal "+VAL+", instead is '"+val.asString(),
                 val.asString().equals(VAL));
    }
    {
      JAnnotationValue widget = clazz.getAnnotationValue("bar@widegetgen:name");
      assertTrue("no bar@widegentgen:name", widget !=  null);
      assertTrue("bar@widegetgen:name does not equal aloha",widget.asString().equals("aloha"));
    }
    assertTrue(clazz.getAnnotationValue("nothinghere") == null);
  }

  public void testXmlWriter() throws XMLStreamException, IOException
  {
    final String MASTER = "testXmlWriter.xml";
    JClass[] classes = mResult.getAllClasses();
    StringWriter xml = new StringWriter();
    JamXmlUtils.getInstance().toXml(classes,xml);
    //
    compare(xml.toString(), MASTER);
  }

  public void testXmlRoundtrip() throws XMLStreamException, IOException
  {
    final String MASTER = "testXmlRoundtrip.xml";
    final String SOURCE = "testXmlWriter.xml";
    JamXmlUtils jxu = JamXmlUtils.getInstance();
    //JClass[] classes = mResult.getAllClasses();
    File source = new File(getMasterDir(),SOURCE);
    JClass[] classes = jxu.createService(new FileInputStream(source)).
      getAllClasses();
    StringWriter xml = new StringWriter();
    JamXmlUtils.getInstance().toXml(classes,xml);
    compare(xml.toString(), MASTER);
  }

  public void testPackageNames()
  {
    JClass clazz = resolved(mLoader.loadClass(DUMMY_EJB+".TraderEJB"));
    JPackage pkg = clazz.getContainingPackage();
    assertTrue("Expected '"+DUMMY_EJB+"', got '"+pkg.getQualifiedName()+"'",
               pkg.getQualifiedName().equals(DUMMY_EJB));
    //
    clazz = resolved(mLoader.loadClass("DefaultPackageClass"));
    pkg = clazz.getContainingPackage();
    assertTrue("Expected '', got '"+pkg.getQualifiedName()+"'",
               pkg.getQualifiedName().equals(""));
    //
    clazz = resolved(mLoader.loadClass("org.TopLevelPackageClass"));
    pkg = clazz.getContainingPackage();
    assertTrue("Expected 'org', got '"+pkg.getQualifiedName()+"'",
               pkg.getQualifiedName().equals("org"));
  }


  public void test175Annotations() throws IOException, XMLStreamException {
    JClass clazz = resolved(mLoader.loadClass(DUMMY+".jsr175.AnnotatedClass"));
    JAnnotation ann = clazz.getAnnotation(RFEAnnotation.class);
    assertTrue("no "+RFEAnnotation.class+ " on "+clazz.getQualifiedName(),
               ann != null);
    if (!is175AnnotationInstanceAvailable()) return; //FIXME test untyped access
    RFEAnnotation rfe = (RFEAnnotation)ann.getAnnotationInstance();
    assertTrue("id = "+rfe.id(), rfe.id() == 4561414);
    assertTrue("synopsis = '"+rfe.synopsis()+"'",
               rfe.synopsis().equals("Balance the federal budget"));
  }

  public void testNested175AnnotationsUntyped() throws IOException, XMLStreamException {
    JClass clazz = resolved(mLoader.loadClass(DUMMY+".jsr175.NestedAnnotatedClass"));
    JAnnotation employeeGroup = clazz.getAnnotation(EmployeeGroupAnnotation.class);
    assertTrue("employeeGroup is null", employeeGroup != null);
    JAnnotationValue employeeListValue = employeeGroup.getValue("employees");
    JClass listType = employeeListValue.getType();
    assertTrue("listType is null", listType != null);
    assertTrue("listType is "+listType.getFieldDescriptor()+", expecting"+
               EmployeeAnnotation[].class.getName(),
               EmployeeAnnotation[].class.getName().equals(listType.getFieldDescriptor()));
    assertTrue("employees list is null", employeeListValue != null);
    JAnnotation[] employeeList = employeeListValue.asAnnotationArray();
    assertTrue("employees list is null", employeeList != null);
    assertTrue("employees list length is "+employeeList.length+", expecting 2",
               employeeList.length == 2);
    JAnnotation boog = employeeList[0];
    assertTrue("boog annotation is null",boog != null);
    //FIXME shouldnt be isCommentsAvailable
    if (isCommentsAvailable()) assertTrue(boog.getSourcePosition() != null);
    {
      JAnnotationValue firstName = boog.getValue("firstName");
      assertTrue("firstName is null",firstName != null);
      assertTrue("firstName is "+firstName.asString(),
                 firstName.asString().equals("Boog"));
      JClass type = firstName.getType();
      assertTrue("firstName type is null",type != null);
      assertTrue("firstName type is "+type.getQualifiedName(),
                 "java.lang.String".equals(type.getQualifiedName()));
    }
    {
      JAnnotationValue aka = boog.getValue("aka");
      assertTrue("aka is null",aka != null);
      JClass type = aka.getType();
      assertTrue("aka type is null",type != null);
      assertTrue("aka type is "+type.getFieldDescriptor(),
                 String[].class.getName().equals(type.getFieldDescriptor()));
      String[] akas = aka.asStringArray();
      assertTrue("akas is null",akas != null);
      assertTrue("akas length is "+akas.length, akas.length == 3);
      assertTrue("akas type is not an array ", type.isArrayType());

      for(int i=0; i<akas.length; i++) {
        assertTrue("akas "+i+" is empty '"+akas[i]+"'",
                   (akas[i] != null && akas[i].trim().length() > 0));
      }
    }
    {
      JAnnotationValue active = boog.getValue("active");
      assertTrue("active is null",active != null);
      assertTrue("active = "+active.asString(),
                 active.asString().equals("TRUE"));
      JClass type = active.getType();
      assertTrue("active type is null",type != null);

      assertTrue("active type is "+type.getQualifiedName(),
               type.getQualifiedName().equals
                 ("org.apache.xmlbeans.test.jam.dummyclasses.jsr175.Constants$Bool"));

      //FIXME javadoc seems to have a bug in it, not telling is it's an enum
      //assertTrue("active type is not an enum", ((ClassImpl)type).isEnumType());
    }
    {
      JAnnotationValue lastName = boog.getValue("lastName");
      assertTrue("lastName is null",lastName != null);
      assertTrue("lastName is "+lastName.asString(),
                 lastName.asString().equals("Powell"));
      JClass lastNameType = lastName.getType();
      assertTrue("street type is null",lastNameType != null);
      assertTrue("lastNameType "+lastNameType.getQualifiedName(),
                 "java.lang.String".equals(lastNameType.getQualifiedName()));
    }
    {
      JAnnotationValue specialDigits = boog.getValue("specialDigits");
      assertTrue("specialDigits is null",specialDigits != null);
      int[] expect = { 8, 6, 7, 5, 3, 0, 9 };
      assertTrue("specialDigits does not contain expected digits",
                 Arrays.equals(expect,specialDigits.asIntArray()));
      JClass specialDigitsType = specialDigits.getType();
      assertTrue("specialDigits type is null",specialDigitsType != null);
      assertTrue("specialDigits type is "+specialDigitsType.getFieldDescriptor()+
                 ", expecting "+int[].class.getName(),
                 specialDigitsType.getFieldDescriptor().equals(int[].class.getName()));
      assertTrue("specialDigits type is not an array ",
                 specialDigitsType.isArrayType());
    }
    {
      JAnnotationValue addressValue = boog.getValue("address");
      assertTrue("address is null",addressValue != null);
      JAnnotation address = addressValue.asAnnotation();
      assertTrue("address is null",address != null);
      //FIXME shouldnt be isCommentsAvailable
      if (isCommentsAvailable()) assertTrue(boog.getSourcePosition() != null);

      {
        JAnnotationValue street = address.getValue("street");
        assertTrue("street is null",street != null);
        assertTrue("street is "+street.asString(),
                   street.asString().equals("123 shady lane"));
        JClass streetType = street.getType();
        assertTrue("street type is null",streetType != null);
        assertTrue("streetType "+streetType.getQualifiedName(),
                   streetType.getQualifiedName().equals("java.lang.String"));
      }
      {
        JAnnotationValue city = address.getValue("city");
        assertTrue("city is null",city != null);
        assertTrue("city is "+city.asString(),
                   city.asString().equals("Cooperstown"));
        JClass cityType = city.getType();
        assertTrue("street type is null",cityType != null);
        assertTrue("cityType "+cityType.getQualifiedName(),
                   cityType.getQualifiedName().equals("java.lang.String"));

      }
      {
        JAnnotationValue zip = address.getValue("zip");
        assertTrue("zip is null",zip != null);
        assertTrue("zip is "+zip.asInt(),
                   zip.asInt() == 123456);
        JClass zipType = zip.getType();
        assertTrue("street type is null",zipType != null);
        assertTrue("zipType "+zipType.getQualifiedName(),
                   zipType.getQualifiedName().equals("int"));
        assertTrue("zipType not primitive", zipType.isPrimitiveType());
      }
    }
  }


  public void testRecursiveResolve() {
    resolveCheckRecursively(mResult.getAllClasses(),new HashSet());
  }


  /**
   * Test comment parsing on the HeavilyCommented dummy class.
   */
  public void testComments() {
    if (!isCommentsAvailable()) return;
    JClass hcImpl = mLoader.loadClass(DUMMY+".HeavilyCommented");
    JMethod[] methods = hcImpl.getDeclaredMethods();
    for(int i=0; i<methods.length; i++) {
      JComment comment = methods[i].getComment();
      assertTrue("'"+comment.getText()+"'\ndoes not match expected\n'" +
                 HEAVILY_COMMENTS[i]+"'",
                 HEAVILY_COMMENTS[i].equals(comment.getText()));
    }
  }


  /**
   * Verify that FooImpl has the correct methods with the correct
   * number of parameters and correct return types.
   */
  public void testFooImplMethods() {
    JClass fooImpl = resolved(mLoader.loadClass(DUMMY+".FooImpl"));
    GoldenInvokable[] methods = GoldenInvokable.createArray(FOOIMPL_METHODS);
    GoldenInvokable.doComparison(fooImpl.getDeclaredMethods(),
                              methods,isParameterNamesKnown(),this);
  }


  public void testClassImports()
  {
    if (!isImportsAvailable()) return;
    JClass clazz = resolved(mLoader.loadClass(DUMMY+".ImportsGalore"));
    JClass[] imports = clazz.getImportedClasses();
    final String[] IMPORTED_CLASSES = {
      "org.apache.xmlbeans.impl.jam.JClass",
      "javax.xml.stream.XMLStreamException",
      "java.util.Properties"
    };
    assertElementsNamed(imports,IMPORTED_CLASSES);
  }


  public void testPackageImports()
  {
    if (!isImportsAvailable()) return;
    JClass clazz = resolved(mLoader.loadClass(DUMMY+".ImportsGalore"));
    JPackage[] imports = clazz.getImportedPackages();
    final String[] IMPORTED_PACKAGES = {
      "java.lang",
      "java.lang.reflect",
      "java.util",
      "javax.xml.stream",
      "org.apache.xmlbeans.impl.jam"
    };
    assertElementsNamed(imports,IMPORTED_PACKAGES);
  }



  public void testInterfaceIsAssignableFrom()
  {
    JClass fooImpl = resolved(mLoader.loadClass(DUMMY+".FooImpl"));
    JClass foo = resolved(mLoader.loadClass(DUMMY+".Foo"));
    assertTrue("Foo should be assignableFrom FooImpl",
               foo.isAssignableFrom(fooImpl));
    assertTrue("FooImpl should not be assignableFrom Foo",
               !fooImpl.isAssignableFrom(foo));
  }

  public void testClassIsAssignableFrom()
  {
    JClass fooImpl = resolved(mLoader.loadClass(DUMMY+".FooImpl"));
    JClass base = resolved(mLoader.loadClass(DUMMY+".Base"));
    assertTrue("Base should be assignableFrom FooImpl",
               base.isAssignableFrom(fooImpl));
    assertTrue("FooImpl should not be assignableFrom Base",
               !fooImpl.isAssignableFrom(base));
  }

  public void testClassIsAssignableFromDifferentClassLoaders()
  {
    JClass baz = resolved(mLoader.loadClass(DUMMY+".Baz"));
    JClass runnable = resolved(mLoader.loadClass("java.lang.Runnable"));
    assertTrue("Runnable should be assignableFrom Baz",
               runnable.isAssignableFrom(baz));
    assertTrue("Baz should not be assignableFrom Runnable",
               !baz.isAssignableFrom(runnable));
  }

  public void testAnnotationPresent()
  {
    if (!isAnnotationsAvailable()) return;
    String ANN = "ejbgen:remote-method";
    JClass ejb = resolved(mLoader.loadClass(DUMMY+".ejb.TraderEJB"));
    JMethod method = ejb.getMethods()[0];
    assertTrue(method.getQualifiedName()+" does not have expected "+ANN+
               " annotation",
               method.getAnnotation(ANN) != null);
  }

  public void testAnnotationValue()
  {
    if (!isAnnotationsAvailable()) return;
    JClass ejb = resolved(mLoader.loadClass(DUMMY+".ejb.TraderEJB"));
    JMethod ejbBuy = ejb.getMethods()[0];
    String CLASS_ANN = "ejbgen:remote-method@isolation-level";
    String CLASS_ANN_VALUE = "Serializable";
    verifyAnnotationValue(ejbBuy,CLASS_ANN,CLASS_ANN_VALUE);
  }


  public void testAnnotationsAndInheritance()
  {
    JClass ejb = resolved(mLoader.loadClass(DUMMY+".ejb.TraderEJB"));
    JClass ienv = resolved(ejb.getInterfaces()[0]);
    JMethod ejbBuy = ejb.getMethods()[0];
    JMethod ienvBuy = ienv.getMethods()[0];
    String INTER_ANN = "ejbgen:remote-method@transaction-attribute";
    String INTER_ANN_VALUE = "NotSupported";
    String CLASS_ANN = "ejbgen:remote-method@isolation-level";
    String CLASS_ANN_VALUE = "Serializable";

    verifyAnnotationAbsent(ejbBuy,INTER_ANN);
    verifyAnnotationAbsent(ienvBuy,CLASS_ANN);

    if (isAnnotationsAvailable()) {
      verifyAnnotationValue(ienvBuy,INTER_ANN,INTER_ANN_VALUE);
      verifyAnnotationValue(ejbBuy,CLASS_ANN,CLASS_ANN_VALUE);
    } else {
      verifyAnnotationAbsent(ienvBuy,INTER_ANN);
      verifyAnnotationAbsent(ejbBuy,CLASS_ANN);
    }
  }




  public void testMultilineTags() {
    if (!isAnnotationsAvailable()) return;
    JClass mt = resolved(mLoader.loadClass(DUMMY+".MultilineTags"));
    JAnnotation ann = mt.getAllJavadocTags()[5];
    compare(resolved(mt), "testMultilineTags.xml");
  }

  public void testMultipleTags() {
    if (!isAnnotationsAvailable()) return;
    JClass mt = resolved(mLoader.loadClass(DUMMY+".ManyTags"));
    JMethod method = mt.getMethods()[0];
    assertTrue("allJavadocTags has "+method.getAllJavadocTags().length+
               " tags, expecting 6",
               (method.getAllJavadocTags().length == 6));
    compare(mt,"testManyTags.xml");
  }


  // ========================================================================
  // Private methods

  private void assertElementsNamed(JElement[] elements, String[] qnames) {
    assertTrue("array not of correct length ["+
               elements.length+","+qnames.length+"]",
               (elements.length == qnames.length));
    for(int i=0; i<elements.length; i++) {
      String qn = elements[i].getQualifiedName();
      assertTrue("item "+i+" not correct ["+qn+","+qnames[i]+"]",
                 qn.equals(qnames[i]));
    }
  }

  private void compare(String result, String masterFileName) {
    try {
      File masterFile = new File(getMasterDir().getAbsolutePath(),masterFileName);
      StringWriter diff = new StringWriter();
      if (masterFile.exists()) {
        FileReader inA = new FileReader(masterFile);
        StringReader inB = new StringReader(result);
        boolean same = Differ.getInstance().diff(inA,inB,diff);
        if (same) return;
      } else {
        System.out.println("WARNING: Missing master file: "+masterFile);
      }
      if (WRITE_RESULT_ON_FAIL) {
        File resultFile = new File(getMasterDir(),WRITE_RESULT_PREFIX+masterFileName);
        FileWriter rout = new FileWriter(resultFile);
        rout.write(result);
        rout.close();
        System.out.println("WARNING: Comparison failed, ignoring, wrote \n"+
                           resultFile);
      }
      if (CONTINUE_ON_COMPARE_FAIL) return;
      fail("Result did not match master at "+masterFile+":\n"+
           diff.toString());
    } catch(IOException ioe) {
      ioe.printStackTrace();
      fail(ioe.getMessage());
    }
  }

  private void compare(JClass clazz, String masterName) {
    try {
      String result = null;
      {
        StringWriter resultWriter = new StringWriter();
        PrintWriter out = new PrintWriter(resultWriter,true);
        JamXmlUtils.getInstance().toXml(new JClass[] {clazz}, out);
        out.flush();
        result = resultWriter.toString();
        /*
        try {
        System.out.println("--------------- "+resultWriter.toString());
        result = prettyPrint(resultWriter.toString());
        } catch(Exception e) {
        e.printStackTrace();
        System.err.flush();
        System.out.println("Problem with result:");
        System.out.println(resultWriter.toString());
        System.out.flush();
        fail("failed to parse result");
        return;
        }
        */
      }
      compare(result,masterName);
    } catch(XMLStreamException xse) {
      xse.printStackTrace();
      fail(xse.getMessage());
    } catch(IOException ioe) {
      ioe.printStackTrace();
      fail(ioe.getMessage());
    }
  }

  private String prettyPrint(String xml) throws Exception {
    return xml;//FIXME
    //FIXME StringBufferInputStream is bad
    //return prettyPrint(new StringBufferInputStream(xml));
  }

  //all this work just because the 173 RI won't pretty print
  private String prettyPrint(InputStream in)  throws Exception {
    StringWriter s = new StringWriter();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(in);
    OutputFormat format = new OutputFormat(doc);

    //format.setIndenting(true);
    //format.setIndent(2);
    XMLSerializer serializer = new XMLSerializer(s, format);
    serializer.serialize(doc);
    return s.toString();
  }


  private void resolveCheckRecursively(JClass[] clazzes, Set resolved) {
    for(int i=0; i<clazzes.length; i++) {
      resolveCheckRecursively(clazzes[i],resolved);
    }
  }

  private void resolveCheckRecursively(JClass clazz, Set set) {
    if (clazz == null || set.contains(clazz)) return;
    {
      // A bit of a hack, seems this is finding some anonymous inner classes
      // in the reflection case.  We don't deal with these.  Just filter them
      // out
      String name = clazz.getSimpleName();
      name = name.substring(name.lastIndexOf('$')+1);
      char x = name.charAt(0);
      if (('0' <= x) && (x <= '9')) {
        set.add(clazz);
        return;
      }
    }
    assertTrue("'"+clazz.getQualifiedName()+"' is not resolved",
               !clazz.isUnresolvedType());
    if (VERBOSE) System.out.println("checking "+clazz.getQualifiedName());
    set.add(clazz);
    resolveCheckRecursively(clazz.getSuperclass(),set);
    resolveCheckRecursively(clazz.getInterfaces(),set);
    {
      //check methods
      JMethod[] methods = clazz.getDeclaredMethods();
      for(int i=0; i<methods.length; i++) {
        if (methods[i].isPrivate()) continue; //this runs afoul of anonymous classes
        resolveCheckRecursively(methods[i].getReturnType(),set);
        JParameter[] params = methods[i].getParameters();
        for(int j=0; j<params.length; j++) {
          resolveCheckRecursively(params[j].getType(),set);
        }
      }
    }
    {
      //check constructors
      JConstructor[] ctors = clazz.getConstructors();
      for(int i=0; i<ctors.length; i++) {
        JParameter[] params = ctors[i].getParameters();
        for(int j=0; j<params.length; j++) {
          resolveCheckRecursively(params[j].getType(),set);
        }
      }
    }
    {
      //check fields
      JField[] fields = clazz.getFields();
      for(int i=0; i<fields.length; i++) {
        resolveCheckRecursively(fields[i].getType(),set);
      }
    }
  }


  private JClass resolved(JClass c) {
    assertTrue("class "+c.getQualifiedName()+" is not resolved",
               !c.isUnresolvedType());
    return c;
  }

  private void verifyAnnotationValue(JAnnotatedElement j, String valueId, String val) {
    JAnnotationValue v = j.getAnnotationValue(valueId);
    assertTrue(/*j.getParent().getQualifiedName()+" '"+*/
            j.getQualifiedName()+"' is missing expected annotation value '"+valueId+"'",
            v != null);
    assertTrue(j.getQualifiedName()+"  annotation '"+valueId+"' does not equal "+
               val,val.equals(v.asString().trim()));
  }

  private void verifyAnnotationAbsent(JAnnotatedElement j, String ann) {
    JAnnotation a = j.getAnnotation(ann);
    assertTrue("'"+j.getQualifiedName()+"' expected to NOT have annotation '"+ann+"'",
                a == null);
  }

  private void dump(JElement j, PrintWriter out) throws XMLStreamException {
  }



}
