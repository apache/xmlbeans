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

import org.apache.xmlbeans.impl.jam.internal.JFileSetImpl;
import org.apache.xmlbeans.impl.jam.internal.JamPrinter;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDClassLoader;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDClassLoaderFactory;
import org.apache.xmlbeans.impl.jam.internal.reflect.RClassLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * <p>This is the entry point into the JAM subsystem.  JFactory is a
 * singleton which builds everything you need to get started with a JAM.</p>
 *
 * <p>Here is a simple usage example which parses all of the source files
 * in weblogic/utils/jam and prints out the classnames that were
 * found.</p>
 *
 * <pre>
 * JFactory factory = JFactory.getInstance();
 * JFileSet files = factory.createFileSet(new File("c:\\weblogic\\dev\\src"));
 * files.include("weblogic\\utils\\jam\\**\\*.java");
 * JClass[] classes = factory.loadSources(files);
 * for(int i=0; i<classes.length; i++) {
 *   System.out.println(classes[i].getQualifiedName());
 * }
 * </pre>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JFactory {

  // ========================================================================
  // Singleton

  /**
   * Returns the singleton JFactory instance.
   */
  public static JFactory getInstance() {
    return INSTANCE;
  }

  private static final JFactory INSTANCE = new JFactory();

  private JFactory() {
  }

  // ========================================================================
  // Constants

  private JClassLoader SYSTEM_CL =
          new RClassLoader(ClassLoader.getSystemClassLoader());
  // REVIEW i think this needs to be even more special

  // ========================================================================
  // Public methods

  /**
   * Create an object which is used to describe a set of java source
   * files to be parsed.  Once created, patterns of files to be
   * included or excluded can be specified on the JFileSet.  See
   * @link JFileSet for more information.
   *
   * @param rootDir The root directory of the fileset.  All include and
   * exclude patterns are relative to this directory.  This File must
   * exist and must be a directory.
   */
  public JFileSet createFileSet(File rootDir) {
    return new JFileSetImpl(rootDir);
  }

  /**
   * Note that this method is guaranteed to return a non-empty array;
   * FileNotFoundException is thrown if no classes are found to parse.
   *
   * @throws FileNotFoundException If no java source files could be
   * located in the given fileset.
   * @throws IOException If an IO error occurred while reading the
   * source files.
   */
  public JClass[] loadSources(JFileSet fs)
          throws IOException, FileNotFoundException {
    return loadSources(fs, null, null, new PrintWriter(System.out));
  }

  /**
   * Note that this method is guaranteed to return a non-empty array;
   * FileNotFoundException is thrown if no classes are found to parse.
   *
   * @throws FileNotFoundException If no java source files could be
   * located in the given fileset.
   * @throws IOException If an IO error occurred while reading the
   * source files.
   */
  public JClass[] loadSources(JFileSet fs,
                              JClassLoader parentLoader,
                              JAnnotationLoader annLoader,
                              PrintWriter log)
          throws IOException, FileNotFoundException {
    JDClassLoader loader = JDClassLoaderFactory.getInstance().
            create(fs, parentLoader, annLoader, log);
    Collection classes = loader.getResolvedClasses();
    JClass[] out = new JClass[classes.size()];
    classes.toArray(out);
    return out;
  }

  /**
   * @param parent Optional parameter which specifies a parent
   * JClassLoader for the created JClassLoader.  If null, the parent
   * is the System JClassLoader.
   *
   * @param annLoader Optional loader for supplemental annotations for
   * classes loaded by the created classloader.
   */
  public JClassLoader createClassLoader(ClassLoader cl,
                                        JClassLoader parent,
                                        JAnnotationLoader annLoader) {
    return new RClassLoader(cl, parent);//FIXME
  }

  public JClassLoader getSystemClassLoader() {
    return SYSTEM_CL;
  }

  // ========================================================================
  // main() method

  /**
   * main method is provided for debugging.
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage:\n java org.apache.xmlbeans.impl.jam.JFactory " +
              "include [include]...");
      System.out.flush();
      System.exit(-1);
    }
    // create the factory
    JFactory factory = new JFactory();
    JFileSet fs = factory.createFileSet(new File("."));
    for (int i = 0; i < args.length; i++) fs.include(args[i]);
    JClass[] classes = factory.loadSources(fs);
    JamPrinter printer = JamPrinter.newInstance();
    PrintWriter out = new PrintWriter(System.out, true);
    for (int i = 0; i < classes.length; i++) {
      printer.print(classes[i], out);
    }
    System.out.flush();
  }
}
