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
package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JServiceParams;
import org.apache.xmlbeans.impl.jam.provider.JClassBuilderParams;
import org.apache.xmlbeans.impl.jam.provider.JPath;

import java.util.*;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JServiceParamsImpl implements JServiceParams, JClassBuilderParams
{
  // ========================================================================
  // Variables

  private Properties mProperties = null;
  private Map mSourceRoot2Scanner = null;
  private Map mClassRoot2Scanner = null;
  private List mClasspath = null;
  private List mSourcepath = null;
  private List mToolClasspath = null;

  private List mIncludeClasses = null;
  private List mExcludeClasses = null;

  private PrintWriter mOut = null;
  private JAnnotationLoader mAnnotationLoader = null;
  private boolean mUseSystemClasspath = true;
  private boolean mVerbose = false;

  // ========================================================================
  // Constructors

  public JServiceParamsImpl() {}

  // ========================================================================
  // Public methods - used by BaseJProvider

  public JAnnotationLoader getAnnotationLoader() {
    return mAnnotationLoader;
  }

  public boolean isUseSystemClasspath() { return mUseSystemClasspath; }

  public boolean isVerbose() { return mVerbose; }

  /**
   * Returns an array containing the qualified names of the classes which
   * are in the Service class set.
   */
  public String[] getAllClassnames() throws IOException {
    Set all = new HashSet();
    if (mIncludeClasses != null) all.addAll(mIncludeClasses);
    for(Iterator i = getAllDirectoryScanners(); i.hasNext(); ) {
      DirectoryScanner ds = (DirectoryScanner)i.next();
      String[] files = ds.getIncludedFiles();
      for(int j=0; j<files.length; j++) {
        all.add(filename2classname(files[j]));
      }
    }
    if (mExcludeClasses != null) all.removeAll(mExcludeClasses);
    String[] out = new String[all.size()];
    all.toArray(out);
    return out;
  }

  /*
  public String[] getSourceClassnames() throws IOException {
    if (mSourceRoot2Scanner == null) return new String[0];
    Set set = new HashSet();
    for(Iterator i = mSourceRoot2Scanner.values().iterator(); i.hasNext(); ) {
      DirectoryScanner ds = (DirectoryScanner)i.next();
      String[] files = ds.getIncludedFiles();
      for(int j=0; j<files.length; j++) {
        set.add(filename2classname(files[j]));
      }
    }
    String[] out = new String[set.size()];
    set.toArray(out);
    return out;
  }*/

  public File[] getSourceFiles() throws IOException {
    if (mSourceRoot2Scanner == null) return new File[0];
    Set set = new HashSet();
    for(Iterator i = mSourceRoot2Scanner.values().iterator(); i.hasNext(); ) {
      DirectoryScanner ds = (DirectoryScanner)i.next();
      String[] files = ds.getIncludedFiles();
      for(int j=0; j<files.length; j++) {
        set.add(new File(ds.getRoot(),files[j]));
      }
    }
    File[] out = new File[set.size()];
    set.toArray(out);
    return out;
  }

  // ========================================================================
  // JServiceFactory implementation

  public void includeSourceFiles(File srcRoot, String pattern) {
    addSourcepath(srcRoot);
    getSourceScanner(srcRoot).include(pattern);
  }

  public void includeClassFiles(File srcRoot, String pattern) {
    addClasspath(srcRoot);
    getClassScanner(srcRoot).include(pattern);
  }

  public void excludeSourceFiles(File srcRoot, String pattern) {
    addSourcepath(srcRoot);
    getSourceScanner(srcRoot).exclude(pattern);
  }

  public void excludeClassFiles(File srcRoot, String pattern) {
    addClasspath(srcRoot);
    getClassScanner(srcRoot).exclude(pattern);
  }

  public void includeClass(String qualifiedClassname) {
    if (mIncludeClasses == null) mIncludeClasses = new ArrayList();
    mIncludeClasses.add(qualifiedClassname);
  }

  public void excludeClass(String qualifiedClassname) {
    if (mExcludeClasses == null) mExcludeClasses = new ArrayList();
    mExcludeClasses.add(qualifiedClassname);
  }

  public void addClasspath(File classpathElement) {
    if (mClasspath == null) mClasspath = new ArrayList();
    mClasspath.add(classpathElement);
  }

  public void addSourcepath(File sourcepathElement) {
    if (mSourcepath == null) mSourcepath = new ArrayList();
    mSourcepath.add(sourcepathElement);
  }

  public void addToolClasspath(File classpathElement) {
    if (mToolClasspath == null) mToolClasspath = new ArrayList();
    mToolClasspath.add(classpathElement);
  }

  public void setProperty(String name, String value) {
    if (mProperties == null) mProperties = new Properties();
    mProperties.setProperty(name,value);
  }

  public void setLogger(PrintWriter out) { mOut = out; }

  public void setVerbose(boolean v) {
    mVerbose = v;
  }

  public void setAnnotationLoader(JAnnotationLoader ann) {
    mAnnotationLoader = ann;
  }

  public void setParentClassLoader(JClassLoader loader) {
  }

  public void setBaseClassLoader(ClassLoader cl) {
  }

  public void setUseSystemClasspath(boolean use) {
    mUseSystemClasspath = use;
  }

  // ========================================================================
  // JClassBuilderParams implementation

  public JPath getInputClasspath() {
    return createJPath(mClasspath);
  }

  public JPath getInputSourcepath() {
    return createJPath(mSourcepath);
  }

  public JPath getToolClasspath() {
    return createJPath(mToolClasspath);
  }

  public PrintWriter getOut() {
    return null;
  }

  public Properties getProperties() {
    return null;
  }

  // ========================================================================
  // Private methods

  /**
   * Converts the given java source or class filename into a qualified
   * classname.  The filename is assumed to be relative to the source or
   * class root.
   */
  private static String filename2classname(String filename) {
    int extDot = filename.lastIndexOf('.');
    if (extDot != -1) filename = filename.substring(0,extDot);
    filename = filename.replace('/','.');
    filename = filename.replace('\\','.');
    return filename;
  }

  /**
   * Returns all of the directory scanners for all class and source
   * roots created in this params object.
   */
  private Iterator getAllDirectoryScanners() {
    Collection out = new ArrayList();
    if (mSourceRoot2Scanner != null) {
      out.addAll(mSourceRoot2Scanner.values());
    }
    if (mClassRoot2Scanner != null) {
      out.addAll(mClassRoot2Scanner.values());
    }
    return out.iterator();
  }

  /**
   * Creates a JPath for the given collection of Files, or returns null
   * if the collections is null or empty.
   */
  private static JPath createJPath(Collection filelist) {
    if (filelist == null || filelist.size() == 0) return null;
    File[] files = new File[filelist.size()];
    filelist.toArray(files);
    return JPath.forFiles(files);
  }

  /**
   * Returns the DirectoryScanner which we have mapped to the given source
   * root, creating a new one if necessary.
   */
  private DirectoryScanner getSourceScanner(File srcRoot) {
    if (mSourceRoot2Scanner == null) mSourceRoot2Scanner = new HashMap();
    DirectoryScanner out = (DirectoryScanner)mSourceRoot2Scanner.get(srcRoot);
    if (out == null) {
      mSourceRoot2Scanner.put(srcRoot,out = new DirectoryScanner(srcRoot));
    }
    return out;
  }

  /**
   * Returns the DirectoryScanner which we have mapped to the given class
   * root, creating a new one if necessary.
   */
  private DirectoryScanner getClassScanner(File clsRoot) {
    if (mClassRoot2Scanner == null) mClassRoot2Scanner = new HashMap();
    DirectoryScanner out = (DirectoryScanner)mClassRoot2Scanner.get(clsRoot);
    if (out == null) {
      mClassRoot2Scanner.put(clsRoot,out = new DirectoryScanner(clsRoot));
    }
    return out;
  }
}