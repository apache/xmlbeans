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
      resolveJavaStructure((Scratch) i.next());
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
        // todo: what about simple content, custom codecs, etc?
        scratch = new Scratch(sType, xmlName, Scratch.ATOMIC_TYPE);
      } else if (sType.isDocumentType()) {
        scratch = new Scratch(sType, XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, sType.getDocumentElementName()), Scratch.ELEMENT);
      } else if (sType.isAttributeType()) {
        scratch = new Scratch(sType, XmlTypeName.forGlobalName(XmlTypeName.ATTRIBUTE, sType.getDocumentElementName()), Scratch.ATTRIBUTE);
      } else if (isSoapArray(sType)) {
        scratch = new Scratch(sType, xmlName, Scratch.SOAPARRAY_REF);
        xmlName = soapArrayTypeName(sType);
        scratch.setAsIf(xmlName);

        // soap arrays unroll like this
        while (xmlName.getComponentType() == XmlTypeName.SOAP_ARRAY) {
          scratch = new Scratch(null, xmlName, Scratch.SOAPARRAY);
          scratchFromXmlName.put(xmlName, scratch);
          xmlName = xmlName.getOuterComponent();
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
        {
          structureCount += 1;
          JavaTypeName javaName = pickUniqueJavaName(scratch.getSchemaType());
          scratch.setJavaName(javaName);
          scratchFromJavaNameString.put(javaName.toString(), scratch);
          return;
        }

      case Scratch.LITERALARRAY_TYPE:
        {
          SchemaType itemType = getLiteralArrayItemType(scratch.getSchemaType());
          Scratch itemScratch = scratchForSchemaType(itemType);
          resolveJavaName(itemScratch);
          scratch.setJavaName(JavaTypeName.forArray(itemScratch.getJavaName(), 1));
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
          resolveJavaName(itemScratch);
          scratch.setJavaName(JavaTypeName.forArray(itemScratch.getJavaName(), arrayName.getNumber()));
          return;
        }

      case Scratch.ELEMENT:
      case Scratch.ATTRIBUTE:
        {
          logVerbose("processing element "+scratch.getXmlName());
          SchemaType contentType = scratch.getSchemaType().getProperties()[0].getType();
          logVerbose("content type is "+contentType.getName());
          if (contentType.isPrimitiveType() /*||
                  //FIXME why is this not a primitive? gross hack to make things work for now
                  contentType.getName().toString().equals("{http://www.w3.org/2001/XMLSchema}int")*/) {
            //REVIEW this is a quick bug fix for the case where an element
            //is of a primitive type.  I'm not completely sure it is the right
            //thing to do.  pcal
            XmlTypeName xtn = XmlTypeName.forSchemaType(contentType);
            scratch.setJavaName(mLoader.lookupPojoFor(xtn).getJavaName());
            scratch.setAsIf(xtn);
            return;
          } else {
            Scratch contentScratch = scratchForSchemaType(contentType);
            logVerbose("content scratch is "+contentScratch);
            resolveJavaName(contentScratch);
            scratch.setJavaName(contentScratch.getJavaName());
            scratch.setAsIf(contentScratch.getXmlName());
            return;
          }
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
        ByNameBean byNameResult = new ByNameBean(btName);
        scratch.setBindingType(byNameResult);
        bindingFile.addBindingType(byNameResult, true, true);
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
   *
   * todo: understand how we want inheritance to work
   */
  private void resolveJavaStructure(Scratch scratch) {
    if (scratch.getCategory() != Scratch.STRUCT_TYPE)
      return;

    if (scratch.isStructureResolved())
      return;

    scratch.setStructureResolved(true);

    SchemaType baseType = scratch.getSchemaType().getBaseType();
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

    // now deal with remaining props
    SchemaProperty[] props = scratch.getSchemaType().getProperties();
    for (int i = 0; i < props.length; i++) {
      QNameProperty prop = (QNameProperty) (props[i].isAttribute() ? seenAttrProps : seenEltProps).get(props[i].getName());
      if (prop != null) {
        // already seen property: verify multiplicity looks cool
        if (prop.isMultiple() != isMultiple(props[i])) {
          // todo: signal nicer error
          throw new IllegalStateException("Can't change multiplicity");
        }

        // todo: think about optionality and nillability too
      } else {
        SchemaType sType = props[i].getType();
        BindingType bType = bindingTypeForSchemaType(sType);

        String propName = pickUniquePropertyName(props[i].getName(), seenMethodNames);
        boolean isMultiple = isMultiple(props[i]);
        JavaTypeName collection = null;
        if (isMultiple)
          collection = JavaTypeName.forArray(bType.getName().getJavaName(), 1);

        prop = new QNameProperty();
        prop.setQName(props[i].getName());
        prop.setAttribute(props[i].isAttribute());
        prop.setSetterName(MethodName.create("set" + propName,
                                             bType.getName().getJavaName()));
        prop.setGetterName(MethodName.create("get" + propName));
        prop.setCollectionClass(collection);
        prop.setBindingType(bType);
        prop.setNillable(props[i].hasNillable() != SchemaProperty.NEVER);
        prop.setOptional(isOptional(props[i]));
        prop.setMultiple(isMultiple);
      }
      scratch.addQNameProperty(prop);
    }
  }

  /**
   * Picks a property name without colliding with names of
   * previously picked getters and setters.
   */
  private String pickUniquePropertyName(QName name, Set seenMethodNames) {
    String baseName = NameUtil.upperCamelCase(name.getLocalPart());
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
   * True if the given SchemaProperty has minOccurs < 1
   */
  private static boolean isOptional(SchemaProperty prop) {
    return (prop.getMinOccurs().signum() == 0);
  }

  /**
   * Returns a collection of QNameProperties for a given schema type that
   * may either be on the mLoader or the current scratch area.
   */
  private Collection extractProperties(SchemaType sType) {
    // case 1: it's in the current area
    Scratch scratch = scratchForSchemaType(sType);
    if (scratch != null) {
      resolveJavaStructure(scratch);
      return scratch.getQNameProperties();
    }

    // case 2: it's in the mLoader
    BindingType bType = mLoader.getBindingType(mLoader.lookupPojoFor(XmlTypeName.forSchemaType(sType)));
    if (!(bType instanceof ByNameBean)) {
      return null;
    }
    Collection result = new ArrayList();
    ByNameBean bnb = (ByNameBean) bType;
    for (Iterator i = bnb.getProperties().iterator(); i.hasNext();) {
      result.add(i.next());
    }

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

    String baseName = NameUtil.getClassNameFromQName(qname);
    String pickedName = baseName;

    for (int i = 1; usedNames.contains(pickedName); i += 1)
      pickedName = baseName + i;

    usedNames.add(pickedName);

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

  /**
   * True if the given schema type is interpreted as a .NET-style
   * array.
   */
  private static boolean isLiteralArray(SchemaType sType) {
    return getLiteralArrayItemType(sType) != null;
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
    public static final int LITERALARRAY_TYPE = 3;
    public static final int SOAPARRAY_REF = 4;
    public static final int SOAPARRAY = 5;
    public static final int ELEMENT = 6;
    public static final int ATTRIBUTE = 7;

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
        if (scratch.getCategory() == Scratch.STRUCT_TYPE)
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
    assert(scratch.getCategory() == Scratch.STRUCT_TYPE);
    JavaTypeName javaName = scratch.getJavaName();

    String packageName = javaName.getPackage();
    String shortClassName = javaName.getShortClassName();
    BindingType baseType = bindingTypeForSchemaType(scratch.getSchemaType().getBaseType());
    String baseJavaname = null;
    if (baseType != null) {
      baseJavaname = baseType.getName().getJavaName().toString();
      if (baseJavaname.equals("java.lang.Object"))
        baseJavaname = null;
    }
    // begin writing class
    mJoust.startFile(packageName, shortClassName);
    mJoust.writeComment("Generated from schema type " + scratch.getXmlName());
    mJoust.startClass(Modifier.PUBLIC, baseJavaname, null);
    Collection props = scratch.getQNameProperties();
    Map fieldNames = new HashMap();
    Set seenFieldNames = new HashSet();

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
      // declare the field
      Variable propertyField =
              mJoust.writeField(Modifier.PRIVATE,
                                jType.toString(),
                                fieldName,
                                null);
      //write getter
      mJoust.startMethod(Modifier.PUBLIC,
                         jType.toString(),
                         prop.getGetterName().getSimpleName(),
                         null, null, null);
      mJoust.writeReturnStatement(propertyField);
      mJoust.endMethodOrConstructor();
      //write setter
      Variable[] params = mJoust.startMethod(Modifier.PUBLIC,
                                             "void",
                                             prop.getSetterName().getSimpleName(),
                                             new String[]{jType.toString()},
                                             new String[]{fieldName},
                                             null);
      mJoust.writeAssignmentStatement(propertyField, params[0]);
      mJoust.endMethodOrConstructor();
    }
    mJoust.endClassOrInterface();
    mJoust.endFile();
  }

  private String pickUniqueFieldName(String getter, Set seenNames) {
    String baseName;

    if (getter.length() > 3 && getter.startsWith("get"))
      baseName = Character.toLowerCase(getter.charAt(3)) + getter.substring(4);
    else
      baseName = "field";

    String fieldName = baseName;
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
