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
package org.apache.xmlbeans.impl.jam;

import java.io.File;
import java.io.PrintWriter;

/**
 * Object which specifies parameters for creating a new JService.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JServiceParams {

  // ========================================================================
  // Public methods

  /**
   * Specifies a set of java source files to be included in the JService.
   * Note that calling this method implicitly includes the 'root' in
   * the sourcepath (as in a call to addSourcepath(root)).
   *
   * @param root Root directory of the source files.
   * @param pattern A relative file pattern (as described above under
   * 'Include and Exclude Patterns').
   * @throws IllegalArgumentException if either argument is null.
   */
  public void includeSourceFiles(File root, String pattern);

  /**
   * Specifies a set of java source files to be excluded in the JService.
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
   * Specifies a set of java class files to be excluded in the JService.
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
   * Specifies a set of java class files to be excluded from the JService.
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
   * Names a specific class to be included in the JService.  Note that
   * this will return an 'unresolved' JClass unless a source or class file
   * for the named class is available in the classpath or sourcepath.
   *
   * @param qualifiedClassname a full-qualified classname
   * @throws IllegalArgumentException if the argument is null or not
   * a valid classname.
   */
  public void includeClass(String qualifiedClassname);

  /**
   * Names a specific class to be excluded in the JService.  Note that
   * this will have no affect if the named class cannot be found in the
   * sourcepath or classpath.
   *
   * @param qualifiedClassname a full-qualified classname
   * @throws IllegalArgumentException if the argument is null or not
   * a valid classname.
   */
  public void excludeClass(String qualifiedClassname);

  /**
   * Adds an element to the JService sourcepath.  The service's JClassLoader
   * will search this path to find a .java file on which to base a JClass
   * when requested to load a class that was not included in the service.
   */
  public void addSourcepath(File sourcepathElement);

  /**
   * Adds an element to the JService classpath.  The service's JClassLoader
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
   * Sets a PrintWriter to which the JService implementation should log
   * errors and debugging information.  If this is never set, all such output
   * will be suppressed.
   *
   * @param out a PrintWriter to write to
   * @throws IllegalArgumentException if the argument is null
   */
  public void setLogger(PrintWriter out);

  /**
   * Sets whether the JService should send verbose output to the logger.
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
   * will be used by the JService implementation to find any libraries
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
   * Sets a JService implementation-dependent property.
   */
  public void setProperty(String name, String value);

}