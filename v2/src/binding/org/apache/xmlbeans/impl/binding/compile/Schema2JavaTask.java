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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeSystem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Schema2JavaTask extends BindingCompilerTask {

  // =========================================================================
  // Variables

  private Path mXsdPath = null;
  private Path mClasspath = null;
  private List mXsdFiles = null;
  private Schema2Java mCompiler;

  // ========================================================================
  // Constructors

  public Schema2JavaTask() {
    mCompiler = new Schema2Java();
  }

  // =========================================================================
  // Task attributes

  /**
   * Sets whether this BindingCompiler should keep any generated java source
   * code it generates.  The default is true.  Note that not all
   * BindingCompilers generate any source code at all, so setting this may
   * have no effect.
   */
  public void setCompileJava(boolean b) {
    mCompiler.setCompileJava(b);
  }

  /**
   * Sets whether this BindingCompiler should keep any generated java source
   * code it generates.  The default is true.  This will have no effect if
   * doCompile is set to false.
   */
  public void setKeepGeneratedJava(boolean b) {
    mCompiler.setKeepGeneratedJava(b);
  }

  /**
   * Sets the location of javac to be invoked.  Default compiler is used
   * if this is not set.  Ignored if doCompile is set to false.
   */
  public void setJavac(String javacPath) {
    mCompiler.setJavac(javacPath);
  }

  /**
   * Sets the classpath to use for compilation of generated sources.
   * The System classpath is used by default.  This is ignored if doCompile is
   * false.
   */
  public void setJavacClasspath(File[] classpath) {
    mCompiler.setJavacClasspath(classpath);
  }

  /**
   * Set the source directories to find the source XSD files.
   */
  public void setSrcdir(Path srcDir) {//FIXME this is a bad name
    if (mXsdPath == null) {
      mXsdPath = srcDir;
    } else {
      mXsdPath.append(srcDir);
    }
  }

  /**
   * Adds a path for source compilation.
   *
   * @return a nested src element.
   */
  public Path createSrc() {
    if (mXsdPath == null) {
      mXsdPath = new Path(getProject());
    }
    return mXsdPath.createPath();
  }


  public void setClasspath(Path path) {
    if (mClasspath == null) {
      mClasspath = path;
    } else {
      mClasspath.append(path);
    }
  }

  public void setClasspathRef(Reference r) {
    createClasspath().setRefid(r);
  }

  public Path createClasspath() {
    if (mClasspath == null) {
      mClasspath = new Path(getProject());
    }
    return mClasspath.createPath();
  }

  // =========================================================================
  // BindingCompilerTask implementation

  /**
   * Based on the parameters set for this Task object, create an instance of
   * the Schema2Java compiler to be executed.
   */
  protected BindingCompiler createCompiler() throws BuildException {
    checkParameters();
    // scan source directories and dest directory for schemas to use
    startScan();
    String[] list = mXsdPath.list();
    for (int i = 0; i < list.length; i++) {
      File srcDir = getProject().resolveFile(list[i]);
      if (!srcDir.exists()) {
        throw new BuildException("srcdir \""
                                 + srcDir.getPath()
                                 + "\" does not exist!", getLocation());
      }
      DirectoryScanner ds = this.getDirectoryScanner(srcDir);
      String[] files = ds.getIncludedFiles();
      scanDir(srcDir, files);
    }
    //build up a schema type system from the input schemas and give it to
    //the compiler
    File[] xsdFiles = (File[]) mXsdFiles.toArray(new File[mXsdFiles.size()]);
    SchemaTypeSystem sts;
    try {
      sts = createSchemaTypeSystem(xsdFiles);
    } catch(IOException ioe) {
      throw new BuildException(ioe);
    } catch(XmlException xe) {
      throw new BuildException(xe);
    }
    mCompiler.setSchemaTypeSystem(sts);
    return mCompiler;
  }



  protected void startScan() {
    mXsdFiles = new ArrayList();
  }

  protected void scanDir(File srcDir, String[] files) {
    for (int i = 0; i < files.length; i++)
      if (files[i].endsWith(".xsd"))
        mXsdFiles.add(new File(srcDir, files[i]));
  }

  protected File[] namesToFiles(String[] names) {
    File[] result = new File[names.length];
    for (int i = 0; i < names.length; i++)
      result[i] = new File(names[i]);
    return result;
  }


  // =========================================================================
  // Private methods

  protected void checkParameters() throws BuildException {
    if (mXsdPath == null) {
      throw new BuildException("srcdir attribute must be set!",
                               getLocation());
    }
    if (mXsdPath.size() == 0) {
      throw new BuildException("srcdir attribute must be set!",
                               getLocation());
    }

  }

}
