/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.binding.tylar;

import java.io.File;
import java.io.IOException;

/**
 * An extension of Tylar which is known to exist as an open directory
 * structure.  This is useful for consumers who may need additional control
 * over the generated artifacts, e.g. to manually perform compilation of
 * generated java source files.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface ExplodedTylar extends Tylar {

  // ========================================================================
  // Public methods - these services are the 'value add' we provide over
  // just a generic Tylar.

  /**
   * Returns the directory on disk in which the tylar is stored.  Never
   * returns null.
   */
  public File getRootDir();

  /**
   * Returns the directory in which generated source files are stored in
   * the tylar.
   */
  public File getSourceDir();

  /**
   * Returns the directory in which generated class files are stored in
   * the tylar.  (Note that this typically is the same as the root dir).
   */
  public File getClassDir();

  /**
   * Returns the directory in which generated schema files are stored in
   * the tylar.
   */
  public File getSchemaDir();


  /**
   * Jars up the exploded tylar directory into the given file and returns
   * a handle to the JarredTylar.  The main advantage of using this method
   * as opposed to jarring it yourself is that you will save you the cost of
   * reparsing the binding file and the schemas in the event that you want to
   * immediately hand the tylar to the runtime.
   *
   * @param jarfile Destination file for the new jar
   * @return A handle to the newly-created tylar
   * @throws java.io.IOException if the specified jarfile already exists or if
   * an error occurs while writing the file.
   */
  public Tylar toJar(File jarfile) throws IOException;
}