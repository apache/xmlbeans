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


/**
 * Represents a resource capable of providing BindingTypes, based on
 * XML and Java component names.  Implementations of this interface
 * may load from files, classpaths, etc.
 */
public interface BindingLoader {

  // ========================================================================
  // Public methods

  /**
   * Returns the BindingType having the given name.
   */
  public BindingType getBindingType(BindingTypeName btName);

  /**
   * Returns the default binding from the given xml type to a POJO (plain-old
   * java object)
   */
  public BindingTypeName lookupPojoFor(XmlTypeName xName);

  /**
   * Returns the default binding from the given xml type to a java class
   * which extends XmlObject.
   */
  public BindingTypeName lookupXmlObjectFor(XmlTypeName xName);

  /**
   * Returns the default binding from the given java type to some xml type.
   */
  public BindingTypeName lookupTypeFor(JavaTypeName jName);

  /**
   * Returns the default binding from the given java type to some xml element.
   */
  public BindingTypeName lookupElementFor(JavaTypeName jName);
}
