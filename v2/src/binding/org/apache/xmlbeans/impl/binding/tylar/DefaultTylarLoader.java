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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import org.apache.xmlbeans.XmlException;

/**
 * Default implementation of TylarLoader.  Currently, only directory and jar
 * tylars are supported.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DefaultTylarLoader implements TylarLoader {

  // ========================================================================
  // Constants

  private static final String FILE_SCHEME = "file";

  // ========================================================================
  // Singleton

  //REVIEW someday we might want to make the default TylarLoader a pluggable
  //parameter in a properties file somewhere.  pcal 12/16/03
  public static final TylarLoader getInstance() {
    return DEFAULT_INSTANCE;
  }

  private static /*final*/ TylarLoader DEFAULT_INSTANCE = new DefaultTylarLoader();

  /**
   * This is a gross quick hack to support pluggability for tylar loader.
   * In the future, we need a cleaner mechansim, probably letting them
   * specifiy the TylarLoader impl class name in some properties file
   * in the classpath.
   *
   * @deprecated eventually; currently there is no other option.
   */
  public static void setInstance(TylarLoader newDefaultLoader) {
    DEFAULT_INSTANCE = newDefaultLoader;
  }

  // ========================================================================
  // Constructor

  protected DefaultTylarLoader() {}

  // ========================================================================
  // Public methods

  /**
   * Loads the tylar from the given uri.
   *
   * @param uri uri of where the tylar is stored.
   * @return
   * @throws IOException if an i/o error occurs while processing
   * @throws XmlException if an error occurs parsing the contents of the tylar.
   */
  public Tylar load(URI uri) throws IOException, XmlException {
    if (uri == null) throw new IllegalArgumentException("null uri");
    String scheme = uri.getScheme();
    if (scheme.equals(FILE_SCHEME)) {
      File file = new File(uri);
      if (!file.exists()) throw new FileNotFoundException(uri.toString());
      if (file.isDirectory()) {
        return ExplodedTylarImpl.load(file);
      } else {
        return JarredTylar.load(file);
      }
    } else {
      throw new IOException("Sorry, the '"+scheme+
                            "' scheme is not supported for loading tylars" +
                            "("+uri+")");
    }
  }
}
