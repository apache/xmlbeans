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

import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyTypeMapping;
import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyContext;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class DefaultProxyMapping implements ProxyTypeMapping {

  // ========================================================================
  // Variables

  private ProxyContext mContext = null;

  // ========================================================================
  // Constructors

  public DefaultProxyMapping() {}

  // ========================================================================
  // ProxyMapping implementation

  //REVIEW we probably should provide an option for using a specific classloader

  public String getDeclaredTypeNameForProxyType(Class proxyType)
  {
    String pkg = proxyType.getPackage().getName();
    pkg = pkg.substring(0,pkg.length() - 4); // 'impl'.length() == 4
    String name = getShortName(proxyType);
    name = name.substring(0,name.length() - 4); // 'Impl'.length() == 4
    return pkg+name;
  }

  public Class getProxyTypeForDeclaredTypeName(String annotationTypeName)
    throws ClassNotFoundException
  {
    int lastDot = annotationTypeName.lastIndexOf('.');
    String pkg = annotationTypeName.substring(0,lastDot+1);
    String clazz = annotationTypeName.substring(lastDot+1);
    return mContext.getClassLoader().loadClass(pkg+"impl."+clazz+"Impl");
  }

  public void init(ProxyContext pc) { mContext = pc; }

  // ========================================================================
  // Private methods

  private static String getShortName(Class clazz) {
    return clazz.getName().substring(clazz.getName().lastIndexOf('.')+1);
  }
}
