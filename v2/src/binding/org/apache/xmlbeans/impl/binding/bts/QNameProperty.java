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

import javax.xml.namespace.QName;

/**
 * A property that addresses an XML element or attribute by name
 * rather than by position.
 */
public class QNameProperty
  extends BindingProperty
{

  // ========================================================================
  // Variables

  private QName theName;
  private boolean isAttribute;
  private boolean isMultiple;
  private boolean isOptional;
  private boolean isNillable;
  private String defaultValue;

  private static final long serialVersionUID = 1L;


  // ========================================================================
  // Constructors

  public QNameProperty()
  {
    super();
  }

  // ========================================================================
  // Public methods

  public QName getQName()
  {
    return theName;
  }

  public void setQName(QName theName)
  {
    this.theName = theName;
  }

  public boolean isAttribute()
  {
    return isAttribute;
  }

  public void setAttribute(boolean attribute)
  {
    isAttribute = attribute;
  }

  public boolean isMultiple()
  {
    return isMultiple;
  }

  public void setMultiple(boolean multiple)
  {
    isMultiple = multiple;
  }

  public boolean isOptional()
  {
    return isOptional;
  }

  public void setOptional(boolean optional)
  {
    isOptional = optional;
  }

  public boolean isNillable()
  {
    return isNillable;
  }

  public void setNillable(boolean nillable)
  {
    isNillable = nillable;
  }

  public String getDefault()
  {
    return defaultValue;
  }

  public void setDefault(String default_value)
  {
    defaultValue = default_value;
  }

  // ========================================================================
  // BindingType implementation


}
