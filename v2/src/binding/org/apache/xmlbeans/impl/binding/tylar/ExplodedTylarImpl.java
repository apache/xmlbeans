package org.apache.xmlbeans.impl.binding.tylar;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.*;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.w3.x2001.xmlSchema.SchemaDocument;
import java.io.*;
import java.util.Collection;
import java.util.ArrayList;
import java.net.URI;

/**
 * Concrete implementation of ExplodedTylar - a tylar which exists in an open
 * directory structure on disk.  Note that this class also implements
 * TylarWriter, which allows the compile time to build up the tylar files
 * and then hand them directly to the runtime if desired.
 */
public class ExplodedTylarImpl
        implements TylarConstants, ExplodedTylar, TylarWriter
 {

  // ========================================================================
  // Constants

  private static final int XML_INDENT = 2;

  // ========================================================================
  // Variables

  private File mRootDir;
  private File mSourceRoot;
  private File mSchemaDir;
  private BindingFile mBindingFile = null;
  private JavaOutputStream mJoust;
  private Collection mSchemaDocuments = null;

  // ========================================================================
  // Constructors

  /**
   * Constructs a new ExplodedTylarImpl in the given directory.  The
   * default JavaOutputStream is used, which simply writes java sources
   * into the 'src' directory of the tylar.
   */
  public ExplodedTylarImpl(File dir) {
    this(dir,null);
    mJoust = new ValidatingJavaOutputStream
            (new SourceJavaOutputStream(new FileWriterFactory(mSourceRoot)));
  }

  /**
   * Constructs a new ExplodedTylarImpl in the given directory and using
   * the given JavaOutputStream.
   */
  public ExplodedTylarImpl(File dir, JavaOutputStream joust) {
    if (dir.exists() && dir.isFile()) {
      throw new IllegalArgumentException("already a file at '"+dir+"'");
    }
    dir.mkdirs();
    mRootDir = dir;
    mSourceRoot = new File(mRootDir,SRC_ROOT);
    mSchemaDir = new File(mRootDir,SCHEMA_DIR);
    mJoust = joust;
  }


  // ========================================================================
  // TylarWriter implementation

  public void writeBindingFile(BindingFile bf) throws IOException {
    mBindingFile = bf;
    writeBindingFile(bf,new File(mRootDir,BINDING_FILE));
  }

  public void writeSchema(SchemaDocument xsd, String schemaFileName)
          throws IOException
  {
    if (mSchemaDocuments == null) mSchemaDocuments = new ArrayList();
    mSchemaDocuments.add(xsd);
    writeXsd(xsd,new File(mSchemaDir,schemaFileName));
  }

  public JavaOutputStream getJavaOutputStream() {
    return mJoust;
  }

  // ========================================================================
  // Tylar implementation

  public BindingFile getBindingFile() throws IOException, XmlException {
    if (mBindingFile == null) {
      mBindingFile = parseBindingFile(new File(mRootDir, BINDING_FILE));
    }
    return mBindingFile;
  }

  public SchemaDocument[] getSchemas() throws IOException, XmlException {
    if (mSchemaDocuments == null) {
      mSchemaDocuments = new ArrayList();
      parseSchemas(mSchemaDir,mSchemaDocuments);
    }
    SchemaDocument[] out = new SchemaDocument[mSchemaDocuments.size()];
    mSchemaDocuments.toArray(out);
    return out;
  }

  public URI getLocation() { return mRootDir.toURI(); }

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
  public File getRootDir() { return mRootDir; }

  public TylarConstants toJar(File jarfile) throws IOException {
    throw new RuntimeException("NYI");
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

  // ========================================================================
  // Private methods

  private static void parseSchemas(File schemaDir, Collection out)
          throws IOException, XmlException
  {
    File[] xsds = schemaDir.listFiles();
    for(int i=0; i<xsds.length; i++) {
      out.add(parseXsd(xsds[i]));
    }
  }

  private static SchemaDocument parseXsd(File file)
          throws IOException, XmlException
  {
    FileReader in = null;
    try {
      in = new FileReader(file);
      return SchemaDocument.Factory.parse(in);
    } catch(IOException ioe) {
      throw ioe;
    } catch(XmlException xe) {
      throw xe;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch(Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  private static void writeXsd(SchemaDocument xsd, File file)
          throws IOException
  {
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
        } catch(Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  private static BindingFile parseBindingFile(File file)
          throws IOException, XmlException
  {
    FileReader in = null;
    try {
      in = new FileReader(file);
      return BindingFile.forDoc(BindingConfigDocument.Factory.parse(in));
    } catch(IOException ioe) {
      throw ioe;
    } catch(XmlException xe) {
      throw xe;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch(Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  private static void writeBindingFile(BindingFile bf, File file)
          throws IOException
  {
    FileWriter out = null;
    try {
      file.getParentFile().mkdirs();
      out = new FileWriter(file);
      BindingConfigDocument doc = bf.write();
      doc.save(out,
               new XmlOptions().setSavePrettyPrint().
               setSavePrettyPrintIndent(XML_INDENT));
      out.flush();
    } catch(IOException ioe) {
      throw ioe;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch(Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }
}