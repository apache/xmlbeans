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

package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JElement;
import org.apache.xmlbeans.impl.jam.JSourcePosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * <p>Generic implementation of JPackage/InternalJPackage that is good
 * enough for all cases, right now.  This might change if we ever wrap
 * a model which natively supports package-level annotation.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JPackageImpl extends BaseJElement implements InternalJPackage {

  // ========================================================================
  // Variables

  private List mRootClasses = null;
  private String mName;
  private JElement mParent = null;

  // ========================================================================
  // Constructors

  public JPackageImpl(String name) {
    mName = name;
  }

  // ========================================================================
  // InternalJPackage implementation

  public void setParent(JElement parent) {
    mParent = parent;
  }

  /**
   * <p>Need this so we can tell arbitrary packages that an arbitrary
   * class is their child.  See JRootImpl constructor.</p>
   */
  public void addClass(JClass clazz) {
    if (mRootClasses == null) mRootClasses = new ArrayList();
    mRootClasses.add(clazz);
  }


  // ========================================================================
  // JElement implementation

  /**
   * The parent of a package is always the root.
   */
  public JElement getParent() {
    return mParent;
  }

  public JElement[] getChildren() {
    return getClasses();
  }

  public String getSimpleName() {
    int lastDot = mName.lastIndexOf('.');
    if (lastDot == -1) return mName;
    return mName.substring(lastDot + 1);
  }

  public String getQualifiedName() {
    return mName;
  }

  //REVIEW maybe we should try to find package.html and use that as
  //source position?
  public JSourcePosition getSourcePosition() {
    return null;
  }

  // ========================================================================
  // JPackage implementation

  public JClass[] getClasses() {
    JClass[] out = new JClass[mRootClasses.size()];
    mRootClasses.toArray(out);
    return out;
  }

  // ========================================================================
  // BaseJElement implementation

  // no support for local package-level comments or annotations

  protected void getLocalAnnotations(Collection out) {
  }

  protected void getLocalComments(Collection out) {
  }

}
