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
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.binding.tylar;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.SourceJavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.WriterFactory;
import org.apache.xmlbeans.XmlOptions;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

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
}