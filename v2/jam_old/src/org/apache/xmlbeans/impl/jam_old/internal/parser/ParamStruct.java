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
package org.apache.xmlbeans.impl.jam_old.internal.parser;

import org.apache.xmlbeans.impl.jam_old.editable.EInvokable;
import org.apache.xmlbeans.impl.jam_old.editable.EParameter;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
/*package*/ class ParamStruct {

  // ========================================================================
  // Variables

  private String mName;
  private String mType;

  // ========================================================================
  // Constructors

  public ParamStruct(String type, String name) {
    init(type,name);
  }

  // ========================================================================
  // Public methods

  public void init(String type, String name) {
    mType = type;
    mName = name;
  }

  public EParameter createParameter(EInvokable e) {
    if (e == null) throw new IllegalArgumentException("null invokable");
    EParameter param = e.addNewParameter();
    param.setSimpleName(mName);
    param.setUnqualifiedType(mType);
    return param;
  }

}
