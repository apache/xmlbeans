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

import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.binding.bts.*;
import org.apache.xmlbeans.impl.binding.tylar.TylarWriter;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JProperty;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class Both2Bind extends BindingCompiler /*implements BindingFileResult*/ {

  // ========================================================================
  // Variables

  private TypeMatcher mMatcher;
  private Map scratchFromXmlName = new LinkedHashMap();
  private Map scratchFromJavaName = new LinkedHashMap();
  private Map scratchFromBindingName = new LinkedHashMap();
  private BindingFile bindingFile = new BindingFile();
  private LinkedList resolveQueue = new LinkedList();
  private JClass[] mJavaTypes = null;
  private SchemaTypeSystem mSchemaTypes = null;

  // ========================================================================
  // Constructor

  public Both2Bind() {}

  // ========================================================================
  // Compilation attributes

  /**
   * Sets the java types to be compiled.  This is required.
   */
  public void setJavaTypesToMatch(JClass[] jclasses) {
    if (jclasses == null) throw new IllegalArgumentException("null jclasses");
    mJavaTypes = jclasses;
  }

  /**
   * Sets the schema types to be compiled.  This is required.
   */
  public void setSchemaTypesToMatch(SchemaTypeSystem sts) {
    if (sts == null) throw new IllegalArgumentException("null sts");
    mSchemaTypes = sts;
  }

  /**
   * Sets a custom TypeMatcher to use for lining up the java and schema types.
   * This is optional; if omitted, a default TypeMatcher will be used.
   * @param matcher
   */
  public void setTypeMatcher(TypeMatcher matcher) {
    mMatcher = matcher;
  }

  // ========================================================================
  // BindingCompiler implementation

  protected void internalBind(TylarWriter tw) {
    bind();
    try {
      tw.writeBindingFile(bindingFile);
    } catch (IOException ioe) {
      logError(ioe);
    }
  }

  // ========================================================================
  // Deprecated methods

  /**
   * @deprecated BindingFile should not be used directly any more - you
   * need to use tylars.  The entry point you should probably use now is
   * BindingCompiler.bindAsJarredTylar().
   */
  public BindingFile getBindingFile() {
    return bindingFile;
  }

  // ========================================================================
  // Private methods

  private void bind() {
    if (mMatcher == null) mMatcher = new DefaultTypeMatcher();
    mMatcher.init(this);
    // Let the passed mMatcher propose any matches it wishes to

    resolveInitiallyMatchedTypes();

    // consider: when to generate warnings for missing matches?

    // Now we recurse through data structures and match up properties,
    // also adding new types to match based on position in props.
    while (moreToResolve()) {
      Scratch scratch = dequeueToResolve();
      resolveBinding(scratch);
    }
  }


  /**
   * Scratch area corresponding to a schema type, used for the binding
   * computation.
   */
  private static class Scratch {
    Scratch(JClass jClass, JavaTypeName javaName, SchemaType schemaType, XmlTypeName xmlName, int category) {
      this.jClass = jClass;
      this.javaName = javaName;
      this.schemaType = schemaType;
      this.xmlName = xmlName;
      this.category = category;
      this.bindingTypeName = BindingTypeName.forPair(javaName, xmlName);
    }

    private BindingType bindingType;
    private SchemaType schemaType; // may be null
    private JavaTypeName javaName;
    private XmlTypeName xmlName;
    private JClass jClass;
    private BindingTypeName bindingTypeName;
    private TypeMatcher.MatchedProperties onBehalfOf;

    private int category;

    // atomic types get a treatAs
    private XmlTypeName asIf;
    private boolean isStructureResolved;

    // categories of Scratch, established at ctor time
    public static final int ATOMIC_TYPE = 1;
    public static final int STRUCT_TYPE = 2;
    public static final int LITERALARRAY_TYPE = 3;
    public static final int SOAPARRAY_REF = 4;
    public static final int SOAPARRAY = 5;
    public static final int ELEMENT = 6;
    public static final int ATTRIBUTE = 7;

    public int getCategory() {
      return category;
    }

    public JClass getJClass() {
      return jClass;
    }

    public JavaTypeName getJavaName() {
      return javaName;
    }

    public BindingTypeName getBindingTypeName() {
      return bindingTypeName;
    }

    public BindingType getBindingType() {
      return bindingType;
    }

    public void setBindingType(BindingType bindingType) {
      this.bindingType = bindingType;
    }

    public SchemaType getSchemaType() {
      return schemaType;
    }

    public XmlTypeName getXmlName() {
      return xmlName;
    }

    public XmlTypeName getAsIf() {
      return asIf;
    }

    public void setAsIf(XmlTypeName xmlName) {
      this.asIf = xmlName;
    }

    public void addQNameProperty(QNameProperty prop) {
      if (!(bindingType instanceof ByNameBean))
        throw new IllegalStateException();
      ((ByNameBean) bindingType).addProperty(prop);
    }

    public Collection getQNameProperties() {
      if (!(bindingType instanceof ByNameBean))
        throw new IllegalStateException();
      return ((ByNameBean) bindingType).getProperties();
    }

    public boolean isStructureResolved() {
      return this.isStructureResolved;
    }

    public void setStructureResolved(boolean isStructureResolved) {
      this.isStructureResolved = isStructureResolved;
    }

    public void setOnBehalfOf(TypeMatcher.MatchedProperties onBehalfOf) {
      this.onBehalfOf = onBehalfOf;
    }

    public TypeMatcher.MatchedProperties getOnBehalfOf() {
      return onBehalfOf;
    }
  }



  private static XmlTypeName normalizedXmlTypeName(SchemaType sType) {
    if (sType.isDocumentType())
      return XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, sType.getDocumentElementName());
    if (sType.isAttributeType())
      return XmlTypeName.forGlobalName(XmlTypeName.ATTRIBUTE, sType.getDocumentElementName());
    return XmlTypeName.forSchemaType(sType);
  }

  /**
   * Returns a schema type which is the closest base type for the given schema
   * type which is builtin and compatible with the given Java class.
   *
   * Or returns null if no builtin base class is known to be compatible.
   */
  private static SchemaType computeCompatibleBuiltin(JavaTypeName javaName, SchemaType sType) {
    // only interesting builtins are simple
    if (!sType.isSimpleType() && sType.getContentType() != SchemaType.SIMPLE_CONTENT)
      return null;

    // See if the java class is actually a compatible primitive
    BindingLoader builtins = BuiltinBindingLoader.getBuiltinBindingLoader(false);         

    // find the closest simple base type
    while (!sType.isSimpleType())
      sType = sType.getBaseType();

    // look for a base type compatible with the given primitive
    while (sType != null) {
      if (null != builtins.getBindingType(BindingTypeName.forPair(javaName, XmlTypeName.forSchemaType(sType))))
        return sType;
      sType = sType.getBaseType();
    }

    return null;
  }

  /**
   * Arrays currently not automatically handled.
   */
  private static boolean isCompatibleArray(JClass jClass, SchemaType sType) {
    return false;
  }

  /**
   * This function goes through all relevant schema types, plus soap
   * array types, and creates a scratch area for each.  Each
   * scratch area is also marked at this time with an XmlTypeName,
   * a schema type, and a category.
   */
  private void resolveInitiallyMatchedTypes() {
    if (mJavaTypes == null) {
      throw new IllegalStateException("javaTypesToMatch was never set");
    }
    if (mSchemaTypes == null) {
      throw new IllegalStateException("schemaTypesToMatch was never set");
    }

    TypeMatcher.MatchedType[] matchedTypes =
            mMatcher.matchTypes(mJavaTypes, mSchemaTypes);
    for (int i = 0; i < matchedTypes.length; i++) {
      Scratch scratch = createScratch(matchedTypes[i].getJClass(), matchedTypes[i].getSType());
      scratchFromBindingName.put(scratch.getBindingTypeName(), scratch);
    }

    // Now run through and make sure we're unique in both S+J
    // and add the matches to the "unique" tables.
    for (Iterator i = scratchIterator(); i.hasNext();) {
      Scratch scratch = (Scratch) i.next();
      boolean skip = false;

      createBindingType(scratch, true);

      if (!scratchFromXmlName.containsKey(scratch.getXmlName()))
        scratchFromXmlName.put(scratch.getXmlName(), scratch);
      else {
        skip = true;
        logError("Both " + scratch.getJavaName() + " and " +
                 ((Scratch) scratchFromXmlName.get(scratch.getXmlName())).getJavaName() +
                 " match Schema " + scratch.getXmlName(),
                 scratch.getJClass(),scratch.getSchemaType());
      }
      // only non-document types are uniquified
      if (!scratch.getSchemaType().isDocumentType()) {
        if (!scratchFromJavaName.containsKey(scratch.getJavaName()))
          scratchFromJavaName.put(scratch.getJavaName(), scratch);
        else {
          skip = true;
          logError("Both " + scratch.getXmlName() + " and " +
                   ((Scratch) scratchFromJavaName.get(scratch.getJavaName())).getXmlName()+
                   " match Java " + scratch.getJavaName(),
                   scratch.getJClass(),
                   scratch.getSchemaType());
        }
      }
      if (!skip) queueToResolve(scratch);
    }
  }

  private static Scratch createScratch(JClass jClass, SchemaType sType) {
    XmlTypeName xmlName = normalizedXmlTypeName(sType);
    JavaTypeName javaName = JavaTypeName.forJClass(jClass);
    Scratch scratch;
    SchemaType simpleBuiltin = computeCompatibleBuiltin(javaName, sType);
    if (simpleBuiltin != null) {
      // simple types are atomic
      // todo: what about simple content, custom codecs, etc?
      scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.ATOMIC_TYPE);
      scratch.setAsIf(XmlTypeName.forSchemaType(simpleBuiltin));
    } else if (sType.isDocumentType()) {
      scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.ELEMENT);
      scratch.setAsIf(XmlTypeName.forSchemaType(sType.getProperties()[0].getType()));
    } else if (sType.isAttributeType()) {
      scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.ATTRIBUTE);
      scratch.setAsIf(XmlTypeName.forSchemaType(sType.getProperties()[0].getType()));
    } else if (isCompatibleArray(jClass, sType)) {
      scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.LITERALARRAY_TYPE);
    } else {
      scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.STRUCT_TYPE);
    }
    return scratch;
  }


  /**
   * Computes a BindingType for a scratch.
   */
  private void createBindingType(Scratch scratch, boolean shouldDefault) {
    if (scratch == null) throw new IllegalArgumentException("null scratch");
    if (scratch.getBindingType() != null) {
      throw new IllegalArgumentException("non-null scratch binding type");
    }

    BindingTypeName btName = BindingTypeName.forPair(scratch.getJavaName(), scratch.getXmlName());

    switch (scratch.getCategory()) {
      case Scratch.ATOMIC_TYPE:
      case Scratch.SOAPARRAY_REF:
      case Scratch.ATTRIBUTE:
        SimpleBindingType simpleResult = new SimpleBindingType(btName);
        simpleResult.setAsIfXmlType(scratch.getAsIf());
        scratch.setBindingType(simpleResult);
        bindingFile.addBindingType(simpleResult, shouldDefault, shouldDefault);
        break;

      case Scratch.ELEMENT:
        SimpleDocumentBinding docResult = new SimpleDocumentBinding(btName);
        docResult.setTypeOfElement(scratch.getAsIf());
        scratch.setBindingType(docResult);
        bindingFile.addBindingType(docResult, shouldDefault, shouldDefault);
        break;

      case Scratch.STRUCT_TYPE:
        ByNameBean byNameResult = new ByNameBean(btName);
        scratch.setBindingType(byNameResult);
        bindingFile.addBindingType(byNameResult, shouldDefault, shouldDefault);
        break;

      case Scratch.LITERALARRAY_TYPE:
        throw new UnsupportedOperationException();

      case Scratch.SOAPARRAY:
        throw new UnsupportedOperationException();

      default:
        throw new IllegalStateException("Unrecognized category");
    }
  }

  /**
   * Looks on both the path and in the current scratch area for
   * the binding type corresponding to the given pair.  Must
   * be called after all the binding types have been created.
   */
  private BindingType bindingTypeForMatchedTypes(JClass jClass, SchemaType sType, TypeMatcher.MatchedProperties onBehalfOf) {
    // note that jClass may differ from property type because of arrays
    BindingTypeName btName = BindingTypeName.forTypes(jClass, sType);

    // First look in locally compiled bindings
    Scratch scratch = (Scratch) scratchFromBindingName.get(btName);
    if (scratch != null)
      return scratch.getBindingType();

    // Then look on path
    BindingType result = getBaseBindingLoader().getBindingType(btName);
    if (result != null)
      return result;

    // Not found?  Then allocate and queue for processing
    scratch = createScratch(jClass, sType);
    scratch.setOnBehalfOf(onBehalfOf);
    createBindingType(scratch, false);
    queueToResolve(scratch);

    return scratch.getBindingType();
  }

  private void queueToResolve(Scratch scratch) {
    resolveQueue.add(scratch);
  }

  private boolean moreToResolve() {
    return !resolveQueue.isEmpty();
  }

  private Scratch dequeueToResolve() {
    return (Scratch) resolveQueue.removeFirst();
  }


  /**
   * Returns an iterator for all the Scratch's
   */
  private Iterator scratchIterator() {
    return scratchFromBindingName.values().iterator();
  }


  private void resolveBinding(Scratch scratch) {
    switch (scratch.getCategory()) {
      case Scratch.ATOMIC_TYPE:
        return; // nothing to do that's not already done

      case Scratch.ELEMENT:
        // must ensure that the element's type is bound to the underlying JClass
        bindingTypeForMatchedTypes(scratch.getJClass(), scratch.getSchemaType().getProperties()[0].getType(), null);
        return;

      case Scratch.STRUCT_TYPE:
        resolveStructure(scratch);
        return;

      case Scratch.LITERALARRAY_TYPE:
      default:
        return;
    }
  }

  private static class SchemaPropertyName {
    QName qName;
    boolean isAttribute;

    public static SchemaPropertyName forProperty(SchemaProperty sProp) {
      return new SchemaPropertyName(sProp.getName(), sProp.isAttribute());
    }

    private SchemaPropertyName(QName qName, boolean attribute) {
      this.qName = qName;
      isAttribute = attribute;
    }

    public QName getQName() {
      return qName;
    }

    public boolean isAttribute() {
      return isAttribute;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SchemaPropertyName)) return false;

      final SchemaPropertyName schemaPropertyName = (SchemaPropertyName) o;

      if (isAttribute != schemaPropertyName.isAttribute) return false;
      if (!qName.equals(schemaPropertyName.qName)) return false;

      return true;
    }

    public int hashCode() {
      int result;
      result = qName.hashCode();
      result = 29 * result + (isAttribute ? 1 : 0);
      return result;
    }
  }

  /**
   * Returns the set of elements and attributes which have a
   * minimum occurance greater than zero.  (In other words,
   * binding to them is not optional if you ever want to
   * serialize your XML out.)  The result Set contains
   * SchemaPropertyNames.
   */
  private static Set computeRequiredProperties(SchemaType sType) {
    Set result = new HashSet();

    SchemaProperty[] sProps = sType.getProperties();
    for (int i = 0; i < sProps.length; i++) {
      if (sProps[i].getMinOccurs().signum() > 0)
        result.add(SchemaPropertyName.forProperty(sProps[i]));
    }

    return result;
  }

  private void resolveStructure(Scratch scratch) {

    if (scratch.getSchemaType().isSimpleType() || scratch.getSchemaType() == XmlObject.type) {
      logError("Java class " + scratch.getJavaName() +
               " does not match Schema type " +
               scratch.getXmlName(),
               scratch.getJClass(),
               scratch.getSchemaType());
      return;
    }

    // todo: check inheritance validity (inheritance in java + xml should match up)

    // todo: when looking at java + schema properties, be aware of inheritance issues

    // now, match up the names
    TypeMatcher.MatchedProperties[] matchedProperties =
            mMatcher.matchProperties(scratch.getJClass(), scratch.getSchemaType());

    // The only requirements:
    // (1) every required schema attribute or element must be accounted for
    // (2) cardinality must match

    Set requiredProperties = computeRequiredProperties(scratch.getSchemaType());

    for (int i = 0; i < matchedProperties.length; i++) {
      SchemaProperty sProp = matchedProperties[i].getSProperty();
      JProperty jProp = matchedProperties[i].getJProperty();

      // first, remove a matched schema property name when seen
      requiredProperties.remove(SchemaPropertyName.forProperty(sProp));

      // Extract property types to recurse on
      JClass jPropType = jProp.getType();
      SchemaType sPropType = sProp.getType();

      // Check cardinality, skip into type
      boolean multiple = isMultiple(sProp);
      JavaTypeName collection = null;
      if (multiple) {
        if (!jPropType.isArrayType()) {
          logError("Property " + jProp + " in " + scratch.getJClass() +
                   " is an array, but " + sProp.getName() + " in " +
                   scratch.getSchemaType() + " is a singleton.",
                   jProp,sProp);
        } else {
          collection = JavaTypeName.forJClass(jPropType);
          jPropType = jPropType.getArrayComponentType();
        }
      }

      // A mMatcher can say that a declared type is "really" another type.
      // The normal mMatcher just returns the same thing back.
      jPropType = mMatcher.substituteClass(jPropType);

      // Queues the binding type for this property for processing if needed
      BindingType bType = bindingTypeForMatchedTypes(jPropType, sPropType, matchedProperties[i]);

      QNameProperty prop = new QNameProperty();
      prop.setQName(sProp.getName());
      prop.setAttribute(sProp.isAttribute());
      prop.setSetterName(MethodName.create(jProp.getSetter()));
      prop.setGetterName(MethodName.create(jProp.getGetter()));
      prop.setCollectionClass(collection);
      prop.setBindingType(bType);
      prop.setNillable(sProp.hasNillable() != SchemaProperty.NEVER);
      prop.setOptional(isOptional(sProp));
      prop.setMultiple(multiple);

      scratch.addQNameProperty(prop);
    }

    if (!requiredProperties.isEmpty()) {
      int missing = requiredProperties.size();
      String reason;
      if (missing > 1) {
        reason = "No match for "+missing+
                " schema element or attribute names.";
      } else {
        SchemaPropertyName spName =
                (SchemaPropertyName)requiredProperties.iterator().next();
        if (spName.isAttribute()) {
          reason = "No match for required attribute "+
                  spName.getQName().getLocalPart();
        } else {
          reason = "No match for required element "+
                  spName.getQName().getLocalPart();
        }
      }
      if (scratch.getOnBehalfOf() == null) {
        logError("Java class " + scratch.getJavaName() +
                 " does not match schema type " +
                 scratch.getXmlName() + " (" + reason + ")",
                 scratch.getJClass(),scratch.getSchemaType());
      } else {
        logError("Java class " + scratch.getJavaName() +
                 " does not match schema type " +
                 scratch.getXmlName() + " (" + reason + ")",
                 scratch.getOnBehalfOf().getJProperty(),
                 scratch.getOnBehalfOf().getSProperty());
      }
    }
  }

  private static boolean isMultiple(SchemaProperty sProp) {
    BigInteger max = sProp.getMaxOccurs();
    if (max == null) return true;
    return (max.compareTo(BigInteger.ONE) > 0);
  }

  private static boolean isOptional(SchemaProperty sProp) {
    BigInteger min = sProp.getMinOccurs();
    return (min.signum() == 0);
  }

}

