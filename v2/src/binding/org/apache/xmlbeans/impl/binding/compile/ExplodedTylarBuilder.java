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
package org.apache.xmlbeans.impl.binding.compile;

import org.w3.x2001.xmlSchema.SchemaDocument;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.apache.xmlbeans.XmlOptions;
import org.apache.tools.ant.BuildException;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

/**
 * Simple implementation of TylarBuilder that just dumps everything in a
 * directory.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ExplodedTylarBuilder implements TylarBuilder {

  // =========================================================================
  // Constants

  private static final int XML_INDENT = 2;

  // =========================================================================
  // Variables

  private File mDir;
  private List mSchemas = new ArrayList();
  private List mBindings = new ArrayList();

  // =========================================================================
  // Constructors

  public ExplodedTylarBuilder(File dir) {
    if (dir == null) throw new IllegalArgumentException("null dir");
    mDir = dir;
  }

  // =========================================================================
  // TylarBuilder implementation

  public void buildTylar(Java2SchemaResult result) throws IOException {
    createTargetDir();
    // write schemas
    writeXsdFiles(result.getSchemas());
    // print the binding file
    writeBindingFile(result.getBindingFile());
  }

  protected void writeBindingFile(BindingFile bf) throws IOException {
    File file = new File(mDir, "binding-file.xml"); //FIXME naming
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      BindingConfigDocument doc = bf.write();
      doc.save(out,
               new XmlOptions().setSavePrettyPrint().
               setSavePrettyPrintIndent(XML_INDENT));
    } catch (IOException ioe) {
      throw ioe;
    } finally {
      try {
        out.close();
      } catch (IOException ohwell) {
        ohwell.printStackTrace();
      }
    }
  }

  protected void writeXsdFiles(SchemaDocument[] xsds) throws IOException {
    // print the schemas
    for (int i = 0; i < xsds.length; i++) {
      File file = new File(mDir, "schema-" + i + ".xsd");//FIXME naming
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(file);
        xsds[i].save(out,
                     new XmlOptions().setSavePrettyPrint().
                     setSavePrettyPrintIndent(XML_INDENT));
      } catch (IOException ioe) {
        throw ioe;
      } finally {
        try {
          out.close();
        } catch (Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  protected void writeJavaFiles(JavaCodeResult jcg) throws IOException {
    Collection classnames = jcg.getToplevelClasses();
    for (Iterator i = classnames.iterator(); i.hasNext();) {
      String className = (String) i.next();
      File javaFile = new File(mDir, className.replace('.', '/') + ".java");
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(javaFile);
        jcg.printSourceCode(className, out);
      } catch (IOException e) {
        throw e;
      } finally {
        try {
          out.close();
        } catch (Exception ohwell) {
          ohwell.printStackTrace();
        }
      }
    }
  }

  protected void createTargetDir() {
    if (!mDir.exists()) {
      if (!mDir.mkdirs()) {
        throw new IllegalArgumentException("failed to create dir " + mDir);
      }
    } else {
      if (!mDir.isDirectory())
        throw new IllegalArgumentException("not a directory: " + mDir);
    }
  }

  public void buildTylar(SchemaToJavaResult result) throws IOException {
    createTargetDir();

    // print the java files
    writeJavaFiles(result.getJavaCodeResult());

    // print the binding file
    writeBindingFile(result.getBindingFileResult());
  }

  /**
   * @deprecated I really think it is a bad idea for SchemaToJavaResult
   * to have the responsibility for printing out the binding file and java
   * code.  It should just return handles to things that get printed out
   * by the tylar builder.  I have already made this change on the
   * java->schema side.
   */
  private void writeBindingFile(BindingFileResult bfg) throws IOException
  {
    File file = new File(mDir, "binding-file.xml"); //FIXME naming
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      bfg.printBindingFile(out);
    }
    catch (IOException ioe) {
      throw ioe;
    }
    finally {
      try {
        out.close();
      }
      catch (IOException ohwell) {
        ohwell.printStackTrace();
      }
    }
  }
}