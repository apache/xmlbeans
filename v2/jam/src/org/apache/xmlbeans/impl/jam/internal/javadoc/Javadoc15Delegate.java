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
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ClassDoc;

/**
 * Provides an interface to 1.5-specific functionality.  The impl of
 * this class is loaded by-name at runtime.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface Javadoc15Delegate {

  public void init(ElementContext ctx);

  public void extractAnnotations(MAnnotatedElement dest,
                                 ProgramElementDoc src);

  public void extractAnnotations(MAnnotatedElement dest,
                                 Parameter src);

  /**
   * Returns true if the given ClassDoc represents an enum.
   */ 
  public boolean isEnum(ClassDoc cd);

}
