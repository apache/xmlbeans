/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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
