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

package org.apache.xmlbeans.impl.jam.editable.impl;

import org.apache.xmlbeans.impl.jam.editable.EServiceParams;
import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JFactory;

import java.io.PrintWriter;

/**
 * FIXME - implement this
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EServiceParamsImpl implements EServiceParams {

  // ========================================================================
  // Variables

  private JClassLoader mParentLoader =
          JFactory.getInstance().getSystemClassLoader();//FIXME
  private JAnnotationLoader mAnnLoader = null;
  private boolean mVerbose = false;

  // ========================================================================
  // Package methods

  /*package*/ JClassLoader getParentClassLoader() {
    return mParentLoader;
  }

  /*package*/ boolean isVerbose() {
    return mVerbose;
  }

  /*package*/ JAnnotationLoader getAnnotationLoader() {
    return mAnnLoader;
  }

  // ========================================================================
  // EServiceParams implementation

  public void setAnnotationLoader(JAnnotationLoader ann) {
    mAnnLoader = ann;
  }

  public void setLogger(PrintWriter out) {
  }

  public void setVerbose(boolean v) {
    mVerbose = v;
  }

  public void setParentClassLoader(JClassLoader loader) {
  }
}
