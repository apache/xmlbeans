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

package org.apache.xmlbeans.impl.jam;

import java.io.File;
import java.io.IOException;

/**
 * <p>Describes a set of input source files which describe the java types to
 * be represented.  Instances of JFileSet are created by JFactory.</p>
 *
 * @deprecated Please us JServiceFactory instead.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JFileSet {

  // ========================================================================
  // Public methods
  
  
  public void include(String pattern);

  public void exclude(String pattern);

  public void setClasspath(String cp);

  public void setCaseSensitive(boolean b);

  // REVIEW: why can't JFileSet just be the following method and none of the
  // others? (davidbau)
  public File[] getFiles() throws IOException;

  //  public boolean setFollowSymlinks(boolean b);

}
