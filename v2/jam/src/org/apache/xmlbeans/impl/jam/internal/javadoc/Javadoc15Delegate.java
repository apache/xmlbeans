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

import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoProxySet;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;

/**
 * Provides an interface to 1.5-specific functionality.  The impl of
 * this class is loaded by-name at runtime.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface Javadoc15Delegate {

  // ========================================================================
  // Factory

  public static class Factory {

    private static final String JAVA15_EXTRACTOR =
        "org.apache.xmlbeans.impl.jam.internal.java15.Javadoc15DelegateImpl";

    public static Javadoc15Delegate create(JamLogger logger) {
      try {
        // See if we can load a 1.5-specific class.  If we can't, don't use
        // the Javadoc15Delegate.
        Class.forName("com.sun.javadoc.AnnotationDesc");
      } catch (ClassNotFoundException e) {
        //FIXME issue14RuntimeWarning(e);
        return null;
      }
      // ok, if we could load that, let's new up the extractor delegate
      try {
        Javadoc15Delegate out = (Javadoc15Delegate)
            Class.forName(JAVA15_EXTRACTOR).newInstance();
        out.init(logger);
        return out;
        // if this fails for any reason, things are in a bad state
      } catch (ClassNotFoundException e) {
        //issue14BuildWarning(e);
      } catch (IllegalAccessException e) {
        //issue14BuildWarning(e);
      } catch (InstantiationException e) {
        //issue14BuildWarning(e);
      }
      return null;
    }
  }

  // ========================================================================
  // Public methods

  /**
   *
   */
  public void extractAnnotations(AnnoProxySet out,
                                 ProgramElementDoc src);

  /**
   *
   */
  public void extractAnnotations(AnnoProxySet out,
                                 ExecutableMemberDoc method,
                                 int paramNum);


  /**
   * Returns true if the given ClassDoc represents an enum.
   */ 
  public boolean isEnum(ClassDoc cd);

  public void init(JamLogger logger);

  public void populateAnnotationTypeIfNecessary(ClassDoc cd,
                                         MClass clazz,
                                         JavadocClassBuilder builder);


  // ========================================================================
  // Deprecated stuff, trying to move away from this

  /**
   * @deprecated
   */
  public void extractAnnotations(MAnnotatedElement dest,
                                 ProgramElementDoc src);

  /**
   * @deprecated
   */
  public void extractAnnotations(MAnnotatedElement dest,
                                 ExecutableMemberDoc method,
                                 Parameter src);

  /**
   * @deprecated
   */
  public static final String ANNOTATION_DEFAULTS_ENABLED_PROPERTY =
  "Javadoc15DelegateImpl.ANNOTATION_DEFAULTS_ENABLED_PROPERTY";

  /**
   * @deprecated
   */
  public void init(ElementContext ctx);

}
