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
import org.apache.xmlbeans.impl.jam.internal.elements.ClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.visitor.CommentInitializer;
import org.apache.xmlbeans.impl.jam.visitor.*;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.provider.ResourcePath;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * <p>Takes settings from the user (through JamServiceParams) and exposes
 * them to the implementation (through JamServiceContext).</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamServiceContextImpl implements JamServiceContext,
  JamServiceParams, ElementContext
{
  // ========================================================================
  // Variables

  private Class mDefaultAnnotationProxyClass = null;
  private Properties mProperties = null;
  private Map mSourceRoot2Scanner = null;
  private Map mClassRoot2Scanner = null;

  private Map m175type2proxyclass = null;
  private Map mTagname2proxyclass = null;

  private List mClasspath = null;
  private List mSourcepath = null;
  private List mToolClasspath = null;

  private List mIncludeClasses = null;
  private List mExcludeClasses = null;

  private PrintWriter mOut = new PrintWriter(System.out,true);
  private boolean mUseSystemClasspath = true;
  private boolean mVerbose = false;
  private MVisitor mCommentInitializer = null;
  private MVisitor mPropertyInitializer = null;
  private List mOtherInitializers = null;
  private List mUnstructuredSourceFiles = null;

  private JamClassLoader mLoader = null;

  // ========================================================================
  // REVIEW

  public void setClassLoader(JamClassLoader loader) {
    mLoader = loader;
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

    Set set = new HashSet();
    if (mSourceRoot2Scanner != null) {
      for(Iterator i = mSourceRoot2Scanner.values().iterator(); i.hasNext(); ) {
        DirectoryScanner ds = (DirectoryScanner)i.next();
        if (mVerbose) verbose(PREFIX+ " checking scanner for dir"+ds.getRoot());
        String[] files = ds.getIncludedFiles();
        for(int j=0; j<files.length; j++) {
          if (mVerbose) verbose(PREFIX+ " ...including a source file "+files[j]);
          set.add(new File(ds.getRoot(),files[j]));
        }
      }
    }
    // also dump unstructured files in there as well.  javadoc doesn't
    // know the difference, but eventually we're going to care
    // when we introduce lazy parsing
    if (mUnstructuredSourceFiles != null) {
      if (mVerbose) verbose(PREFIX+ "adding "+mUnstructuredSourceFiles.size()+
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


  public ResourcePath getInputClasspath() {
    return createJPath(mClasspath);
  }

  public ResourcePath getInputSourcepath() {
    return createJPath(mSourcepath);
  }

  public ResourcePath getToolClasspath() {
    return createJPath(mToolClasspath);
  }

  public PrintWriter getOut() {
    return new PrintWriter(System.out,true);
  }

  public String getProperty(String name) {
    return (mProperties == null) ? null : mProperties.getProperty(name);
  }

  public JamClassLoader getParentClassLoader() {
    return null;
  }

  public void register175AnnotationProxy(Class proxy, String jsr175type) {
    validateProxyClass(proxy);
    ClassImpl.validateClassName(jsr175type);
    if (m175type2proxyclass == null) {
      m175type2proxyclass = new HashMap();
    } else {
      Class current = (Class)m175type2proxyclass.get(jsr175type);
      if (current != null) {
        throw new IllegalArgumentException("A proxy is already registered for "
          +jsr175type+": "+current.getName());
      }
    }
    m175type2proxyclass.put(jsr175type,proxy);
  }

  public void registerJavadocTagProxy(Class proxy, String tagname) {
    validateProxyClass(proxy);
    if (proxy == null) throw new IllegalArgumentException("null class");
    if (tagname == null) throw new IllegalArgumentException("null tagname");
    //fixme validate tagname
    if (mTagname2proxyclass == null) {
      mTagname2proxyclass = new HashMap();
    } else {
      Class current = (Class)mTagname2proxyclass.get(tagname);
      if (current != null) {
        throw new IllegalArgumentException("A proxy is already registered for "
          +tagname+": "+current.getName());
      }
    }
    mTagname2proxyclass.put(tagname,proxy);
  }


  public MVisitor getInitializer() {
    List initers = new ArrayList();
    // for now, we don't have a default comment initializer.  may need to
    // change this someday.
    if (mCommentInitializer != null) initers.add(mCommentInitializer);
    // initers.add((mCommentInitializer != null) ? mCommentInitializer :
    //             new CommentInitializer());
    initers.add((mPropertyInitializer != null) ? mPropertyInitializer :
                new PropertyInitializer());
    if (mOtherInitializers != null) initers.addAll(mOtherInitializers);
    // now go
    MVisitor[] inits = new MVisitor[initers.size()];
    initers.toArray(inits);
    return new CompositeMVisitor(inits);
  }

  // ========================================================================
  // JamServiceParams implementation


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


  public void includeSourceFile(File file) {
    if (file == null) throw new IllegalArgumentException("null file");
    if (mVerbose) verbose(PREFIX+ "adding source "+file.getAbsoluteFile());
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
      if (mVerbose) verbose(PREFIX+ "including '"+pattern+"' under "+sourcepath[i]);
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
      if (mVerbose) verbose(PREFIX+ "including '"+pattern+"' under "+classpath[i]);
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
      if (mVerbose) verbose(PREFIX+ "EXCLUDING '"+pattern+"' under "+sourcepath[i]);
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
      if (mVerbose) verbose(PREFIX+ "EXCLUDING '"+pattern+"' under "+classpath[i]);
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

  //public void setLogger(PrintWriter out) { mOut = out; }

  public void setVerbose(boolean v) {
    mVerbose = v;
  }

  public void setParentClassLoader(JamClassLoader loader) {
    throw new IllegalStateException("NYI"); //FIXME
  }

  public void setUseSystemClasspath(boolean use) {
    mUseSystemClasspath = use;
  }

  public void setDefaultAnnotationProxyClass(Class proxy) {
    validateProxyClass(proxy);
    mDefaultAnnotationProxyClass = proxy;
  }

  // ========================================================================
  // JamServiceContext implementation

  public boolean isUseSystemClasspath() { return mUseSystemClasspath; }

  public boolean isVerbose() { return mVerbose; }

  // ========================================================================
  // JamLogger implementation

  public void verbose(String msg) {
    if (mVerbose) mOut.println(msg);
  }

  public void verbose(Throwable t) {
    if (mVerbose) t.printStackTrace(mOut);
  }

  public void warning(Throwable t) {
    error(t);//FIXME
  }

  public void warning(String w) {
    error(w);//FIXME
  }

  public void error(Throwable t) {
    t.printStackTrace(mOut);
  }

  public void error(String msg) {
    mOut.println(msg);
  }

  // ========================================================================
  // ElementContext implementation

  public JamClassLoader getClassLoader() { return mLoader; }

  public AnnotationProxy createProxyForTag(String tagname) {
    Class pc = null;
    if (mTagname2proxyclass != null) {
      pc = (Class)mTagname2proxyclass.get(tagname);
      if (pc == null) pc = DefaultAnnotationProxy.class;
    } else {
      pc = DefaultAnnotationProxy.class;
    }
    return createProxy(pc);
  }

  public AnnotationProxy createProxyForAnnotationType(String jsr175typename) {
    Class pc = null;
    if (m175type2proxyclass != null) {
      pc = (Class)m175type2proxyclass.get(jsr175typename);
      if (pc == null) pc = DefaultAnnotationProxy.class;
    } else {
      pc = DefaultAnnotationProxy.class;
    }
    return createProxy(pc);
  }

  // ========================================================================
  // Static utilities

  /**
   * <p>Checks to make sure the given class is an acceptable subclass of
   * AnnotationProxy, throws an IllegalArgumentException if not.</p>
   */
  public static void validateProxyClass(Class proxy) {
    if (proxy == null) throw new IllegalArgumentException("null proxy class");
    if (!AnnotationProxy.class.isAssignableFrom(proxy)) {
      throw new IllegalArgumentException(proxy.getName()+
        " does not extend from "+AnnotationProxy.class.getName());
    }
    if (!Modifier.isPublic(proxy.getModifiers())) {
      throw new IllegalArgumentException(proxy.getName()+" is not public");
    }
    if (Modifier.isAbstract(proxy.getModifiers())) {
      throw new IllegalArgumentException(proxy.getName()+" is abstract.");
    }
    try {
      Constructor dfltCtor = proxy.getConstructor(new Class[0]);
      if (!Modifier.isPublic(dfltCtor.getModifiers())) {
        throw new IllegalArgumentException("The default constructor on"+
          proxy.getName()+" is not public.");
      }
    } catch(NoSuchMethodException nsme) {
      throw new IllegalArgumentException(proxy.getName()+
        " does not have a default constructor");
    }
  }


  // ========================================================================
  // Private methods

  private static final String PREFIX = "[JamServiceContextImpl] ";

  private File getPathRootForFile(File[] sourcepath, File sourceFile) {
    if (sourcepath == null) throw new IllegalArgumentException("null sourcepath");
    if (sourcepath.length == 0) throw new IllegalArgumentException("empty sourcepath");
    if (sourceFile == null) throw new IllegalArgumentException("null sourceFile");
    sourceFile = sourceFile.getAbsoluteFile();
    if (mVerbose) verbose(PREFIX+"Getting root for "+sourceFile+"...");
    for(int i=0; i<sourcepath.length; i++) {
      if (mVerbose) verbose(PREFIX+"...looking in "+sourcepath[i]);
      if (isContainingDir(sourcepath[i],sourceFile)) {
        if (mVerbose) verbose(PREFIX+"...found it!");
        return sourcepath[i].getAbsoluteFile();
      }
    }
    throw new IllegalArgumentException(sourceFile+" is not in the given path.");
  }

  /**
   * Returns true if the given dir contains the given file.
   */
  private boolean isContainingDir(File dir, File file) {
    if (mVerbose) verbose(PREFIX+ "... ...isContainingDir "+dir+"  "+file);
    if (file == null) return false;
    if (dir.equals(file)) {
      if (mVerbose) verbose(PREFIX+ "... ...yes!");
      return true;
    }
    return isContainingDir(dir,file.getParentFile());
  }


  private AnnotationProxy createProxy(Class clazz) {
    if (clazz == null) clazz = mDefaultAnnotationProxyClass;
    AnnotationProxy p;
    if (clazz != null) {
      try {
        //hopefully, it's pretty unlikely anything will go wrong, since
        //we validate all proxy classes on the way in
        p = (AnnotationProxy)clazz.newInstance();
        p.init(this);
        return p;
      } catch (IllegalAccessException iae) {
        error(iae);
      } catch (ClassCastException cce) {
        error(cce);
      } catch (InstantiationException ie) {
        error(ie);
      }
    }
    p = new DefaultAnnotationProxy();
    p.init(this);
    return p;
  }

  /**
   * Converts the sourceFile to a pattern expression relative to the
   * given root.
   */
  private String source2pattern(File root, File sourceFile) {
    if (mVerbose) verbose(PREFIX+ "source2pattern "+root+"  "+sourceFile);
    //REVIEW this is a bit cheesy
    String r = root.getAbsolutePath();
    String s = sourceFile.getAbsolutePath();
    if (mVerbose) {
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