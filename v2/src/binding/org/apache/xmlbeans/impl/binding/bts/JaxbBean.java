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

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Represents a binding that can line up properties based on either
 * name (like ByNameBean) or position (i.e., Schema Particle), as
 * required by JAXB.
 */
public class JaxbBean extends BindingType {

  // ========================================================================
  // Variables

  private Map partProps = new LinkedHashMap(); // XmlTypeName -> prop (particles)
  private Map eltProps = new LinkedHashMap(); // QName -> prop (elts)
  private Map attProps = new LinkedHashMap(); // QName -> prop (attrs)

  // ========================================================================
  // Construtors

  public JaxbBean(BindingTypeName btName) {
    super(btName);
  }

  public JaxbBean(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    super(node);

    org.apache.xml.xmlbeans.bindingConfig.JaxbBean jbNode = (org.apache.xml.xmlbeans.bindingConfig.JaxbBean) node;

    org.apache.xml.xmlbeans.bindingConfig.ParticleProperty[] ppropArray = jbNode.getParticlePropertyArray();
    for (int i = 0; i < ppropArray.length; i++) {
      addProperty(BindingProperty.forNode(ppropArray[i]));
    }

    org.apache.xml.xmlbeans.bindingConfig.QnameProperty[] qpropArray = jbNode.getQnamePropertyArray();
    for (int i = 0; i < qpropArray.length; i++) {
      addProperty(BindingProperty.forNode(qpropArray[i]));
    }
  }

  // ========================================================================
  // Public methods

  /**
   * Returns an unmodifiable collection of QNameProperty objects.
   */
  public Collection getProperties() {
    List result = new ArrayList();
    result.addAll(partProps.values());
    result.addAll(eltProps.values());
    result.addAll(attProps.values());
    return Collections.unmodifiableCollection(result);
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
  public void addProperty(BindingProperty newProp) {
    if (newProp instanceof ParticleProperty) {
      partProps.put(((ParticleProperty) newProp).getXmlName(), newProp);
    } else if (newProp instanceof QNameProperty) {
      QNameProperty qProp = (QNameProperty) newProp;
      if (qProp.isAttribute())
        attProps.put(qProp.getQName(), newProp);
      else
        eltProps.put(qProp.getQName(), newProp);
    } else {
      throw new IllegalArgumentException();
    }
  }

  // ========================================================================
  // BindingType implementation

  /**
   * This function copies an instance back out to the relevant part of the XML file.
   *
   * Subclasses should override and call super.write first.
   */
  protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    org.apache.xml.xmlbeans.bindingConfig.JaxbBean jbNode = (org.apache.xml.xmlbeans.bindingConfig.JaxbBean) super.write(node);
    for (Iterator i = getProperties().iterator(); i.hasNext();) {
      BindingProperty bProp = (BindingProperty) i.next();
      if (bProp instanceof ParticleProperty) {
        org.apache.xml.xmlbeans.bindingConfig.ParticleProperty ppNode = jbNode.addNewParticleProperty();
        bProp.write(ppNode);
      } else {
        org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode = jbNode.addNewQnameProperty();
        bProp.write(qpNode);

      }
    }
    return jbNode;
  }
}
