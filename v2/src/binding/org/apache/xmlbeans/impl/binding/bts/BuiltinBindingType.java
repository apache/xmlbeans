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
import org.apache.xmlbeans.XmlException;

/**
 * A "builtin" binding type is one that doesn't explicitly specify
 * how the conversion works because it is assumed that the runtime
 * has builtin knowledge of how to make it happen.  Instances should
 * only be created by BuiltinBindingLoader.
 */
public class BuiltinBindingType extends BindingType {

  // ========================================================================
  // Constructors

  // note: only this one constructor; builtin binding types can't be loaded
  public BuiltinBindingType(BindingTypeName btName) {
    super(btName);
  }

    public void accept(BindingTypeVisitor visitor)
        throws XmlException
    {
        visitor.visit(this);
    }
}
