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
package org.apache.xmlbeans.impl.jam.annogen.tools;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.JClass;

import java.io.File;
import java.io.IOException;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class AnnogenTask extends Task {

  // ========================================================================
  // Variables

  private Annogen mAnnogen = new Annogen();
  private Path mSrcDir = null;
  private String mIncludes = "**/*.java";

  // ========================================================================
  // Constructors

  public AnnogenTask() {}

  // ========================================================================
  // Public methods

  /**
   * Sets the directory into which source files should be generated.
   * @param f
   */
  public void setOutputDir(File f) {
    mAnnogen.setOutputDir(f);
  }

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

  /**
   * Includes source files matching the given patten.  Pattern is relative
   * to srcDir.
   */
  public void setIncludes(String includes) {
    if (includes == null) throw new IllegalArgumentException("null includes");
    mIncludes = includes;
  }

  public void setPre15CompatibilityMode(boolean b) {
    mAnnogen.setPre15CompatibilityMode(b);
  }


  // ========================================================================
  // Task implementation

  public void execute() throws BuildException {
    if (mSrcDir == null) {
      throw new BuildException("'srcDir' must be specified");
    }
    JamServiceFactory jsf = JamServiceFactory.getInstance();
    JamServiceParams p = jsf.createServiceParams();
    p.includeSourcePattern(path2files(mSrcDir),mIncludes);
    try {
      JamService js = jsf.createService(p);
      JClass[] classes = js.getAllClasses();
      mAnnogen.addAnnotationClasses(classes);
      log("Generating annotation impls for the following classes:");
      for(int i=0; i<classes.length; i++) {
        log("  "+classes[i].getQualifiedName());
      }
      mAnnogen.doCodegen();
      log("...done.");
    } catch(IOException ioe) {
      throw new BuildException(ioe);
    }
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
