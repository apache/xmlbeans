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
import org.apache.xmlbeans.impl.binding.tylar.DebugTylarWriter;
import org.apache.xmlbeans.impl.binding.joust.Variable;
import org.apache.xmlbeans.impl.binding.joust.CompilingJavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;
import org.w3.x2001.xmlSchema.SchemaDocument;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.List;
import java.math.BigInteger;
import java.io.*;
import java.lang.reflect.Modifier;

/**
 * This is the "JAXRPC-style" Schema-to-bts compiler.
 */
public class Schema2Java extends BindingCompiler {

  // ========================================================================
  // Variables

  private Set usedNames = new HashSet();
  private SchemaTypeSystem sts = null;
  private Map scratchFromXmlName = new LinkedHashMap();
  private Map scratchFromSchemaType = new HashMap(); // for convenience
  private Map scratchFromJavaNameString = new HashMap(); // for printing
  private BindingLoader mLoader;
  private int structureCount;
  private BindingFile bindingFile = new BindingFile();
  private JavaOutputStream mJoust = null;
  private CompilingJavaOutputStream mDefaultJoust = null;
  private boolean mJaxRpcRules;

  // ========================================================================
  // Constants

  private static String[] PRIMITIVE_TYPES =
  {"int", "boolean", "float", "long", "double", "short", "byte", "char"};
  private static String[] BOXED_TYPES =
  {"java.lang.Integer", "java.lang.Boolean", "java.lang.Float",
   "java.lang.Long", "java.lang.Double", "java.lang.Short", "java.lang.Byte",
   "java.lang.Character"};
  private static String WILDCARD_ELEMENT_MAPPING = "javax.xml.soap.SOAPElement";
  private static String WILDCARD_ATTRIBUTE_MAPPING = "javax.xml.soap.SOAPElement";
  private static final String xsns = "http://www.w3.org/2001/XMLSchema";

  // ========================================================================
  // Constructors

  /**
   * Consturcts a Schema2Java to bind the types in the given type system.
   */
  public Schema2Java(SchemaTypeSystem s) {
    setSchemaTypeSystem(s);
  }

  /**
   * If you use this, you absolutely have to call setInput later.  This is
   * here just as a convenience for Schema2JavaTask.
   */
  /*package*/
  Schema2Java() {
  }

  /*package*/
  void setSchemaTypeSystem(SchemaTypeSystem s) {
    if (s == null) throw new IllegalArgumentException("null sts");
    sts = s;
  }

  // ========================================================================
  // Public methods

  /**
   * Sets whether javac should be run on the generated java sources.
   * The default is true.
   */
  public void setCompileJava(boolean b) {
    assertCompilationStarted(false);
    getDefaultJoust().setDoCompile(b);
  }

  /**
   * Sets the location of javac to be invoked.  Default compiler is used
   * if this is not set.  Ignored if doCompile is set to false.  Also note
   * that not all BindingCompilers generate any source code at all, so
   * setting this may have no effect.
   */
  public void setJavac(String javacPath) {
    assertCompilationStarted(false);
    getDefaultJoust().setJavac(javacPath);
  }

  /**
   * Sets the classpath to use for compilation of generated sources.
   * The System classpath is used by default.  This is ignored if doCompile is
   * false.  Also note that not all BindingCompilers generate any source
   * code at all, so setting this may have no effect.
   */
  public void setJavacClasspath(File[] classpath) {
    assertCompilationStarted(false);
    getDefaultJoust().setJavacClasspath(classpath);
  }

  /**
   * Sets whether this BindingCompiler should keep any generated java source
   * code it generates.  The default is true.  This will have no effect if
   * doCompile is set to false.  Also note that not all BindingCompilers
   * generate any source code at all, so setting this may have no effect in
   * any event.
   */
  public void setKeepGeneratedJava(boolean b) {
    assertCompilationStarted(false);
    getDefaultJoust().setKeepGenerated(b);
  }

  /**
   * Sets whether the compiler should use the JAX-RPC rules for mapping simple
   * types and XMLNames to Java. By default, the XMLBeans rules are used,
   * which are in fact almost identical.
   */
  public void setJaxRpcRules(boolean b) {
    assertCompilationStarted(false);
    mJaxRpcRules = b;
  }

  // ========================================================================
  // BindingCompiler implementation

  /**
   * We override this method because we need the bindAs... tylar to include
   * the defaultJoust.
   */
  protected ExplodedTylarImpl createDefaultExplodedTylarImpl(File tylarDestDir)
          throws IOException {
    CompilingJavaOutputStream joust = getDefaultJoust();
    joust.setSourceDir(new File(tylarDestDir, TylarConstants.SRC_ROOT));
    joust.setCompilationDir(tylarDestDir);
    mJoust = joust;
    return ExplodedTylarImpl.create(tylarDestDir, mJoust);
  }

  /**
   * Computes the binding.  Note that the given TylarWriter MUST provide
   * a JavaOutputStream or an IllegalArgumentException will be thrown.  Note
   * also that if you call this method, the various parameters on this object
   * pertaining to java compilation (e.g. setJavacPath) will be ignored.
   */
  protected void internalBind(TylarWriter writer) {
    if (sts == null) throw new IllegalStateException("SchemaTypeSystem not set");
    if ((mJoust = writer.getJavaOutputStream()) == null) {
      //sanity check
      throw new IllegalArgumentException("The specified TylarWriter does not " +
                                         "provide a JavaOutputStream, and so it cannot be used with " +
                                         "schema2java.");
    }
    bind();
    try {
      writer.writeBindingFile(bindingFile);
      writer.writeSchemaTypeSystem(sts);
    } catch (IOException ioe) {
      if (!logError(ioe)) return;
    }
    //FIXME also write the input schemas
    writeJavaFiles();
  }

  // ========================================================================
  // Private methods

  private CompilingJavaOutputStream getDefaultJoust() {
    if (mDefaultJoust == null) {
      mDefaultJoust = new CompilingJavaOutputStream();
      mDefaultJoust.setLogger(this);
    }
    return mDefaultJoust;
  }

  private void bind() {
    //sanity check
    if (mJoust == null) throw new IllegalStateException("joust not set");

    setBuiltinBindingLoader(BuiltinBindingLoader.getBuiltinBindingLoader(mJaxRpcRules));
    mLoader = super.getBaseBindingLoader();

    // Every type or global element or global attribute is dropped into
    // one of a number of categories.  (See Scratch constants.)
    //
    // Based on the category, a java class is either defined or found
    // that maches with the schema type.
    //
    // The phases work as follows:
    //
    // 1. categorize and allocate Scratches
    // 2. write or find java names for each xml type
    // 3. allocate binding types for each scratch (done in the same pass as 2)
    // 4. fill in the java getter/setter structure of any complex types


    // 1. categorize and allocate Scratches
    createScratchArea();

    // 2. write or find java names for each xml type
    // 3. allocate binding types for each scratch (done in the same pass as 2)
    for (Iterator i = scratchIterator(); i.hasNext();) {
      Scratch scratch = (Scratch) i.next();
      resolveJavaName(scratch);
      createBindingType(scratch);
    }

    // 4. fill in the java getter/setter structure of any complex types
    for (Iterator i = scratchIterator(); i.hasNext();) {
      Scratch scratch = (Scratch) i.next();
      resolveJavaStructure(scratch);
      resolveJavaEnumeration(scratch);
      resolveJavaArray(scratch);
    }
  }

