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
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JFactory;
import org.apache.xmlbeans.impl.jam.JFileSet;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Ant task definition for binding in the start-with-java case.
 * The real work is delegated to another class, Java2Schema.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class Java2SchemaTask extends BindingCompilerTask {

  // =========================================================================
  // Variables

  private Path mSourcepath = null;
  private Path mClasspath = null;
  private String mIncludes = null;
  private Path mSrcDir = null;

  // =========================================================================
  // Task attributes

  /**
   * Set the source directories to find the source Java files.
   */
  public void setSrcdir(Path srcDir) {
    if (mSrcDir == null) {
      mSrcDir = srcDir;
    } else {
      mSrcDir.append(srcDir);
    }
  }

  public void setSourcepath(Path path) {
    if (mSourcepath == null) {
      mSourcepath = path;
    } else {
      mSourcepath.append(path);
    }
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

  public void setSourcepathRef(Reference r) {
    createSourcepath().setRefid(r);
  }

  public Path createClasspath() {
    if (mClasspath == null) {
      mClasspath = new Path(getProject());
    }
    return mClasspath.createPath();
  }

  public Path createSourcepath() {
    if (mSourcepath == null) {
      mSourcepath = new Path(getProject());
    }
    return mSourcepath.createPath();
  }

  public void setIncludes(String includes) {
    if (includes == null) throw new IllegalArgumentException("null includes");
    mIncludes = includes;
  }

  public void setCompileSources(boolean ignoredRightNow) {}

  public void setCopySources(boolean ignoredRightNow) {}

  // =========================================================================
  // BindingCompilerTask implementation

  protected BindingCompiler getCompilerToExecute() throws BuildException {
    //FIXME refactor this so the functionality is shared and consistent with
    //Both2BindTask
    if (mIncludes == null) {
      //FIXME we need to improve/expand the ways in which the input source set
      //is passed to us
      throw new BuildException("The 'includes' attribute must be set.");
    }
    JFactory jf = JFactory.getInstance();
    String[] list = mSrcDir.list();
    if (list.length == 0) throw new BuildException("srcDir attribute required");
    if (list.length > 1) throw new BuildException("multiple srcDirs NYI");
    JFileSet fs = jf.createFileSet(new File(list[0]));
    StringTokenizer st = new StringTokenizer(mIncludes,",");
    while(st.hasMoreTokens()) fs.include(st.nextToken().trim());
    String classpathString = null;
    if (mClasspath != null) {
      //this will be removed after jam factory is refactored
      fs.setClasspath(classpathString = mClasspath.toString());
    }
    final JClass[] classes;
    try {
      classes = jf.loadSources(fs,null,null,null,null,classpathString);
    } catch(IOException ioe) {
      ioe.printStackTrace();
      throw new BuildException(ioe);
    }
    return new Java2Schema(classes);
  }
}