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

import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;

/**
 * A by-name binding is one that connects XML and Java based on the
 * QNames of XML elements and attributes, rather than by sequencing
 * or particle trees.
 */
public class ByNameBean extends BindingType {

  // ========================================================================
  // Variables

  private List props = new ArrayList(); // of QNameProperties
  private Map eltProps = new HashMap(); // QName -> prop (elts)
  private Map attProps = new HashMap(); // QName -> prop (attrs)
  private GenericXmlProperty anyElement;
  private GenericXmlProperty anyAttribute;

  // ========================================================================
  // Constructors

  public ByNameBean(BindingTypeName btName) {
    super(btName);
  }

  public ByNameBean(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    super(node);

    org.apache.xml.xmlbeans.bindingConfig.ByNameBean bnNode =
            (org.apache.xml.xmlbeans.bindingConfig.ByNameBean) node; 

    org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty gxp =
            bnNode.getAnyProperty();
    if (gxp != null)
      setAnyElementProperty((GenericXmlProperty) BindingProperty.forNode(gxp));

    gxp = bnNode.getAnyAttributeProperty();
    if (gxp != null)
      setAnyAttributeProperty((GenericXmlProperty) BindingProperty.forNode(gxp));

    org.apache.xml.xmlbeans.bindingConfig.QnameProperty[] propArray =
            bnNode.getQnamePropertyArray();

    for (int i = 0; i < propArray.length; i++) {
      addProperty((QNameProperty) BindingProperty.forNode(propArray[i]));
    }
  }

  // ========================================================================
  // Public methods

  public GenericXmlProperty getAnyElementProperty() {
    return anyElement;
  }

  public void setAnyElementProperty(GenericXmlProperty prop) {
    anyElement = prop;
  }

  public GenericXmlProperty getAnyAttributeProperty() {
    return anyAttribute;
  }

  public void setAnyAttributeProperty(GenericXmlProperty prop) {
    anyAttribute = prop;
  }
    
  /**
   * Returns an unmodifiable collection of QNameProperty objects.
   */
  public Collection getProperties() {
    return Collections.unmodifiableCollection(props);
  }

  /**
   * Looks up a property by attribute name, null if no match.
   */
  public QNameProperty getPropertyForAttribute(QName name) {
    return (QNameProperty) attProps.get(name);
  }

  /**
   * Looks up a property by element name, null if no match.
   */
  public QNameProperty getPropertyForElement(QName name) {
    return (QNameProperty) eltProps.get(name);
  }

  /**
   * Adds a new property
   */
  public void addProperty(QNameProperty newProp) {
    if (newProp.isAttribute() ? attProps.containsKey(newProp.getQName()) : eltProps.containsKey(newProp.getQName()))
      throw new IllegalArgumentException("duplicate property: " + newProp.getQName() + " in type " + this);

    props.add(newProp);
    if (newProp.isAttribute())
      attProps.put(newProp.getQName(), newProp);
    else
      eltProps.put(newProp.getQName(), newProp);
  }

  // ========================================================================
  // BindingType implementation

  /**
   * This function copies an instance back out to the relevant part of the XML file.
   *
   * Subclasses should override and call super.write first.
   */
  protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    org.apache.xml.xmlbeans.bindingConfig.ByNameBean bnNode =
            (org.apache.xml.xmlbeans.bindingConfig.ByNameBean) super.write(node);

    if (anyElement != null)
      anyElement.write(bnNode.addNewAnyProperty());
    if (anyAttribute != null)
      anyAttribute.write(bnNode.addNewAnyAttributeProperty());
    for (Iterator i = props.iterator(); i.hasNext();) {
      QNameProperty qProp = (QNameProperty) i.next();
      org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode = bnNode.addNewQnameProperty();
      qProp.write(qpNode);
    }
    return bnNode;
  }

    public void accept(BindingTypeVisitor visitor) throws XmlException
    {
        visitor.visit(this);
    }


}