  /**
   * This function goes through all relevant schema types, plus soap
   * array types, and creates a scratch area for each.  Each
   * scratch area is also marked at this time with an XmlTypeName,
   * a schema type, and a category.
   */
  private void createScratchArea() {
    logVerbose("creating scratch area...");
    for (Iterator i = allTypeIterator(); i.hasNext();) {
      SchemaType sType = (SchemaType) i.next();
      logVerbose("processing schema type "+sType);

      XmlTypeName xmlName = XmlTypeName.forSchemaType(sType);
      Scratch scratch;

      if (sType.isSimpleType()) {
        // simple types are atomic
        // the order of these checks is important: an enumeration of lists will
        // be both an enumeration and a list
        // todo: what about simple content, custom codecs, etc?
        if (isEnumeration(sType))
          scratch = new Scratch(sType, xmlName, Scratch.ENUM_TYPE);
        else if (isList(sType))
          scratch = new Scratch(sType, xmlName, Scratch.LIST_TYPE);
        else
          scratch = new Scratch(sType, xmlName, Scratch.ATOMIC_TYPE);
      } else if (sType.isDocumentType()) {
        scratch = new Scratch(sType, XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, sType.getDocumentElementName()), Scratch.ELEMENT);
      } else if (sType.isAttributeType()) {
        scratch = new Scratch(sType, XmlTypeName.forGlobalName(XmlTypeName.ATTRIBUTE, sType.getAttributeTypeAttributeName()), Scratch.ATTRIBUTE);
      } else if (isSoapArray(sType)) {
        scratch = new Scratch(sType, xmlName, Scratch.SOAPARRAY_REF);
        XmlTypeName altXmlName = soapArrayTypeName(sType);
        scratch.setAsIf(altXmlName);

        // soap arrays unroll like this
        while (altXmlName.getComponentType() == XmlTypeName.SOAP_ARRAY) {
          Scratch altScratch = new Scratch(sType, altXmlName, Scratch.SOAPARRAY);
          scratchFromXmlName.put(altXmlName, altScratch);
          altXmlName = altXmlName.getOuterComponent();
        }
      } else if (isLiteralArray(sType)) {
        scratch = new Scratch(sType, xmlName, Scratch.LITERALARRAY_TYPE);
      } else {
        scratch = new Scratch(sType, xmlName, Scratch.STRUCT_TYPE);
      }

      scratchFromXmlName.put(xmlName, scratch);
      scratchFromSchemaType.put(sType, scratch);
      logVerbose("registered scratch "+scratch.getXmlName()+" for "+sType);
    }
  }

  /**
   * Computes a JavaTypeName for each scratch.  Notice that structures and
   * atoms can be computed directly, but arrays, elements, etc, need
   * to defer to other scratch areas, so this is a resolution
   * process that occurs in dependency order.
   */
  private void resolveJavaName(Scratch scratch) {
    if (scratch == null) {
      logVerbose("FIXME null scratch, ignoring for now");
      return;
    }
    if (scratch == null) throw new IllegalArgumentException("null scratch");
    logVerbose("Resolving " + scratch.getXmlName());
    // already resolved (we recurse to do in dependency order)
    if (scratch.getJavaName() != null)
      return;

    switch (scratch.getCategory()) {
      case Scratch.ATOMIC_TYPE:
        {
          resolveSimpleScratch(scratch);
          return;
        }

      case Scratch.STRUCT_TYPE:
      case Scratch.ENUM_TYPE:
        {
          structureCount += 1;
          JavaTypeName javaName = pickUniqueJavaName(scratch.getSchemaType());
          scratch.setJavaName(javaName);
          scratchFromJavaNameString.put(javaName.toString(), scratch);
          return;
        }

      case Scratch.LIST_TYPE:
        {
          SchemaType itemType = getListItemType(scratch.getSchemaType());
          Scratch itemScratch = scratchForSchemaType(itemType);
          JavaTypeName itemName = null;
          if (itemScratch == null) {
            itemName = getTypeNameFromLoader(itemType);
          }
          else {
            // The type is in the current scratch area
            resolveJavaName(itemScratch);
            itemName = itemScratch.getJavaName();
          }
          if (itemName != null)
            scratch.setJavaName(JavaTypeName.forArray(itemName, 1));
          return;
        }
      case Scratch.LITERALARRAY_TYPE:
        {
          SchemaType itemType = getLiteralArrayItemType(scratch.getSchemaType());
          boolean nillable = scratch.getSchemaType().
              getProperties()[0].hasNillable() != SchemaProperty.NEVER;
          Scratch itemScratch = scratchForSchemaType(itemType);
          JavaTypeName itemName = null;
          if (itemScratch == null) {
            itemName = getTypeNameFromLoader(itemType);
          }
          else
          {
              resolveJavaName(itemScratch);
              itemName = itemScratch.getJavaName();
          }
          if (itemName != null)
              if (nillable)
              {
                JavaTypeName boxedName = getBoxedName(itemName);
                if (boxedName != null)
                  scratch.setJavaName(JavaTypeName.forArray(boxedName, 1));
                else
                  scratch.setJavaName(JavaTypeName.forArray(itemName, 1));
              }
              else
                scratch.setJavaName(JavaTypeName.forArray(itemName, 1));
          return;
        }

      case Scratch.SOAPARRAY_REF:
        {
          XmlTypeName soapArrayName = scratch.getAsIf();
          Scratch arrayScratch = scratchForXmlName(soapArrayName);
          resolveJavaName(arrayScratch);
          scratch.setJavaName(arrayScratch.getJavaName());
          scratch.setAsIf(arrayScratch.getXmlName());
          return;
        }

      case Scratch.SOAPARRAY:
        {
          XmlTypeName arrayName = scratch.getXmlName();
          XmlTypeName itemName = arrayName.getOuterComponent();
          Scratch itemScratch = scratchForXmlName(itemName);
          JavaTypeName itemJavaName = null;
          if (itemScratch == null) {
              itemJavaName = getTypeNameFromLoader(itemName);
              if (itemJavaName == null) {
                logError("Could not find reference to type \"" +
                  itemName.getQName() + "\"", null, scratch.getSchemaType());
                itemJavaName = JavaTypeName.forString("unknown");
              }
          }
          else {
              resolveJavaName(itemScratch);
              itemJavaName = itemScratch.getJavaName();
          }
          scratch.setJavaName(JavaTypeName.forArray(itemJavaName, arrayName.getNumber()));
          return;
        }

      case Scratch.ELEMENT:
      case Scratch.ATTRIBUTE:
        {
          logVerbose("processing element "+scratch.getXmlName());
          SchemaType contentType = scratch.getSchemaType().getProperties()[0].getType();
          boolean nillable = scratch.getSchemaType().
              getProperties()[0].hasNillable() != SchemaProperty.NEVER;
          logVerbose("content type is "+contentType.getName());
          JavaTypeName contentName = null;
          Scratch contentScratch = scratchForSchemaType(contentType);
          logVerbose("content scratch is "+contentScratch);
          if (contentScratch == null)
          {
              XmlTypeName treatAs = XmlTypeName.forSchemaType(contentType);
              BindingType bType = mLoader.getBindingType(mLoader.
                  lookupPojoFor(treatAs));
              if (bType != null)
              {
                  contentName = bType.getName().getJavaName();
                  scratch.setAsIf(treatAs);
              }
              else if (contentType.isBuiltinType())
                  logError("Builtin type " + contentType.getName() + " is not supported",
                      null, contentType);
              else
                  throw new IllegalStateException(contentType.getName().toString()+
                      " type is not on mLoader");
          }
          else
          {
              resolveJavaName(contentScratch);
              contentName = contentScratch.getJavaName();
              scratch.setAsIf(contentScratch.getXmlName());
          }
          if (contentName != null)
              if (nillable)
              {
                JavaTypeName boxedName = getBoxedName(contentName);
                if (boxedName != null)
                  scratch.setJavaName(boxedName);
                else
                  scratch.setJavaName(contentName);
              }
              else
                scratch.setJavaName(contentName);
          return;
        }

      default:
        throw new IllegalStateException("Unrecognized category");
    }
  }

  /**
   * Computes a BindingType for a scratch.
   */
  private void createBindingType(Scratch scratch) {
    assert(scratch.getBindingType() == null);
    logVerbose("createBindingType for "+scratch);
    BindingTypeName btName = BindingTypeName.forPair(scratch.getJavaName(), scratch.getXmlName());

    switch (scratch.getCategory()) {
      case Scratch.ATOMIC_TYPE:
      case Scratch.SOAPARRAY_REF:
      case Scratch.ATTRIBUTE:
        SimpleBindingType simpleResult = new SimpleBindingType(btName);
        simpleResult.setAsIfXmlType(scratch.getAsIf());
        scratch.setBindingType(simpleResult);
        bindingFile.addBindingType(simpleResult, shouldBeFromJavaDefault(btName), true);
        break;

      case Scratch.ELEMENT:
        SimpleDocumentBinding docResult = new SimpleDocumentBinding(btName);
        docResult.setTypeOfElement(scratch.getAsIf());
        scratch.setBindingType(docResult);
        bindingFile.addBindingType(docResult, shouldBeFromJavaDefault(btName), true);
        break;

      case Scratch.STRUCT_TYPE:
        BindingType structResult;
        if (scratch.getSchemaType().getContentType() == SchemaType.SIMPLE_CONTENT)
          structResult = new SimpleContentBean(btName);
        else
          structResult = new ByNameBean(btName);
        scratch.setBindingType(structResult);
        bindingFile.addBindingType(structResult, true, true);
        break;

      case Scratch.ENUM_TYPE:
        JaxrpcEnumType enumResult = new JaxrpcEnumType(btName);
        enumResult.setGetValueMethod(JaxrpcEnumType.DEFAULT_GET_VALUE);
        enumResult.setFromValueMethod(JaxrpcEnumType.DEFAULT_FROM_VALUE);
        enumResult.setFromStringMethod(JaxrpcEnumType.DEFAULT_FROM_STRING);
        enumResult.setToXMLMethod(JaxrpcEnumType.DEFAULT_TO_XML);
        scratch.setBindingType(enumResult);
        bindingFile.addBindingType(enumResult, true, true);
        break;

      case Scratch.LIST_TYPE:
        ListArrayType listResult = new ListArrayType(btName);
        scratch.setBindingType(listResult);
        bindingFile.addBindingType(listResult, shouldBeFromJavaDefault(btName), true);
        break;

      case Scratch.LITERALARRAY_TYPE:
        WrappedArrayType arrayResult = new WrappedArrayType(btName);
        scratch.setBindingType(arrayResult);
        bindingFile.addBindingType(arrayResult, shouldBeFromJavaDefault(btName), true);
        break;

      case Scratch.SOAPARRAY:
        WrappedArrayType soapArray = new WrappedArrayType(btName);
        scratch.setBindingType(soapArray);
        bindingFile.addBindingType(soapArray, shouldBeFromJavaDefault(btName), false);
        break;

      default:
        throw new IllegalStateException("Unrecognized category");
    }
  }

  /**
   * Returns true if the given btName would be the first (unique)
   * default java->xml binding to be entered in the java-to-type or
   * java-to-element tables.
   */
  private boolean shouldBeFromJavaDefault(BindingTypeName btName) {
    JavaTypeName jName = btName.getJavaName();
    XmlTypeName xName = btName.getXmlName();
    if (xName.isSchemaType()) {
      return (bindingFile.lookupTypeFor(jName) == null &&
              mLoader.lookupTypeFor(jName) == null);
    }
    if (xName.getComponentType() == XmlTypeName.ELEMENT) {
      return (bindingFile.lookupElementFor(jName) == null &&
              mLoader.lookupElementFor(jName) == null);
    }
    return false;
  }

  /**
   * Now we resolve the structural aspects (property names) for each
   * scratch.
   */
  private void resolveJavaStructure(Scratch scratch) {
    if (scratch.getCategory() != Scratch.STRUCT_TYPE)
      return;

    if (scratch.isStructureResolved())
      return;

    scratch.setStructureResolved(true);

    SchemaType schemaType = scratch.getSchemaType();
    SchemaType baseType = schemaType.getBaseType();
    int derivationType  = schemaType.getDerivationType();
    Collection baseProperties = null;
    if (baseType != null)
      baseProperties = extractProperties(baseType);
    if (baseProperties == null)
      baseProperties = Collections.EMPTY_LIST;

    // sort properties based on QName attr/elt
    Map seenAttrProps = new HashMap();
    Map seenEltProps = new HashMap();
    Set seenMethodNames = new HashSet();
    seenMethodNames.add("getClass");

    for (Iterator i = baseProperties.iterator(); i.hasNext();) {
      QNameProperty prop = (QNameProperty) i.next();
      if (prop.isAttribute())
        seenAttrProps.put(prop.getQName(), prop);
      else
        seenEltProps.put(prop.getQName(), prop);

      // todo: probably this collision avoidance should be using Java introspection instead
      if (prop.getGetterName() != null)
        seenMethodNames.add(prop.getGetterName());
      if (prop.getSetterName() != null)
        seenMethodNames.add(prop.getSetterName());
    }

    if (schemaType.getContentType() == SchemaType.SIMPLE_CONTENT) {
      // Go up the type hierarchy to find the first simple type ancestor of
      // this complex type
      while (!baseType.isSimpleType())
        baseType = baseType.getBaseType();
      // we have to add a '_value' property to hold the value corresponding to the
      // content of the XML elem
      BindingType bType = extractBindingType(baseType);
      if (bType == null)
        throw new IllegalStateException("Type " + baseType.getName() +
          "not found in type loader");
      String propName = "_value";
      SimpleContentProperty prop = new SimpleContentProperty();
      prop.setSetterName(MethodName.create("set" + propName,
          bType.getName().getJavaName()));
      prop.setGetterName(MethodName.create("get" + propName));
      prop.setBindingType(bType);
      scratch.setSimpleContentProperty(prop);
    }
    else {
      // Handle the element wildcards
      if (schemaType.hasElementWildcards()) {
        // First, we have to see if it's just one wildcard or more
        boolean multiple = countWildcards(schemaType.getContentModel()) > 1;
        // We have to look at the base type and check multiplicity
        if (baseType != null &&
          baseType.getBuiltinTypeCode() != SchemaType.BTC_ANY_TYPE) {
          boolean hasBaseElementWildcards = baseType.hasElementWildcards();
          boolean baseMultiple = countWildcards(baseType.getContentModel()) > 1;
          if (hasBaseElementWildcards && multiple != baseMultiple)
            logError("Could not bind type\"" + schemaType.getName() +
                    "\" because its base type \"" + baseType.getName() +
                    "\" has only one element wildcard and the current type has more.",
                    null, schemaType);
        }
        GenericXmlProperty prop = new GenericXmlProperty();
        String propName = "_any";
        BindingType bType = getWildcardElementBindingType(multiple);
        prop.setSetterName(MethodName.create("set" + propName,
            bType.getName().getJavaName()));
        prop.setGetterName(MethodName.create("get" + propName));
        prop.setBindingType(bType);
        scratch.setAnyElementProperty(prop);
      }
    }

    if (derivationType == SchemaType.DT_RESTRICTION)
    {
        // Derivation type is restriction, so no new properties may be added
        return;
    }

    // Handle the attribute wildcards
    // No check is necessary, because it always maps to the same type if present
    if (schemaType.hasAttributeWildcards()) {
      String propName = "_anyAttribute";
      GenericXmlProperty prop = new GenericXmlProperty();
      BindingType bType = getWildcardAttributeBindingType();
      prop.setSetterName(MethodName.create("set" + propName,
          bType.getName().getJavaName()));
      prop.setGetterName(MethodName.create("get" + propName));
      prop.setBindingType(bType);
      scratch.setAnyAttributeProperty(prop);
    }

    // Now deal with remaining props
    SchemaProperty[] props = schemaType.getProperties();
    for (int i = 0; i < props.length; i++) {
      QNameProperty prop = (QNameProperty) (props[i].isAttribute() ? seenAttrProps : seenEltProps).get(props[i].getName());
      if (prop != null) {
        // already seen property: verify multiplicity looks cool
        if (prop.isMultiple() != isMultiple(props[i])) {
            logError("Could not bind element \"" + props[i].getName() +
                    "\" because the corresponding element in the base type has a " +
                    "different 'maxOccurs' value", null, props[i]);
        }

        // todo: think about optionality and nillability too
      } else {
        SchemaType sType = props[i].getType();
        BindingType bType = bindingTypeForSchemaType(sType);
        if (bType == null)
          throw new IllegalStateException("Type " + sType.getName() +
            "not found in type loader");

        String propName = pickUniquePropertyName(props[i].getName(), seenMethodNames);
        boolean isMultiple = isMultiple(props[i]);
        JavaTypeName collection = null;

        prop = new QNameProperty();
        prop.setQName(props[i].getName());
        prop.setAttribute(props[i].isAttribute());
        prop.setSetterName(MethodName.create("set" + propName,
                                             bType.getName().getJavaName()));
        prop.setGetterName(MethodName.create("get" + propName));
        prop.setNillable(props[i].hasNillable() != SchemaProperty.NEVER);
        prop.setOptional(isOptional(props[i]));
        prop.setMultiple(isMultiple);
        if (prop.isNillable() || prop.isOptional())
          bType = findBoxedType(bType);
        prop.setBindingType(bType);
        if (prop.isMultiple())
                collection = JavaTypeName.forArray(bType.getName().getJavaName(), 1);
        prop.setCollectionClass(collection);
      }
      scratch.addQNameProperty(prop);
    }
  }

  /**
   * Resolves a Java array
   */
  private void resolveJavaArray(Scratch scratch)
  {
    if (scratch.getCategory() != Scratch.LITERALARRAY_TYPE &&
        scratch.getCategory() != Scratch.LIST_TYPE &&
        scratch.getCategory() != Scratch.SOAPARRAY)
      return;

    if (scratch.isStructureResolved())
      return;

    scratch.setStructureResolved(true);

    BindingType scratchBindingType = scratch.getBindingType();
    if (scratchBindingType instanceof WrappedArrayType) {
      WrappedArrayType bType = (WrappedArrayType) scratchBindingType;
      // todo: fix this
      if (scratch.getCategory() == Scratch.SOAPARRAY) {
        XmlTypeName typexName = scratch.getXmlName();
        XmlTypeName itemxName = typexName.getOuterComponent();
        Scratch itemxScratch = scratchForXmlName(itemxName);
        BindingType itemxType;
        if (itemxScratch == null)
          itemxType = mLoader.getBindingType(mLoader.lookupPojoFor(itemxName));
        else
          itemxType = itemxScratch.getBindingType();
        // The rule is: if the soap array type looks like a literal array
        // then the name and nillability are the same as for literal arrays
        // Otherwise, the name is not significant and nillability is false
        if (getLiteralArrayItemType(scratch.getSchemaType()) == null) {
          bType.setItemName(new QName("", "foo"));
          bType.setItemNillable(false);
        }
        else {
          SchemaProperty prop = scratch.getSchemaType().getProperties()[0];
          bType.setItemName(prop.getName());
          bType.setItemNillable(prop.hasNillable() != SchemaProperty.NEVER);
        }

        if (itemxType != null) {
          if (bType.isItemNillable())
            itemxType = findBoxedType(itemxType);
          bType.setItemType(itemxType.getName());
        }
        else
          bType.setItemType(bType.getName());
        return;
      }
      JavaTypeName itemName = scratch.getJavaName().getArrayItemType(1);
      assert(itemName != null);
      SchemaType sType = getLiteralArrayItemType(scratch.getSchemaType());
      assert sType != null : "This was already checked and determined to be non-null";
      SchemaProperty prop = scratch.getSchemaType().getProperties()[0];
      bType.setItemName(prop.getName());
      BindingType itemType = bindingTypeForSchemaType(sType);
      if (itemType == null)
        throw new IllegalStateException("Type " + sType.getName() +
          " not found in type loader");

      bType.setItemNillable(prop.hasNillable() != SchemaProperty.NEVER);
      if (bType.isItemNillable())
        itemType = findBoxedType(itemType);
      bType.setItemType(itemType.getName());
    }
    else if (scratchBindingType instanceof ListArrayType) {
      ListArrayType bType = (ListArrayType) scratchBindingType;
      JavaTypeName itemName = scratch.getJavaName().getArrayItemType(1);
      assert (itemName != null);
      SchemaType sType = getListItemType(scratch.getSchemaType());
      assert (sType != null);
      BindingType itemType = bindingTypeForSchemaType(sType);
      if (itemType == null)
        throw new IllegalStateException("Type " + sType.getName() +
          " not found in the type loader");
      bType.setItemType(itemType.getName());
    }
    else
      throw new IllegalStateException();
  }

  /**
   * Resolves a Java enumeration
   */
  private void resolveJavaEnumeration(Scratch scratch)
  {
    if (scratch.getCategory() != Scratch.ENUM_TYPE)
      return;

    if (scratch.isStructureResolved())
      return;

    scratch.setStructureResolved(true);

    SchemaType baseType = scratch.getSchemaType().getBaseType();

    BindingType bType = bindingTypeForSchemaType(baseType);
    assert bType != null : "Binding type for schema type \"" + baseType +
      "\" not found on the mLoader";

    ((JaxrpcEnumType) scratch.getBindingType()).setBaseType(bType);
  }

  /**
   * Picks a property name without colliding with names of
   * previously picked getters and setters.
   */
  private String pickUniquePropertyName(QName name, Set seenMethodNames) {
    String baseName = NameUtil.upperCamelCase(name.getLocalPart(), mJaxRpcRules);
    String propName = baseName;
    for (int i = 1; ; i += 1) {
      String getter = "get" + propName;
      String setter = "set" + propName;

      if (!seenMethodNames.contains(getter) &&
              !seenMethodNames.contains(setter)) {
        seenMethodNames.add(getter);
        seenMethodNames.add(setter);
        return propName;
      }
      propName = baseName + i;
    }
  }

  /**
   * True if the given SchemaProperty has maxOccurs > 1
   */
  private static boolean isMultiple(SchemaProperty prop) {
    return (prop.getMaxOccurs() == null || prop.getMaxOccurs().compareTo(BigInteger.ONE) > 0);
  }

  /**
   * True if the given SchemaProperty has minOccurs < 1 and maxOccurs <= 1
   */
  private static boolean isOptional(SchemaProperty prop) {
      return (prop.getMinOccurs().signum() == 0 && !isMultiple(prop));
  }

  /**
   * Returns a collection of QNameProperties for a given schema type that
   * may either be on the mLoader or the current scratch area.
   */
  private Collection extractProperties(SchemaType sType) {
    // case 1: it's in the current area
    Scratch scratch = scratchForSchemaType(sType);
    if (scratch != null) {
      // The type found may not be a structure
      if (scratch.getCategory() == Scratch.STRUCT_TYPE) {
        resolveJavaStructure(scratch);
        return scratch.getQNameProperties();
      }
      else
        return null;
    }

    // case 2: it's in the mLoader
    BindingType bType = mLoader.getBindingType(mLoader.lookupPojoFor(XmlTypeName.forSchemaType(sType)));

    Collection result = new ArrayList();
    if (bType instanceof ByNameBean) {
      ByNameBean bnb = (ByNameBean) bType;
      for (Iterator i = bnb.getProperties().iterator(); i.hasNext();) {
        result.add(i.next());
      }
    }
    else if (bType instanceof SimpleContentBean) {
      SimpleContentBean scb = (SimpleContentBean) bType;
      for (Iterator i = scb.getAttributeProperties().iterator(); i.hasNext();) {
        result.add(i.next());
      }
    }
    else
      return null;

    return result;
  }

  /**
   * Returns the simple type which the base of a complex Type with simpleContent
   */
  private BindingType extractBindingType(SchemaType sType) {
    // case 1: it's in the current area
    Scratch scratch = scratchForSchemaType(sType);
    if (scratch != null)
      return scratch.getBindingType();

    // case 2: it's in the mLoader
    BindingType bType = mLoader.getBindingType(mLoader.lookupPojoFor(XmlTypeName.
            forSchemaType(sType)));

    return bType;
  }

  /**
   * Returns a BindingType representing the default type for <xs:any> content
   */
  private BindingType getWildcardElementBindingType(boolean multiple) {
    JavaTypeName javaName;
    javaName = JavaTypeName.forString(WILDCARD_ELEMENT_MAPPING);
    if (multiple)
      javaName = JavaTypeName.forArray(javaName, 1);
    XmlTypeName xmlName = XmlTypeName.forTypeNamed(new QName(xsns, "anyType"));
    return new SimpleBindingType(BindingTypeName.forPair(javaName, xmlName));
  }

  /**
   * Returns a BindingType representing the default type for <xs:anyAttribute>
   */
  private BindingType getWildcardAttributeBindingType() {
    JavaTypeName javaName;
    javaName = JavaTypeName.forString(WILDCARD_ATTRIBUTE_MAPPING);
    XmlTypeName xmlName = XmlTypeName.forTypeNamed(new QName(xsns, "anyType"));
    return new SimpleBindingType(BindingTypeName.forPair(javaName, xmlName));
  }

  private JavaTypeName getBoxedName(JavaTypeName jName)
  {
    // We could use a map here and initialize it on first use
    for (int i = 0; i < PRIMITIVE_TYPES.length; i++)
      if (PRIMITIVE_TYPES[i].equals(jName.toString()))
        return JavaTypeName.forString(BOXED_TYPES[i]);

    return null;
  }

  /**
   * Returns the boxed version of the given binding type
   */
  private BindingType findBoxedType(BindingType type)
  {
    BindingTypeName btName = type.getName();
    JavaTypeName javaName = btName.getJavaName();
    BindingType result = null;
    JavaTypeName boxedJavaName = getBoxedName(javaName);
    if (boxedJavaName != null)
    {
      // This is a primitive type
      BindingTypeName boxedName = BindingTypeName.forPair(boxedJavaName, btName.getXmlName());
      // If the type is in the current scratch area, create a new boxed type
      if (scratchForXmlName(btName.getXmlName()) != null)
      {
        result = bindingFile.getBindingType(boxedName);
        if (result == null)
        {
          result = changeJavaName((SimpleBindingType) type, boxedName);
          bindingFile.addBindingType(result, false, false);
        }
        return result;
      }
      // If this is a type available on the mLoader, try to locate
      // the boxed type corresponding to it.
      result = mLoader.getBindingType(boxedName);
      if (result != null)
        return result;

      // Type is not in the current scratch area nor on the mLoader
      // We create it and add it to the file
      result = bindingFile.getBindingType(boxedName);
      if (result == null)
      {
        result = changeJavaName((SimpleBindingType) type, boxedName);
        bindingFile.addBindingType(result, false, false);
      }
      return result;
    }

    return type;
  }

  /**
   * Creates a new binding type based on the given binding type and bearing the
   * new JavaTypeName. This is necessary to created boxed versions of
   * binding types based on Java primitive types.
   */
  private BindingType changeJavaName(SimpleBindingType bType, BindingTypeName btName)
  {
    SimpleBindingType result = new SimpleBindingType(btName);
    result.setAsIfXmlType(bType.getAsIfXmlType());
    result.setWhitespace(bType.getWhitespace());
    return result;
  }

  /**
   * True for a schema type that is a SOAP array.
   */
  private static boolean isSoapArray(SchemaType sType) {
    // SOAP Array definition must be put on the compiletime classpath
    while (sType != null) {
      String signature = XmlTypeName.forSchemaType(sType).toString();

      // captures both SOAP 1.1 and SOAP 1.2+
      if (signature.equals("t=Array@http://schemas.xmlsoap.org/soap/encoding/") ||
              signature.startsWith("t=Array@http://www.w3.org/")
              && signature.endsWith("/soap-encoding"))
        return true;
      sType = sType.getBaseType();
    }
    return false;
  }

  private static final QName arrayType = new QName("http://schemas.xmlsoap.org/soap/encoding/", "arrayType");

  /**
   * Returns an XmlTypeName describing a SOAP array.
   */
  private static XmlTypeName soapArrayTypeName(SchemaType sType) {
    // first, look for wsdl:arrayType default - this will help us with multidimensional arrays
    SOAPArrayType defaultArrayType = null;
    SchemaLocalAttribute attr = sType.getAttributeModel().getAttribute(arrayType);
    if (attr != null)
      defaultArrayType = ((SchemaWSDLArrayType) attr).getWSDLArrayType();

    // method 1: trust wsdl:arrayType
    if (defaultArrayType != null)
      return XmlTypeName.forSoapArrayType(defaultArrayType);

    // method 2: SOAP 1.2 equivalent?
    // todo: track what do WSDLs do in the world of SOAP 1.2.

    // method 3: look at the type of a unique element.
    SchemaType itemType = XmlObject.type;
    SchemaProperty[] props = sType.getElementProperties();
    if (props.length == 1)
      itemType = props[0].getType();

    return XmlTypeName.forNestedNumber(XmlTypeName.SOAP_ARRAY, 1, XmlTypeName.forSchemaType(itemType));
  }

  /**
   * Climbs the structure of a schema type to find the namespace within
   * which it was defined.
   */
  private String findContainingNamespace(SchemaType sType) {
    for (; ;) {
      if (sType.isDocumentType())
        return sType.getDocumentElementName().getNamespaceURI();
      else if (sType.isAttributeType())
        return sType.getAttributeTypeAttributeName().getNamespaceURI();
      else if (sType.getName() != null)
        return sType.getName().getNamespaceURI();
      sType = sType.getOuterType();
    }
  }

  /**
   * Searches the content of a complex type to see if more than one <xs:any>
   * wildcard was defined
   */
  private int countWildcards(SchemaParticle p)
  {
    int totalWildcards = 0;
    switch (p.getParticleType()) {
      case SchemaParticle.ALL:
      case SchemaParticle.SEQUENCE:
        {
          SchemaParticle[] children = p.getParticleChildren();
          for (int i = 0; i < children.length; i++)
            totalWildcards += countWildcards(children[i]);
        }
        break;
      case SchemaParticle.CHOICE:
        {
          SchemaParticle[] children = p.getParticleChildren();
          for (int i = 0; i < children.length; i++) {
            int n = countWildcards(children[i]);
            if (n > totalWildcards)
              totalWildcards = n;
          }
        }
        break;
      case SchemaParticle.ELEMENT:
        break;
      case SchemaParticle.WILDCARD:
        totalWildcards = p.getIntMaxOccurs();
        break;
      }
    return totalWildcards;
  }

  /**
   * Picks a unique fully-qualified Java class name for the given schema
   * type.  Uses and updates the "usedNames" set.
   */
  private JavaTypeName pickUniqueJavaName(SchemaType sType) {
    QName qname = null;
    while (qname == null) {
      if (sType.isDocumentType())
        qname = sType.getDocumentElementName();
      else if (sType.isAttributeType())
        qname = sType.getAttributeTypeAttributeName();
      else if (sType.getName() != null)
        qname = sType.getName();
      else if (sType.getContainerField() != null) {
        qname = sType.getContainerField().getName();
        if (qname.getNamespaceURI().length() == 0)
          qname = new QName(findContainingNamespace(sType), qname.getLocalPart());
      }
      sType = sType.getOuterType();
    }

    String baseName = NameUtil.getClassNameFromQName(qname, mJaxRpcRules);
    String pickedName = baseName;

    for (int i = 1; usedNames.contains(pickedName.toLowerCase()); i += 1)
      pickedName = baseName + i;

    usedNames.add(pickedName.toLowerCase());

    return JavaTypeName.forString(pickedName);
  }

  /**
   * Resolves an atomic scratch all at once, including its
   * JavaTypeName and basedOn fields.
   *
   * This resolution method sets up a scratch so that is
   * is "based on" another binding type.  It finds the
   * underlying binding type by climing the base type
   * chain, and grabbing the first hit.
   */
  private void resolveSimpleScratch(Scratch scratch) {
    assert(scratch.getCategory() == Scratch.ATOMIC_TYPE);
    logVerbose("resolveSimpleScratch "+scratch.getXmlName());
    if (scratch.getJavaName() != null)
      return;

    SchemaType baseType = scratch.getSchemaType().getBaseType();
    while (baseType != null) {
      // find a base type within this type system
      Scratch basedOnScratch = scratchForSchemaType(baseType);
      if (basedOnScratch != null) {
        if (basedOnScratch.getCategory() != Scratch.ATOMIC_TYPE)
          throw new IllegalStateException("Atomic types should only inherit from atomic types");
        resolveSimpleScratch(basedOnScratch);
        scratch.setJavaName(basedOnScratch.getJavaName());
        scratch.setAsIf(basedOnScratch.getXmlName());
        return;
      }

      // or if not within this type system, find the base type on the mLoader
      XmlTypeName treatAs = XmlTypeName.forSchemaType(baseType);
      BindingType basedOnBinding = mLoader.getBindingType(mLoader.lookupPojoFor(treatAs));
      if (basedOnBinding != null) {
        scratch.setJavaName(basedOnBinding.getName().getJavaName());
        scratch.setAsIf(treatAs);
        return;
      }

      // or go to the next base type up
      baseType = baseType.getBaseType();
    }

    // builtin at least should give us xs:anyType
    throw new IllegalStateException("Builtin binding type loader is not on mLoader.");
  }

  /**
   * Searches on the mLoader for the given schema type and
   * returns the java name of the type found or errors
   * if it cannot find the type
   */
  private JavaTypeName getTypeNameFromLoader(SchemaType sType) {
    return getTypeNameFromLoader(XmlTypeName.forSchemaType(sType));
  }

  private JavaTypeName getTypeNameFromLoader(XmlTypeName typeName) {
    BindingType bType = mLoader.getBindingType(mLoader.
      lookupPojoFor(typeName));
    if (bType != null)
      return bType.getName().getJavaName();

    return null;
  }

  /**
   * Looks on both the mLoader and in the current scratch area for
   * the binding type corresponding to the given schema type.  Must
   * be called after all the binding types have been created.
   */
  private BindingType bindingTypeForSchemaType(SchemaType sType) {
    Scratch scratch = scratchForSchemaType(sType);
    if (scratch != null)
      return scratch.getBindingType();
    return mLoader.getBindingType(mLoader.lookupPojoFor(XmlTypeName.forSchemaType(sType)));
  }

  /**
   * Returns the scratch area for a given schema type.  Notice that
   * SOAP arrays have an XmlTypeName but not a schema type.
   */
  private Scratch scratchForSchemaType(SchemaType sType) {
    return (Scratch) scratchFromSchemaType.get(sType);
  }

  /**
   * Returns the scratch area for a given XmlTypeName.
   */
  private Scratch scratchForXmlName(XmlTypeName xmlName) {
    return (Scratch) scratchFromXmlName.get(xmlName);
  }

  /**
   * Returns the scratch area for a given JavaTypeName.  Notice that only
   * structures generate a java class, so not non-strucuture scratch areas
   * cannot be referenced this way.
   */
  private Scratch scratchForJavaNameString(String javaName) {
    return (Scratch) scratchFromJavaNameString.get(javaName);
  }

  /**
   * Extracts the schema type for the array items for a literal array.
   */
  private static SchemaType getLiteralArrayItemType(SchemaType sType) {
    // consider: must the type be named "ArrayOf..."?

    if (sType.isSimpleType() || sType.getContentType() == SchemaType.SIMPLE_CONTENT)
      return null;
    SchemaProperty[] prop = sType.getProperties();
    if (prop.length != 1 || prop[0].isAttribute())
      return null;
    BigInteger max = prop[0].getMaxOccurs();
    if (max != null && max.compareTo(BigInteger.ONE) <= 0)
      return null;
    return prop[0].getType();
  }

  private static SchemaType getListItemType(SchemaType sType) {
    return sType.getListItemType();
  }

  /**
   * True if the given schema type is interpreted as a .NET-style
   * array.
   */
  private static boolean isLiteralArray(SchemaType sType) {
    return getLiteralArrayItemType(sType) != null;
  }

  /**
   * True if the given schema type is an enumeration type.
   */
  private static boolean isEnumeration(SchemaType sType) {
    return sType.getEnumerationValues() != null;
  }

  /**
   * True if the given schema type is a list type
   */
  private static boolean isList(SchemaType sType) {
    return getListItemType(sType) != null;
  }

  /**
   * Scratch area corresponding to a schema type, used for the binding
   * computation.
   */
  private static class Scratch {
    Scratch(SchemaType schemaType, XmlTypeName xmlName, int category) {
      this.schemaType = schemaType;
      this.xmlName = xmlName;
      this.category = category;
    }

    private BindingType bindingType;
    private SchemaType schemaType; // may be null
    private JavaTypeName javaName;
    private XmlTypeName xmlName;

    private int category;

    // atomic types get a treatAs
    private XmlTypeName asIf;
    private boolean isStructureResolved;

    // categories of Scratch, established at ctor time
    public static final int ATOMIC_TYPE = 1;
    public static final int STRUCT_TYPE = 2;
    public static final int ENUM_TYPE = 3;
    public static final int LIST_TYPE = 4;
    public static final int LITERALARRAY_TYPE = 5;
    public static final int SOAPARRAY_REF = 6;
    public static final int SOAPARRAY = 7;
    public static final int ELEMENT = 8;
    public static final int ATTRIBUTE = 9;

    public int getCategory() {
      return category;
    }

    public JavaTypeName getJavaName() {
      return javaName;
    }

    public void setJavaName(JavaTypeName javaName) {
      this.javaName = javaName;
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

    public void setXmlName(XmlTypeName xmlName) {
      this.xmlName = xmlName;
    }

    public XmlTypeName getAsIf() {
      return asIf;
    }

    public void setAsIf(XmlTypeName xmlName) {
      this.asIf = xmlName;
    }

    public void addQNameProperty(QNameProperty prop) {
      if (bindingType instanceof ByNameBean)
        ((ByNameBean) bindingType).addProperty(prop);
      else if (bindingType instanceof SimpleContentBean)
        ((SimpleContentBean) bindingType).addProperty(prop);
      else
        throw new IllegalStateException();
    }

    public Collection getQNameProperties() {
      if (bindingType instanceof ByNameBean)
        return ((ByNameBean) bindingType).getProperties();
      else if (bindingType instanceof SimpleContentBean)
        return ((SimpleContentBean) bindingType).getAttributeProperties();
      else
        throw new IllegalStateException();
    }

    public void setSimpleContentProperty(SimpleContentProperty prop) {
      if (bindingType instanceof SimpleContentBean)
        ((SimpleContentBean) bindingType).setSimpleContentProperty(prop);
      else
        throw new IllegalStateException();
    }

    public SimpleContentProperty getSimpleContentProperty() {
      if (bindingType instanceof SimpleContentBean)
        return ((SimpleContentBean) bindingType).getSimpleContentProperty();
      else
        return null;
    }

    public void setAnyAttributeProperty(GenericXmlProperty prop) {
      if (bindingType instanceof ByNameBean)
        ((ByNameBean) bindingType).setAnyAttributeProperty(prop);
      else if (bindingType instanceof SimpleContentBean)
        ((SimpleContentBean) bindingType).setAnyAttributeProperty(prop);
      else
        throw new IllegalStateException();
    }

    public GenericXmlProperty getAnyAttributeProperty() {
      if (bindingType instanceof ByNameBean)
        return ((ByNameBean) bindingType).getAnyAttributeProperty();
      else if (bindingType instanceof SimpleContentBean)
        return ((SimpleContentBean) bindingType).getAnyAttributeProperty();
      else
        throw new IllegalStateException();
    }

    public void setAnyElementProperty(GenericXmlProperty prop) {
      if (bindingType instanceof ByNameBean)
        ((ByNameBean) bindingType).setAnyElementProperty(prop);
      else
        throw new IllegalStateException();
    }

    public GenericXmlProperty getAnyElementProperty() {
      if (bindingType instanceof  ByNameBean)
        return ((ByNameBean) bindingType).getAnyElementProperty();
      else if (bindingType instanceof SimpleContentBean)
        return null;
      else
        throw new IllegalStateException();
    }

    public boolean isStructureResolved() {
      return this.isStructureResolved;
    }

    public void setStructureResolved(boolean isStructureResolved) {
      this.isStructureResolved = isStructureResolved;
    }

    public String toString() {
      return getJavaName()+"<->"+getXmlName();
    }
  }

  /**
   * Returns an iterator for all the Scratch's
   */
  private Iterator scratchIterator() {
    return scratchFromXmlName.values().iterator();
  }

  /**
   * Returns an iterator for all the schema types
   */
  private Iterator allTypeIterator() {
    class AllTypeIterator implements Iterator {
      int index;
      List allSeenTypes;

      AllTypeIterator(SchemaTypeSystem sts) {
        allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(sts.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));
//        allSeenTypes.addAll(Arrays.asList(XmlBeans.getBuiltinTypeSystem().globalTypes()));
        index = 0;
      }

      public boolean hasNext() {
        return index < allSeenTypes.size();
      }

      public Object next() {
        SchemaType next = (SchemaType) allSeenTypes.get(index);
        allSeenTypes.addAll(Arrays.asList(next.getAnonymousTypes()));
        index += 1;
        return next;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    }

    return new AllTypeIterator(sts);
  }

  /**
   * Iterates over all the top-level Java class names generated
   * by this binding.  Used by getToplevelClasses.
   */
  private class TopLevelClassNameIterator implements Iterator {
    private final Iterator si = scratchIterator();
    private Scratch next = nextStructure();

    public boolean hasNext() {
      return next != null;
    }

    public Object next() {
      // todo: need to strip off x= from xmlobject name
      String result = next.getJavaName().toString();
      next = nextStructure();
      return result;
    }

    private Scratch nextStructure() {
      while (si.hasNext()) {
        Scratch scratch = (Scratch) si.next();
        if (scratch.getCategory() == Scratch.STRUCT_TYPE ||
          scratch.getCategory() == Scratch.ENUM_TYPE)
          return scratch;
      }
      return null;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  // ========================================================================
  // Java Codegen methods
  //
  // REVIEW it may be worth factoring these methods back out into a separate
  // class someday.  Somebody conceivably might want to plug in here (at their
  // own risk, of course).   pcal 12/12/03

  private void writeJavaFiles() {
    Collection classnames = getToplevelClasses();
    for (Iterator i = classnames.iterator(); i.hasNext();) {
      String className = (String) i.next();
      printSourceCode(className);
    }
  }

  /**
   * Returns a collection of fully-qualified Java class name strings
   * generated by this binding.
   */
  public Collection getToplevelClasses() {
    return new AbstractCollection() {
      public Iterator iterator() {
        return new TopLevelClassNameIterator();
      }

      public int size() {
        return structureCount;
      }
    };
  }

  /**
   * Prints the Java source code for the given generated java class name.
   */
  private void printSourceCode(String topLevelClassName) {
    Scratch scratch = scratchForJavaNameString(topLevelClassName);
    if (scratch == null) {
      logError("Could not find scratch for " + topLevelClassName); //?
      return;
    }
    try {
      printClass(scratch);
    } catch (IOException ioe) {
      logError(ioe);
    }
  }

  /**
   * Prints out a java class for the schema struct represented by the given
   * Scratch object.
   *
   */
  private void printClass(Scratch scratch) throws IOException {
    assert(scratch.getCategory() == Scratch.STRUCT_TYPE ||
      scratch.getCategory() == Scratch.ENUM_TYPE);
    JavaTypeName javaName = scratch.getJavaName();

    String packageName = javaName.getPackage();
    String shortClassName = javaName.getShortClassName();
    BindingType baseType = null;
    SchemaType bSchemaType = scratch.getSchemaType().getBaseType();
    if (!bSchemaType.isSimpleType())
      baseType = bindingTypeForSchemaType(bSchemaType);
    String baseJavaname = null;
    if (baseType != null) {
      baseJavaname = baseType.getName().getJavaName().toString();
      if (baseJavaname.equals("java.lang.Object"))
        baseJavaname = null;
    }
    // begin writing class
    mJoust.startFile(packageName, shortClassName);
    mJoust.writeComment("Generated from schema type " + scratch.getXmlName());

    if (scratch.getCategory() == Scratch.STRUCT_TYPE)
      printJavaStruct(scratch, baseJavaname);
    else
      printJavaEnum(scratch, baseJavaname);

    mJoust.endFile();
  }

  private void printJavaStruct(Scratch scratch, String baseJavaName) throws IOException {
    mJoust.startClass(Modifier.PUBLIC, baseJavaName, null);
    Set seenFieldNames = new HashSet();
    // Write out the special "_value" property, if needed
    SimpleContentProperty scprop = scratch.getSimpleContentProperty();
    if (scprop != null) {
      String fieldName = pickUniqueFieldName(scprop.getGetterName().getSimpleName(),
        seenFieldNames);
      JavaTypeName jType = scprop.getTypeName().getJavaName();
      addJavaBeanProperty(fieldName, jType.toString(),
        scprop.getGetterName().getSimpleName(), scprop.getSetterName().getSimpleName());
    }

    // Write out the Generic Xml properties, if needed
    GenericXmlProperty gxprop = scratch.getAnyElementProperty();
    if (gxprop != null) {
      String fieldName = pickUniqueFieldName(gxprop.getGetterName().getSimpleName(),
        seenFieldNames);
      JavaTypeName jType = gxprop.getTypeName().getJavaName();
      addJavaBeanProperty(fieldName, jType.toString(),
        gxprop.getGetterName().getSimpleName(), gxprop.getSetterName().getSimpleName());
    }
    gxprop = scratch.getAnyAttributeProperty();
    if (gxprop != null) {
      String fieldName = pickUniqueFieldName(gxprop.getGetterName().getSimpleName(),
        seenFieldNames);
      JavaTypeName jType = gxprop.getTypeName().getJavaName();
      addJavaBeanProperty(fieldName, jType.toString(),
        gxprop.getGetterName().getSimpleName(), gxprop.getSetterName().getSimpleName());
    }

    Collection props = scratch.getQNameProperties();
    Map fieldNames = new HashMap();

    // pick field names
    for (Iterator i = props.iterator(); i.hasNext();) {
      QNameProperty prop = (QNameProperty) i.next();
      fieldNames.put(prop, pickUniqueFieldName(prop.getGetterName().getSimpleName(),
                                               seenFieldNames));
    }

    // print fields, getters, and setters
    for (Iterator i = props.iterator(); i.hasNext();) {
      QNameProperty prop = (QNameProperty) i.next();
      JavaTypeName jType = prop.getTypeName().getJavaName();

      if (prop.getCollectionClass() != null) {
        jType = prop.getCollectionClass();
      }
      String fieldName = (String) fieldNames.get(prop);
      addJavaBeanProperty(fieldName, jType.toString(),
        prop.getGetterName().getSimpleName(), prop.getSetterName().getSimpleName());
    }
    mJoust.endClassOrInterface();
  }

  private void addJavaBeanProperty(String name, String type, String getter, String setter)
    throws IOException {
    // declare the field
    Variable propertyField =
            mJoust.writeField(Modifier.PRIVATE,
                              type,
                              name,
                              null);
    //write getter
    mJoust.startMethod(Modifier.PUBLIC,
                       type,
                       getter,
                       null, null, null);
    mJoust.writeReturnStatement(propertyField);
    mJoust.endMethodOrConstructor();
    //write setter
    Variable[] params = mJoust.startMethod(Modifier.PUBLIC,
                                           "void",
                                           setter,
                                           new String[]{type},
                                           new String[]{name},
                                           null);
    mJoust.writeAssignmentStatement(propertyField, params[0]);
    mJoust.endMethodOrConstructor();
  }

  private void printJavaEnum(Scratch scratch, String baseJavaName) throws IOException {
    Set seenFieldNames = new HashSet();
    XmlAnySimpleType[] enumValues = scratch.getSchemaType().getEnumerationValues();
    JaxrpcEnumType enumType = (JaxrpcEnumType) scratch.getBindingType();
    JavaTypeName baseType = enumType.getBaseTypeName().getJavaName();
    EnumerationPrintHelper enumHelper =
            new EnumerationPrintHelper(baseType, mJoust.getExpressionFactory(),
                    scratch.getSchemaType());

    // figure out what import statements we need
    boolean useArrays = enumHelper.isArray() || enumHelper.isBinary();
    if (useArrays) {
      mJoust.writeImportStatement("java.util.Arrays");
    }
    mJoust.writeImportStatement("java.util.HashMap");
    mJoust.writeImportStatement("java.util.Map");
    if (enumHelper.isArray()) {
      mJoust.writeImportStatement("java.util.StringTokenizer");
    }
    mJoust.writeImportStatement("org.apache.xmlbeans.impl.util.XsTypeConverter");
    mJoust.startClass(Modifier.PUBLIC, baseJavaName, null);

    // Assign appropriate names to the fields we use
    boolean matchOk = true;
    String[] fieldNames = new String[enumValues.length];
    String instanceVarName = "value";
    String instanceMapName = "map";
    boolean isQName = enumValues.length > 0 && enumValues[0] instanceof XmlQName;
    for (int i = 0; i < enumValues.length; i++) {
      String tentativeName = NameUtil.lowerCamelCase(isQName ?
              ((XmlQName) enumValues[i]).getQNameValue().getLocalPart() :
              enumValues[i].getStringValue(),
              true, false);
      if (!NameUtil.isValidJavaIdentifier(tentativeName)) {
        matchOk = false;
        break;
      }
      if (seenFieldNames.contains(tentativeName)) {
        matchOk = false;
        break;
      }
      // If we got here, we found a suitable name for this constant
      seenFieldNames.add(tentativeName);
      fieldNames[i] = tentativeName;
    }
    if (!matchOk) {
      // One or more values could not map to a valid Java identifier
      // As per JAX-RPC, rename all identifiers to 'value1', 'value2', etc
      for (int i = 0; i < enumValues.length; i++) {
        String name = "value" + (i + 1);
        fieldNames[i] = name;
      }
    }
    else {
      // Check if the instance var name collides with any of the constants
      while (seenFieldNames.contains(instanceVarName))
        instanceVarName = "x" + instanceVarName;
      // Check the map var name
      while (seenFieldNames.contains(instanceMapName))
        instanceMapName = "x" + instanceMapName;
    }

    // We have the names, generate the class!
    // ======================================
    // Private fields and constructor
    Variable instanceVar =
            mJoust.writeField(Modifier.PRIVATE,
                    baseType.toString(),
                    instanceVarName,
                    null);
    Variable instanceMap =
            mJoust.writeField(Modifier.PRIVATE | Modifier.STATIC,
                    "Map",
                    instanceMapName,
                    mJoust.getExpressionFactory().createVerbatim("new HashMap()"));
    Variable[] params = mJoust.startConstructor(Modifier.PROTECTED,
            new String[] {baseType.toString()},
            new String[] {"value"},
            null);
    mJoust.writeAssignmentStatement(instanceVar, params[0]);
    mJoust.endMethodOrConstructor();
    Variable[] constants = new Variable[enumValues.length];

    // Constants of the enumeration base type
    for (int i = 0; i < enumValues.length; i++) {
      XmlAnySimpleType enumValue = enumValues[i];
      constants[i] =
              mJoust.writeField(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
                      baseType.toString(),
                      "_" + fieldNames[i],
                      enumHelper.getInitExpr(enumValue));
    }

    // Constants of enumeration type
    String shortClassName = scratch.getJavaName().getShortClassName();
    for (int i = 0; i < enumValues.length; i++) {
      mJoust.writeField(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
              shortClassName,
              fieldNames[i],
              mJoust.getExpressionFactory().createVerbatim("new " + shortClassName + "(_" + fieldNames[i] + ")"));
    }

    // Implementation of the getValue() method
    mJoust.startMethod(Modifier.PUBLIC,
            baseType.toString(),
            enumType.getGetValueMethod().getSimpleName(),
            null, null, null);
    mJoust.writeReturnStatement(instanceVar);
    mJoust.endMethodOrConstructor();

    // Implementation of the fromValue() method
    mJoust.startMethod(Modifier.PUBLIC | Modifier.STATIC,
            shortClassName,
            enumType.getFromValueMethod().getSimpleName(),
            new String[] {baseType.toString()},
            new String[] {"value"},
            null);
    String valueVarName = "value";
    if (useArrays) {
      mJoust.writeStatement(shortClassName + " new" + valueVarName + " = new " +
        shortClassName + "(" + valueVarName + ")");
      valueVarName = "new" + valueVarName;
    }
    mJoust.writeStatement("if (" + instanceMapName + ".containsKey(" +
            (useArrays ? valueVarName : enumHelper.getObjectVersion("value")) +
            ")) return (" + shortClassName + ") " +
            instanceMapName + ".get(" +
            (useArrays ? valueVarName : enumHelper.getObjectVersion("value"))
            + ")");
    mJoust.writeStatement("else throw new IllegalArgumentException()");
    mJoust.endMethodOrConstructor();

    // Implementation of the fromString() method
    params = mJoust.startMethod(Modifier.PUBLIC | Modifier.STATIC,
            shortClassName,
            enumType.getFromStringMethod().getSimpleName(),
            new String[] {"String"},
            new String[] {"value"},
            null);
    if (enumHelper.isArray()) {
      // Here we have to tokenize the input string, then build up an array
      // of the enumeration's type and then call fromValue()
      final String STRING_PARTS = "parts";
      final String BASETYPE_ARRAY = "array";
      mJoust.writeStatement("String[] " + STRING_PARTS + "= org.apache.xmlbeans.impl.values.XmlListImpl.split_list(value)");
      mJoust.writeStatement(baseType.toString() + " " + BASETYPE_ARRAY + " = new " +
        baseType.getArrayItemType(1).toString() + "[" + STRING_PARTS + ".length" + "]");
      mJoust.writeStatement("for (int i = 0; i < " + BASETYPE_ARRAY + ".length; i++) " +
        BASETYPE_ARRAY + "[i] = " +
        enumHelper.getFromStringExpr(mJoust.getExpressionFactory().createVerbatim(
                 STRING_PARTS + "[i]")).getMemento().toString());
      mJoust.writeReturnStatement(mJoust.getExpressionFactory().createVerbatim(
            enumType.getFromValueMethod().getSimpleName() + "(" + BASETYPE_ARRAY + ")"));
    }
    else
    {
      mJoust.writeReturnStatement(mJoust.getExpressionFactory().createVerbatim(
            enumType.getFromValueMethod().getSimpleName() + "(" +
            enumHelper.getFromStringExpr(params[0]).getMemento().toString() +
            ")"));
    }
    mJoust.endMethodOrConstructor();

    // Implementation of the toXml() method
    mJoust.startMethod(Modifier.PUBLIC,
            "String",
            enumType.getToXMLMethod().getSimpleName(),
            null, null, null);
    if (enumHelper.isArray()) {
      final String STRING_LIST = "list";
      mJoust.writeStatement("StringBuffer " + STRING_LIST + " = new StringBuffer()");
      mJoust.writeStatement("for (int i = 0; i < " + instanceVarName + ".length; i++) " +
        STRING_LIST + ".append(" + enumHelper.getToXmlString(instanceVar, "i") +
        ").append(' ')");
      mJoust.writeReturnStatement(mJoust.getExpressionFactory().createVerbatim(STRING_LIST + ".toString()"));
    }
    else {
      mJoust.writeReturnStatement(enumHelper.getToXmlExpr(instanceVar));
    }
    mJoust.endMethodOrConstructor();

    // Implementation of the toString() method
    mJoust.startMethod(Modifier.PUBLIC,
            "String",
            "toString",
            null, null, null);
    if (enumHelper.isArray()) {
      final String STRING_LIST = "list";
      mJoust.writeStatement("StringBuffer " + STRING_LIST + " = new StringBuffer()");
      mJoust.writeStatement("for (int i = 0; i < " + instanceVarName + ".length; i++) " +
        STRING_LIST + ".append(String.valueOf(" +
        instanceVarName + "[i]" +
        ")).append(' ')");
      mJoust.writeReturnStatement(mJoust.getExpressionFactory().createVerbatim(STRING_LIST + ".toString()"));
    }
    else {
      mJoust.writeReturnStatement(mJoust.getExpressionFactory().createVerbatim(
            "String.valueOf(" + instanceVarName + ")"));
    }
    mJoust.endMethodOrConstructor();

    // Implementation of the equals() method
    params = mJoust.startMethod(Modifier.PUBLIC,
            "boolean",
            "equals",
            new String[] {"Object"},
            new String[] {"obj"},
            null);
    mJoust.writeStatement("if (this == obj) return true");
    mJoust.writeStatement("if (!(obj instanceof " + shortClassName + ")) return false");
    mJoust.writeStatement("final " + shortClassName + " x = (" + shortClassName + ") obj");
    if (enumHelper.isArray() && enumHelper.isBinary()) {
      mJoust.writeStatement("if (x." + instanceVarName + ".length != " + instanceVarName +
        ".length) return false");
      mJoust.writeStatement("boolean b = true");
      mJoust.writeStatement("for (int i = 0; i < " + instanceVarName + ".length && b; i++) " +
        "b &= Arrays.equals(x." + instanceVarName + "[i], " + instanceVarName + "[i])");
      mJoust.writeStatement("return b");
    }
    else if (enumHelper.isArray())
      mJoust.writeStatement("if (Arrays.equals(x." + instanceVarName + ", " + instanceVarName + ")) return true");
    else
      mJoust.writeStatement("if (" + enumHelper.getEquals("x." + instanceVarName, instanceVarName) + ") return true");
    mJoust.writeReturnStatement(mJoust.getExpressionFactory().createBoolean(false));
    mJoust.endMethodOrConstructor();

    // Implementation of the hashCode() method
    mJoust.startMethod(Modifier.PUBLIC,
            "int",
            "hashCode",
            null, null, null);
    if (enumHelper.isArray()) {
      mJoust.writeStatement("int val = 0;");
      mJoust.writeStatement("for (int i = 0; i < " + instanceVarName + ".length; i++) " +
        "{ val *= 19; val += " +
        enumHelper.getHashCode(instanceVarName + "[i]").getMemento().toString() +
        "; }");
      mJoust.writeStatement("return val");
    }
    else
      mJoust.writeReturnStatement(enumHelper.getHashCode(instanceVarName));
    mJoust.endMethodOrConstructor();

    // Static class code
    mJoust.startStaticInitializer();
    for (int i = 0; i < fieldNames.length; i++) {
      String fieldName = fieldNames[i];
      if (useArrays)
        mJoust.writeStatement(instanceMapName + ".put(" +
              fieldName + ", " + fieldName + ")");
      else
        mJoust.writeStatement(instanceMapName + ".put(" +
              enumHelper.getObjectVersion("_" + fieldName) + ", " +
              fieldName + ")");
    }
    mJoust.endMethodOrConstructor();
    mJoust.endClassOrInterface();
  }

  private String pickUniqueFieldName(String getter, Set seenNames) {
    String baseName;

    if (getter.length() > 3 && getter.startsWith("get"))
      baseName = Character.toLowerCase(getter.charAt(3)) + getter.substring(4);
    else
      baseName = "field";

    String fieldName = baseName;
    if (!NameUtil.isValidJavaIdentifier(fieldName))
      fieldName = "_" + fieldName;
    for (int i = 1; seenNames.contains(fieldName); i += 1)
      fieldName = baseName + i;

    seenNames.add(fieldName);
    return fieldName;
  }


  // ========================================================================
  // main method - for quick debugging

  public static void main(String[] schemas) {
    try {
      File[] schemaFiles = new File[schemas.length];
      for (int i = 0; i < schemas.length; i++) schemaFiles[i] = new File(schemas[i]);
      XmlObject[] xsds = new XmlObject[schemas.length];
      for (int i = 0; i < xsds.length; i++) {
        xsds[i] = SchemaDocument.Factory.parse(new File(schemas[i]));
      }
      SchemaTypeSystem sts =
              XmlBeans.compileXsd(xsds,
                                  XmlBeans.getBuiltinTypeSystem(),
                                  null);
      Schema2Java s2j = new Schema2Java(sts);
      s2j.setVerbose(true);
      s2j.setJaxRpcRules(true);
      TylarWriter tw = new DebugTylarWriter();
      s2j.bind(tw);
      tw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.err.flush();
    System.out.flush();
  }
}
