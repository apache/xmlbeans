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
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.xmlbeans.impl.binding.tylar.Tylar;
import org.apache.xmlbeans.impl.binding.logger.MessageSink;
import org.apache.xmlbeans.impl.binding.logger.SimpleMessageSink;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.w3.x2001.xmlSchema.SchemaDocument;
import java.io.File;
import java.io.IOException;

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
   * that compiler.  It is guaranteed that this method will not be called
   * until after the execute() method has begun.
   */
  protected abstract BindingCompiler getCompilerToExecute()
          throws BuildException;

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
//FIXME temporarily non-final to accomodate transition of Both2BindTask
  public void execute() throws BuildException {
    if (mDestDir == null && mDestJar == null) {
      throw new BuildException("must specify destdir or destjar");
    }
    Tylar tylar = null;
    try {
      BindingCompiler bc = getCompilerToExecute();
      bc.setIgnoreSevereErrors(mIgnoreErrors);
      bc.setMessageSink(createMessageSink());
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
    SchemaTypeLoader soapencLoader = org.apache.xmlbeans.impl.schema.SoapEncSchemaTypeSystem.get();
    SchemaTypeLoader xsdLoader = XmlBeans.getBuiltinTypeSystem();
    return XmlBeans.compileXsd(xsds, XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {xsdLoader, soapencLoader}), null);
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

  private  MessageSink createMessageSink() {
    //FIXME this should be an AntBindingLogger
    return new SimpleMessageSink();
  }
}
