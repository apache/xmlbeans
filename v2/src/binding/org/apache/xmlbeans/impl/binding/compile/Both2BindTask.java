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

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Both2BindTask extends BindingCompilerTask {

  // =========================================================================
  // Variables

  private Both2Bind mCompiler;
  private Path mSrc = null;
  private Path mClasspath = null;
  private List mXsdFiles = null;
  private List mJavaFiles = null;
  private List mSchemaFilesets = new ArrayList();
  private File mSchema = null;

  // ========================================================================
  // Constructors

  public Both2BindTask() {
    // create the BindingCompiler object that we're going to populate and
    // return in createCompiler()
    mCompiler = new Both2Bind();
  }

  // =========================================================================
  // BindingCompilerTask implementation

  protected BindingCompiler getCompilerToExecute() throws BuildException {
    // validate some parameters
    if (mSrc == null || mSrc.size() == 0) {
      throw new BuildException("srcdir attribute must be set!",
              getLocation());
    }
    // scan source directories and dest directory to build up
    startScan();
    String[] list = mSrc.list();
    for (int i = 0; i < list.length; i++) {
      File srcDir = getProject().resolveFile(list[i]);
      if (!srcDir.exists()) {
        throw new BuildException("srcdir \""
                + srcDir.getPath()
                + "\" does not exist!", getLocation());
      }
      DirectoryScanner ds = this.getDirectoryScanner(srcDir);
      String[] files = ds.getIncludedFiles();
      scanJavaDir(srcDir, files);
    }
    // now scan XSD files
    // single file
    if (mSchema != null) {
      if (!mSchema.exists())
        throw new BuildException("schema " + mSchema + " does not exist!", getLocation());
      mXsdFiles.add(mSchema);
    }

    for (int i = 0; i < mSchemaFilesets.size(); i++) {
      scanSchemaFileset((FileSet) mSchemaFilesets.get(i));
    }
    File[] xsdFiles = (File[]) mXsdFiles.toArray(new File[mXsdFiles.size()]);
    File[] javaFiles = (File[]) mJavaFiles.toArray(new File[mJavaFiles.size()]);

    // bind
    try {
      String cp = (mClasspath == null) ? null : mClasspath.toString();
      //FIXME when we allow them to set up a base tylar, we need to take
      //those loaders into account here
      mCompiler.setSchemaTypesToMatch(createSchemaTypeSystem(xsdFiles));
      mCompiler.setJavaTypesToMatch(loadJClasses(javaFiles,cp));
    } catch (IOException e) {
      log(e.getMessage());
      throw new BuildException(e);
    } catch (XmlException e) {
      log(e.getMessage());
      throw new BuildException(e);
    }
    return mCompiler;
  }

  // =========================================================================
  // Task attributes

  /**
   * Set the source directories to find the source Java files.
   */
  public void setSrcdir(Path srcDir) {
    if (mSrc == null) {
      mSrc = srcDir;
    } else {
      mSrc.append(srcDir);
    }
  }

  /**
   * Sets a single schema.
   */
  public void setSchema(File file) {
    mSchema = file;
  }

  /**
   * Adds a fileset for source XSD files
   */
  public void addSchema(FileSet fileSet) {
    mSchemaFilesets.add(fileSet);
  }

  /**
   * Sets the typematcher to use.  Must be a fully-qualified
   * class name for a class that implements the TypeMatcher interface.
   */
  public void setTypeMatcher(String typeMatcherClassName) {
    if (typeMatcherClassName != null) {
      try {
        Class mclass = Class.forName(typeMatcherClassName);
        Object matcher = mclass.newInstance();
        if (!(matcher instanceof TypeMatcher)) {
          throw new BuildException(typeMatcherClassName+" does not implement "+
                  TypeMatcher.class.getName());
        }
        mCompiler.setTypeMatcher((TypeMatcher)matcher);
        log("both2Bind using matcher class " + typeMatcherClassName);
      } catch(ClassNotFoundException cnfe){
        throw new BuildException(cnfe);
      } catch(InstantiationException ie) {
        throw new BuildException(ie);
      } catch(IllegalAccessException iae) {
        throw new BuildException(iae);
      }
    }
  }

  /**
   * Adds a path for source compilation.
   *
   * @return a nested src element.
   */
  public Path createSrc() {
    if (mSrc == null) {
      mSrc = new Path(getProject());
    }
    return mSrc.createPath();
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
  // Task implementation

  protected void startScan() {
    mXsdFiles = new ArrayList();
    mJavaFiles = new ArrayList();
  }

  protected void scanJavaDir(File srcDir, String[] files) {
    for (int i = 0; i < files.length; i++) {
      if (files[i].endsWith(".java"))
        mJavaFiles.add(new File(srcDir, files[i]));
    }
  }

  protected void scanSchemaFileset(FileSet fs) {
    File fromDir = fs.getDir(getProject());
    DirectoryScanner ds = fs.getDirectoryScanner(getProject());
    String[] srcFiles = ds.getIncludedFiles();
    for (int i = 0; i < srcFiles.length; i++) {
      if (srcFiles[i].endsWith(".xsd"))
        mXsdFiles.add(new File(fromDir, srcFiles[i]));
    }

  }

  protected File[] namesToFiles(String[] names) {
    File[] result = new File[names.length];
    for (int i = 0; i < names.length; i++)
      result[i] = new File(names[i]);
    return result;
  }
}