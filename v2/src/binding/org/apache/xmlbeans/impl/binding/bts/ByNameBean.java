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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A by-name binding is one that connects XML and Java based on the
 * QNames of XML elements and attributes, rather than by sequencing
 * or particle trees.
 */
public class ByNameBean extends BindingType
{
  private static final long serialVersionUID = 1L;


  // ========================================================================
  // Variables

  private List props = new ArrayList(); // of QNameProperties
  private Map eltProps = new HashMap(); // QName -> prop (elts)
  private Map attProps = new HashMap(); // QName -> prop (attrs)
  private GenericXmlProperty anyElement;
  private GenericXmlProperty anyAttribute;

  // ========================================================================
  // Constructors

  public ByNameBean()
  {
  }

  public ByNameBean(BindingTypeName btName)
  {
    super(btName);
  }

  // ========================================================================
  // Public methods

  public GenericXmlProperty getAnyElementProperty()
  {
    return anyElement;
  }

  public void setAnyElementProperty(GenericXmlProperty prop)
  {
    anyElement = prop;
  }

  public GenericXmlProperty getAnyAttributeProperty()
  {
    return anyAttribute;
  }

  public void setAnyAttributeProperty(GenericXmlProperty prop)
  {
    anyAttribute = prop;
  }

  /**
   * Returns an unmodifiable collection of QNameProperty objects.
   */
  public Collection getProperties()
  {
    return Collections.unmodifiableCollection(props);
  }

  /**
   * Looks up a property by attribute name, null if no match.
   */
  public QNameProperty getPropertyForAttribute(QName name)
  {
    return (QNameProperty) attProps.get(name);
  }

  /**
   * Looks up a property by element name, null if no match.
   */
  public QNameProperty getPropertyForElement(QName name)
  {
    return (QNameProperty) eltProps.get(name);
  }

  /**
   * Adds a new property
   */
  public void addProperty(QNameProperty newProp)
  {
    if (hasProperty(newProp))
      throw new IllegalArgumentException("duplicate property: " + newProp.getQName() + " in type " + this);

    props.add(newProp);
    if (newProp.isAttribute())
      attProps.put(newProp.getQName(), newProp);
    else
      eltProps.put(newProp.getQName(), newProp);
  }

  public boolean hasProperty(QNameProperty newProp)
  {
    final QName prop_qname = newProp.getQName();
    return newProp.isAttribute() ? attProps.containsKey(prop_qname) :
      eltProps.containsKey(prop_qname);
  }

  // ========================================================================
  // BindingType implementation

  public void accept(BindingTypeVisitor visitor) throws XmlException
  {
    visitor.visit(this);
  }

    public String toString()
    {
        return "org.apache.xmlbeans.impl.binding.bts.ByNameBean{" +
            super.toString() + "-" +
            "props=" + (props == null ? null : "size:" + props.size() + props) +
            "}";
    }


}
