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
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.BindingType;

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

  // ========================================================================
  // Constructors

  public ByNameBean(BindingTypeName btName) {
    super(btName);
  }

  public ByNameBean(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    super(node);

    org.apache.xml.xmlbeans.bindingConfig.QnameProperty[] propArray =
            ((org.apache.xml.xmlbeans.bindingConfig.ByNameBean) node).getQnamePropertyArray();

    for (int i = 0; i < propArray.length; i++) {
      addProperty((QNameProperty) BindingProperty.forNode(propArray[i]));
    }
  }

  // ========================================================================
  // Public methods

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
      throw new IllegalArgumentException();

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

    for (Iterator i = props.iterator(); i.hasNext();) {
      QNameProperty qProp = (QNameProperty) i.next();
      org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode = bnNode.addNewQnameProperty();
      qProp.write(qpNode);
    }
    return bnNode;
  }


}
