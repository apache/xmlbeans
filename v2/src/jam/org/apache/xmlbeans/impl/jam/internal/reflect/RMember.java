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

package org.apache.xmlbeans.impl.jam.internal.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Collection;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.BaseJElement;

/**
 * Reflection-backed implementation of JMember.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
/*package*/ 
abstract class RMember extends BaseJElement implements JMember {

  // ========================================================================
  // Variables

  private Member mMember;
  protected JClassLoader mLoader;

  // ========================================================================
  // Constructors
  
  public RMember(Member m, JClassLoader loader) {
    mLoader = loader;
    mMember = m;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() {
    return RClassLoader.getClassFor(mMember.getDeclaringClass(),mLoader);
  }

  public JElement[] getChildren() { return null; }

  public String getSimpleName() { return mMember.getName(); }

  public String getQualifiedName() { return mMember.getName(); } //FIXME

  // ========================================================================
  // JMember implementation

  public boolean isFinal() { 
    return Modifier.isFinal(mMember.getModifiers());
  }

  public boolean isAbstract() { 
    return Modifier.isAbstract(mMember.getModifiers());
  }

  public boolean isProtected() { 
    return Modifier.isProtected(mMember.getModifiers());
  }

  public boolean isPublic() { 
    return Modifier.isPublic(mMember.getModifiers());
  }

  public boolean isPrivate() { 
    return Modifier.isPrivate(mMember.getModifiers());
  }

  public boolean isStatic() { 
    return Modifier.isStatic(mMember.getModifiers());
  }

  public boolean isPackagePrivate() {
    return !isPublic() && !isProtected() && !isPrivate();
  }

  public int getModifiers() { return mMember.getModifiers(); }

  /**
   * We're never going to know this.
   */
  public JSourcePosition getSourcePosition() { return null; }

  // ========================================================================
  // JMember implementation
  
  public JClass getContainingClass() {
    return RClassLoader.getClassFor(mMember.getDeclaringClass(),mLoader);
  }

  public boolean isSynthetic() { return false; }//FIXME?

  // ========================================================================
  // BaseJElement implementation

  /**
   * We can't implement this until JSR175 is here.
   */
  protected  void getLocalAnnotations(Collection out) {}

  /**
   * We can't ever implement this.
   */
  protected  void getLocalComments(Collection out) {}

}
