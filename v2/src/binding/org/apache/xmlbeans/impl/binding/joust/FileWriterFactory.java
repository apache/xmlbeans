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

package org.apache.xmlbeans.impl.binding.joust;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Implementation of WriterFactory which creates files for new classes under
 * a specified source root.
 */
public class FileWriterFactory implements WriterFactory {

  // ========================================================================
  // Constants

  // might need to make this settable someday
  private static final String EXTENSION = ".java";
  private static final char PACKAGE_SEPARATOR = '.';

  // ========================================================================
  // Variables

  private File mSourceRoot;

  // ========================================================================
  // Constructors

  public FileWriterFactory(File sourceRoot) {
    if (sourceRoot == null) throw new IllegalArgumentException();
    mSourceRoot = sourceRoot;
  }

  // ========================================================================
  // WriterFactory implementation

  public Writer createWriter(String packageName, String className)
          throws IOException {
    return new FileWriter(createFile(packageName,className));
  }

  // ========================================================================
  // Public methods

  /**
   * Returns the raw file instead, in case the caller is clever and knows we
   * are a FileWriterFactory.
   */
  public File createFile(String packageName, String className)
          throws IOException
  {
    File dir = new File(mSourceRoot, packageName.replace
                                     (PACKAGE_SEPARATOR, File.separatorChar));
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new IOException("Failed to create directory " + dir);
      }
    }
    return new File(dir, className + EXTENSION);
  }
}