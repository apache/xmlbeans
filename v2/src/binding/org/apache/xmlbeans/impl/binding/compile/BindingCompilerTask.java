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
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.xmlbeans.impl.binding.tylar.Tylar;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.w3.x2001.xmlSchema.SchemaDocument;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Abstract base class for Ant Task classes which drive a BindingCompiler.
 * By using this base class for all binding tasks, we can help ensure
 * consistency among the various tasks which produce tylars.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BindingCompilerTask extends MatchingTask {

  // ========================================================================
  // Constants

  //used by createSchemaTypeSystem
  private static final SchemaTypeLoader SCHEMA_LOADER =
          XmlBeans.typeLoaderForClassLoader
          (SchemaDocument.class.getClassLoader());

  // ========================================================================
  // Variables

  private File mDestDir = null;
  private File mDestJar = null;
  private boolean mVerbose = false;
  private boolean mIgnoreErrors = false;

  // ========================================================================
  // Abstract methods

  /**
   * Subclasses are only responsible for getting additional attributes
   * from the ant script and creating a BindingCompiler; this is how we get
   * that compiler.  This will method    * never be called until after the execute() method has begun.
   */
  protected abstract BindingCompiler createCompiler() throws BuildException;

  // ========================================================================
  // Task Attributes - these are common to all of the tasks

  public void setDestDir(File dir) {
    if (mDestJar != null) {
      throw new BuildException("You can set only one of destjar and destdir");
    }
    mDestDir = dir;
  }

  public void setDestJar(File jar) throws BuildException {
    if (mDestDir != null) {
      throw new BuildException("You can set only one of destjar and destdir");
    }
    mDestJar = jar;
  }

  public void setVerbose(boolean v) {
    mVerbose = v;
  }

  public void setIgnoreErrors(boolean v) { mIgnoreErrors = v; }

  // ========================================================================
  // Task implementation

  /**
   * Drives the compilation process.  Note that this is final - subclasses
   * should not override - they're only responsible for providing a
   * BindingCompiler.
   */
  public final void execute() throws BuildException {
    if (mDestDir == null && mDestJar == null) {
      throw new BuildException("must specify destdir or destjar");
    }
    Tylar tylar = null;
    try {
      BindingCompiler bc = createCompiler();
      bc.setIgnoreSevereErrors(mIgnoreErrors);
      bc.setLogger(createLogger());
      bc.setVerbose(mVerbose);
      if (mDestDir != null) {
        tylar = bc.bindAsExplodedTylar(mDestDir);
      } else if (mDestJar != null) {
        tylar = bc.bindAsJarredTylar(mDestJar);
      } else {
        throw new IllegalStateException();
      }
    } catch(Exception unexpected) {
      unexpected.printStackTrace();
      throw new BuildException(unexpected);
    }
    if (tylar == null) {
      throw new BuildException("fatal errors encountered, "+
                               "see log for details.");
    }
    log("binding task complete, output at "+tylar.getLocation());

  }

  // ========================================================================
  // Protected methods

  /**
   * Utility method for creating a SchemaTypeSystem from a set of
   * xsd files.
   */
  public static SchemaTypeSystem createSchemaTypeSystem(File[] xsdFiles)
          throws IOException, XmlException
  {
    XmlObject[] xsds = new XmlObject[xsdFiles.length];
    for (int i = 0; i < xsdFiles.length; i++) {
        xsds[i] = parseSchemaFile(xsdFiles[i]);
    }
    return XmlBeans.compileXsd(xsds, XmlBeans.getBuiltinTypeSystem(), null);
  }


  // ========================================================================
  // Private methods

  private static SchemaDocument parseSchemaFile(File file)
          throws IOException, XmlException
  {
      XmlOptions options = new XmlOptions();
      options.setLoadLineNumbers();
      options.setLoadMessageDigest();
      return (SchemaDocument)SCHEMA_LOADER.parse
              (file, SchemaDocument.type, options);
  }

  private  BindingLogger createLogger() {
    //FIXME this should be an AntBindingLogger
    SimpleBindingLogger logger = new SimpleBindingLogger();
    if (mVerbose) logger.setThresholdLevel(Level.FINEST);
    return logger;
  }
}
