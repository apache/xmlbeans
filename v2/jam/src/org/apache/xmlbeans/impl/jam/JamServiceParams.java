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

package org.apache.xmlbeans.impl.jam;

import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.annotation.JavadocTagParser;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;

import java.io.File;
import java.io.PrintWriter;

/**
 * <p>Used to specify the parameters with which a JamService will be
 * created.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JamServiceParams {

  // ========================================================================
  // Public methods

  /**
   * <p>Include a single java source file to be viewed.  Note that if your
   * code is able to understand the file/package structure in which the
   * source exists, you may get better performance by using the various
   * include... methods which take a sourcepath parameter.</p>
   */
  public void includeSourceFile(File file);

  /**
   * Specifies a set of java source files to be included in the JamService.
   *
   * <p>Note that calling this method implicitly includes the 'root' in
   * the sourcepath (exactly as if addSourcepath(root) had been called).</p>
   *
   * @param sourcepath Root directory/ies containing source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void includeSourcePattern(File[] sourcepath, String pattern);

  /**
   * Specifies a set of java source files to be excluded in the JamService.
   * Note that calling this method implicitly includes the 'sourcepath' in
   * the sourcepath (as in a call to addSourcepath(sourcepath)).
   *
   * @param sourcepath Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void excludeSourcePattern(File[] sourcepath, String pattern);

  /**
   * Specifies a set of java class files to be excluded in the JamService.
   * Note that calling this method implicitly includes the 'classpath' in
   * the classpath (as in a call to addClasspath(classpath)).
   *
   * @param classpath Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void includeClassPattern(File[] classpath, String pattern);

  /**
   * Specifies a set of java class files to be excluded from the JamService.
   * Note that calling this method implicitly includes the 'classpath' in
   * the classpath (as in a call to addClasspath(classpath)).
   *
   * @param classpath Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void excludeClassPattern(File[] classpath, String pattern);

  /**
   * <p>Includes a single source File in the JamService.  The sourcepath parameter
   * should identify the source sourcepath of the java source file; the sourceFile
   * parameter must be under the sourcepath subtree.</p>
   *
   * <p>For example, if a class "foo.bar.MyClass" is stored in a file
   * "c:/myproject/src/foo/bar/MyClass.java", that class could be included in
   * the service by calling</p>
   *
   * <pre>
   *  includeSourceFile(new File("c:/myproject/src"),
   *                    new File("c:/myproject/src/foo/bar/MyClass.java"));
   * </pre>
   *
   * <p>Note that this equivalent to calling</p>
   *
   * <pre>
   *  includeSourceFiles(new File("c:/myproject/src"),"foo/bar/MyClass.java");
   * </pre>
   *
   * <p>If you are calling this method and have more than one sourcepath directory,
   * and do not readily know which is the correct sourcepath for a given source
   * File, you can use the getRootForFile() utility method to determine the
   * correct sourcepath to use.</p>
   *
   * <p>Note that calling this method implicitly includes the 'sourcepath' in
   * the sourcepath (exactly as if addSourcepath(sourcepath) had been called).</p>
   *
   * @param sourcepath source sourcepath for the java source file
   * @param sourceFile the java source file
   * @throws IllegalArgumentException if either argument is null or if
   * sourcepath is not an ancestor of sourceFile in the file system.
   */
  public void includeSourceFile(File[] sourcepath, File sourceFile);

  /**
   * <p>Excludes a single source File in the JamService in exactly the same
   * way theat includeSourceFile() includes a source file.
   */
  public void excludeSourceFile(File[] sourcepath, File sourceFile);

  /**
   * <p>Includes a single class File in the JamService in exactly the same
   * way theat includeSourceFile() includes a source file.
   */
  public void includeClassFile(File[] sourcepath, File sourceFile);

  /**
   * <p>Excludes a single class File in the JamService in exactly the same
   * way theat includeSourceFile() includes a source file.
   */
  public void excludeClassFile(File[] sourcepath, File sourceFile);

  /**
   * Names a specific class to be included in the JamService.  Note that
   * this will return an 'unresolved' JClass unless a source or class file
   * for the named class is available in the classpath or sourcepath.
   *
   * @param qualifiedClassname a full-qualified classname
   * @throws IllegalArgumentException if the argument is null or not
   * a valid classname.
   */
  public void includeClass(String qualifiedClassname);


  /**
   * Names a specific class to be excluded in the JamService.  Note that
   * this will have no affect if the named class cannot be found in the
   * sourcepath or classpath.
   *
   * @param qualifiedClassname a full-qualified classname
   * @throws IllegalArgumentException if the argument is null or not
   * a valid classname.
   */
  public void excludeClass(String qualifiedClassname);

  /**
   * Adds an elements to the JamService sourcepath.  The service's JamClassLoader
   * will search this path to find a .java file on which to base a JClass
   * when requested to load a class that was not included in the service.
   */
  public void addSourcepath(File sourcepathElement);

  /**
   * Adds an elements to the JamService classpath.  The service's JamClassLoader
   * will search this path to find a .class file on which to base a JClass
   * when requested to load a class that was not included in the service
   * and for which no source could be found in the sourcepath.
   *
   * @param classpathElement elements of the classpath
   * @throws IllegalArgumentException if the argument is null
   */
  public void addClasspath(File classpathElement);

  /**
   * Sets a PrintWriter to which the JamService implementation should log
   * errors and debugging information.  If this is never set, all such output
   * will be suppressed.
   *
   * @param out a PrintWriter to write to
   * @throws IllegalArgumentException if the argument is null
   */
  public void setLoggerWriter(PrintWriter out);

  /**
   * </p>Enables verbose debugging output from all instances of the given
   * class.</p>
   */
  public void setVerbose(Class c);


  /**
   * <p>Enables or suppresses the logging of warning messages.  By default
   * this is true.</p>
   */
  public void setShowWarnings(boolean b);

  /**
   * Sets the parent JamClassLoader of the service JamClassLoader.
   *
   * @param loader the parent loaer
   * @throws IllegalArgumentException if the argument is null
   */
  public void setParentClassLoader(JamClassLoader loader);

  /**
   * Adds an elements to the tool classpath.  This is the classpath that
   * will be used by the JamService implementation to find any libraries
   * on which it depends.  This classpath distinct from the service classpath
   * set by addClasspath().
   *
   * @param classpathElement elements of the classpath
   * @throws IllegalArgumentException if the argument is null
   */
  public void addToolClasspath(File classpathElement);

  /**
   * <p>Utility method which, given an array of Files representing a series of
   * java source roots, returns the first one in array order which is an
   * ancestor of the given sourceFile in the file hierarchy.  Returns null if
   * the sourceFile is not under any of the given sourceRoots</p>
   *
   * @param sourceRoots roots to check
   * @param sourceFile source file
   * @return the sourceRoot that contains the sourceFile, or null if none
   * does.
   * @throws IllegalArgumentException if either argument is null.
   */
  //public File getRootForFile(File[] sourceRoots, File sourceFile);


  /**
   * <p>Registers a given class to serve as proxy for a JSR 175 annotation
   * type (i.e. an extension of <code>java.lang.annotation.Annotation</code>).
   * JAM will create instances of the given class to act as proxies to
   * declarations of the given 175 annotation type.  The proxy class must
   * extend <code>TypedAnnotationProxyBase</code>.</p>
   *
   * <p>Note that the 175 Annotation type is specified by name (as opposed
   * to Class) so that it is not required for the Annotation class to be
   * compiled.  However, this name MUST be a fully-qualified java
   * classname.</p>
   *
   * <pYou must register a unique TypedAnnotationProxyBase subclass for every JSR175
   * Annotation classname.  This is because JAM (like JSR175) will only
   * expose one annotation of a given (proxy) type per java elements.</p>
   *
   * <p>However, it is acceptable (and often desirable) to register the same
   * TypedAnnotationProxyBase subclass for both a javadoc tag and a 175 annotation
   * type.  This allows JAM to provide your application with a unified view of
   * java metadata, whether it is represented as javadoc tags or JSR175
   * annotations.</p>
   *
   * @throws IllegalArgumentException if the proxyClass parameter is null,
   * does not have a public default constructor, or is not a subclass of
   * <code>TypedAnnotationProxyBase</code>.
   * @throws IllegalArgumentException if the annotationName
   * is null or not a valid java classname.
   * @throws IllegalArgumentException if a proxy has already been registered
   * for the given 175 annotation class.
   */
  public void registerAnnotationProxy(Class annotationProxyClass,
                                      String annotationName);

  //DOCME
  public void setPropertyInitializer(MVisitor initializer);

  //DOCME
  public void addInitializer(MVisitor initializer);

  //DOCME
  public void setJavadocTagParser(JavadocTagParser tp);


  /**
   * <p>Sets the subclass of TypedAnnotationProxyBase to be instantiated when no
   * proxy is registered for a given tag or 175 type.  Instances of the
   * default proxy class are never returned via the JAM API, but they are
   * used internally to implement the default, untyped mapping; you can use
   * this method to customize the way an unproxied annotation's values get
   * mapped into the a ValueMap.</p>
   *
   * @throws IllegalArgumentException if the proxyClass parameter is null,
   * does not have a public default constructor, or is not a subclass of
   * <code>TypedAnnotationProxyBase</code>.
   */
  public void setDefaultAnnotationProxyClass(Class c);

  /**
   * <p>Specifies whether the JAM Service should load classes from the system
   * classpath.  The default for this is true, and you shouldn't set it to
   * false unless you really know what you are doing.</p>
   */
  public void setUseSystemClasspath(boolean use);

  /**
   * <p>Adds a custom JamClassBuilder which will be consulted by the
   * JamClassLoader when constructing representation of java types.  The given
   * class builder will have priority over priority over JAM's default
   * source- and reflection-based ClassBuilders.  If this method id
   * called more than once, the extra class builders will be prioritized
   * in the order in which they were added.</p>
   */
  public void addClassBuilder(JamClassBuilder cb);

  /**
   * DOCME
   */
  public void addClassLoader(ClassLoader cl);

  /**
   * <p>Sets an implementation-specific property.</p>
   */
  public void setProperty(String name, String value);

  /**
   * <p>Sets whether warnings should be displayed when running under
   * JDK 1.4.  The default is true.</p>
   */ 
  public void set14WarningsEnabled(boolean b);

  /**
   * @deprecated use setVerbose(Class).  This is the same as
   * setVerbose(Object.class).
   */
  public void setVerbose(boolean v);



}