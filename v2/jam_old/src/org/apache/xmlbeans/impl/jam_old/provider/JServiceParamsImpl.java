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

package org.apache.xmlbeans.impl.jam_old.provider;

import org.apache.xmlbeans.impl.jam_old.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam_old.JClassLoader;
import org.apache.xmlbeans.impl.jam_old.JServiceParams;
import org.apache.xmlbeans.impl.jam_old.internal.DirectoryScanner;
import org.apache.xmlbeans.impl.jam_old.provider.JStoreParams;
import org.apache.xmlbeans.impl.jam_old.provider.JPath;

import java.util.*;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JServiceParamsImpl implements JServiceParams, JStoreParams
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
  private JClassLoader mParentClassloader = null;
  private EClassInitializer mCommentInitializer = null;
  private List mOtherInitializers = null;

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

  public void includeSourceFile(File root, File sourceFile) {
    includeSourceFiles(root,source2pattern(root,sourceFile));
  }

  public void excludeSourceFile(File root, File sourceFile) {
    excludeSourceFiles(root,source2pattern(root,sourceFile));
  }

  public void includeClassFile(File root, File classFile) {
    includeClassFiles(root,source2pattern(root,classFile));
  }

  public void excludeClassFile(File root, File classFile) {
    excludeClassFiles(root,source2pattern(root,classFile));
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
    mParentClassloader = loader;
  }

  public void setUseSystemClasspath(boolean use) {
    mUseSystemClasspath = use;
  }

  public void setCommentInitializer(EClassInitializer init) {
    mCommentInitializer = init;
  }

  public void addCustomInitializer(EClassInitializer init) {
    if (mOtherInitializers == null) mOtherInitializers = new ArrayList();
    mOtherInitializers.add(init);
  }

  // ========================================================================
  // JStoreParams implementation

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
    return new PrintWriter(System.out,true);
  }

  public Properties getProperties() {
    return null;
  }

  public JClassLoader getParentClassLoader() {
    return null;
  }

  public File getRootForFile(File[] sourceRoots, File sourceFile) {
    if (sourceRoots == null) throw new IllegalArgumentException("null roots");
    if (sourceFile == null) throw new IllegalArgumentException("null file");
    String f = sourceFile.getAbsolutePath();
    for(int i=0; i<sourceRoots.length; i++) {
      if (f.startsWith(sourceRoots[i].getAbsolutePath())) {//cheesy?
        return sourceRoots[i];
      }
    }
    return null;
  }

  public EClassInitializer getInitializer() {
    List initers = new ArrayList();
    if (mCommentInitializer != null) {
      initers.add(mCommentInitializer);
    } else {
      initers.add(DefaultCommentProcessor.getInstance());
    }
    if (mOtherInitializers != null) initers.addAll(mOtherInitializers);
    // now go
    EClassInitializer[] inits = new EClassInitializer[initers.size()];
    initers.toArray(inits);
    return new CompositeClassInitializer(inits);
  }


  // ========================================================================
  // Private methods

  /**
   * Converts the sourceFile to a pattern expression relative to the
   * given root.
   */
  private String source2pattern(File root, File sourceFile) {
    //REVIEW this is a bit cheesy
    String r = root.getAbsolutePath();
    String s = sourceFile.getAbsolutePath();
    return s.substring(r.length());
  }

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