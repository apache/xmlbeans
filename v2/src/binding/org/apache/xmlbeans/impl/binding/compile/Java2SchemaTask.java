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
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.internal.DirectoryScanner;

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
    JamServiceFactory jf = JamServiceFactory.getInstance();
    JamServiceParams params = jf.createServiceParams();
    //
    // process the included sources
    //
    String[] list = mSrcDir.list();
    if (list.length == 0) throw new BuildException("srcDir attribute required");
    if (list.length > 1) throw new BuildException("multiple srcDirs NYI");
    File[] sourceRoots = path2files(mSrcDir);
    StringTokenizer st = new StringTokenizer(mIncludes,",");
    while(st.hasMoreTokens()) {
      params.includeSourcePattern(sourceRoots,st.nextToken().trim());
    }
//params.setVerbose(DirectoryScanner.class);    
    //
    // add the sourcepath and classpath, if specified
    //
    if (mSourcepath != null) {
      File[] files = path2files(mSourcepath);
      for(int i=0; i<files.length; i++) params.addSourcepath(files[i]);
    }
    if (mClasspath != null) {
      File[] files = path2files(mClasspath);
      for(int i=0; i<files.length; i++) params.addClasspath(files[i]);
    }
    //
    // create service, get classes, return compiler
    //
    JamService service;
    try {
      service = jf.createService(params);
    } catch(IOException ioe) {
      throw new BuildException(ioe);
    }
    return new Java2Schema_new(service.getAllClasses());
  }

  // ========================================================================
  // Private methods

  private File[] path2files(Path path) {
    String[] list = path.list();
    File[] out = new File[list.length];
    for(int i=0; i<out.length; i++) {
      out[i] = new File(list[i]).getAbsoluteFile();
    }
    return out;
  }
}