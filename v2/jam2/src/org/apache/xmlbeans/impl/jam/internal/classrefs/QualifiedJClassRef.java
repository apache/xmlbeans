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
package org.apache.xmlbeans.impl.jam.internal.classrefs;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JamClassLoader;

/**
 * <p>Reference to a JClass by qualified name which is resolved lazily.  Note
 * that resolved references are not cached, which makes it more likely that
 * a JClasses will become available for garbage collection.  The performance
 * hit here is probably not significant, but someday we might want to provide
 * switch to enable caching of references.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class QualifiedJClassRef implements JClassRef {

  // ========================================================================
  // Variables

  private String mQualifiedClassname;
  private JamClassLoader mClassLoader;

  // ========================================================================
  // Factory

  /**
   * Creates a new JClassRef for a qualified class or type name.
   */
  public static JClassRef create(String qcname,
                                 JClassRefContext ctx) {
    if (qcname == null) throw new IllegalArgumentException("null qcname");
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    return create(qcname,ctx.getClassLoader());
  }

  /**
   * Creates a new JClassRef for a qualified class or type name.
   */
  public static JClassRef create(String qcname, JamClassLoader cl) {
    if (qcname == null) throw new IllegalArgumentException("null qcname");
    if (cl == null) throw new IllegalArgumentException("null ctx");
    return new QualifiedJClassRef(qcname,cl);
  }

  // ========================================================================
  // Constructors

  private QualifiedJClassRef(String qcname, JamClassLoader cl) {
    mClassLoader = cl;
    mQualifiedClassname = qcname;
  }

  // ========================================================================
  // JClassRef implementation

  public JClass getRefClass() {
    return mClassLoader.loadClass(mQualifiedClassname);
  }

  public String getQualifiedName() {
    return mQualifiedClassname;
  }
}