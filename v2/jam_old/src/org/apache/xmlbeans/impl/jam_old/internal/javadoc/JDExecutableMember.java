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


import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Tag;
import java.util.Collection;
import org.apache.xmlbeans.impl.jam_old.*;

/**
 * Abstract base class for JDConstructor and JDMethod.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class JDExecutableMember extends JDMember {

  // ========================================================================
  // Variables

  private ExecutableMemberDoc mMember;
  private JParameter[] mParameters = null;

  // ========================================================================
  // Constructors

  /**
   *
   */
  protected JDExecutableMember(ExecutableMemberDoc m, JClassLoader loader) {
    super(m,loader);
    mMember = m;
  }

  // ========================================================================
  // JElement implementation

  public JElement[] getChildren() { return getParameters(); }

  /**
   * Override this so that we can exclude @param tags - these get hund
   * on the JParameters.
   */
  protected void getLocalAnnotations(Collection out) {
    com.sun.javadoc.Tag[] tags = mMember.tags();
    if (tags == null || tags.length == 0) return;
    for(int i=0; i<tags.length; i++) {
      if (!(tags[i] instanceof ParamTag)) {
          Tag t = tags[i];
          JSourcePosition sp = JDFactory.getInstance().createSourcePosition(mMember.position());
          out.add(JDFactory.getInstance().createAnnotation(this, t.name(), t.text(), sp));
      }
    }
  }

  // ========================================================================
  // JExecutableMember implementation

  public JParameter[] getParameters() {
    ParamTag[] tags = mMember.paramTags();
    if (mParameters == null) {
      Parameter[] params = mMember.parameters();
      if (params == null || params.length == 0) {
        mParameters = NO_PARAMETER;
      } else {
        mParameters = new JDParameter[params.length];
        for(int i=0; i<params.length; i++) {
          mParameters[i] = JDFactory.getInstance().createParameter(params[i],
                                                                   this,
                                                                   mLoader);
        }
      }
    }
    return mParameters;
  }

  public JClass[] getExceptionTypes() {
    return JDClass.getClasses(mMember.thrownExceptions(),mLoader);
  }

  // ========================================================================
  // Package methods

  // this is here for the benefit of JDParameter.getLocalAnnotations()
  // as well as subclasses.
  public ExecutableMemberDoc getMember() { return mMember; }

}
