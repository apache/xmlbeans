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


import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.ArrayJClass;
import org.apache.xmlbeans.impl.jam.internal.PrimitiveJClass;


/**
 * <p>javadoc-backed implementation of JClassLoader.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDClassLoader implements JClassLoader
{
  // ========================================================================
  // Variables

  private RootDoc mRootDoc = null;
  private Map mName2Package = new HashMap();
  private Map mFd2Class = new HashMap();
  private JAnnotationLoader mAnnotationLoader = null;//FIXME
  private JClassLoader mParentLoader;

  // ========================================================================
  // Constructor

  public JDClassLoader(RootDoc doc, JClassLoader loader) {
    mRootDoc = doc;
    ClassDoc[] c = doc.classes();
    mParentLoader = (loader != null) ? loader :
      JFactory.getInstance().getSystemClassLoader();
    for(int i=0; i<c.length; i++) {
      //REVIEW kinda bad to be passing references to ourself around in
      //the constructor.  I think it's ok here, though.  No?
      JClass clazz = JDFactory.getInstance().createClass(c[i],this);
      mFd2Class.put(clazz.getFieldDescriptor(),clazz);
    }
  }

  // ========================================================================
  // JClassLoader implementation

  public JClassLoader getParent() { return mParentLoader; }

  public JClass loadClass(String fd)
  {
    fd = fd.trim();//REVIEW is this paranoid?
    if (fd.startsWith("[")) {
      return ArrayJClass.createClassFor(fd,this);
    } else {
      if (fd.equals("java.lang.Object")) return mParentLoader.loadClass(fd);
      JClass out = (JClass)mFd2Class.get(fd);
      if (out != null) return out;
      ClassDoc jc = mRootDoc.classNamed(fd);
      if (jc != null) {
        mFd2Class.put(fd,out = JDFactory.getInstance().createClass(jc,this));
        return out;
      } else {
        return mParentLoader.loadClass(fd);
      }
    }
  }

  public JAnnotationLoader getAnnotationLoader() { 
    return mAnnotationLoader; 
  }

  public JPackage getPackage(String named) {
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      out = JDFactory.getInstance().createPackage(named);
      mName2Package.put(named,out);
    }
    return out;
  }

  // ========================================================================
  // Public methods

  /**
   * Returns an unmodifiable collection containing the JClasses which
   * have been resolved by this JClassLoader.
   */
  public Collection getResolvedClasses() {
    return Collections.unmodifiableCollection(mFd2Class.values());
  }

  // ========================================================================
  // Static utilities

  /*package*/ static JClass getClassFor(Type t, JClassLoader loader) {
    return loader.loadClass(getFieldDescriptorFor(t));
  }

  /**
   * Returns a classfile-style field descriptor for the given type.
   * This has to be called to get a name for a javadoc type that can
   * be used with Class.forName(), JRootContext.getClass(), or
   * JClass.forName().
   */
  /*package*/ static String getFieldDescriptorFor(Type t) {
    String dim = t.dimension();
    if (dim == null || dim.length() == 0) { 
      return t.qualifiedTypeName();
    } else {
      StringWriter out = new StringWriter();
      for(int i=0, iL=dim.length()/2; i<iL; i++) out.write("[");
      JClass primClass = 
	PrimitiveJClass.getPrimitiveClassForName(t.qualifiedTypeName());
      if (primClass != null) { //i.e. if primitive
        out.write(primClass.getFieldDescriptor());
      } else {
        out.write("L");
        out.write(t.qualifiedTypeName());
        out.write(";");
      } 
      return out.toString();
    } 
  }

}
