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

import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Represent a file search path, such as a classpath or sourcepath.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JPath {

  // ========================================================================
  // Factory

  public static JPath forFiles(File[] files) {
    return new JPath(files);
  }

  // ========================================================================
  // Constants

  //public static final JPath EMPTY_JPATH = new JPath(new File[]{});

  // ========================================================================
  // Variables

  private File[] mFiles;

  // ========================================================================
  // Constructors

  private JPath(File[] files) {
    if (files == null) throw new IllegalArgumentException("null files");
    mFiles = files;
  }

  // ========================================================================
  // Public methods

  public URI[] toUriPath() {
    URI[] out = new URI[mFiles.length];
    for(int i=0; i<mFiles.length; i++) {
      out[i] = mFiles[i].toURI();
    }
    return out;
  }

  public URL[] toUrlPath() throws MalformedURLException {
    URL[] out = new URL[mFiles.length];
    for(int i=0; i<mFiles.length; i++) {
      out[i] = mFiles[i].toURL();
    }
    return out;
  }

  // ========================================================================
  // Object implementation

  /**
   * Returns the path as a single string in which each file component is
   * separates by File.pathSeparatorChar.
   */
  public String toString() {
    StringWriter out = new StringWriter();
    for(int i=0; i<mFiles.length; i++) {
      out.write(mFiles[i].getAbsolutePath());
      out.write(File.pathSeparatorChar);
    }
    return out.toString();
  }
}
