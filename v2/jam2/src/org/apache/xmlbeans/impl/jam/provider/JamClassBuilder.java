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

import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.internal.elements.ClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

/**
 * <p>Implemented by providers to build and initialize classes on demand.
 * The main responsibility a JAM provider has is writing an extension of this
 * class.
 * </p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class JamClassBuilder {

  // ========================================================================
  // Variable

  private ElementContext mContext = null;

  // ========================================================================
  // Public methods

  public void init(ElementContext ctx) {
    if (mContext != null) {
      throw new IllegalStateException("init called more than once");
    }
    mContext = ctx;
  }

  // ========================================================================
  // Protected methods

  /**
   * <p>When a JamClassBuilder decides that it is going to be able
   * to respond to a build() request, it must call this method to get an
   * initial instance of EClass.  It should then initialize the EClass
   * as appropriate and then return it.</p>
   *
   * @param packageName qualified name of the package that contains the
   * class to create
   * @param className simple name of the class to create.
   * @param importSpecs array of import specs to be used in the class,
   * or null if not known or relevant.  Import specs are only needed if
   * the builder is planning on setting any unqualified type references
   * on the class.
   */
  protected EClass createClass(String packageName,
                               String className,
                               String[] importSpecs)
  {
    if (mContext == null) throw new IllegalStateException("init not called");
    if (packageName == null) throw new IllegalArgumentException("null pkg");
    if (className == null) throw new IllegalArgumentException("null class");
    return new ClassImpl(packageName,className,mContext,importSpecs);
  }

  protected JamLogger getLogger() {
    if (mContext == null) throw new IllegalStateException("init not called");
    return mContext;
  }

  // ========================================================================
  // Abstract methods
  
  /**
   * <p>This is called by JAM when it attempts to load a class.  If the
   * builder has access to an artifact (typically a java source or classfile)
   * that represents the given type, it should call createClass() to get
   * a new instance of EClass, populate that instance, and then return it.
   * It should not perform any caching - if an EClass is going to be returned,
   * it should be a new instance.</p>
   *
   * <p>If not artififact is available, the builder should just return null,
   * signalling that other JamClassBuilders should attempt to build the
   * class.</p>
   *
   * @param packageName
   * @param className
   * @return
   */ 
  public abstract EClass build(String packageName, String className);
}