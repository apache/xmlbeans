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

package org.apache.xmlbeans.impl.jam_old.internal;

import org.apache.xmlbeans.impl.jam_old.JClass;
import org.apache.xmlbeans.impl.jam_old.JPackage;

/**
 * This is the JClass impl that is returned when a java type cannot be
 * resolved.  It has only a name.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class UnresolvedJClass extends BuiltinJClass {

  // ========================================================================
  // Variables

  private String mName;
  private JClassHelper mHelper;

  // ========================================================================
  // Constructor

  public UnresolvedJClass(String name) {
    if (name == null) throw new IllegalArgumentException("null name");
    mName = name;
    mHelper = new JClassHelper(this);
  }

  // ========================================================================
  // JClass impl

  public String getSimpleName() {
    String out = getQualifiedName();
    int lastDot = out.lastIndexOf('.');
    return (lastDot == -1) ? out : out.substring(lastDot+1);
  }    

  public String getQualifiedName() { return mName; }

  public String getFieldDescriptor() { return mName; }

  public JPackage getContainingPackage() { return null; }

  public boolean isAssignableFrom(JClass c) { 
    return mHelper.isAssignableFrom(c);
  }

  public boolean isUnresolved() { return true; }
}
