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

import org.apache.xmlbeans.XmlException;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simpel-content binding is one that connects XML and Java based on the
 * QNames of XML attributes, and a special property to represent
 * the simple content of a complexType who's content type is a simple type.
 */
public class SimpleContentBean extends BindingType
{

  // ========================================================================
  // Variables

  private SimpleContentProperty simpleContentProperty;
  private GenericXmlProperty anyAttributeProperty;
  private Map attProps = new LinkedHashMap(); // QName -> prop (attrs)

  private static final long serialVersionUID = 1L;



  // ========================================================================
  // Constructors

  public SimpleContentBean()
  {
  }

  public SimpleContentBean(BindingTypeName btName)
  {
    super(btName);
  }

  // ========================================================================
  // Public methods

  /**
   * Looks up a property by attribute name, null if no match.
   */
  public QNameProperty getPropertyForAttribute(QName name)
  {
    return (QNameProperty) attProps.get(name);
  }

  public Collection getAttributeProperties()
  {
    return Collections.unmodifiableCollection(attProps.values());
  }

  public SimpleContentProperty getSimpleContentProperty()
  {
    return simpleContentProperty;
  }

  public void setSimpleContentProperty(SimpleContentProperty simpleContentProperty)
  {
    this.simpleContentProperty = simpleContentProperty;
  }

  public GenericXmlProperty getAnyAttributeProperty()
  {
    return anyAttributeProperty;
  }

  public void setAnyAttributeProperty(GenericXmlProperty prop)
  {
    anyAttributeProperty = prop;
  }


  /**
   * Adds a new property
   */
  public void addProperty(QNameProperty newProp)
  {
    final boolean att = newProp.isAttribute();
    if (!att) {
      final String msg = "property must be an attribute: " + newProp;
      throw new IllegalArgumentException(msg);
    }
    if (attProps.containsKey(newProp.getQName()))
      throw new IllegalArgumentException("duplicate property: " + newProp);

    attProps.put(newProp.getQName(), newProp);
  }

  // ========================================================================
  // BindingType implementation

  public void accept(BindingTypeVisitor visitor) throws XmlException
  {
    visitor.visit(this);
  }


}
