/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.binding.bts.*;
import org.apache.xmlbeans.impl.binding.tylar.TylarWriter;
import org.apache.xmlbeans.impl.binding.tylar.TylarConstants;
import org.apache.xmlbeans.impl.binding.tylar.ExplodedTylarImpl;
import org.apache.xmlbeans.impl.binding.compile.internal.Java2SchemaAnnotationHelper;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.w3.x2001.xmlSchema.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Arrays;
import java.io.IOException;
import java.io.StringWriter;


/**
 * Takes a set of Java source inputs and generates a set of XML schemas to
 * which those input should be bound, as well as a binding configuration file
 * which describes to the runtime subsystem how the un/marshalling should
 * be performed.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class Java2Schema extends BindingCompiler {

  // =========================================================================
  // Constants

  //FIXME these should be hidden away down in Java2SchemaAnnotationHelper
  public static final String TAG_CT               = "xsdgen:complexType";
  public static final String TAG_CT_EXCLUDE       = TAG_CT+".exclude";
  public static final String TAG_CT_TYPENAME      = TAG_CT+".typeName";
  public static final String TAG_CT_TARGETNS      = TAG_CT+".targetNamespace";
  public static final String TAG_CT_ROOT          = TAG_CT+".rootElement";
  public static final String TAG_CT_IGNORESUPER   = TAG_CT+".ignoreSuper";

  private static final String TAG_EL               = "xsdgen:element";

  public static final String TAG_EL_NAME          = TAG_EL+".name";
  public static final String TAG_EL_NILLABLE      = TAG_EL+".nillable";
  public static final String TAG_EL_EXCLUDE       = TAG_EL+".exclude";
  public static final String TAG_EL_ASTYPE        = TAG_EL+".astype";

  public static final String TAG_AT               = "xsdgen:attribute";
  public static final String TAG_AT_NAME          = TAG_AT+".name";

  public static final String TAG_ISSETTER         = "xsdgen:isSetMethodFor";

  // this is the character that replaces invalid characters when creating new
  // xml names.
  private static final char SAFE_CHAR = '_';

  // =========================================================================
  // Variables

  // the bindings that we're building up
  private BindingFile mBindingFile;

  // the full loader: bindingFile + baseLoader
  private BindingLoader mLoader;

  // maps a targetnamespace (string) to a schemadocument that we are creating
  private Map mTns2Schemadoc = new HashMap();

  // the input classes
  private JClass[] mClasses;

  private Java2SchemaAnnotationHelper mAnnHelper =
    Java2SchemaAnnotationHelper.getInstance();

  // Set of JClasses for which element annotations have already been processed.
  private Set mCheckForElements = new HashSet();

  private boolean mOrderPropertiesBySource = false;

  // =========================================================================
  // Constructors

  public Java2Schema(JClass[] classesToBind) {
    if (classesToBind == null) {
      throw new IllegalArgumentException("null classes");
    }
    mClasses = classesToBind;
  }

  // ========================================================================
  // Public methods

  /**
   * <p>Sets whether elements within generated schema types should be ordered
   * according to the source order of their corresponding java properties.
   * The default is false, meaning that they will instead be ordered
   * alphabetically.</p>
   *
   * @param b
   */
  public void setOrderPropertiesBySource(boolean b) {
    mOrderPropertiesBySource = b;
  }

  // ========================================================================
  // BindingCompiler implementation

  /**
   * Does the binding work on the inputs passed to the constructor and writes
   * out the tylar.
   */
  protected void internalBind(TylarWriter writer) {
    mBindingFile = new BindingFile();
    mLoader = CompositeBindingLoader.forPath
            (new BindingLoader[] {mBindingFile, super.getBaseBindingLoader()});
    { // generate/bind types
      for(int i=0; i<mClasses.length; i++) {
        if (!mAnnHelper.isExclude(mClasses[i])) {
          getBindingTypeFor(mClasses[i]);
        }
      }
    }
    { // generate/bind elements
      for(Iterator i = mCheckForElements.iterator(); i.hasNext(); ) {
        ensureElementsExistFor((JClass)i.next());
      }
    }
    //
    SchemaDocument[] xsds = null;
    try {
      writer.writeBindingFile(mBindingFile);
      //REVIEW maybe we should just include the schema and let super class write it out?
      xsds = getGeneratedSchemaDocuments();
      for(int i=0; i<xsds.length; i++) {
        writer.writeSchema(xsds[i],"schema-"+i+".xsd");
      }
    } catch(IOException ioe) {
      logError(ioe);
    }
    SchemaTypeSystem sts = null;
    {
      try {
        sts = Schema2JavaTask.createSchemaTypeSystem(xsds);
        if (sts == null) {
          throw new IllegalStateException("createSchemaTypeSystem returned null");
        }
      } catch(XmlException xe) {
        ExplodedTylarImpl.showXsbError(xe,null,"write",TylarConstants.SHOW_XSB_ERRORS);
      }
      if (sts != null) {
        try {
          writer.writeSchemaTypeSystem(sts);
        } catch(IOException ioe) {
          ExplodedTylarImpl.showXsbError(ioe,null,"compile",TylarConstants.SHOW_XSB_ERRORS);
        }
      }
    }
  }

  // ========================================================================
  // Private methods

  private SchemaDocument.Schema findOrCreateSchema(String tns) {
    if (tns == null) throw new IllegalArgumentException();
    tns = tns.trim();
    SchemaDocument doc = (SchemaDocument)mTns2Schemadoc.get(tns);
    if (doc == null) {
      doc = SchemaDocument.Factory.newInstance();
      SchemaDocument.Schema xsd = doc.addNewSchema();
      xsd.setTargetNamespace(tns);
      xsd.setElementFormDefault(FormChoice.QUALIFIED);
      mTns2Schemadoc.put(tns,doc);
    }
    return doc.getSchema();
  }

  private SchemaDocument[] getGeneratedSchemaDocuments() {
    Collection list = mTns2Schemadoc.values();
    SchemaDocument[] out = new SchemaDocument[list.size()];
    list.toArray(out);
    return out;
  }

  /**
   * Returns a bts BindingType for the given JClass.  If such a type
   * has not yet been registered with the loader, it will be created.
   *
   * @param clazz Java type for which to return a binding.
   */
  private BindingType getBindingTypeFor(JClass clazz) {
    if (clazz == null) throw new IllegalArgumentException("null clazz");
    mCheckForElements.add(clazz);
    BindingTypeName btn = mLoader.lookupTypeFor(getJavaName(clazz));
    if (btn != null) {
      BindingType out = mLoader.getBindingType(btn);
      if (out != null) return out;
    }
    return createBindingTypeFor(clazz);
  }

  private void ensureElementsExistFor(JClass clazz) {
    if (clazz == null) throw new IllegalArgumentException("null clazz");
    BindingTypeName btn = mLoader.lookupTypeFor(JavaTypeName.forJClass(clazz));
    if (btn == null) {
      logWarning("No bindings produced for "+clazz.getQualifiedName()+", " +
                 "skipping element generation");
      return;
    }
    QName typeQName = btn.getXmlName().getQName();
    QName[] tes = mAnnHelper.getTargetElements(clazz);
    if (tes != null) {
      for(int i=0; i<tes.length; i++ ) {
        logVerbose("creating target element "+tes[i]);
        // create an appropriate schema element, if appropriate
        SchemaDocument.Schema destSchema =
          findOrCreateSchema(tes[i].getNamespaceURI());
        if (!containsElementNamed(destSchema, tes[i].getLocalPart())) {
          TopLevelElement elem = destSchema.addNewElement();
          elem.setName(tes[i].getLocalPart());
          elem.setType(typeQName);
        }
        // create a binding entry
        BindingTypeName docBtName = BindingTypeName.forPair
          (getJavaName(clazz),
           XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, tes[i]));
        SimpleDocumentBinding sdb = new SimpleDocumentBinding(docBtName);
        sdb.setTypeOfElement(XmlTypeName.forTypeNamed(typeQName));
        mBindingFile.addBindingType(sdb,true,true);
      }
    }
  }

  private boolean containsElementNamed(SchemaDocument.Schema xsd, String elementName) {
    // this is fairly gross, ma
    TopLevelElement[] es = xsd.getElementArray();
    for(int i=0; i<es.length; i++) {
      if (elementName.equals(es[i].getName())) return true;
    }
    return false;
  }

  /**
   * Creates a bts BindingType for the given JClass and registers t with the
   * loader.  Note that this method assumes that a BindingType does not
   * already exist for the given JClass.
   *
   * @param clazz Java type for which to generate a binding.
   */
  private BindingType createBindingTypeFor(JClass clazz) {
    logVerbose("** creating binding type for "+clazz.getQualifiedName()+" "+clazz.isPrimitiveType());
    // create the schema type
    QName targetQname = mAnnHelper.getTargetTypeName(clazz);
    SchemaDocument.Schema schema = findOrCreateSchema(targetQname.getNamespaceURI());
    TopLevelComplexType xsType = schema.addNewComplexType();
    xsType.setName(targetQname.getLocalPart());
    // deal with inheritance - see if it extends anything
    JClass superclass = clazz.getSuperclass();
    // we have to remember whether we created an ExtensionType because that
    // is where the sequence of properties have to go - note that this
    // gets passed into the SchemaPropertyFacade created below.  It's
    // unfortunate that the SchemaDocument model does not allow us to deal
    // with this kind of thing in a more elegant and polymorphic way.
    ExtensionType extType = null;
    BindingType superBindingType = null;
    if (superclass != null && !superclass.isObjectType() &&
      !mAnnHelper.isIgnoreSuper(clazz)) {
      // FIXME we're ignoring interfaces at the moment
      superBindingType = getBindingTypeFor(superclass);
      ComplexContentDocument.ComplexContent ccd = xsType.addNewComplexContent();
      extType = ccd.addNewExtension();
      extType.setBase(superBindingType.getName().getXmlName().getQName());
    }
    // create a binding type
    BindingTypeName btname = BindingTypeName.forPair(getJavaName(clazz),
                                                     XmlTypeName.forTypeNamed(targetQname));
    ByNameBean bindType = new ByNameBean(btname);
    mBindingFile.addBindingType(bindType,true,true);
    if (clazz.isPrimitiveType()) {
      // it's good to have registerd the dummy type, but don't go further
      logError("Unexpected simple type",clazz);
      return bindType;
    }
    //add super's props first
    if (superBindingType != null) {
      //REVIEW: will it ever be possible to have another type as the super type?
      //REVIEW: is copy by ref safe here?
      //TODO: deal with java->schema name collisions across inherited types
      ByNameBean super_type = (ByNameBean)superBindingType;
      for(Iterator itr = super_type.getProperties().iterator() ; itr.hasNext() ; ) {
        final QNameProperty prop = (QNameProperty)itr.next();
        bindType.addProperty(prop);
      }
      bindType.setAnyAttributeProperty(super_type.getAnyAttributeProperty());
      bindType.setAnyElementProperty(super_type.getAnyElementProperty());
    }
    {
      // run through the class' properties to populate the binding and xsdtypes
      SchemaPropertyFacade facade = new SchemaPropertyFacade
        (xsType,extType,bindType,targetQname.getNamespaceURI());
      Map props2issetters = new HashMap();
      getIsSetters(clazz,props2issetters);
      bindProperties(clazz.getDeclaredProperties(),props2issetters,facade);
      facade.finish();
    }
    return bindType;
  }

  private void getIsSetters(JClass clazz, Map outPropname2jmethod) {
    JMethod[] methods = clazz.getDeclaredMethods();
    for(int i=0; i<methods.length; i++) {
      JAnnotation ann = methods[i].getAnnotation(TAG_ISSETTER);
      if (ann != null) {
        if (!methods[i].getReturnType().getQualifiedName().equals("boolean")) {
          logWarning("Method "+methods[i].getQualifiedName()+" is marked "+
                     TAG_ISSETTER+"\nbut it does not return boolean."+
                     "Ignoring.");
          continue;
        }
        if (methods[i].getParameters().length > 0) {
          logWarning("Method "+methods[i].getQualifiedName()+" is marked "+
                     TAG_ISSETTER+"\nbut takes arguments.  Ignoring.");
          continue;
        }
        JAnnotationValue propNameVal = ann.getValue(JAnnotation.SINGLE_VALUE_NAME);
        if (propNameVal == null) {
          logWarning("Method "+methods[i].getQualifiedName()+" is marked "+
                     TAG_ISSETTER+"\nbut but no property name is given.  Ignoring");
          continue;
        }
        outPropname2jmethod.put(propNameVal.asString(),methods[i]);
      }
    }
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
  private void bindProperties(JProperty[] props,
                              Map props2issetters,
                              SchemaPropertyFacade facade) {
    if (mOrderPropertiesBySource) placeInSourceOrder(props);
    for(int i=0; i<props.length; i++) {
      if (mAnnHelper.getAnnotation(props[i],TAG_EL_EXCLUDE,false)) {
        logVerbose("Marked excluded, skipping",props[i]);
        continue;
      }
      if (props[i].getGetter() == null || props[i].getSetter() == null) {
        logVerbose("Does not have both getter and setter, skipping",props[i]);
        continue; // REVIEW this might have to change someday
      }
      String propName;
      { // determine the property name to use and set it
        propName = mAnnHelper.getAnnotation(props[i], TAG_AT_NAME, null);
        if (propName != null) {
          facade.newAttributeProperty(props[i]);
        } else {
          facade.newElementProperty(props[i]);
          propName = mAnnHelper.getAnnotation(props[i], TAG_EL_NAME,
                                              props[i].getSimpleName());
        }
        facade.setSchemaName(propName);
      }
      { // determine the property type to use and set it
        JClass propType = null;
        String annotatedType = mAnnHelper.getAnnotation(props[i],TAG_EL_ASTYPE,null);
        if (annotatedType == null) {
          facade.setType(propType = props[i].getType());
        } else {
          if (props[i].getType().isArrayType()) {
            //THIS IS A QUICK GROSS HACK THAT SHOULD BE REMOVED.
            //IF SOMEONE WANTS TO AS TYPE AN ARRAY PROPERTY, THEY NEED
            //TO ASTYPE IT TO THE ARRAY TYPE THEMSELVES
            annotatedType = "[L"+annotatedType+";";
          }
          propType = props[i].getType().forName(annotatedType);
          if (propType.isUnresolvedType()) {
            logError("Could not find class named '"+
                    propType.getQualifiedName()+"'",props[i]);
          } else {
            facade.setType(propType);
          }
        }
      }
      { // set the getters and setters
        facade.setGetter(props[i].getGetter());
        facade.setSetter(props[i].getSetter());
      }
      {
        JMethod issetter = (JMethod)props2issetters.get(propName);
        if (issetter != null) facade.setIssetter(issetter);
      }
      { // determine if the property is nillable
        JAnnotation a = props[i].getAnnotation(TAG_EL_NILLABLE);
        if (a != null) {
          // if the tag is there but empty, set it to true.  is that weird?
          JAnnotationValue val = a.getValue(JAnnotation.SINGLE_VALUE_NAME);
          if (val == null || val.asString().trim().length() == 0) {
            facade.setNillable(true);
          } else {
            facade.setNillable(val.asBoolean());
          }
        }
      }
    }
  }


  /**
   * Returns a JavaTypeName for the given JClass.  Might want to pool these.
   */
  private JavaTypeName getJavaName(JClass jc) {
    return JavaTypeName.forString(jc.getQualifiedName());
  }

  /**
   * Returns a QName for the type bound to the given JClass.
   */
  private QName getQnameFor(JClass clazz) {
    getBindingTypeFor(clazz);  //ensure that we've bound it
    JavaTypeName jtn = JavaTypeName.forString(clazz.getQualifiedName());
    BindingTypeName btn = mLoader.lookupTypeFor(jtn);
    logVerbose("BindingTypeName is "+btn,clazz);
    BindingType bt = mLoader.getBindingType(btn);
    if (bt != null) return bt.getName().getXmlName().getQName();
    logError("could not get qname",clazz);
    return new QName("ERROR",clazz.getQualifiedName());
  }



  /**
   * Checks the given XML name to ensure that it is a valid XMLName according
   * to the XML 1.0 Recommendation.  If it is not, the name is mangled so
   * as to make it a valid name.  This should be called before setting the
   * name on every schema fragment we create.
   */
  private static String makeNcNameSafe(String name) {
    // it's probably pretty rare that a name isn't valid, so let's just do
    // an optimistic check first without writing out a new string.
    if (name == null || XMLChar.isValidNCName(name) || name.length() == 0) {
      return name;
    }
    // ok, we have to mangle it
    StringWriter out = new StringWriter();
    char ch = name.charAt(0);
    if(!XMLChar.isNCNameStart(ch)) {
      out.write(SAFE_CHAR);
    } else {
      out.write(ch);
    }
    for (int i=1; i < name.length(); i++ ) {
      ch = name.charAt(i);
       if (!XMLChar.isNCName(ch)) {
         out.write(ch);
       }
    }
    return out.toString();
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
   *
   * This class really wouldn't be necessary if the SchemaDocument model
   * were a bit more user-friendly.
   */
  class SchemaPropertyFacade {

    // =======================================================================
    // Variables

    private TopLevelComplexType mXsType;
    private ExtensionType mExtensionType = null;
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
                                ExtensionType extType, //may be null
                                ByNameBean bt,
                                String tns) {
      if (xsType == null) throw new IllegalArgumentException("null xsType");
      if (bt == null) throw new IllegalArgumentException("null bt");
      if (tns == null) throw new IllegalArgumentException("null tns");
      mXsType = xsType;
      mExtensionType = extType;
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
      if (mXsSequence == null) {
        // nest it inside the extension element if they specified one,
        // otherwise just do it in the complexType
        mXsSequence = (mExtensionType != null) ?
          mExtensionType.addNewSequence() : mXsType.addNewSequence();
      }
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
      if (name == null) throw new IllegalArgumentException("null name");
      name = makeNcNameSafe(name);
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
    public void setGetter(JMethod g) {
      mBtsProp.setGetterName(MethodName.create(g));
    }

    /**
     * Sets the name of the java setter for this property.
     */
    public void setSetter(JMethod s) {
      mBtsProp.setSetterName(MethodName.create(s));
    }

    /**
     * Sets the name of the java setter for this property.
     */
    public void setIssetter(JMethod s) {
      mBtsProp.setIssetterName(MethodName.create(s));
    }

    /**
     * Sets the type of the property.  Currently handles arrays
     * correctly but not collections.
     */
    public void setType(JClass propType) {
      if (mXsElement != null) {
        if (propType.isArrayType()) {
          if (propType.getArrayDimensions() != 1) {
            logError("Multidimensional arrays NYI",mSrcContext); //FIXME
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
        if (propType.isArrayType()) {
          logError("Array properties cannot be mapped to xml attributes",
                  mSrcContext);
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
        logError("Attributes cannot be nillable:",mSrcContext);
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
      if (mBtsProp != null && !mBtsType.hasProperty(mBtsProp))
          mBtsType.addProperty(mBtsProp);
    }

    /**
     * Initialize a new QName property in the bts type
     */
    private void newBtsProperty() {
      if (mBtsProp != null) addBtsProperty(); //if not 1st one, add old one
      mBtsProp = new QNameProperty();
    }
  }

  private static Comparator SOURCE_POSITION_COMPARATOR = new Comparator() {

    public int compare(Object o, Object o1) {
      JSourcePosition p1 = ((JElement)o).getSourcePosition();
      JSourcePosition p2 = ((JElement)o).getSourcePosition();
      if (p1 == null) return (p2 == null) ? 0 : -1;
      if (p2 == null) return 1;
      return (p1.getLine() < p2.getLine()) ? -1 :
        (p1.getLine() > p2.getLine()) ? 1 : 0;
    }
  };

  /**
   * Sorts the given array in place so that the elements are ordered by
   * their sourcePosition line numbers.
   */
  private static void placeInSourceOrder(JElement[] elements) {
    Arrays.sort(elements,SOURCE_POSITION_COMPARATOR);
  }
}