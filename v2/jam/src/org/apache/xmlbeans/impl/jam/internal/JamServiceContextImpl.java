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

  private PrintWriter mOut = new PrintWriter(System.out);
  private boolean mUseSystemClasspath = true;
  private boolean mVerbose = false;
  private ElementVisitor mCommentInitializer = null;
  private ElementVisitor mPropertyInitializer = null;
  private List mOtherInitializers = null;

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


  public ElementVisitor getInitializer() {
    List initers = new ArrayList();
    initers.add((mCommentInitializer != null) ? mCommentInitializer :
                new CommentInitializer());
    initers.add((mPropertyInitializer != null) ? mPropertyInitializer :
                new PropertyInitializer());
    if (mOtherInitializers != null) initers.addAll(mOtherInitializers);
    // now go
    ElementVisitor[] inits = new ElementVisitor[initers.size()];
    initers.toArray(inits);
    return new CompositeElementVisitor(inits);
  }

  // ========================================================================
  // JamServiceParams implementation



  //DOCME
  public void setCommentInitializer(ElementVisitor initializer) {
    mCommentInitializer = initializer;
  }

  //DOCME
  public void setPropertyInitializer(ElementVisitor initializer) {
    mPropertyInitializer = initializer;
  }

  //DOCME
  public void addInitializer(ElementVisitor initializer) {
    if (mOtherInitializers == null) mOtherInitializers = new ArrayList();
    mOtherInitializers.add(initializer);
  }


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

  public void debug(String msg) {
    if (mVerbose) mOut.println(msg);
  }

  public void debug(Throwable t) {
    if (mVerbose) t.printStackTrace(mOut);
  }

  public void warning(Throwable t) {
    error(t);//FIXME
  }

  public void error(Throwable t) {
    t.printStackTrace(mOut);
  }

  // ========================================================================
  // ElementContext implementation

  public JamClassLoader getClassLoader() { return mLoader; }

  public AnnotationProxy createProxyForTag(String tagname) {
    return createProxy((Class)mTagname2proxyclass.get(tagname));
  }

  public AnnotationProxy createProxyForAnnotationType(String jsr175typename) {
    return createProxy((Class)m175type2proxyclass.get(jsr175typename));
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