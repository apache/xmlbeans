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

package org.apache.xmlbeans.impl.jam.internal.javadoc;

import com.sun.javadoc.FieldDoc;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JField;

/**
 * javadoc-backed implementation of JClass.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDField extends JDMember implements JField {

  // ========================================================================
  // Variables

  private FieldDoc mField;

  // ========================================================================
  // Constructors
  
  public JDField(FieldDoc f, JClassLoader loader) {
    super(f,loader);
    mField = f;
  }

  // ========================================================================
  // JField implementation

  public JClass getType() { 
    return JDClassLoader.getClassFor(mField.type(),mLoader); 
  }

  public boolean isTransient() { return mField.isTransient(); }

  public boolean isVolatile() { return mField.isVolatile(); }

}
