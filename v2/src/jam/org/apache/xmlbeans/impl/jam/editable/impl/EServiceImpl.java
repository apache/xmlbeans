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
package org.apache.xmlbeans.impl.jam.editable.impl;

import org.apache.xmlbeans.impl.jam.editable.EService;
import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.*;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EServiceImpl implements EService, JClassLoader {

  // ========================================================================
  // Variables

  private Map mClasses = new HashMap();
  private JClassLoader mBaseClassLoader;
  private JAnnotationLoader mAnnotationLoader = null;

  // ========================================================================
  // Constructors

  public EServiceImpl(EServiceParamsImpl params) {
    mBaseClassLoader = params.getParentClassLoader();
    mAnnotationLoader = params.getAnnotationLoader();
  }

  // ========================================================================
  // EService implementation

  public EClass addNewClass(String packageName, String className) {
    EClassImpl out = new EClassImpl(packageName,className,this);
    mClasses.put(out.getQualifiedName(),out);
    return out;
  }

  public EClass addNewClass(JClass copyme) {
    throw new IllegalStateException("NYI");
  }

  public JClassLoader getClassLoader() {
    return this;
  }

  // ========================================================================
  // JService implementation

  public String[] getClassNames() {
    String[] out = new String[mClasses.values().size()];
    mClasses.keySet().toArray(out);
    return out;
  }

  public JClassIterator getClasses() {
    return new JClassIterator(this,getClassNames());
  }

  // ========================================================================
  // JClassLoader implementation

  public JClass loadClass(String classname) {
    JClass out = (JClass)mClasses.get(classname);
    if (out != null) return out;
    return mBaseClassLoader.loadClass(classname);
  }

  public JPackage getPackage(String qualifiedPackageName) {
    return mBaseClassLoader.getPackage(qualifiedPackageName);
  }

  public JAnnotationLoader getAnnotationLoader() {
    return mAnnotationLoader;
  }

  public JClassLoader getParent() {
    return mBaseClassLoader;
  }
}
