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
 * A binding of a simple user-defined type that operates by
 * delegating to another well-known (e.g., builtin) binding.
 */
public class ListArrayType extends BindingType
{

  // ========================================================================
  // Variables

  private BindingTypeName itemType;

  private static final long serialVersionUID = 1L;



  // ========================================================================
  // Constructors

  public ListArrayType()
  {
  }

  public ListArrayType(BindingTypeName btName)
  {
    super(btName);
  }


  public void accept(BindingTypeVisitor visitor)
    throws XmlException
  {
    visitor.visit(this);
  }


  // ========================================================================
  // Public methods

  public BindingTypeName getItemType()
  {
    return itemType;
  }

  public void setItemType(BindingTypeName itemType)
  {
    this.itemType = itemType;
  }
}
