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
package org.apache.xmlbeans.impl.jam.annogen.internal;

import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoTypeRegistry;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoType;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Default annotation registry, just maps declared types to '-Impl' classes
 * in the '.impl.' subpackage.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class DefaultAnnoTypeRegistry implements AnnoTypeRegistry {

  // ========================================================================
  // Constants

  public static final String SUB_PACKAGE = "impl";
  public static final String CLASSNAME_SUFFIX = "Impl";

  // ========================================================================
  // Variables

  private Map mDeclaredClass2AT = new HashMap();
  private Map mProxyClass2AT = new HashMap();

  // ========================================================================
  // Constructors

  public DefaultAnnoTypeRegistry() {}

  // ========================================================================
  // ProxyMapping implementation

  public AnnoType getAnnoTypeForRequestedClass(Class requestedClass)
    throws ClassNotFoundException
  {
    if (AnnoProxy.class.isAssignableFrom(requestedClass)) {
      return getAnnoTypeForProxyType(requestedClass);
    } else {
      // ok, this means they requested it by the declared type
      AnnoType out = getAnnoTypeForDeclaredType(requestedClass);
      if (!out.getDeclaredClass().isAssignableFrom(out.getProxyClass())) {
        // do this sanity check so that they won't get a mysterious
        // classcast in their code.
        throw new ClassNotFoundException
          ("The proxy for the requested annotation type '"+
           out.getDeclaredClass()+" does not implement the annotation "+
           "interface.  This is probably because it was compiled in " +
           "1.4-safe mode.  To access this annotation, please request "+
           "the proxy type: '"+out.getProxyClass()+"'");
      } else {
        return out;
      }
    }
  }


  public AnnoType getAnnoTypeForDeclaredType(Class declaredType)
      throws ClassNotFoundException
  {
    AnnoType out = (AnnoType)mDeclaredClass2AT.get(declaredType);
    if (out != null) return out;
    String declaredName = declaredType.getName();
    int lastDot = declaredName.lastIndexOf('.');
    String pkg = declaredName.substring(0,lastDot+1);
    String clazz = declaredName.substring(lastDot+1);
    //REVIEW it seems pretty reasonable to expect these things to be in
    //the same classloader, but we made need to revisit this someday
    Class proxyType = declaredType.getClassLoader().
        loadClass(pkg+SUB_PACKAGE+"."+clazz+CLASSNAME_SUFFIX);
    out = new AnnoType(declaredType,proxyType);
    addToCache(out);
    return out;
  }

  public AnnoType getAnnoTypeForProxyType(Class proxyClass)
      throws ClassNotFoundException
  {
    AnnoType out = (AnnoType)mProxyClass2AT.get(proxyClass);
    if (out != null) return out;
    String pkg = proxyClass.getPackage().getName();
    pkg = pkg.substring(0,pkg.length() - 4); // 'impl'.length() == 4
    String name = getShortName(proxyClass);
    name = name.substring(0,name.length() - 4); // 'Impl'.length() == 4
    //REVIEW it seems pretty reasonable to expect these things to be in
    //the same classloader, but we made need to revisit this someday
    Class declClass = proxyClass.getClassLoader().loadClass(pkg+name);
    out = new AnnoType(declClass,proxyClass);
    addToCache(out);
    return out;
  }


  // ========================================================================
  // Private methods

  private static String getShortName(Class clazz) {
    return clazz.getName().substring(clazz.getName().lastIndexOf('.')+1);
  }

  private void addToCache(AnnoType at) {
    mDeclaredClass2AT.put(at.getDeclaredClass(),at);
    mProxyClass2AT.put(at.getProxyClass(),at);
  }
}