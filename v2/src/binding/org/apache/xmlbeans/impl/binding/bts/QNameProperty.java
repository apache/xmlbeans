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

import javax.xml.namespace.QName;

/**
 * A property that addresses an XML element or attribute by name
 * rather than by position.
 */
public class QNameProperty extends BindingProperty {

  // ========================================================================
  // Variables

  private QName theName;
  private boolean isAttribute;
  private boolean isMultiple;
  private boolean isOptional;
  private boolean isNillable;
  private String defaultValue;

  // ========================================================================
  // Constructors

  public QNameProperty() {
    super();
  }

  public QNameProperty(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node) {
    super(node);
    org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode =
            (org.apache.xml.xmlbeans.bindingConfig.QnameProperty) node;
    theName = qpNode.getQname();
    isAttribute = qpNode.getAttribute();
    isMultiple = qpNode.getMultiple();
    isNillable = qpNode.getNillable();
    isOptional = qpNode.getOptional();
    defaultValue = qpNode.getDefault();
  }

  // ========================================================================
  // Public methods

  public QName getQName() {
    return theName;
  }

  public void setQName(QName theName) {
    this.theName = theName;
  }

  public boolean isAttribute() {
    return isAttribute;
  }

  public void setAttribute(boolean attribute) {
    isAttribute = attribute;
  }

  public boolean isMultiple() {
    return isMultiple;
  }

  public void setMultiple(boolean multiple) {
    isMultiple = multiple;
  }

  public boolean isOptional() {
    return isOptional;
  }

  public void setOptional(boolean optional) {
    isOptional = optional;
  }

  public boolean isNillable() {
    return isNillable;
  }

  public void setNillable(boolean nillable) {
    isNillable = nillable;
  }

  public String getDefault() {
    return defaultValue;
  }

  public void setDefault(String default_value) {
    defaultValue = default_value;
  }

  // ========================================================================
  // BindingType implementation

  /**
   * This function copies an instance back out to the relevant part of the XML file.
   *
   * Subclasses should override and call super.write first.
   */
  protected org.apache.xml.xmlbeans.bindingConfig.BindingProperty write(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node) {
    node = super.write(node);

    org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode =
            (org.apache.xml.xmlbeans.bindingConfig.QnameProperty) node;

    qpNode.setQname(theName);
    if (isAttribute)
      qpNode.setAttribute(true);
    if (isMultiple)
      qpNode.setMultiple(true);
    if (isOptional)
      qpNode.setOptional(true);
    if (isNillable)
      qpNode.setNillable(true);
    return qpNode;
  }


}
