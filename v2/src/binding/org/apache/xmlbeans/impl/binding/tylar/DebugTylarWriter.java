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

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.SourceJavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.WriterFactory;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.File;

/**
 * Implementation of TylarWriter which simply dumps everything it gets to some
 * Writer.  This can be useful for quick debugging.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DebugTylarWriter implements TylarWriter, WriterFactory {

  // ========================================================================
  // Variables

  private PrintWriter mOut;
  private JavaOutputStream mJoust;
  private XmlOptions mOptions;

  // ========================================================================
  // Constructors

  public DebugTylarWriter() {
    this(new PrintWriter(System.out,true));
  }

  public DebugTylarWriter(PrintWriter out) {
    mJoust = new SourceJavaOutputStream(this);
    mOut = out;
    mOptions = new XmlOptions();
    mOptions.setSavePrettyPrint();
  }

  // ========================================================================
  // Public methods

  /**
   * Utility method which writes the contents of the given tylar to our
   * PrintWriter.  Useful for debugging, provides a quick dump of the contents
   * of an arbitrary Tylar.
   *
   * @param t Tylar to dump
   * @throws IOException
   */
  public void write(Tylar t) throws IOException {
    mOut.println("==== Dumping Type Library contents... =================");
    mOut.println("location = "+t.getLocation());
    mOut.println("description = "+t.getDescription());
    BindingFile[] bfs = t.getBindingFiles();
    for(int i=0; i<bfs.length; i++) {
      mOut.println("---- Binding File -------------------------------------");
      writeBindingFile(bfs[i]);
    }
    SchemaDocument[] xsds = t.getSchemas();
    for(int i=0; i<xsds.length; i++) {
      mOut.println("---- Schema -------------------------------------------");
      writeSchema(xsds[i],null);
    }
    mOut.println("==== End Type Library contents ========================");
    mOut.flush();
  }

  // ========================================================================
  // TylarWriter implementation

  public void writeBindingFile(BindingFile bf) throws IOException {
    bf.write().save(mOut,mOptions);
  }

  public void writeSchema(SchemaDocument xsd, String fp) throws IOException {
    xsd.save(mOut,mOptions);
  }

  public void writeSchemaTypeSystem(SchemaTypeSystem sts) throws IOException {
    //FIXME implement me
  }

  public JavaOutputStream getJavaOutputStream() {
    return mJoust;
  }

  public void close() {
    mOut.flush();
  }

  // ========================================================================
  // WriterFactory implementation

  public Writer createWriter(String packageName, String className)
          throws IOException {
    return mOut;
  }

  // ========================================================================
  // main method

  public static void main(String[] args) {
    try {
      TylarLoader loader = DefaultTylarLoader.getInstance();
      Tylar tylar = loader.load(new File(args[0]).toURI());
      new DebugTylarWriter().write(tylar);
    } catch(Exception e) {
      e.printStackTrace();
    }
    System.out.flush();
  }
}