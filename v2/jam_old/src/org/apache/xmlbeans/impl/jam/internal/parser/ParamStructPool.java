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
package org.apache.xmlbeans.impl.jam.internal.parser;

import org.apache.xmlbeans.impl.jam.editable.EInvokable;
import org.apache.xmlbeans.impl.jam.editable.EParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Utility class which the parser uses to store a list of method
 * or constructor parameters during lookahead.  The structures
 * get reused for efficiency.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ParamStructPool {

  // ========================================================================
  // Constants

  private static final boolean VERBOSE = true;

  // ========================================================================
  // Variables

  private List mList = new ArrayList();
  private int mLength = 0;

  // ========================================================================
  // Public methods

  public void setParametersOn(EInvokable e) {
    for(int i=0; i<mLength; i++) {
      ParamStruct struct = (ParamStruct)mList.get(i);
      struct.createParameter(e);
    }
  }

  public void add(String type, String name) {
    mLength++;
    if (mLength >= mList.size()) {
      mList.add(new ParamStruct(type,name));
    } else {
      ((ParamStruct)mList.get(mLength)).init(type,name);
    }
  }

  public void clear() {
    mLength = 0;
  }

}
