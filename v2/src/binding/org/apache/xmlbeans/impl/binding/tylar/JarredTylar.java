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

import java.io.*;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.XmlException;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * A tylar that has been loaded from a jar file.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JarredTylar implements Tylar {

  // ========================================================================
  // Constants

  private static final char[] OTHER_SEPCHARS = {'\\'};
  private static final char SEPCHAR = '/';

  private static final boolean VERBOSE = false;

  private static final String BINDING_FILE =
          normalizeEntryName(TylarConstants.BINDING_FILE);

  private static final String SCHEMA_DIR =
          normalizeEntryName(TylarConstants.SCHEMA_DIR);

  // ========================================================================
  // Variables

  private File mJarFile;
  private BindingFile mBindingFile = null;
  private Collection mSchemas = null;

  // ========================================================================
  // Factory methods

  /**
   * Loads a Tylar directly from the given jar file.  This method parses all
   * of the tylar's binding artifacts; if it doesn't throw an exception,
   * you can be sure that the tylars binding files and schemas are valid.
   *
   * @param jarFile file containing the tylar
   * @return Handle to the tylar
   * @throws IOException
   */
  public static Tylar load(File jarFile) throws IOException, XmlException {
    FileInputStream fin = new FileInputStream(jarFile);
    HackJarInputStream in = new HackJarInputStream(fin);
    JarEntry entry;
    BindingFile bf = null;
    Collection schemas = null;
    while ((entry = in.getNextJarEntry()) != null) {
      if (entry.isDirectory()) continue;
      String name = normalizeEntryName(entry.getName());
      if (name.equals(BINDING_FILE)) {
        if (VERBOSE) System.out.println("parsing binding file "+name);
        //FIXME this doesn't always work
        //  bf = BindingFile.forDoc(BindingConfigDocument.Factory.parse(in));
        // so we do this instead
        bf = BindingFile.forDoc(BindingConfigDocument.Factory.parse
                                (getEntryContents(in)));
      } else if (name.startsWith(SCHEMA_DIR)) {
        if (schemas == null) schemas = new ArrayList();
        if (VERBOSE) System.out.println("parsing schema "+name);
        //FIXME this doesn't work
        //   schemas.add(SchemaDocument.Factory.parse(in));
        // so we do this instead
        schemas.add(SchemaDocument.Factory.parse
                    (new StringReader(getEntryContents(in))));
      } else {
        if (VERBOSE) {
          System.out.println("ignoring unknown jar entry: "+name);
          System.out.println("  looking for "+BINDING_FILE+" or "+SCHEMA_DIR);
        }
      }
      in.closeEntry();
    }
    if (bf == null) {
      throw new IOException
            (jarFile+" is not a tylar: it does not contain a binding file");
    }
    in.reallyClose();
    return new JarredTylar(jarFile,bf,schemas);
  }


  public ClassLoader createClassLoader(ClassLoader parent) {
    try {
      return new URLClassLoader(new URL[] {mJarFile.toURL()},parent);
    } catch(MalformedURLException mue){
      throw new RuntimeException(mue); //FIXME this is bad
    }
  }

  // ========================================================================
  // Constructors

  /*package*/ JarredTylar(File jarFile,
                          BindingFile bf, //can be null
                          Collection schemas) { //can be null
    mJarFile = jarFile;
    mBindingFile = bf;
    mSchemas = schemas;
  }


  // ========================================================================
  // Tylar implementation

  public BindingFile getBindingFile() {
    return mBindingFile;
  }

  public SchemaDocument[] getSchemas() {
    if (mSchemas == null) return new SchemaDocument[0];
    SchemaDocument[] out = new SchemaDocument[mSchemas.size()];
    mSchemas.toArray(out);
    return out;
  }

  public URI getLocation() {
    return mJarFile.toURI();
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
   * This is a temporary hack around a problem I don't fully understand yet.
   * For some reason, the SchemaDocument.Factory and BindingDocument.Factory
   * parse methods don't deal very well with being handed the raw
   * JavaInputStream.  Instead, we spoonfeed them by building up the contents
   * into a buffer here and then handing it off to them.  Probably some
   * encoding thing, not sure what is going wrong, but at least this works
   * for now.
   *
   * The trace I get when thing go awry looks something like this:
   *
   * org.apache.xmlbeans.XmlException: error: Premature end of file.
   *    at org.apache.xmlbeans.impl.store.Root$SaxLoader.load(Root.java:802)
   *    at org.apache.xmlbeans.impl.store.Root.loadXml(Root.java:1075)
   *    at org.apache.xmlbeans.impl.store.Root.loadXml(Root.java:1061)
   * ...
   */
  private static String getEntryContents(JarInputStream in) throws IOException {
    StringWriter writer = new StringWriter();
    byte[] buffer = new byte[2056];
    int count = 0;
    while ((count = in.read(buffer, 0, buffer.length)) != -1) {
      writer.write(new String(buffer, 0, count));
    }
    if (VERBOSE) {
      System.out.println("=== SCHEMA CONTENTS ===");
      System.out.println(writer.toString());
      System.out.println("=== END SCHEMA CONTENTS ===");
    }
    return writer.toString();
  }

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
