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
 * A property that addresses an XML element or attribute by name
 * rather than by position.
 */
public class ParentInstanceFactory
  extends JavaInstanceFactory
{

  // ========================================================================
  // Variables
  private MethodName createObjectMethod;

  private static final long serialVersionUID = 1L;



  // ========================================================================
  // Constructors

  public ParentInstanceFactory()
  {
    super();
  }

  public ParentInstanceFactory(MethodName factoryMethod)
  {
    super();
    setCreateObjectMethod(factoryMethod);
  }

  // ========================================================================
  // Public methods
  public MethodName getCreateObjectMethod()
  {
    return createObjectMethod;
  }

  public void setCreateObjectMethod(MethodName createObjectMethod)
  {
    this.createObjectMethod = createObjectMethod;
  }

  // ========================================================================
  // BindingType implementation

}
