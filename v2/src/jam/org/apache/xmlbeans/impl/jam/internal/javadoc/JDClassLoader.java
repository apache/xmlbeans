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
 * @deprecated Should be removed along with JFactory.
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

  public static JClass getClassFor(Type t, JClassLoader loader) {
    return loader.loadClass(getFieldDescriptorFor(t));
  }

  /**
   * Returns a classfile-style field descriptor for the given type.
   * This has to be called to get a name for a javadoc type that can
   * be used with Class.forName(), JRootContext.getClass(), or
   * JClass.forName().
   */
  public static String getFieldDescriptorFor(Type t) {
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




