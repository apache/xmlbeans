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

import java.io.Serializable;

/**
 * Represents a Java+XML component and a rule for getting between
 * them.
 */
public abstract class BindingType
  implements Serializable
{


  // ========================================================================
  // Variables

  private BindingTypeName name;

  private static final long serialVersionUID = 1L;


  // ========================================================================
  // Constructors

  /**
   * This kind of constructor is used when making a new one out of the blue.
   *
   * Subclasses should call super(..) when defining constructors that init new BindingTypes.
   */
  protected BindingType()
  {
  }

  /**
   * This kind of constructor is used when making a new one out of the blue.
   *
   * Subclasses should call super(..) when defining constructors that init new BindingTypes.
   */
  protected BindingType(BindingTypeName btName)
  {
    setName(btName);
  }

  // ========================================================================
  // Public methods

  public final BindingTypeName getName()
  {
    return name;
  }

  public final void setName(BindingTypeName name)
  {
    this.name = name;
  }

  public abstract void accept(BindingTypeVisitor visitor)
    throws XmlException;


  // ========================================================================
  // Object implementation

  public String toString()
  {
    return getClass().getName() + "[" +
      getName().getJavaName() + "; " +
      getName().getXmlName() + "]";
  }
}
