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
import java.util.ArrayList;
import java.util.Collection;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.FileWriterFactory;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.SourceJavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.ValidatingJavaOutputStream;
import org.apache.xmlbeans.impl.common.JarHelper;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * Concrete implementation of ExplodedTylar - a tylar which exists in an open
 * directory structure on disk.  Note that this class also implements
 * TylarWriter, which allows the compile time to build up the tylar files
 * and then hand them directly to the runtime if desired.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ExplodedTylarImpl
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
  private JavaOutputStream mJoust;
  private Collection mSchemaDocuments = null;

  // ========================================================================
  // Factory methods

  /**
   * Creates a new tylar from the given directory.  The directory must exist
   * or be creatable.  The default JavaOutputStream will be used for codegen.
   */
  public static ExplodedTylarImpl create(File dir) throws IOException {
    return create(dir, createDefaultJoust(dir));
  }

  /**
   * Loads a tylar from the given directory.  The directory must exist
   * and contain at least a binding file.  The default JavaOutputStream
   * will be used for codegen.
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
      if (dir.isFile()) throw new IOException("already a file at '" + dir + "'");
    } else {
      if (!dir.mkdirs()) throw new IOException("Failed to create " + dir);
    }
    return new ExplodedTylarImpl(dir, null, null, joust);
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
    return new ExplodedTylarImpl(dir, bf, schemas, joust);
  }

  // ========================================================================
  // Constructors

  /**
   * Constructs a new ExplodedTylarImpl in the given directory and using
   * the given JavaOutputStream.
   */
  private ExplodedTylarImpl(File dir, // must exist
                            BindingFile bindingFile, // null ok
                            Collection schemas, // null ok
                            JavaOutputStream joust)    // null ok
  {
    mRootDir = dir;
    mSourceRoot = new File(mRootDir, SRC_ROOT);
    mSchemaDir = new File(mRootDir, SCHEMA_DIR);
    mJoust = joust;
    mBindingFile = bindingFile;
    mSchemaDocuments = schemas;
  }


  // ========================================================================
  // TylarWriter implementation

  public void writeBindingFile(BindingFile bf) throws IOException {
    mBindingFile = bf;
    writeBindingFile(bf, new File(mRootDir, BINDING_FILE));
  }

  public void writeSchema(SchemaDocument xsd, String schemaFileName)
          throws IOException {
    if (mSchemaDocuments == null) mSchemaDocuments = new ArrayList();
    mSchemaDocuments.add(xsd);
    writeXsd(xsd, new File(mSchemaDir, schemaFileName));
  }

  public JavaOutputStream getJavaOutputStream() {
    return mJoust;
  }

  // ========================================================================
  // Tylar implementation

  public BindingFile getBindingFile() {
    return mBindingFile;
  }

  public SchemaDocument[] getSchemas() {
    if (mSchemaDocuments == null) return new SchemaDocument[0];
    SchemaDocument[] out = new SchemaDocument[mSchemaDocuments.size()];
    mSchemaDocuments.toArray(out);
    return out;
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
    j.jarDir(mRootDir,jarfile);
    return new JarredTylar(jarfile,mBindingFile,mSchemaDocuments);
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
      return new URLClassLoader(new URL[] {mSourceRoot.toURL()},parent);
    } catch(MalformedURLException mue){
      throw new RuntimeException(mue); //FIXME this is bad
    }
  }

  // ========================================================================
  // Private methods

  private static JavaOutputStream createDefaultJoust(File dir) {
    return new ValidatingJavaOutputStream
            (new SourceJavaOutputStream(new FileWriterFactory(dir)));
  }

  private static void parseSchemas(File schemaDir, Collection out)
          throws IOException, XmlException {
    File[] xsds = schemaDir.listFiles();
    for (int i = 0; i < xsds.length; i++) {
      if (VERBOSE) System.out.println("parsing "+xsds[i]);
      out.add(parseXsd(xsds[i]));
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