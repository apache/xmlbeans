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
import org.apache.xmlbeans.impl.common.XmlWhitespace;

/**
 * A binding of a simple user-defined type that operates by
 * delegating to another well-known (e.g., builtin) binding.
 */
public class SimpleBindingType extends BindingType
{

  // ========================================================================
  // Variables

  private XmlTypeName asIfXmlType;
  private int whitespace = XmlWhitespace.WS_UNSPECIFIED;

  private static final long serialVersionUID = 1L;


  // ========================================================================
  // Constructors

  public SimpleBindingType()
  {
  }

  public SimpleBindingType(BindingTypeName btName)
  {
    super(btName);
  }

  public void accept(BindingTypeVisitor visitor) throws XmlException
  {
    visitor.visit(this);
  }

  // ========================================================================
  // Public methods

  // typically the "as if" type is the closest base builtin type.
  public XmlTypeName getAsIfXmlType()
  {
    return asIfXmlType;
  }

  public void setAsIfXmlType(XmlTypeName asIfXmlType)
  {
    this.asIfXmlType = asIfXmlType;
  }

  // question: do we want an "as if Java type" as well?

  public BindingTypeName getAsIfBindingTypeName()
  {
    if (getAsIfXmlType() == null) {
      throw new IllegalStateException("SimpleBindingType must have" +
                                      " an asIfXmlType " + this);
    }
    return BindingTypeName.forPair(getName().getJavaName(), getAsIfXmlType());
  }


  /**
   * Gets whitespace facet -- use the constants from
   * org.apache.xmlbeans.impl.common.XmlWhitespace
   *
   * @return whitespace constant from XmlWhitespace
   */
  public int getWhitespace()
  {
    return whitespace;
  }

  /**
   * Sets whitespace facet -- use the constants from
   * org.apache.xmlbeans.impl.common.XmlWhitespace
   *
   * @param ws  whitespace constant from XmlWhitespace
   */
  public void setWhitespace(int ws)
  {
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
