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

import java.io.File;
import java.io.PrintWriter;

/**
 * Object which specifies parameters for creating a new JResult.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JResultParams {

  // ========================================================================
  // Public methods

  /**
   * Specifies a set of java source files to be included in the JResult.
   *
   * <p>Note that calling this method implicitly includes the 'root' in
   * the sourcepath (exactly as if addSourcepath(root) had been called).</p>
   *
   * @param root Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void includeSourceFiles(File root, String pattern);

  /**
   * Specifies a set of java source files to be excluded in the JResult.
   * Note that calling this method implicitly includes the 'root' in
   * the sourcepath (as in a call to addSourcepath(root)).
   *
   * @param root Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void excludeSourceFiles(File root, String pattern);


  /**
   * Specifies a set of java class files to be excluded in the JResult.
   * Note that calling this method implicitly includes the 'root' in
   * the classpath (as in a call to addClasspath(root)).
   *
   * @param root Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void includeClassFiles(File root, String pattern);

  /**
   * Specifies a set of java class files to be excluded from the JResult.
   * Note that calling this method implicitly includes the 'root' in
   * the classpath (as in a call to addClasspath(root)).
   *
   * @param root Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void excludeClassFiles(File root, String pattern);

  /**
   * <p>Includes a single source File in the JResult.  The root parameter
   * should identify the source root of the java source file; the sourceFile
   * parameter must be under the root subtree.</p>
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
   * <p>If you are calling this method and have more than one root directory,
   * and do not readily know which is the correct root for a given source
   * File, you can use the getRootForFile() utility method to determine the
   * correct root to use.</p>
   *
   * <p>Note that calling this method implicitly includes the 'root' in
   * the sourcepath (exactly as if addSourcepath(root) had been called).</p>
   *
   * @param root source root for the java source file
   * @param sourceFile the java source file
   * @throws IllegalArgumentException if either argument is null or if
   * root is not an ancestor of sourceFile in the file system.
   */
  public void includeSourceFile(File root, File sourceFile);

  /**
   * <p>Excludes a single source File in the JResult in exactly the same
   * way theat includeSourceFile() includes a source file.
   */
  public void excludeSourceFile(File root, File sourceFile);

  /**
   * <p>Includes a single class File in the JResult in exactly the same
   * way theat includeSourceFile() includes a source file.
   */
  public void includeClassFile(File root, File sourceFile);

  /**
   * <p>Excludes a single class File in the JResult in exactly the same
   * way theat includeSourceFile() includes a source file.
   */
  public void excludeClassFile(File root, File sourceFile);

  /**
   * Names a specific class to be included in the JResult.  Note that
   * this will return an 'unresolved' JClass unless a source or class file
   * for the named class is available in the classpath or sourcepath.
   *
   * @param qualifiedClassname a full-qualified classname
   * @throws IllegalArgumentException if the argument is null or not
   * a valid classname.
   */
  public void includeClass(String qualifiedClassname);


  /**
   * Names a specific class to be excluded in the JResult.  Note that
   * this will have no affect if the named class cannot be found in the
   * sourcepath or classpath.
   *
   * @param qualifiedClassname a full-qualified classname
   * @throws IllegalArgumentException if the argument is null or not
   * a valid classname.
   */
  public void excludeClass(String qualifiedClassname);

  /**
   * Adds an element to the JResult sourcepath.  The service's JClassLoader
   * will search this path to find a .java file on which to base a JClass
   * when requested to load a class that was not included in the service.
   */
  public void addSourcepath(File sourcepathElement);

  /**
   * Adds an element to the JResult classpath.  The service's JClassLoader
   * will search this path to find a .class file on which to base a JClass
   * when requested to load a class that was not included in the service
   * and for which no source could be found in the sourcepath.
   *
   * @param classpathElement element of the classpath
   * @throws IllegalArgumentException if the argument is null
   */
  public void addClasspath(File classpathElement);



  /**
   * Sets a loader for external annotations to be used in the service.
   *
   * @param ann an implementation of JAnnotationLoader
   * @throws IllegalArgumentException if the argument is null
   */
  public void setAnnotationLoader(JAnnotationLoader ann);

  /**
   * Sets a PrintWriter to which the JResult implementation should log
   * errors and debugging information.  If this is never set, all such output
   * will be suppressed.
   *
   * @param out a PrintWriter to write to
   * @throws IllegalArgumentException if the argument is null
   */
  public void setLogger(PrintWriter out);

  /**
   * Sets whether the JResult should send verbose output to the logger.
   * Has no effect if setLogger() is never called.
   *
   * @param v whether or not boolean output is enabled.
   */
  public void setVerbose(boolean v);

  /**
   * Sets the parent JClassLoader of the service JClassLoader.
   *
   * @param loader the parent loaer
   * @throws IllegalArgumentException if the argument is null
   */
  public void setParentClassLoader(JClassLoader loader);

  //public void setBaseClassLoader(ClassLoader cl);

  /**
   * Adds an element to the tool classpath.  This is the classpath that
   * will be used by the JResult implementation to find any libraries
   * on which it depends.  This classpath distinct from the service classpath
   * set by addClasspath().
   *
   * @param classpathElement element of the classpath
   * @throws IllegalArgumentException if the argument is null
   */
  public void addToolClasspath(File classpathElement);

  /**
   * Specifies whether the JAM Service should load classes from the system
   * classpath.  The default for this is true.
   */
  public void setUseSystemClasspath(boolean use);

  /**
   * Sets a JResult implementation-dependent property.
   */
  public void setProperty(String name, String value);

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
  public File getRootForFile(File[] sourceRoots, File sourceFile);
}