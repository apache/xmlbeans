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


import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Tag;
import java.util.Collection;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.BaseJElement;

/**
 * javadoc-backed implementation of JMember.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class JDMember extends BaseJElement implements JMember 
{

  // ========================================================================
  // Variables

  protected JClassLoader mLoader;
  private ProgramElementDoc mPE;
  private JSourcePosition mPosition = null;
  private JComment[] mLocalComments = null;

  // ========================================================================
  // Constructors
  
  protected JDMember(ProgramElementDoc pe, JClassLoader loader) {
    mLoader = loader;
    mPE = pe;
  }

  // ========================================================================
  // JElement implementation

  public String getSimpleName() { return mPE.name(); }

  public String getQualifiedName() { return mPE.qualifiedName(); }

  // ========================================================================
  // Partial BaseJElement implementation

  protected void getLocalAnnotations(Collection out) {
    com.sun.javadoc.Tag[] tags = mPE.tags();
    if (tags != null) {
      for(int i=0; i<tags.length; i++) {
        Tag t = tags[i];
        JSourcePosition sp = JDFactory.getInstance().createSourcePosition(mPE.position());
        out.add(JDFactory.getInstance().createAnnotation(this, t.name(), t.text(), sp));
      }
    }
  }

  protected void getLocalComments(Collection out) {
    String txt = mPE.commentText();
    if (txt == null) return;
    txt = txt.trim();
    if (txt.length() == 0) return;
    out.add(JDFactory.getInstance().createComment(mPE.commentText()));
  }

  // ========================================================================
  // JElement implementation - overridden by some

  public JElement getParent() {
    return JDClassLoader.getClassFor(mPE.containingClass(),mLoader);
  }

  public JElement[] getChildren() { return null; } //FIXME

  // ========================================================================
  // JMember implementation

  public int getModifiers() { return mPE.modifierSpecifier(); }

  public boolean isPackagePrivate() { return mPE.isPackagePrivate(); }

  public boolean isProtected() { return mPE.isProtected(); }

  public boolean isPublic() { return mPE.isPublic(); }

  public boolean isPrivate() { return mPE.isPrivate(); }

  public JSourcePosition getSourcePosition() { 
    if (mPosition == null) {
      mPosition = JDFactory.getInstance().createSourcePosition(mPE.position());
    }
    return mPosition;
  }
  
  public JClass getContainingClass() {
    if (mPE.containingClass() == null) return null;
    return JDClassLoader.getClassFor(mPE.containingClass(),mLoader);
  }

  // ========================================================================
  // Impls inhertied by JMethod, JClass et al.  Javadoc exposes these
  // attributes on abstractions where they don't make sense
  // (e.g. constructors), but we'll take advantage of that here

  public boolean isFinal() { return mPE.isFinal(); }

  public boolean isStatic() { return mPE.isStatic(); }

}
