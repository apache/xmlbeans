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

package org.apache.xmlbeans.impl.binding.tylar;

import java.io.*;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * Default implementation of TylarLoader.  Currently, only directory and jar
 * tylars are supported.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DefaultTylarLoader implements TylarLoader, TylarConstants {

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
   *
   */
  public Tylar load(ClassLoader cl) throws IOException, XmlException {
    if (cl == null) throw new IllegalArgumentException("null stream");
    return new RuntimeTylar(cl);
  }


  // ========================================================================
  // Everything below this line is deprecated and will be removed ASAP


  private static final String FILE_SCHEME = "file";

  private static final char[] OTHER_SEPCHARS = {'\\'};

  private static final char SEPCHAR = '/';

  private static final boolean VERBOSE = false;

  private static final String BINDING_FILE_JARENTRY =
          normalizeEntryName(TylarConstants.BINDING_FILE).toLowerCase();

  private static final String SCHEMA_DIR_JARENTRY =
          normalizeEntryName(TylarConstants.SCHEMA_DIR).toLowerCase();

  private static final String SCHEMA_EXT = ".xsd";

  private static final String STS_PREFIX = "schema"+SEPCHAR+"system"+SEPCHAR;

  /**
   * Loads the tylar from the given uri.
   *
   * @param uri uri of where the tylar is stored.
   * @return
   * @throws IOException if an i/o error occurs while processing
   * @throws XmlException if an error occurs parsing the contents of the tylar.
   */
  /*
  public Tylar load(URI uri) throws IOException, XmlException
  {
    return load(new URL[]{new URL(uri.toString())});
  }

  public Tylar load(URI[] uris) throws IOException, XmlException {
    URL[] urls = new URL[uris.length];
    for(int i=0; i<uris.length; i++) {
      urls[i] = new URL(uris[i].toString());
    }
    return load(urls);
  }

  public Tylar load(JarInputStream jar) throws IOException, XmlException {
    if (jar == null) throw new IllegalArgumentException("null stream");
    return loadFromJar(jar,null);
  } */
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
    //String scheme = uri.getScheme();
    File file = null;
    try {
      file = new File(uri);
    } catch(Exception ignore) {}
    if (file != null && file.exists() && file.isDirectory()) {
      return ExplodedTylarImpl.load(file);
    } else {
      return loadFromJar(new JarInputStream(uri.toURL().openStream()),uri);
    }
  }

  public Tylar load(URI[] uris) throws IOException, XmlException {
    Tylar[] tylars = new Tylar[uris.length];
    for(int i=0; i<tylars.length; i++) {
      tylars[i] = load(uris[i]);
    }
    return new CompositeTylar(tylars);
  }

  public Tylar load(JarInputStream jar) throws IOException, XmlException {
    if (jar == null) throw new IllegalArgumentException("null stream");
    return loadFromJar(jar,null);
  }

  // ========================================================================
  // Private methods

  /**
   * Loads a Tylar directly from the stream. given jar file.  This method
   * parses all of the tylar's binding artifacts; if it doesn't throw an
   * exception, you can be sure that the tylars binding files and schemas are
   * valid.
   *
   * @param jin input stream on the jar file.  This should NOT be a
   * JarInputStream
   * @param source uri from which the tylar was retrieved.  This is used
   * for informational purposes only and is not required.
   * @return Handle to the tylar
   * @throws IOException
   */
  protected static Tylar loadFromJar(JarInputStream jin, URI source)
          throws IOException, XmlException
  {
    if (jin == null) throw new IllegalArgumentException("null stream");
    //FIXME in the case where sourceURI is null, we could look in the
    //manifest or someplace to try to get at least some useful information
    JarEntry entry;
    BindingFile bf = null;
    Collection schemas = null;
    StubbornInputStream stubborn = new StubbornInputStream(jin);
    String stsName = null;
    while ((entry = jin.getNextJarEntry()) != null) {
      String name = normalizeEntryName(entry.getName());
      if (name.endsWith(""+SEPCHAR)) {
        if (name.startsWith(STS_PREFIX) &&
          name.length() > STS_PREFIX.length()) {
          // the name of the sts is the name of the only directory under
          // schema/system
          stsName = STS_PACKAGE+"."+name.substring(STS_PREFIX.length(),name.length()-1);
          if (VERBOSE) System.out.println("sts name is "+stsName);
        }
        continue;
      }
      name = name.toLowerCase();
      if (name.equals(BINDING_FILE_JARENTRY)) {
        if (VERBOSE) System.out.println("parsing binding file "+name);
        bf = BindingFile.forDoc(BindingConfigDocument.Factory.parse(stubborn));
      } else if (name.startsWith(SCHEMA_DIR_JARENTRY) &&
              name.endsWith(SCHEMA_EXT)) {
        if (schemas == null) schemas = new ArrayList();
        if (VERBOSE) System.out.println("parsing schema "+name);
        schemas.add(SchemaDocument.Factory.parse(stubborn));
      } else {
        if (VERBOSE) {
          System.out.println("ignoring unknown jar entry: "+name);
          System.out.println("  looking for "+BINDING_FILE_JARENTRY+" or "+
                             SCHEMA_DIR_JARENTRY);
        }
      }
      jin.closeEntry();
    }
    if (VERBOSE) System.out.println("Done reading jar entries");
    if (bf == null) {
      throw new IOException
              ("resource at '"+source+
               "' is not a tylar: it does not contain a binding file");
    }
    jin.close();
    if (VERBOSE) System.out.println("Done reading jar entries");
    SchemaTypeSystem sts = null;
    if (stsName != null && source != null) {
      {
        try {
          URLClassLoader ucl = new URLClassLoader(new URL[] {source.toURL()});
          sts = SchemaTypeSystemImpl.forName(stsName,ucl);
          if (sts == null) throw new IllegalStateException("null returned by SchemaTypeSystemImpl.forName()");
          if (VERBOSE) System.out.println("successfully loaded schema type system");
        } catch(Exception e) {
          ExplodedTylarImpl.showXsbError(e,source,"read",TylarConstants.SHOW_XSB_ERRORS);
        }
      }
    }
    return new TylarImpl((source == null) ? null : new URL[]{source.toURL()},
                         bf,schemas,sts);
  }
  // ========================================================================
  // Private methods

  /**
   * Canonicalizes the given zip entry path so that we can look for what
   * we want without having to worry about different slashes or
   * leading slashes or anything else that can go wrong.
   */
  private static final String normalizeEntryName(String name) {
    name = name.trim();
    for(int i=0; i<OTHER_SEPCHARS.length; i++) {
      name = name.replace(OTHER_SEPCHARS[i],SEPCHAR);
    }
    if (name.charAt(0) == SEPCHAR) name = name.substring(1);
    return name;
  }


  /**
   * This is another hack around what I believe is an xbeans bug - it
   * closes the stream on us.  When we're reading out of a jar, we want
   * to parse a whole bunch of files from the same stream - this class
   * just intercepts the close() call and ignores it until we call
   * reallyClose().
   */
  private static class StubbornInputStream extends FilterInputStream {

    StubbornInputStream(InputStream in) { super(in); }

    public void close() {}

    public void reallyClose() throws IOException {
      super.close();
    }
  }

  // ========================================================================
  // Private methods

  /**
   * Loads a Tylar directly from the stream. given jar file.  This method
   * parses all of the tylar's binding artifacts; if it doesn't throw an
   * exception, you can be sure that the tylars binding files and schemas are
   * valid.
   *
   * @param jin input stream on the jar file.  This should NOT be a
   * JarInputStream
   * @param source uri from which the tylar was retrieved.  This is used
   * for informational purposes only and is not required.
   * @return Handle to the tylar
   * @throws IOException

  protected static Tylar loadFromJar(JarInputStream jin, URI source)
          throws IOException, XmlException
  {
    if (jin == null) throw new IllegalArgumentException("null stream");
    //FIXME in the case where sourceURI is null, we could look in the
    //manifest or someplace to try to get at least some useful information
    JarEntry entry;
    BindingFile bf = null;
    Collection schemas = null;
    StubbornInputStream stubborn = new StubbornInputStream(jin);
    String stsName = null;
    while ((entry = jin.getNextJarEntry()) != null) {
      String name = normalizeEntryName(entry.getName());
      if (name.endsWith(""+SEPCHAR)) {
        if (name.startsWith(STS_PREFIX) &&
          name.length() > STS_PREFIX.length()) {
          // the name of the sts is the name of the only directory under
          // schema/system
          stsName = STS_PACKAGE+"."+name.substring(STS_PREFIX.length(),name.length()-1);
          if (VERBOSE) System.out.println("sts name is "+stsName);
        }
        continue;
      }
      name = name.toLowerCase();
      if (name.equals(BINDING_FILE_JARENTRY)) {
        if (VERBOSE) System.out.println("parsing binding file "+name);
        bf = BindingFile.forDoc(BindingConfigDocument.Factory.parse(stubborn));
      } else if (name.startsWith(SCHEMA_DIR_JARENTRY) &&
              name.endsWith(SCHEMA_EXT)) {
        if (schemas == null) schemas = new ArrayList();
        if (VERBOSE) System.out.println("parsing schema "+name);
        schemas.add(SchemaDocument.Factory.parse(stubborn));
      } else {
        if (VERBOSE) {
          System.out.println("ignoring unknown jar entry: "+name);
          System.out.println("  looking for "+BINDING_FILE_JARENTRY+" or "+
                             SCHEMA_DIR_JARENTRY);
        }
      }
      jin.closeEntry();
    }
    if (VERBOSE) System.out.println("Done reading jar entries");
    if (bf == null) {
      throw new IOException
              ("resource at '"+source+
               "' is not a tylar: it does not contain a binding file");
    }
    jin.close();
    if (VERBOSE) System.out.println("Done reading jar entries");
    SchemaTypeSystem sts = null;
    if (stsName != null && source != null) {
      {
        try {
          URLClassLoader ucl = new URLClassLoader(new URL[] {source.toURL()});
          sts = SchemaTypeSystemImpl.forName(stsName,ucl);
          if (sts == null) throw new IllegalStateException("null returned by SchemaTypeSystemImpl.forName()");
          if (VERBOSE) System.out.println("successfully loaded schema type system");
        } catch(Exception e) {
          ExplodedTylarImpl.showXsbError(e,source,"read",TylarConstants.SHOW_XSB_ERRORS);
        }
      }
    }
    return new TylarImpl(source,bf,schemas,sts);
  }
   */

  // ========================================================================
  // Private methods
