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

/**
 * BindingType for root elements.
 */
public class SimpleDocumentBinding extends BindingType {

  // ========================================================================
  // Variables

  private XmlTypeName typeOfElement;

  // ========================================================================
  // Constructors

  public SimpleDocumentBinding(BindingTypeName btname) {
    super(btname);
  }

  public SimpleDocumentBinding(org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding node) {
    super(node);
    typeOfElement = XmlTypeName.forString(node.getTypeOfElement());
  }

  public SimpleDocumentBinding(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    this((org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding) node);
  }

  // ========================================================================
  // Public methods

  public XmlTypeName getTypeOfElement() {
    return typeOfElement;
  }

  public void setTypeOfElement(XmlTypeName typeOfElement) {
    this.typeOfElement = typeOfElement;
  }

  // ========================================================================
  // BindingType implementation

  /**
   * This function copies an instance back out to the relevant part of the XML file.
   *
   * Subclasses should override and call super.write first.
   */
  protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding sdbNode =
            (org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding) super.write(node);
    sdbNode.setTypeOfElement(typeOfElement.toString());
    return sdbNode;
  }

    public void accept(BindingTypeVisitor visitor) throws XmlException
    {
        visitor.visit(this);
    }
}
