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

package org.apache.xmlbeans.impl.jam.internal.reflect;


import java.util.Collection;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.BaseJElement;

/**
 * <p>Reflection-backed implementation of JParameter.  Note that
 * reflection does not provide a first-class representation of method
 * parameters - this representation is synthesized from the
 * information available to us in the Method or Constructor.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
/*package*/ final class RParameter extends BaseJElement
	      implements JParameter 
{

  // ========================================================================
  // Factory
  
  public static final JParameter[] createParameters
    (Class[] types, JElement parent, JClassLoader loader) 
  {
    JParameter[] out = new JParameter[types.length];
    //REVIEW maybe we want to expose an interface to make the naming
    //rule pluggable?
    for(int i=0; i<out.length; i++) {
      out[i] = new RParameter("param"+i,
			      RClassLoader.getClassFor(types[i],loader),
			      parent);
    }
    return out;
  }

  // ========================================================================
  // Variables

  private JElement mParent;
  private String mName;
  private JClass mType;

  // ========================================================================
  // Constructors
  
  private RParameter(String name,
		     JClass type, 
		     JElement parent) {
    mName = name;
    mType = type;
    mParent = parent;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() { return mParent; }

  public JElement[] getChildren() { return null; }
  
  public String getSimpleName() { return mName; }

  public String getQualifiedName() { return mName; } //FIXME

  public JSourcePosition getSourcePosition() { return null; }

  // ========================================================================
  // BaseJElement implementation

  /**
   * We can't implement this until JSR175 is here.
   */
  protected void getLocalAnnotations(Collection out) {}

  /**
   * We can't ever implement this.
   */
  protected void getLocalComments(Collection out) {}

  // ========================================================================
  // JParameter implementation

  public JClass getType() { return mType; }
}
