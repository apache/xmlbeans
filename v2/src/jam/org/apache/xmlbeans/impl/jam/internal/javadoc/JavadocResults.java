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

import com.sun.javadoc.RootDoc;
import java.lang.reflect.Method;

/**
 * Used by JDClassLoaderFactory to ensure that we always stash the results
 * of a javadoc run in a place where we can get them.  This is particularly
 * painful because one implements a javdoc doclet by providing a static
 * callback method.  In the case of multiple classloaders, extra care must be
 * taken to ensure that we can stash away the RootDoc result received in
 * the callback and retrieve it later.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JavadocResults {

  // ========================================================================
  // Singleton

  private static final JavadocResults INSTANCE = new JavadocResults();

  // ========================================================================
  // Variables

  private ThreadLocal mRootsPerThread = new ThreadLocal();

  // ========================================================================
  // Public methods

  public static void prepare() {
    Thread.currentThread().
            setContextClassLoader(JavadocResults.class.getClassLoader());
  }

  public static void setRoot(RootDoc root) {
    try {
      Object holder = getHolder();
      Method setter = holder.getClass().getMethod("_setRoot",
                                                  new Class[] { RootDoc.class });
      setter.invoke(holder,new Object[] {root});
    } catch(Exception e) {
      e.printStackTrace();
      throw new IllegalStateException();//FIXME??
    }
  }

  public static RootDoc getRoot() {
    try {
      Object holder = getHolder();
      Method getter = holder.getClass().getMethod("_getRoot",new Class[0]);
      return (RootDoc)getter.invoke(holder,null);
    } catch(Exception e) {
      e.printStackTrace();
      throw new IllegalStateException();//FIXME??
    }
  }

  // ========================================================================
  // DO NOT CALL THESE METHODS

  public void _setRoot(RootDoc root) { mRootsPerThread.set(root); }

  public RootDoc _getRoot() { return (RootDoc)mRootsPerThread.get(); }

  public static JavadocResults getInstance() { return INSTANCE; }

  private static Object getHolder() throws Exception {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class clazz = classLoader.loadClass(JavadocResults.class.getName());
    Method method = clazz.getMethod("getInstance",new Class[0]);
    return method.invoke(null,new Object[0]);
  }

}
