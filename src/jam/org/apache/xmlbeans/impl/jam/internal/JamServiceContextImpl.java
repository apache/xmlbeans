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

package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.annotation.DefaultAnnotationProxy;
import org.apache.xmlbeans.impl.jam.annotation.JavadocTagParser;
import org.apache.xmlbeans.impl.jam.annotation.WhitespaceDelimitedTagParser;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.provider.CompositeJamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.provider.ResourcePath;
import org.apache.xmlbeans.impl.jam.visitor.CompositeMVisitor;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.PropertyInitializer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>Takes settings from the user (through JamServiceParams) and exposes
 * them to the implementation (through JamServiceContext).</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamServiceContextImpl extends JamLoggerImpl implements JamServiceContext,
  JamServiceParams, ElementContext
{

  // ========================================================================
  // Constants

  private static final char INNER_CLASS_SEPARATOR = '$';

  // ========================================================================
  // Variables

  private boolean m14WarningsEnabled = false;
  private Properties mProperties = null;
  private Map mSourceRoot2Scanner = null;
  private Map mClassRoot2Scanner = null;


  private List mClasspath = null;
  private List mSourcepath = null;
  private List mToolClasspath = null;

  private List mIncludeClasses = null;
  private List mExcludeClasses = null;


  private boolean mUseSystemClasspath = true;


  private JavadocTagParser mTagParser = null;
  private MVisitor mCommentInitializer = null;
  private MVisitor mPropertyInitializer = new PropertyInitializer();
  private List mOtherInitializers = null;
  private List mUnstructuredSourceFiles = null;
  private List mClassLoaders = null;
  private List mBaseBuilders = null;

  private JamClassLoader mLoader = null;

  // ========================================================================
  // REVIEW

  public void setClassLoader(JamClassLoader loader) {
    mLoader = loader;
  }

  public JamClassBuilder getBaseBuilder() {
    if (mBaseBuilders == null || mBaseBuilders.size() == 0) {
      return null;
    }
    if (mBaseBuilders.size() == 1) {
      return (JamClassBuilder)mBaseBuilders.get(0);
    }
    JamClassBuilder[] comp = new JamClassBuilder[mBaseBuilders.size()];
    mBaseBuilders.toArray(comp);
    return new CompositeJamClassBuilder(comp);
  }

  public JavadocTagParser getTagParser() {
    if (mTagParser == null) {
      mTagParser = new WhitespaceDelimitedTagParser();
      mTagParser.init(this);
    }
    return mTagParser;
  }


  // ========================================================================
  // Constructors

  public JamServiceContextImpl() {}

  // ========================================================================
  // Public methods - used by BaseJProvider

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
 //System.out.println("including "+files[j]);        
        // exclude inner classes - they will be on disk as .class files with
        // a '$' in the name
        if (files[j].indexOf(INNER_CLASS_SEPARATOR) == -1) {
          all.add(filename2classname(files[j]));
        }
      }
    }
    if (mExcludeClasses != null) all.removeAll(mExcludeClasses);
    String[] out = new String[all.size()];
    all.toArray(out);
    return out;
  }

  public JamLogger getLogger() { return this; }

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
    Set set = new HashSet();
    if (mSourceRoot2Scanner != null) {
      for(Iterator i = mSourceRoot2Scanner.values().iterator(); i.hasNext(); ) {
        DirectoryScanner ds = (DirectoryScanner)i.next();
        if (isVerbose(this)) {
          verbose(PREFIX+ " checking scanner for dir"+ds.getRoot());
        }
        String[] files = ds.getIncludedFiles();
        for(int j=0; j<files.length; j++) {
          if (isVerbose(this)) {
            verbose(PREFIX+ " ...including a source file "+files[j]);
          }
          set.add(new File(ds.getRoot(),files[j]));
        }
      }
    }
    // also dump unstructured files in there as well.  javadoc doesn't
    // know the difference, but eventually we're going to care
    // when we introduce lazy parsing
    if (mUnstructuredSourceFiles != null) {
      if (isVerbose(this)) verbose(PREFIX+ "adding "+mUnstructuredSourceFiles.size()+
                            " other source files");
      set.addAll(mUnstructuredSourceFiles);
    }
    File[] out = new File[set.size()];
    set.toArray(out);
    return out;
  }

  public File[] getUnstructuredSourceFiles() {
    if (mUnstructuredSourceFiles == null) return null;
    File[] out = new File[mUnstructuredSourceFiles.size()];
    mUnstructuredSourceFiles.toArray(out);
    return out;
  }


  public ResourcePath getInputClasspath() { return createJPath(mClasspath); }

  public ResourcePath getInputSourcepath() { return createJPath(mSourcepath); }

  public ResourcePath getToolClasspath() { return createJPath(mToolClasspath); }

  public String getProperty(String name) {
    return (mProperties == null) ? null : mProperties.getProperty(name);
  }


  public MVisitor getInitializer() {
    List initers = new ArrayList();
    // for now, we don't have a default comment initializer.  may need to
    // change this someday.
    if (mCommentInitializer != null) initers.add(mCommentInitializer);
    // initers.add((mCommentInitializer != null) ? mCommentInitializer :
    //             new CommentInitializer());
    if (mPropertyInitializer != null) initers.add(mPropertyInitializer);
    if (mOtherInitializers != null) initers.addAll(mOtherInitializers);
    // now go
    MVisitor[] inits = new MVisitor[initers.size()];
    initers.toArray(inits);
    return new CompositeMVisitor(inits);
  }

  // ========================================================================
  // JamServiceParams implementation

  public void addClassBuilder(JamClassBuilder builder) {
    if (mBaseBuilders == null) mBaseBuilders = new ArrayList();
    mBaseBuilders.add(builder);
  }

  //DOCME
  public void setCommentInitializer(MVisitor initializer) {
    mCommentInitializer = initializer;
  }

  //DOCME
  public void setPropertyInitializer(MVisitor initializer) {
    mPropertyInitializer = initializer;
  }

  //DOCME
  public void addInitializer(MVisitor initializer) {
    if (mOtherInitializers == null) mOtherInitializers = new ArrayList();
    mOtherInitializers.add(initializer);
  }

  //DOCME
  public void setJavadocTagParser(JavadocTagParser tp) {
    mTagParser = tp;
    tp.init(this); //FIXME this is a little broken to do this here
  }

  public void includeSourceFile(File file) {
    if (file == null) throw new IllegalArgumentException("null file");
    file = file.getAbsoluteFile();
    if (isVerbose(this)) verbose(PREFIX+ "adding source ");
    if (!file.exists()) throw new IllegalArgumentException(file+" does not exist");
    if (file.isDirectory()) throw new IllegalArgumentException
      (file+" cannot be included as a source file because it is a directory.");    
    if (mUnstructuredSourceFiles == null) {
      mUnstructuredSourceFiles = new ArrayList();
    }
    mUnstructuredSourceFiles.add(file.getAbsoluteFile());
  }

  public void includeSourcePattern(File[] sourcepath, String pattern) {
    if (sourcepath == null) throw new IllegalArgumentException("null sourcepath");
    if (sourcepath.length == 0) throw new IllegalArgumentException("empty sourcepath");
    if (pattern == null) throw new IllegalArgumentException("null pattern");
    pattern = pattern.trim();
    if (pattern.length() == 0) throw new IllegalArgumentException("empty pattern");
    for(int i=0; i<sourcepath.length; i++) {
      if (isVerbose(this)) verbose(PREFIX+ "including '"+pattern+"' under "+sourcepath[i]);
      addSourcepath(sourcepath[i]);
      getSourceScanner(sourcepath[i]).include(pattern);
    }
  }

  public void includeClassPattern(File classpath[], String pattern) {
    if (classpath == null) throw new IllegalArgumentException("null classpath");
    if (classpath.length == 0) throw new IllegalArgumentException("empty classpath");
    if (pattern == null) throw new IllegalArgumentException("null pattern");
    pattern = pattern.trim();
    if (pattern.length() == 0) throw new IllegalArgumentException("empty pattern");
    for(int i=0; i<classpath.length; i++) {
      if (isVerbose(this)) verbose(PREFIX+ "including '"+pattern+"' under "+classpath[i]);
      addClasspath(classpath[i]);
      getClassScanner(classpath[i]).include(pattern);
    }
  }

  public void excludeSourcePattern(File[] sourcepath, String pattern) {
    if (sourcepath == null) throw new IllegalArgumentException("null sourcepath");
    if (sourcepath.length == 0) throw new IllegalArgumentException("empty sourcepath");
    if (pattern == null) throw new IllegalArgumentException("null pattern");
    pattern = pattern.trim();
    if (pattern.length() == 0) throw new IllegalArgumentException("empty pattern");
    for(int i=0; i<sourcepath.length; i++) {
      if (isVerbose(this)) verbose(PREFIX+ "EXCLUDING '"+pattern+"' under "+sourcepath[i]);
      addSourcepath(sourcepath[i]);
      getSourceScanner(sourcepath[i]).exclude(pattern);
    }
  }

  public void excludeClassPattern(File[] classpath, String pattern) {
    if (classpath == null) throw new IllegalArgumentException("null classpath");
    if (classpath.length == 0) throw new IllegalArgumentException("empty classpath");
    if (pattern == null) throw new IllegalArgumentException("null pattern");
    pattern = pattern.trim();
    if (pattern.length() == 0) throw new IllegalArgumentException("empty pattern");
    for(int i=0; i<classpath.length; i++) {
      if (isVerbose(this)) verbose(PREFIX+ "EXCLUDING '"+pattern+"' under "+classpath[i]);
      addClasspath(classpath[i]);
      getClassScanner(classpath[i]).exclude(pattern);
    }
  }

  public void includeSourceFile(File[] sourcepath, File sourceFile) {
    File root = getPathRootForFile(sourcepath,sourceFile);
    includeSourcePattern(new File[] {root}, source2pattern(root,sourceFile));
  }

  public void excludeSourceFile(File[] sourcepath, File sourceFile) {
    File root = getPathRootForFile(sourcepath,sourceFile);
    excludeSourcePattern(new File[] {root}, source2pattern(root,sourceFile));
  }

  public void includeClassFile(File[] classpath, File classFile) {
    File root = getPathRootForFile(classpath,classFile);
    includeClassPattern(new File[] {root}, source2pattern(root,classFile));
  }

  public void excludeClassFile(File[] classpath, File classFile) {
    File root = getPathRootForFile(classpath,classFile);
    excludeClassPattern(new File[] {root}, source2pattern(root,classFile));
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
    if (mClasspath == null) {
      mClasspath = new ArrayList();
    } else {
      if (mClasspath.contains(classpathElement)) return;
    }
    mClasspath.add(classpathElement);
  }

  public void setLoggerWriter(PrintWriter out) {
    super.setOut(out);//FIXME
  }

  public void setJamLogger(JamLogger logger) {
    throw new IllegalStateException("NYI");  //have to untangle some mess first
  }


  public void addSourcepath(File sourcepathElement) {
    if (mSourcepath == null) {
      mSourcepath = new ArrayList();
    } else {
      if (mSourcepath.contains(sourcepathElement)) return;
    }
    mSourcepath.add(sourcepathElement);
  }

  public void addToolClasspath(File classpathElement) {
    if (mToolClasspath == null) {
      mToolClasspath = new ArrayList();
    } else {
      if (mToolClasspath.contains(classpathElement)) return;
    }
    mToolClasspath.add(classpathElement);
  }

  public void setProperty(String name, String value) {
    if (mProperties == null) mProperties = new Properties();
    mProperties.setProperty(name,value);
  }

  public void set14WarningsEnabled(boolean b) {
    m14WarningsEnabled = b;
  }


  //public void setLogger(PrintWriter out) { mOut = out; }


  public void setParentClassLoader(JamClassLoader loader) {
    throw new IllegalStateException("NYI"); //FIXME
  }

  public void setUseSystemClasspath(boolean use) {
    mUseSystemClasspath = use;
  }

  public void addClassLoader(ClassLoader cl) {
    if (mClassLoaders == null) mClassLoaders = new ArrayList();
    mClassLoaders.add(cl);
  }

  // ========================================================================
  // JamServiceContext implementation

  //public boolean isUseSystemClasspath() { return mUseSystemClasspath; }

  public ClassLoader[] getReflectionClassLoaders() {                     
    if (mClassLoaders == null) {
      if (mUseSystemClasspath) {
        return new ClassLoader[] { ClassLoader.getSystemClassLoader() };
      } else {
        return new ClassLoader[0];
      }
    } else {
      ClassLoader[] out = new ClassLoader[mClassLoaders.size()+
        (mUseSystemClasspath ? 1 : 0)];
      for(int i=0; i<mClassLoaders.size(); i++) {
        out[i] = (ClassLoader)mClassLoaders.get(i);
      }
      if (mUseSystemClasspath) {
        out[out.length-1] = ClassLoader.getSystemClassLoader();
      }
      return out;
    }
  }

  public boolean is14WarningsEnabled() { return m14WarningsEnabled; }

  // ========================================================================
  // ElementContext implementation

  public JamClassLoader getClassLoader() { return mLoader; }

  public AnnotationProxy createAnnotationProxy(String jsr175typename) {
    AnnotationProxy out = new DefaultAnnotationProxy();
    out.init(this);
    return out;
  }

  // ========================================================================
  // Private methods

  private static final String PREFIX = "[JamServiceContextImpl] ";

  private File getPathRootForFile(File[] sourcepath, File sourceFile) {
    if (sourcepath == null) throw new IllegalArgumentException("null sourcepath");
    if (sourcepath.length == 0) throw new IllegalArgumentException("empty sourcepath");
    if (sourceFile == null) throw new IllegalArgumentException("null sourceFile");
    sourceFile = sourceFile.getAbsoluteFile();
    if (isVerbose(this)) verbose(PREFIX+"Getting root for "+sourceFile+"...");
    for(int i=0; i<sourcepath.length; i++) {
      if (isVerbose(this)) verbose(PREFIX+"...looking in "+sourcepath[i]);
      if (isContainingDir(sourcepath[i].getAbsoluteFile(),sourceFile)) {
        if (isVerbose(this)) verbose(PREFIX+"...found it!");
        return sourcepath[i].getAbsoluteFile();
      }
    }
    throw new IllegalArgumentException(sourceFile+" is not in the given path.");
  }

  /**
   * Returns true if the given dir contains the given file.
   */
  private boolean isContainingDir(File dir, File file) {
    if (isVerbose(this)) verbose(PREFIX+ "... ...isContainingDir "+dir+"  "+file);
    if (file == null) return false;
    if (dir.equals(file)) {
      if (isVerbose(this)) verbose(PREFIX+ "... ...yes!");
      return true;
    }
    return isContainingDir(dir,file.getParentFile());
  }

  /**
   * Converts the sourceFile to a pattern expression relative to the
   * given root.
   */
  private String source2pattern(File root, File sourceFile) {
    if (isVerbose(this)) verbose(PREFIX+ "source2pattern "+root+"  "+sourceFile);
    //REVIEW this is a bit cheesy
    String r = root.getAbsolutePath();
    String s = sourceFile.getAbsolutePath();
    if (isVerbose(this)) {
      verbose(PREFIX+ "source2pattern returning "+s.substring(r.length()+1));
    }
    return s.substring(r.length()+1);
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
   * Creates a ResourcePath for the given collection of Files, or returns null
   * if the collections is null or empty.
   */
  private static ResourcePath createJPath(Collection filelist) {
    if (filelist == null || filelist.size() == 0) return null;
    File[] files = new File[filelist.size()];
    filelist.toArray(files);
    return ResourcePath.forFiles(files);
  }

  /**
   * Returns the DirectoryScanner which we have mapped to the given source
   * root, creating a new one if necessary.
   */
  private DirectoryScanner getSourceScanner(File srcRoot) {
    if (mSourceRoot2Scanner == null) mSourceRoot2Scanner = new HashMap();
    DirectoryScanner out = (DirectoryScanner)mSourceRoot2Scanner.get(srcRoot);
    if (out == null) {
      mSourceRoot2Scanner.put(srcRoot,out = new DirectoryScanner(srcRoot,this));
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
      mClassRoot2Scanner.put(clsRoot,out = new DirectoryScanner(clsRoot,this));
    }
    return out;
  }
}