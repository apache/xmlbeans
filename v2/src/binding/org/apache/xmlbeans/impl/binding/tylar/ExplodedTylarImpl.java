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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.FileWriterFactory;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.SourceJavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.ValidatingJavaOutputStream;
import org.apache.xmlbeans.impl.common.JarHelper;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;
import org.apache.xmlbeans.impl.tool.SchemaCodeGenerator;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * Concrete implementation of ExplodedTylar - a tylar which exists in an open
 * directory structure on disk.  Note that this class also implements
 * TylarWriter, which allows the compile time to build up the tylar files
 * and then hand them directly to the runtime if desired.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ExplodedTylarImpl extends BaseTylarImpl
        implements TylarConstants, ExplodedTylar, TylarWriter {

  // ========================================================================
  // Constants

  private static final int XML_INDENT = 2;
  private static final boolean VERBOSE = false;

  // ========================================================================
  // Variables

  private File mRootDir;
  private File mSourceRoot;
  private File mSchemaDir;
  private BindingFile mBindingFile = null;
  private JavaOutputStream mJoust = null;
  private Collection mSchemaDocuments = null;
  private SchemaTypeSystem mSchemaTypeSystem = null;

  // ========================================================================
  // Factory methods

  /**
   * Creates a new tylar from the given directory.  The directory must exist
   * or be creatable.  The default JavaOutputStream will be used for output.
   */
  public static ExplodedTylarImpl create(File dir) throws IOException {
    return create(dir, createDefaultJoust(dir));
  }

  /**
   * Loads a tylar from the given directory.  The directory must exist
   * and contain at least a binding file.  The default JavaOutputStream
   * will be used for output.
   */
  public static ExplodedTylarImpl load(File dir)
          throws IOException, XmlException {
    return load(dir, createDefaultJoust(dir));
  }

  /**
   * Creates a new tylar from the given directory.  The directory must exist
   * or be creatable.
   */
  public static ExplodedTylarImpl create(File dir, JavaOutputStream joust)
          throws IOException {
    if (dir.exists()) {
      if (dir.isFile()) throw new IOException("Already a file at " + dir);
    } else {
      if (!dir.mkdirs()) throw new IOException("Failed to create " + dir);
    }
    return new ExplodedTylarImpl(dir, null, null, null, joust);
  }

  /**
   * Loads a tylar from the given directory.  The directory must exist
   * and contain at least a binding file.
   */
  public static ExplodedTylarImpl load(File dir, JavaOutputStream joust)
          throws IOException, XmlException {
    if (dir.exists()) {
      if (dir.isFile()) throw new IOException(dir + " is a file");
    } else {
      throw new IOException("No such directory " + dir);
    }
    BindingFile bf = parseBindingFile(new File(dir, BINDING_FILE));
    Collection schemas = new ArrayList();
    parseSchemas(new File(dir, SCHEMA_DIR), schemas);
    SchemaTypeSystem sts = null;
    {
      //try to read the xsbs.  first we need to figure out the sts name.
      try {
        File stsDir = new File(dir, "schema"+SEP+"system");
        String stsName = stsDir.list()[0];
        ClassLoader cl = new URLClassLoader(new URL[]{dir.toURL()});
        sts = SchemaTypeSystemImpl.forName(STS_PACKAGE+"."+stsName,cl);
        if (sts == null) throw new IllegalStateException("null returned by SchemaTypeSystemImpl.forName()");
        if (VERBOSE) System.out.println
          ("[XBEANS] loaded schema type system '"+stsName+"'");
      } catch(Exception e) {
        showXsbError(e,dir.toURI(),"read",TylarConstants.SHOW_XSB_ERRORS);
      }
    }

    return new ExplodedTylarImpl(dir, bf, schemas, sts, joust);
  }

  // ========================================================================
  // Constructors

  /**
   * Constructs a new ExplodedTylarImpl in the given directory and using
   * the given JavaOutputStream.
   */
  private ExplodedTylarImpl(File dir, // must exist
                            BindingFile bindingFile, // null ok
                            Collection schemas, // null ok,
                            SchemaTypeSystem sts, // null ok
                            JavaOutputStream joust)    // null ok
  {
    mRootDir = dir;
    mSourceRoot = new File(mRootDir, SRC_ROOT);
    mSchemaDir = new File(mRootDir, SCHEMA_DIR);
    mJoust = joust;
    mBindingFile = bindingFile;
    mSchemaDocuments = schemas;
    mSchemaTypeSystem = sts;
  }


  // ========================================================================
  // TylarWriter implementation

  public void writeBindingFile(BindingFile bf) throws IOException {
    mBindingFile = bf;
    writeBindingFile(bf, new File(mRootDir, BINDING_FILE));
  }

  public void writeSchema(SchemaDocument xsd, String schemaFileName)
          throws IOException {
    if (schemaFileName == null) {
      throw new IllegalArgumentException("null schemaFileName");
    }
    if (mSchemaDocuments == null) mSchemaDocuments = new ArrayList();
    mSchemaDocuments.add(xsd);
    writeXsd(xsd, new File(mSchemaDir, schemaFileName));
  }

  public void writeSchemaTypeSystem(SchemaTypeSystem sts) throws IOException {
    if (sts == null) throw new IllegalArgumentException("null sts");
    mSchemaTypeSystem = sts;
    try {
      SchemaCodeGenerator.saveTypeSystem(sts,
                                         mRootDir,
                                         null,null,null);
    } catch(Exception e) {
      showXsbError(e,mRootDir.toURI(),"write",TylarConstants.SHOW_XSB_ERRORS);
    }
  }

  public JavaOutputStream getJavaOutputStream() {
    return mJoust;
  }

  public void close() throws IOException {
    if (mJoust != null) mJoust.close();
  }


  // ========================================================================
  // Public static methods

  public static void showXsbError(Throwable e,
                                  URI where,
                                  String readOrWrite,
                                  boolean showTrace)
  {
    System.out.println
      ("[XBEANS] Notice: an unexpected error occurred while trying to\n "+
       readOrWrite+" a binary version of your schemas"+
       ((where == null) ? "." : " at\n"+where+"\n")+
       "\nYour bindings are ok, but you may suffer some performance\n" +
       "degradation if your schemas are very large or complicated.\n"+
       "(exception is "+e.getClass()+", message is '"+e.getMessage()+"'");
    if (showTrace) e.printStackTrace();
    System.out.flush();
  }

  // ========================================================================
  // Tylar implementation

  public BindingFile[] getBindingFiles() {
    return new BindingFile[] {mBindingFile};
  }

  public SchemaDocument[] getSchemas() {
    if (mSchemaDocuments == null) return new SchemaDocument[0];
    SchemaDocument[] out = new SchemaDocument[mSchemaDocuments.size()];
    mSchemaDocuments.toArray(out);
    return out;
  }

  public SchemaTypeLoader getSchemaTypeLoader() {
    if (mSchemaTypeSystem == null) {
      mSchemaTypeSystem = getDefaultSchemaTypeSystem();
    }
    return mSchemaTypeSystem;
  }

  public URI getLocation() {
    return mRootDir.toURI();
  }

  //not sure we ever need this
  public void resetCaches() {
    mSchemaDocuments = null;
    mBindingFile = null;
  }

  // ========================================================================
  // ExplodedTylar implementation

  /**
   * Returns the directory on disk in which the tylar is stored.  Never
   * returns null.
   */
  public File getRootDir() {
    return mRootDir;
  }

  public Tylar toJar(File jarfile) throws IOException {
    JarHelper j = new JarHelper();
    jarfile.getParentFile().mkdirs(); //make sure the path exists
    j.jarDir(mRootDir,jarfile);
    return new TylarImpl(jarfile.toURI(),mBindingFile,mSchemaDocuments);
  }

  public File getSourceDir() {
    return mSourceRoot;
  }

  public File getClassDir() {
    return mRootDir;
  }

  public File getSchemaDir() {
    return mSchemaDir;
  }

  public ClassLoader createClassLoader(ClassLoader parent) {
    try {
      return new URLClassLoader(new URL[] {mRootDir.toURL()},parent);
    } catch(MalformedURLException mue){
      throw new RuntimeException(mue); //FIXME this is bad
    }
  }

  // ========================================================================
  // Private methods

  private static JavaOutputStream createDefaultJoust(File dir) {
    File srcDir = new File(dir,TylarConstants.SRC_ROOT);
    return new ValidatingJavaOutputStream
            (new SourceJavaOutputStream(new FileWriterFactory(srcDir)));
  }

  private static SchemaTypeSystem buildSts(Collection xsds)
    throws XmlException
  {
    SchemaDocument[] x = new SchemaDocument[xsds.size()];
    xsds.toArray(x);
    return XmlBeans.compileXsd(x,XmlBeans.getBuiltinTypeSystem(),null);
  }

  private static void parseSchemas(File schemaDir, Collection out)
          throws IOException, XmlException {
    File[] xsds = schemaDir.listFiles();
    if (xsds != null) {
      for (int i = 0; i < xsds.length; i++) {
        if (VERBOSE) System.out.println("parsing "+xsds[i]);
        out.add(parseXsd(xsds[i]));
      }
    }
  }

  private static SchemaDocument parseXsd(File file)
          throws IOException, XmlException {
    FileReader in = null;
    try {
      in = new FileReader(file);
      return SchemaDocument.Factory.parse(in);
    } catch (IOException ioe) {
      throw ioe;
    } catch (XmlException xe) {
      throw xe;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  private static void writeXsd(SchemaDocument xsd, File file)
          throws IOException {
    FileOutputStream out = null;
    try {
      file.getParentFile().mkdirs();
      out = new FileOutputStream(file);
      xsd.save(out,
               new XmlOptions().setSavePrettyPrint().
               setSavePrettyPrintIndent(XML_INDENT));
    } catch (IOException ioe) {
      throw ioe;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  private static BindingFile parseBindingFile(File file)
          throws IOException, XmlException {
    FileReader in = null;
    try {
      in = new FileReader(file);
      return BindingFile.forDoc(BindingConfigDocument.Factory.parse(in));
    } catch (IOException ioe) {
      throw ioe;
    } catch (XmlException xe) {
      throw xe;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  private static void writeBindingFile(BindingFile bf, File file)
          throws IOException {
    FileWriter out = null;
    try {
      file.getParentFile().mkdirs();
      out = new FileWriter(file);
      BindingConfigDocument doc = bf.write();
      doc.save(out,
               new XmlOptions().setSavePrettyPrint().
               setSavePrettyPrintIndent(XML_INDENT));
      out.flush();
    } catch (IOException ioe) {
      throw ioe;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }
}