/*
  private static Tylar load(ClassLoader loader,
                            String stsName,
                            String[] xsds,
                            URI source)
    throws XmlException, IOException
  {
    SchemaTypeSystem sts = null;
    BindingFile bf = null;
    {
      InputStream in = loader.getResourceAsStream(BINDING_FILE_JARENTRY);
      bf = BindingFile.forDoc(BindingConfigDocument.Factory.parse(in));
    }
    if (stsName != null) {
      try {
      sts = SchemaTypeSystemImpl.forName(stsName,loader);
      } catch(Exception e) {
        ExplodedTylarImpl.showXsbError(e,source,"read",TylarConstants.SHOW_XSB_ERRORS);
      }
    }
    if (sts == null) {

    }



  }
  */

  /**
   * Canonicalizes the given zip entry path so that we can look for what
   * we want without having to worry about different slashes or
   * leading slashes or anything else that can go wrong.

  private static final String normalizeEntryName(String name) {
    name = name.trim();
    for(int i=0; i<OTHER_SEPCHARS.length; i++) {
      name = name.replace(OTHER_SEPCHARS[i],SEPCHAR);
    }
    if (name.charAt(0) == SEPCHAR) name = name.substring(1);
    return name;
  }
   */

  /**
   * This is another hack around what I believe is an xbeans bug - it
   * closes the stream on us.  When we're reading out of a jar, we want
   * to parse a whole bunch of files from the same stream - this class
   * just intercepts the close() call and ignores it until we call
   * reallyClose().

  private static class StubbornInputStream extends FilterInputStream {

    StubbornInputStream(InputStream in) { super(in); }

    public void close() {}

    public void reallyClose() throws IOException {
      super.close();
    }
  }
   */
  /**
   * Grab the contents of the current entry and stuffs them into a string -
   * sometimes useful for debugging.
   */
  /*
  private static String getEntryContents(JarInputStream in) throws IOException {
  StringWriter output = new StringWriter();
  byte[] buffer = new byte[2056];
  int count = 0;
  while ((count = in.read(buffer, 0, buffer.length)) != -1) {
  output.write(new String(buffer, 0, count));
  }
  if (VERBOSE) {
  System.out.println("=== ENTRY CONTENTS ===");
  System.out.println(output.toString());
  System.out.println("=== ENTRY CONTENTS ===");
  }
  return output.toString();
  }
  */

}
