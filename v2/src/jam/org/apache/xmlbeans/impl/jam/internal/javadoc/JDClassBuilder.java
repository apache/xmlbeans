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

import org.apache.xmlbeans.impl.jam.provider.JClassBuilder;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.internal.JServiceParamsImpl;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.ClassDoc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDClassBuilder implements JClassBuilder {

  // ========================================================================
  // Variables

  private RootDoc mRootDoc;

  // ========================================================================
  // Factory

  public static JClassBuilder create(JServiceParamsImpl params)
          throws IOException
  {
    File[] files = params.getSourceFiles();
    String sourcePath = (params.getInputSourcepath() == null) ? null :
            params.getInputSourcepath().toString();
    String classPath = (params.getInputClasspath() == null) ? null :
            params.getInputClasspath().toString();
    return JDClassLoaderFactory.getInstance().
            createBuilder(files,
                          params.getAnnotationLoader(),
                          params.getOut(),
                          sourcePath,
                          classPath,
                          null);//FIXME glean javadoc args from props
  }

  // ========================================================================
  // Constructors

  /*package*/ JDClassBuilder(RootDoc rd) {
    if (rd == null) throw new IllegalArgumentException("null rd");
    mRootDoc = rd;
  }

  // ========================================================================
  // JClassBuilder implementation

  public JClass buildJClass(String qualifiedName, JClassLoader loader) {
    ClassDoc jd = mRootDoc.classNamed(qualifiedName);
    if (jd != null) {
      return JDFactory.getInstance().createClass(jd,loader);
    } else {
      return null;
    }
  }
}
