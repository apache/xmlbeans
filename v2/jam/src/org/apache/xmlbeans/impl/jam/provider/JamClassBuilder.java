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

import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.internal.elements.ClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

/**
 * <p>Implemented by providers to build and initialize classes on demand.
 * The main responsibility a JAM provider has is writing an extension of this
 * class.
 * </p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class JamClassBuilder {

  // ========================================================================
  // Variables

  private ElementContext mContext = null;
  private JamLogger mLogger = null;
  private static boolean mWarningAlreadyIssued = false;

  // ========================================================================
  // Public methods

  public void init(ElementContext ctx) {
    if (mContext != null) {
      throw new IllegalStateException("init called more than once");
    }
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    mContext = ctx;
    mLogger = ctx.getLogger();
  }

  // ========================================================================
  // Protected methods

  /**
   * <p>When a JamClassBuilder decides that it is going to be able
   * to respond to a build() request, it must call this method to get an
   * initial instance of MClass to return.</p>
   *
   * @param packageName qualified name of the package that contains the
   * class to create
   * @param className simple name of the class to create.
   * @param importSpecs array of import specs to be used in the class,
   * or null if not known or relevant.  Import specs are only needed if
   * the builder is planning on setting any unqualified type references
   * on the class.
   */
  protected MClass createClassToBuild(String packageName,
                                      String className,
                                      String[] importSpecs,
                                      JamClassPopulator pop)
  {
    if (mContext == null) throw new IllegalStateException("init not called");
    if (packageName == null) throw new IllegalArgumentException("null pkg");
    if (className == null) throw new IllegalArgumentException("null class");
    if (pop == null) throw new IllegalArgumentException("null pop");
    className = className.replace('.','$');
    ClassImpl out = new ClassImpl(packageName,className,mContext,importSpecs,pop);
    return out;
  }

  /**
   * <p>When a JamClassBuilder decides that it is going to be able
   * to respond to a build() request, it must call this method to get an
   * initial instance of MClass to return.</p>
   *
   * @param packageName qualified name of the package that contains the
   * class to create
   * @param className simple name of the class to create.
   * @param importSpecs array of import specs to be used in the class,
   * or null if not known or relevant.  Import specs are only needed if
   * the builder is planning on setting any unqualified type references
   * on the class.
   */
  protected MClass createClassToBuild(String packageName,
                                      String className,
                                      String[] importSpecs)
  {
    if (mContext == null) throw new IllegalStateException("init not called");
    if (packageName == null) throw new IllegalArgumentException("null pkg");
    if (className == null) throw new IllegalArgumentException("null class");
    className = className.replace('.','$');
    ClassImpl out = new ClassImpl(packageName,className,mContext,importSpecs);
    return out;
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
   * that represents the given type, it should call createClassToBuild() to get
   * a new instance of MClass and then return it.  No caching should be
   * performed - if an MClass is going to be returned, it should be a new
   * instance returned by createClassToBuild()</p>
   *
   * <p>If no artififact is available, the builder should just return null,
   * signalling that other JamClassBuilders should attempt to build the
   * class.</p>
   *
   * @param packageName
   * @param className
   * @return
   */ 
  public abstract MClass build(String packageName, String className);

  // ========================================================================
  // Protected methods

  protected void issue14BuildWarning(Throwable t) {
    if (!mWarningAlreadyIssued) {
      getLogger().warning("This build of JAM was produced under JDK 1.4." +
                      "Even though you are running under JDK 1.5, "+
                      "JSR175-style annotations will not be available");
      getLogger().verbose(t);
      mWarningAlreadyIssued = true;
    }
  }

  protected void issue14RuntimeWarning(Throwable t) {
    if (!mWarningAlreadyIssued) {
      getLogger().warning("You are running under a pre-1.5 JDK.  JSR175-style "+
                      "source annotations will not be available");
      getLogger().verbose(t);
      mWarningAlreadyIssued = true;
    }
  }


}