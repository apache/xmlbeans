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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.w3.x2001.xmlSchema.SchemaDocument;

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

  private static final char[] OTHER_SEPCHARS = {'\\'};
  private static final char SEPCHAR = '/';

  private static final boolean VERBOSE = true;

  private static final String BINDING_FILE_JARENTRY =
          normalizeEntryName(TylarConstants.BINDING_FILE);

  private static final String SCHEMA_DIR_JARENTRY =
          normalizeEntryName(TylarConstants.SCHEMA_DIR);

  private static final String SCHEMA_EXT = ".xsd";

  // ========================================================================
  // Singleton

  //REVIEW someday we might want to make the default TylarLoader a pluggable
  //parameter in a properties file somewhere.  pcal 12/16/03
  public static final TylarLoader getInstance() {
    return DEFAULT_INSTANCE;
  }

  private static /*final*/ TylarLoader
          DEFAULT_INSTANCE = new DefaultTylarLoader();

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
  public Tylar load(URI uri) throws IOException, XmlException
  {
    if (uri == null) throw new IllegalArgumentException("null uri");
    String scheme = uri.getScheme();
    if (scheme.equals(FILE_SCHEME)) {
      File file;
      try {
        file = new File(uri);
      } catch(Exception e) {
        //sometimes File can't deal for some reason, so as a last ditch
        //we assume it's a jar and read the stream directly
        return loadFromJar(uri.toURL().openStream(),uri);
      }
      if (!file.exists()) throw new FileNotFoundException(uri.toString());
      if (file.isDirectory()) {
        return ExplodedTylarImpl.load(file);
      } else {
        return loadFromJar(file);
      }
    } else {
      throw new IOException("Sorry, the '"+scheme+
                            "' scheme is not supported for loading tylars" +
                            "("+uri+")");
    }
  }

  // ========================================================================
  // Static utility methods

  /**
   * Loads a Tylar directly from the given jar file.  This method parses all
   * of the tylar's binding artifacts; if it doesn't throw an exception,
   * you can be sure that the tylars binding files and schemas are valid.
   *
   * @param jarFile file containing the tylar
   * @return Handle to the tylar
   * @throws IOException
   */
  public static Tylar loadFromJar(File jarFile)
          throws IOException, XmlException
  {
    if (jarFile == null) throw new IllegalArgumentException("null file");
    return loadFromJar(new FileInputStream(jarFile),jarFile.toURI());
  }

  /**
   * Loads a Tylar directly from the stream. given jar file.  This method
   * parses all of the tylar's binding artifacts; if it doesn't throw an
   * exception, you can be sure that the tylars binding files and schemas are
   * valid.
   *
   * @param in input stream on the jar file.  This should NOT be a
   * JarInputStream
   * @param source uri from which the tylar was retrieved.  This is used
   * for informational purposes only, but it is required.
   * @return Handle to the tylar
   * @throws IOException
   */
  public static Tylar loadFromJar(InputStream in, URI source)
          throws IOException, XmlException
  {
    // FIXME this will be broken if the InputStream is already a
    // JarInputStream (or some InputStream in front of a JIS).
    // Gotta sort this out. pcal 12/18/03
    if (in == null) throw new IllegalArgumentException("null in");
    if (source == null) throw new IllegalArgumentException("null uri");
    HackJarInputStream hin = new HackJarInputStream(in);
    JarEntry entry;
    BindingFile bf = null;
    Collection schemas = null;
    if (VERBOSE) System.out.println("Reading jar entries");
    while ((entry = hin.getNextJarEntry()) != null) {
      if (entry.isDirectory()) continue;
      String name = normalizeEntryName(entry.getName());
      if (name.equals(BINDING_FILE_JARENTRY)) {
        if (VERBOSE) System.out.println("parsing binding file "+name);
        bf = BindingFile.forDoc(BindingConfigDocument.Factory.parse(hin));
      } else if (name.startsWith(SCHEMA_DIR_JARENTRY) &&
              name.endsWith(SCHEMA_EXT)) {
        if (schemas == null) schemas = new ArrayList();
        if (VERBOSE) System.out.println("parsing schema "+name);
        schemas.add(SchemaDocument.Factory.parse(hin));
      } else {
        if (VERBOSE) {
          System.out.println("ignoring unknown jar entry: "+name);
          System.out.println("  looking for "+BINDING_FILE_JARENTRY+" or "+
                             SCHEMA_DIR_JARENTRY);
        }
      }
      hin.closeEntry();
    }
    if (VERBOSE) System.out.println("Done reading jar entries");
    if (bf == null) {
      throw new IOException
              ("resource at '"+source+
               "' is not a tylar: it does not contain a binding file");
    }
    hin.reallyClose();
    return new TylarImpl(source,bf,schemas);
  }
  // ========================================================================
  // Private methods

  /**
   * Canonicalizes the given zip entry path so that we can look for what
   * we want without having to worry about different slashes or
   * leading slashes or anything else that can go wrong.
   */
  private static final String normalizeEntryName(String name) {
    name = name.toLowerCase().trim();
    for(int i=0; i<OTHER_SEPCHARS.length; i++) {
      name = name.replace(OTHER_SEPCHARS[i],SEPCHAR);
    }
    if (name.charAt(0) == SEPCHAR) name = name.substring(1);
    return name;
  }

  /**
   * Grab the contents of the current entry and stuffs them into a string -
   * sometimes useful for debugging.
   */
  /*
  private static String getEntryContents(JarInputStream in) throws IOException {
  StringWriter writer = new StringWriter();
  byte[] buffer = new byte[2056];
  int count = 0;
  while ((count = in.read(buffer, 0, buffer.length)) != -1) {
  writer.write(new String(buffer, 0, count));
  }
  if (VERBOSE) {
  System.out.println("=== ENTRY CONTENTS ===");
  System.out.println(writer.toString());
  System.out.println("=== ENTRY CONTENTS ===");
  }
  return writer.toString();
  }
  */

  /**
   * This is another hack around what I believe is an xbeans bug - it
   * closes the stream on us.  When we're reading out of a jar, we want
   * to parse a whole bunch of files from the same stream - this class
   * just intercepts the close call.
   */
  private static class HackJarInputStream extends JarInputStream {

    HackJarInputStream(InputStream in) throws IOException {
      super(in);
    }

    public void close() {}

    public void reallyClose() throws IOException {
      super.close();
    }
  }

}
