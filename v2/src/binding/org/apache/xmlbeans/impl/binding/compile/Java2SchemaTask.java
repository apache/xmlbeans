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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JFactory;
import org.apache.xmlbeans.impl.jam.JFileSet;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument;

import java.io.File;
import java.io.IOException;

/**
 * Ant task definition for binding in the start-with-java case.
 * The real work is delegated to another class, Java2Schema.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class Java2SchemaTask extends Task {

  // =========================================================================
  // Variables

  private File mDestDir = null;
  private File mTempDir = null;
  private Path mSourcepath = null;
  private Path mClasspath = null;
  private String mIncludes = null;
  private Path mSrcDir = null;

  // =========================================================================
  // Task attributes

  public void setDestDir(File dir) { mDestDir = dir; }

  public void setTempDir(File dir) { mTempDir = dir; }

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
    mIncludes = includes;
  }

  // =========================================================================
  // Task implementation

   /**
   * Execute the task.
   */
  public void execute() throws BuildException {
    JFactory jf = JFactory.getInstance();
    String[] list = mSrcDir.list();
    if (list.length != 1) throw new IllegalStateException("NYI");
    JFileSet fs = jf.createFileSet(new File("."/*list[0]*/));
    fs.include(mIncludes);
    if (mClasspath != null) {
      System.out.println(mClasspath.toString()+"  set classpath");
      fs.setClasspath(mClasspath.toString());
    }
    final JClass[] classes;
    try {
      classes = jf.loadSources(fs);
    } catch(IOException ioe) {
      ioe.printStackTrace();
      throw new BuildException(ioe);
    }
    JavaToSchemaInput input = new JavaToSchemaInput() {
      public JClass[] getJClasses() { return classes; }
      public TylarLoader getTylarLoader() { return null; }
    };
    Java2Schema j2b = new Java2Schema(input);
    TylarBuilder tb = new ExplodedTylarBuilder(mDestDir);
    JavaToSchemaResult result = j2b.bind();
    try {
      tb.buildTylar(result);
    } catch(IOException ioe) {
      ioe.printStackTrace();
      throw new BuildException(ioe);
    }
    log("Java2Schema complete, output in "+mDestDir);
  }

  // =========================================================================
  // Private methods


}
