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

package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.JProvider;
import org.apache.xmlbeans.impl.jam.internal.JServiceParamsImpl;
import org.apache.xmlbeans.impl.jam.internal.reflect.RClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDClassBuilder;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Singleton which is the DefaultJProvider to be used in the current VM.
 * This is the Provider to which the ServiceFactory delegates.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DefaultJProvider extends BaseJProvider {

  // ========================================================================
  // Constants

  private static final JProvider INSTANCE = new DefaultJProvider();

  // ========================================================================
  // Singleton

  public static JProvider getInstance() { return INSTANCE; }

  // ========================================================================
  // BaseJProvider implementation

  public JClassBuilder createJClassBuilder(JClassBuilderParams params)
          throws IOException
  {
    List builderList = new ArrayList();
    if (params.getInputSourcepath() != null) {
      builderList.add(createSourceService(params));
    }
    if (params.getInputClasspath() != null) {
      builderList.add(createClassService(params));
    }
    JClassBuilder[] builderArray = new JClassBuilder[builderList.size()];
    builderList.toArray(builderArray);
    return new CompositeJClassBuilder(builderArray);
  }

  // ========================================================================
  // Public methods

  public JClassBuilder createSourceService(JClassBuilderParams jp)
          throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    return JDClassBuilder.create((JServiceParamsImpl)jp);
  }

  public JClassBuilder createClassService(JClassBuilderParams jp)
          throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    JPath cp = jp.getInputClasspath();
    if (cp == null) {
      return RClassBuilder.getSystemClassBuilder();
    } else {
      URL[] urls = cp.toUrlPath();
      return RClassBuilder.getClassBuilderFor(new URLClassLoader(urls));
    }
  }


}