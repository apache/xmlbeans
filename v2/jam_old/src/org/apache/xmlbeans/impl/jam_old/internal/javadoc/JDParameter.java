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

package org.apache.xmlbeans.impl.jam_old.internal.javadoc;


import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.xmlbeans.impl.jam_old.*;
import org.apache.xmlbeans.impl.jam_old.internal.BaseJElement;

/**
 * Javadoc-backed implementation of JParameter.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDParameter extends BaseJElement implements JParameter 
{

  // ========================================================================
  // Variables

  private Parameter mParameter;
  private JDExecutableMember mParent;
  private JClassLoader mLoader;

  // ========================================================================
  // Constructors
  
  public JDParameter(Parameter p,
                     JDExecutableMember parent,
                     JClassLoader loader) {
    mParameter = p;
    mParent = parent;
    mLoader = loader;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() { return mParent; }

  public JElement[] getChildren() { return null; }
  
  public String getSimpleName() { 
    return mParameter.name();
  }

  public String getQualifiedName() { 
    return mParameter.name(); 
  }

  public JSourcePosition getSourcePosition() { 
    return mParent.getSourcePosition(); //close enough
  }

  // ========================================================================
  // BaseJElement implementation

  protected void getLocalAnnotations(Collection out) { 
    ParamTag[] tags = mParent.getMember().paramTags();
    if (tags == null || tags.length == 0) return;
    tags = getParamTagsFor(tags,mParameter);
    if (tags == null || tags.length == 0) return;
    for(int i=0; i<tags.length; i++) {
      ParamTag t = tags[i];
      JSourcePosition sp = JDFactory.getInstance().createSourcePosition(t.position());
      out.add(JDFactory.getInstance().createAnnotation(this, t.name(), t.parameterComment(), sp));
    }
  }

  // javadoc doesn't recognize @param comments, unfortunately.  FIXME
  // we could be clever and figure them out ourselves, i suppose.
  protected void getLocalComments(Collection out) {}

  // ========================================================================
  // JParameter implementation

  public JClass getType() {
    return JDClassLoader.getClassFor(mParameter.type(),mLoader);
  }

  // ========================================================================
  // Private methods

  /**
   * Given a set of ParamTags, returns those which apply to the given
   * parameter.
   */
  private ParamTag[] getParamTagsFor(ParamTag[] all, Parameter p) {
    List list = null;
    for(int i=0; i<all.length; i++) {
      if (all[i].parameterName().equals(p.name())) {
	if (list == null) list = new ArrayList();
	list.add(all[i]);
      }
    }
    if (list == null) return null;
    ParamTag[] out = new ParamTag[list.size()];
    list.toArray(out);
    return out;
  }

}
