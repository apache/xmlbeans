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

import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.common.XmlWhitespace;

/**
 * A binding of a simple user-defined type that operates by
 * delegating to another well-known (e.g., builtin) binding.
 */
public class SimpleBindingType extends BindingType {

  // ========================================================================
  // Variables

  private XmlTypeName asIfXmlType;
  private int whitespace = XmlWhitespace.WS_UNSPECIFIED;

  // ========================================================================
  // Constructors

  public SimpleBindingType(BindingTypeName btName) {
    super(btName);
  }

  public SimpleBindingType(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    super(node);
    org.apache.xml.xmlbeans.bindingConfig.SimpleType stNode = (org.apache.xml.xmlbeans.bindingConfig.SimpleType) node;
    org.apache.xml.xmlbeans.bindingConfig.AsXmlType as_xml = stNode.getAsXml();
    asIfXmlType = XmlTypeName.forString(as_xml.getStringValue());

    if (as_xml.isSetWhitespace()) {
      org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.Enum ws =
              as_xml.getWhitespace();
      if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.PRESERVE)) {
        whitespace = XmlWhitespace.WS_PRESERVE;
      } else if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.REPLACE)) {
        whitespace = XmlWhitespace.WS_REPLACE;
      } else if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.COLLAPSE)) {
        whitespace = XmlWhitespace.WS_COLLAPSE;
      } else {
        throw new AssertionError("invalid whitespace: " + ws);
      }

    }
  }

  protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node) {
    org.apache.xml.xmlbeans.bindingConfig.SimpleType stNode = (org.apache.xml.xmlbeans.bindingConfig.SimpleType) super.write(node);

    org.apache.xml.xmlbeans.bindingConfig.AsXmlType as_if = stNode.addNewAsXml();
    as_if.setStringValue(asIfXmlType.toString());

    switch (whitespace) {
      case XmlWhitespace.WS_UNSPECIFIED:
        break;
      case XmlWhitespace.WS_PRESERVE:
        as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.PRESERVE);
        break;
      case XmlWhitespace.WS_REPLACE:
        as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.REPLACE);
        break;
      case XmlWhitespace.WS_COLLAPSE:
        as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.COLLAPSE);
        break;
      default:
        throw new AssertionError("invalid whitespace: " + whitespace);
    }


    stNode.setAsXml(as_if);
    return stNode;
  }

  // ========================================================================
  // Public methods

  // typically the "as if" type is the closest base builtin type.
  public XmlTypeName getAsIfXmlType() {
    return asIfXmlType;
  }

  public void setAsIfXmlType(XmlTypeName asIfXmlType) {
    this.asIfXmlType = asIfXmlType;
  }

  // question: do we want an "as if Java type" as well?

  public BindingTypeName getAsIfBindingTypeName() {
    return BindingTypeName.forPair(getName().getJavaName(), asIfXmlType);
  }


  /**
   * Gets whitespace facet -- use the constants from
   * org.apache.xmlbeans.impl.common.XmlWhitespace
   *
   * @return whitespace constant from XmlWhitespace
   */
  public int getWhitespace() {
    return whitespace;
  }

  /**
   * Sets whitespace facet -- use the constants from
   * org.apache.xmlbeans.impl.common.XmlWhitespace
   *
   * @param ws  whitespace constant from XmlWhitespace
   */
  public void setWhitespace(int ws) {
    switch (ws) {
      case XmlWhitespace.WS_UNSPECIFIED:
      case XmlWhitespace.WS_PRESERVE:
      case XmlWhitespace.WS_REPLACE:
      case XmlWhitespace.WS_COLLAPSE:
        whitespace = ws;
        break;
      default:
        throw new IllegalArgumentException("invalid whitespace: " + ws);
    }
  }
}
