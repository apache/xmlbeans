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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;

/**
 * Represents a Java+XML component and a rule for getting between
 * them.
 */
public abstract class BindingType {

  // ========================================================================
  // Variables

  private final BindingTypeName btName;

  // ========================================================================
  // Constructors

  /**
   * This kind of constructor is used when making a new one out of the blue.
   *
   * Subclasses should call super(..) when defining constructors that init new BindingTypes.
   */
  protected BindingType(BindingTypeName btName) {
    this.btName = btName;
  }

  /**
   * This constructor loads an instance from an XML file
   *
   * Subclasses should have ctors of the same signature and call super(..) first.
   */
  protected BindingType(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    this.btName = BindingTypeName.forPair(
            JavaTypeName.forString(node.getJavatype()),
            XmlTypeName.forString(node.getXmlcomponent()));
  }


  // ========================================================================
  // Protected methods

  /**
   * This function copies an instance back out to the relevant part of the XML file.
   *
   * Subclasses should override and call super.write first.
   */
  protected org.apache.xml.xmlbeans.bindingConfig.BindingType write
          (org.apache.xml.xmlbeans.bindingConfig.BindingType node)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(kinds.typeForClass(this.getClass()));
    node.setJavatype(btName.getJavaName().toString());
    node.setXmlcomponent(btName.getXmlName().toString());
    return node;
  }

  // ========================================================================
  // Public methods

  public final BindingTypeName getName() {
    return btName;
  }

  public abstract void accept(BindingTypeVisitor visitor)
    throws XmlException;

  // ========================================================================
  // Static initialization

  /* REGISTRY OF SUBCLASSES */

  private static final Class[] ctorArgs = new Class[]{org.apache.xml.xmlbeans.bindingConfig.BindingType.class};

  public static BindingType loadFromBindingTypeNode(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    try {
      Class clazz = kinds.classForType(node.schemaType());
      return (BindingType) clazz.getConstructor(ctorArgs).newInstance(new Object[]{node});
    } catch (Exception e) {
      throw (IllegalStateException) new IllegalStateException("Cannot load class for " + node.schemaType() + ": should be registered.").initCause(e);
    }
  }

  /**
   * Should only be called by BindingFile, when loading up bindingtypes
   */
  static KindRegistry kinds = new KindRegistry();

  public static void registerClassAndType(Class clazz, SchemaType type) {
    if (!BindingType.class.isAssignableFrom(clazz))
      throw new IllegalArgumentException("Classes must inherit from BindingType");
    if (!org.apache.xml.xmlbeans.bindingConfig.BindingType.type.isAssignableFrom(type))
      throw new IllegalArgumentException("Schema types must inherit from binding-type");
    kinds.registerClassAndType(clazz, type);
  }

  static {
    registerClassAndType(JaxbBean.class, org.apache.xml.xmlbeans.bindingConfig.JaxbBean.type);
    registerClassAndType(ByNameBean.class, org.apache.xml.xmlbeans.bindingConfig.ByNameBean.type);
    registerClassAndType(WrappedArrayType.class, org.apache.xml.xmlbeans.bindingConfig.WrappedArray.type);
    registerClassAndType(SimpleBindingType.class, org.apache.xml.xmlbeans.bindingConfig.SimpleType.type);
    registerClassAndType(SimpleDocumentBinding.class, org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding.type);
  }

  // ========================================================================
  // Object implementation

  public String toString() {
    return getClass().getName() + "[" + btName.getJavaName() + "; " + btName.getXmlName() + "]";
  }
}
