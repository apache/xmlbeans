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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
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
   * Sets the binding rules to JAX-RPC rules if true. By default, the
   * binding rules are set to XMLBeans rules, slightly different when
   * it comes to mapping built-in types and XML Names.
   */
  public void setJaxRpcRules(boolean jaxRpc) {
    mCompiler.setJaxRpcRules(jaxRpc);
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
  protected BindingCompiler getCompilerToExecute() throws BuildException {
    checkParameters();
    // scan source directories and dest directory for schemas to use
    startScan();
    String[] list = mXsdPath.list();
    Project p = getProject();
    for (int i = 0; i < list.length; i++) {
      File srcDir = (p == null) ? new File(list[i]) : p.resolveFile(list[i]);
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
