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
package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.binding.bts.*;
import org.apache.xmlbeans.impl.binding.tylar.ExplodedTylar;
import org.apache.xmlbeans.impl.binding.tylar.TylarWriter;
import org.apache.xmlbeans.impl.binding.tylar.ExplodedTylarImpl;
import org.apache.xmlbeans.impl.binding.tylar.Tylar;
import org.apache.xmlbeans.impl.jam.*;
import org.w3.x2001.xmlSchema.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;


/**
 * Takes a set of Java source inputs and generates a set of XML schemas to
 * which those input should be bound, as well as a binding configuration file
 * which describes to the runtime subsystem how the un/marshalling should
 * be performed.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class Java2Schema {

  // =========================================================================
  // Constants

  private static final String JAVA_URI_SCHEME      = "java:";
  private static final String JAVA_NAMESPACE_URI   = "language_builtins";
  private static final String JAVA_PACKAGE_PREFIX  = "java.";

  private static final String TAG_CT               = "xsdgen:complexType";
  private static final String TAG_CT_TYPENAME      = TAG_CT+".typeName";
  private static final String TAG_CT_TARGETNS      = TAG_CT+".targetNamespace";
  private static final String TAG_CT_ROOT          = TAG_CT+".rootElement";

  private static final String TAG_EL               = "xsdgen:element";
  private static final String TAG_EL_NAME          = TAG_EL+".name";
  private static final String TAG_EL_NILLABLE      = TAG_EL+".nillable";
  private static final String TAG_EL_EXCLUDE       = TAG_EL+".exclude";
  private static final String TAG_EL_ASTYPE        = TAG_EL+".astype";

  private static final String TAG_AT               = "xsdgen:attribute";
  private static final String TAG_AT_NAME          = TAG_AT+".name";

  // If true, the 'bind' methods will always try to return something,
  // even if severe errors were encountered.  Turn this on only for
  // debugging.
  private static final boolean IGNORE_SEVERE_ERRORS = false;

  // =========================================================================
  // Variables

  private BindingFile mBindingFile;
  private BindingLoader mLoader;
  private SchemaDocument mSchemaDocument;
  private SchemaDocument.Schema mSchema;
  private JavaSourceSet mInput;
  private BindingLogger mLogger = null;
  private boolean mAnySevereErrors = false;

  // =========================================================================
  // Constructors

  /**
   * Initializes a Java2Schema instance to perform binding on the given
   * inputs, but does not actually do any binding work.
   */
  public Java2Schema(JavaSourceSet jtsi, BindingLogger logger) {
    if (jtsi == null) throw new IllegalArgumentException("null jtsi");
    if (logger == null) throw new IllegalArgumentException("null logger");
    mInput = jtsi;
    mLogger = logger;
  }

  // =========================================================================
  // Public methods

  /**
   * Performs the binding and returns an exploded tylar in the specified
   * directory.  Returns null if any severe errors were encountered.
   */
  public ExplodedTylar bindAsExplodedTylar(File tylarDestDir)
  {
    Java2SchemaResult result = bind();
    if (result == null) return null;
    ExplodedTylarImpl tylar = null;
    try {
      tylar = ExplodedTylarImpl.create(tylarDestDir);
    } catch(IOException ioe) {
      logError(ioe);
      return null;
    }
    if (!tylarDestDir.exists()) {
      if (!tylarDestDir.mkdirs()) {
        logError("failed to create "+tylarDestDir);
        return null;
      }
    }
    buildTylar(result,(TylarWriter)tylar);
    return mAnySevereErrors && !IGNORE_SEVERE_ERRORS ? null : tylar;
  }

  /**
   * Performs the binding and returns a tylar in the specified jar file.
   * Returns null if any severe errors were encountered.
   */
  public Tylar bindAsJarredTylar(File tylarJar) {
    File tempDir = null;
    try {
      tempDir = createTempDir();
      tempDir.deleteOnExit();//REVIEW maybe we should delete it ourselves?
      ExplodedTylar et = bindAsExplodedTylar(tempDir);
      return et.toJar(tylarJar);
    } catch(IOException ioe) {
      logError(ioe);
      return null;
    }
  }

  /**
   * Does the binding work on the inputs passed to the constructor and returns
   * the raw result object.  Returns null if any severe errors were
   * encountered.
   *
   * Although this method is public, it should only be used if you know what
   * you're doing - you're probably better off using one of the other 'bind'
   * methods which return tylars.
   */
  public Java2SchemaResult bind() {
    JClass[] classes = mInput.getJClasses();
    mBindingFile = new BindingFile();
    mLoader = PathBindingLoader.forPath
            (new BindingLoader[] {mBindingFile,
                                  BuiltinBindingLoader.getInstance()});
    mSchemaDocument = SchemaDocument.Factory.newInstance();
    mSchema = mSchemaDocument.addNewSchema();
    if (classes.length > 0) {
      //FIXME how should we determine the targetnamespace for the schema?
      //here we just derive it from the first class in the list
      mSchema.setTargetNamespace(getTargetNamespace(classes[0]));
    }
    for(int i=0; i<classes.length; i++) getBindingTypeFor(classes[i]);
    if (mAnySevereErrors && !IGNORE_SEVERE_ERRORS) return null;
    //build the result object
    final BindingFile bf = mBindingFile;
    final SchemaDocument[] schemas = {mSchemaDocument};
    return new Java2SchemaResult() {
      public BindingFile getBindingFile() { return bf; }
      public SchemaDocument[] getSchemas() { return schemas; }
    };
  }


  // ========================================================================
  // Private methods

  private static File createTempDir() throws IOException
  {
    //FIXME this is not great
    String prefix = "java2schema-"+System.currentTimeMillis();
    File directory = null;
    File f = File.createTempFile(prefix, null);
    directory = f.getParentFile();
    f.delete();
    File out = new File(directory, prefix);
    if (!out.mkdirs()) throw new IOException("Uknown problem creating temp file");
    return out;
  }

  /**
   * Feeds a tylar builder with the given compilation results.
   */
  private void buildTylar(Java2SchemaResult result, TylarWriter builder) {
    try {
      builder.writeBindingFile(result.getBindingFile());
      SchemaDocument[] xsds = result.getSchemas();
      for(int i=0; i<xsds.length; i++) {
        builder.writeSchema(xsds[i],"schema-"+i+".xsd");//FIXME dumb naming
      }
    } catch(IOException ioe) {
      logError(ioe);
    }
  }

  /**
   * Returns a bts BindingType for the given JClass.  If such a type
   * has not yet been registered with the loader, it will be created.
   *
   * @param clazz Java type for which to return a binding.
   */
  private BindingType getBindingTypeFor(JClass clazz) {
    BindingType out =
            mLoader.getBindingType(mLoader.lookupTypeFor(getJavaName(clazz)));
    if (out == null) out = createBindingTypeFor(clazz);
    return out;
  }

  /**
   * Creates a bts BindingType for the given JClass and registers t with the
   * loader.  Note that this method assumes that a BindingType does not
   * already exist for the given JClass.
   *
   * @param clazz Java type for which to generate a binding.
   */
  private BindingType createBindingTypeFor(JClass clazz) {
    // create the schema type
    TopLevelComplexType xsType = mSchema.addNewComplexType();
    String tns = getTargetNamespace(clazz);
    String xsdName = getAnnotation(clazz,TAG_CT_TYPENAME,clazz.getSimpleName());
    QName qname = new QName(tns,xsdName);
    xsType.setName(xsdName);
    // deal with inheritance - see if it extends anything
    JClass superclass = clazz.getSuperclass();
    if (superclass != null && !superclass.isObject()) {
      // FIXME we're ignoring interfaces at the moment
      BindingType superBindingType = getBindingTypeFor(superclass);
      ComplexContentDocument.ComplexContent ccd = xsType.addNewComplexContent();
      ExtensionType et = ccd.addNewExtension();
      et.setBase(superBindingType.getName().getXmlName().getQName());
    }
    // create a binding type
    BindingTypeName btname = BindingTypeName.forPair(getJavaName(clazz),
                                                     XmlTypeName.forTypeNamed(qname));
    ByNameBean bindType = new ByNameBean(btname);
    mBindingFile.addBindingType(bindType,true,true);
    if (clazz.isPrimitive()) {
      // it's good to have registerd the dummy type, but don't go further
      logError(clazz,"Unexpected simple type");
      return bindType;
    }
    String rootName = getAnnotation(clazz,TAG_CT_ROOT,null);
    if (rootName != null) {
      QName rootQName = new QName(tns, rootName);
      BindingTypeName docBtName =
              BindingTypeName.forPair(getJavaName(clazz),
                                      XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, rootQName));
      SimpleDocumentBinding sdb = new SimpleDocumentBinding(docBtName);
      sdb.setTypeOfElement(btname.getXmlName());
      mBindingFile.addBindingType(sdb,true,true);
    }
    // run through the class' properties to populate the binding and xsdtypes
    SchemaPropertyFacade facade = new SchemaPropertyFacade(xsType,bindType,tns);
    bindProperties(clazz.getProperties(),facade);
    facade.finish();
    // check to see if they want to create a root elements from this type
    JAnnotation[] anns = clazz.getAnnotations(TAG_CT_ROOT);
    for(int i=0; i<anns.length; i++) {
      TopLevelElement root = mSchema.addNewElement();
      root.setName(anns[i].getStringValue());
      root.setType(qname);
      // FIXME still not entirely clear to me what we should do about
      // the binding file here
    }
    return bindType;
  }

  /**
   * Runs through a set of JProperties to creates schema and bts elements
   * to represent those properties.  Note that the details of manipulating the
   * schema and bts are encapsulated within the supplied SchemaPropertyFacade;
   * this method is only responsible for inspecting the properties and their
   * annotations and setting the correct attributes on the facade.
   *
   * @param props Array of JProperty objects to potentially be bound.
   * @param facade Allows us to create and manipulate properties,
   * hides the dirty work
   */
  private void bindProperties(JProperty[] props, SchemaPropertyFacade facade) {
    for(int i=0; i<props.length; i++) {
      if (getAnnotation(props[i],TAG_EL_EXCLUDE,false)) {
        logVerbose(props[i],"Marked excluded, skipping");
        continue;
      }
      if (props[i].getGetter() == null || props[i].getSetter() == null) {
        logVerbose(props[i],"Does not have both getter and setter, skipping");
        continue; // REVIEW this might have to change someday
      }
      { // determine the property name to use and set it
        String propName = getAnnotation(props[i],TAG_AT_NAME,null);
        if (propName != null) {
          facade.newAttributeProperty(props[i]);
          facade.setSchemaName(propName);
        } else {
          facade.newElementProperty(props[i]);
          facade.setSchemaName(getAnnotation
                         (props[i],TAG_EL_NAME,props[i].getSimpleName()));
        }
      }
      { // set the getters and setters
        facade.setGetter(props[i].getGetter().getSimpleName());
        facade.setSetter(props[i].getSetter().getSimpleName());
      }
      { // determine the property type to use and set it
        String annotatedType = getAnnotation(props[i],TAG_EL_ASTYPE,null);
        if (annotatedType == null) {
          facade.setType(props[i].getType());
        } else {
          JClass clazz = props[i].getType().forName(annotatedType);
          if (clazz.isUnresolved()) {
            logError(props[i],"Could not find class named '"+
                              clazz.getQualifiedName()+"'");
          } else {
            facade.setType(clazz);
          }
        }
      }
      { // determine if the property is nillable
        JAnnotation a = props[i].getAnnotation(TAG_EL_NILLABLE);
        if (a != null) {
          // if the tag is there but empty, set it to true.  is that weird?
          if (a.getStringValue().trim().length() == 0) {
            facade.setNillable(true);
          } else {
            facade.setNillable(a.getBooleanValue());
          }
        }
      }
    }
  }

  // ========================================================================
  // Private utility methods

  /**
   * Returns a JavaTypeName for the given JClass.  Might want to pool these.
   */
  private JavaTypeName getJavaName(JClass jc) {
    return JavaTypeName.forString(jc.getQualifiedName());
  }

  /**
   * Returns the string value of a named annotation, or the provided default
   * if the annotation is not present.
   * REVIEW seems like having this functionality in jam would be nice
   */
  private String getAnnotation(JElement elem, String annName, String dflt) {
    JAnnotation ann = elem.getAnnotation(annName);
    return (ann == null) ? dflt : ann.getStringValue();
  }

  /**
   * Returns the boolean value of a named annotation, or the provided default
   * if the annotation is not present.
   * REVIEW seems like having this functionality in jam would be nice
   */
  private boolean getAnnotation(JElement elem, String annName, boolean dflt) {
    JAnnotation ann = elem.getAnnotation(annName);
    return (ann == null) ? dflt : ann.getBooleanValue();
  }

  /**
   * Returns a QName for the type bound to the given JClass.
   */
  private QName getQnameFor(JClass clazz) {
    getBindingTypeFor(clazz);  //ensure that we've bound it
    JavaTypeName jtn = JavaTypeName.forString(clazz.getQualifiedName());
    BindingTypeName btn = mLoader.lookupTypeFor(jtn);
    logVerbose(clazz,"BindingTypeName is "+btn);
    BindingType bt = mLoader.getBindingType(btn);
    if (bt != null) return bt.getName().getXmlName().getQName();
    logError(clazz,"could not get qname");
    return new QName("ERROR",clazz.getQualifiedName());
  }

  /**
   * Returns a target namespace that should be used for the given class.
   * This takes annotations into consideration.
   */
  private String getTargetNamespace(JClass clazz) {
    JAnnotation ann = clazz.getAnnotation(TAG_CT_TARGETNS);
    if (ann != null) return ann.getStringValue();
    // Ok, they didn't specify it in the markup, so we have to
    // synthesize it from the classname.
    String pkg_name;
    if (clazz.isPrimitive()) {
      pkg_name = JAVA_NAMESPACE_URI;
    } else {
      JPackage pkg = clazz.getContainingPackage();
      pkg_name = (pkg == null) ? "" : pkg.getQualifiedName();
      if (pkg_name.startsWith(JAVA_PACKAGE_PREFIX)) {
        pkg_name = JAVA_NAMESPACE_URI+"."+
                pkg_name.substring(JAVA_PACKAGE_PREFIX.length());
      }
    }
    return JAVA_URI_SCHEME + pkg_name;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.  The binding process should attempt
   * to continue even after such errors are encountered so as to identify
   * as many errors as possible in a single pass.
   */
  private void logError(JElement context, Throwable error) {
    mAnySevereErrors = true;
    mLogger.log(Level.SEVERE,null,error,context);
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.  The binding process should attempt
   * to continue even after such errors are encountered so as to identify
   * as many errors as possible in a single pass.
   */
  private void logError(JElement context, String msg) {
    mAnySevereErrors = true;
    mLogger.log(Level.SEVERE,msg,null,context);
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.  The binding process should attempt
   * to continue even after such errors are encountered so as to identify
   * as many errors as possible in a single pass.
   */
  private void logError(String msg) {
    mAnySevereErrors = true;
    mLogger.log(Level.SEVERE,msg,null);
  }

  /**
   * Logs a message that fatal error that occurred.
   */
  private void logError(Throwable t) {
    mAnySevereErrors = true;
    mLogger.log(Level.SEVERE,null,t);
  }

  /**
   * Logs an informative message that should be printed only in 'verbose'
   * mode.
   */
  private void logVerbose(JElement context, String msg) {
    mLogger.log(Level.FINEST,msg,null,context);
  }

  /*

  private static boolean isXmlObj(JClass clazz) {
    try {
      JClass xmlObj = clazz.forName("org.apache.xmlbeans.XmlObject");
      return xmlObj.isAssignableFrom(clazz);
    } catch(Exception e) {
      e.printStackTrace(); //FIXME
      return false;
    }
  }
  */



  /**
   * Inner class which encapsulates the creation of schema properties and
   * property bindings and presents them as a unified interface, a kind of
   * 'virtual property.'  This is used by the bindProperties() method, and
   * allows that function to concentrate on inspecting the java types and
   * annotations. This class hides all of the dirty work associated with
   * constructing and initializing a BTS property and either an XSD element
   * or attribute.
   *
   * Note that in some sense, this class behaves as both a factory and a kind
   * of cursor.  It is capable of creating a new virtual property
   * on a given BTS/XSD type pair, and any operations on the facade will
   * apply to that property until the next property is created
   * (via newAttributeProperty or newElementProperty).
   */
  class SchemaPropertyFacade {

    // =======================================================================
    // Variables

    private TopLevelComplexType mXsType;
    private String mXsTargetNamespace;
    private LocalElement mXsElement = null; // exactly one of these two is
    private Attribute mXsAttribute = null;  // remains null
    private Group mXsSequence = null;
    private List mXsAttributeList = null;
    private ByNameBean mBtsType;
    private QNameProperty mBtsProp = null;
    private JElement mSrcContext = null;

    // =======================================================================
    // Constructors

    public SchemaPropertyFacade(TopLevelComplexType xsType,
                                ByNameBean bt,
                                String tns) {
      if (xsType == null) throw new IllegalArgumentException("null xsType");
      if (bt == null) throw new IllegalArgumentException("null bt");
      if (tns == null) throw new IllegalArgumentException("null tns");
      mXsType = xsType;
      mBtsType = bt;
      mXsTargetNamespace = tns;
    }

    // =======================================================================
    // Public methods

    /**
     * Creates a new element property and sets this facade represent it.
     * Note that either this method or newAttributeProperty must be called prior
     * to doing any work with the facade.  Also note that you need to
     * completely finish working with each property before moving onto
     * the next via newElementProperty or newAttributeProperty.
     * *
     * @param srcContext A JAM element that represents the java source
     * artifact that is being bound to the property.  This is used
     * only for error reporting purposes.
     */
    public void newElementProperty(JElement srcContext) {
      newBtsProperty();
      mSrcContext = srcContext;
      if (mXsSequence == null) mXsSequence = mXsType.addNewSequence();
      mXsElement = mXsSequence.addNewElement();
      mXsAttribute = null;
    }

    /**
     * Creates a new attribute property and sets this facade represent it.
     * Note that either this method or newElementProperty must be called prior
     * to doing any work with the facade.  Also note that you need to
     * completely finish working with each property before moving onto
     * the next via newElementProperty or newAttributeProperty.
     *
     * @param srcContext A JAM element that represents the java source
     * artifact that is being bound to the property.  This is used
     * only for error reporting purposes.
     */
    public void newAttributeProperty(JElement srcContext) {
      newBtsProperty();
      mBtsProp.setAttribute(true);
      mSrcContext = srcContext;
      mXsElement = null;
      if (mXsAttributeList == null) mXsAttributeList = new ArrayList();
      mXsAttributeList.add(mXsAttribute = Attribute.Factory.newInstance());
    }

    /**
     * Sets the name of this property (element or attribute) in the
     * generated schema.
     */
    public void setSchemaName(String name) {
      if (mXsElement != null) {
        mXsElement.setName(name);
      } else if (mXsAttribute != null) {
        mXsAttribute.setName(name);
      } else {
        throw new IllegalStateException();
      }
      mBtsProp.setQName(new QName(mXsTargetNamespace,name));
    }

    /**
     * Sets the name of the java getter for this property.
     */
    public void setGetter(String g) { mBtsProp.setGetterName(g); }

    /**
     * Sets the name of the java setter for this property.
     */
    public void setSetter(String s) { mBtsProp.setSetterName(s); }

    /**
     * Sets the type of the property.  Currently handles arrays
     * correctly but not collections.
     */
    public void setType(JClass propType) {
      if (mXsElement != null) {
        if (propType.isArray()) {
          if (propType.getArrayDimensions() != 1) {
            logError(mSrcContext,"Multidimensional arrays NYI"); //FIXME
          }
          JClass componentType = propType.getArrayComponentType();
          mXsElement.setMaxOccurs("unbounded");
          mXsElement.setType(getQnameFor(componentType));
          mBtsProp.setMultiple(true);
          mBtsProp.setCollectionClass //FIXME
                  (JavaTypeName.forString(componentType.getQualifiedName()+"[]"));
          mBtsProp.setBindingType(getBindingTypeFor(componentType));
        } else {
          mXsElement.setType(getQnameFor(propType));
          mBtsProp.setBindingType(getBindingTypeFor(propType));
        }
      } else if (mXsAttribute != null) {
        if (propType.isArray()) {
          logError(mSrcContext,
                   "Array properties cannot be mapped to xml attributes");
        } else {
          mXsAttribute.setType(getQnameFor(propType));
          mBtsProp.setBindingType(getBindingTypeFor(propType));
        }
      } else {
        throw new IllegalStateException();
      }
    }

    /**
     * Sets whether the property should be bound as nillable.
     */
    public void setNillable(boolean b) {
      if (mXsElement != null) {
        mXsElement.setNillable(b);
        mBtsProp.setNillable(b);
      } else if (mXsAttribute != null) {
        logError(mSrcContext,"Attributes cannot be nillable:");
      } else {
        throw new IllegalStateException();
      }
    }

    /**
     * This method should always be called when finished building up
     * a type.  It is a hack around an xbeans bug in which the sequences and
     * attributes are output in the order in which they were added (the
     * schema for schemas says the attributes always have to go last).
     */
    public void finish() {
      addBtsProperty();
      if (mXsAttributeList != null) {
        Attribute[] array = new Attribute[mXsAttributeList.size()];
        mXsAttributeList.toArray(array);
        mXsType.setAttributeArray(array);
      }
    }

    // =======================================================================
    // Private methods

    /**
     * Adds the current bts property to the bts type.  This has to be called
     * for every property.  We do this last because ByNameBean won't
     * let us add more than one prop for same name (name is always blank
     * initially).
     */
    private void addBtsProperty() {
      if (mBtsProp != null) mBtsType.addProperty(mBtsProp);
    }

    /**
     * Initialize a new QName property in the bts type
     */
    private void newBtsProperty() {
      if (mBtsProp != null) addBtsProperty(); //if not 1st one, add old one
      mBtsProp = new QNameProperty();
    }
  }

}