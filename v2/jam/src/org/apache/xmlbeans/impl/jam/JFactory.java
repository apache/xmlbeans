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



import java.io.*;
import java.util.Collection;
import org.apache.xmlbeans.impl.jam.internal.JFileSetImpl;
import org.apache.xmlbeans.impl.jam.internal.JamPrinter;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDClassLoader;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDClassLoaderFactory;
import org.apache.xmlbeans.impl.jam.provider.ReflectionClassBuilder;


/**
 * Start here! Entry point into the JAM subsystem.  JFactory is a singleton
 * builds everything you need to get started with a JAM.
 *
 * Here is a simple usage example which parses all of the source files
 * in weblogic/utils/jam and prints out the classnames that were
 * found.
 *
 * <pre>
 * JFactory factory = JFactory.getInstance();
 * JFileSet files = factory.createFileSet(new File("c:\\weblogic\\dev\\src"));
 * files.include("weblogic\\utils\\jam\\**\\*.java");
 * JClass[] classes = factory.loadSources(files);
 * for(int i=0; i < classes.length; i++) {
 *   System.out.println(classes[i].getQualifiedName());
 * }
 * </pre>
 *
 * @deprecated Please us JServiceFactory instead.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JFactory {

  // ========================================================================
  // Singleton

  /**
   * Returns the singleton JFactory instance.
   */
  public static JFactory getInstance() { return INSTANCE; }

  private static JFactory INSTANCE = new JFactory();

  private JFactory() {}

  // ========================================================================
  // Constants

  private JClassLoader SYSTEM_CL =
          ReflectionClassBuilder.createRClassLoader
          (ClassLoader.getSystemClassLoader());
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
          throws IOException, FileNotFoundException
  {
    return loadSources(fs,null,null,new PrintWriter(System.out));
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
    throws IOException, FileNotFoundException
  {
    return loadSources(fs,parentLoader,annLoader,log,null);
  }


  /**
   * Note that this method is guaranteed to return a non-empty array;
   * FileNotFoundException is thrown if no classes are found to parse.
   *
   * @param sourcePath A semicolon-separated path on which
   * sources will be located for resolving types that are not included
   * in the filest.  It is ignored if null.
   *
   * @throws FileNotFoundException If no java source files could be
   * located in the given fileset.
   * @throws IOException If an IO error occurred while reading the
   * source files.
   */
  public JClass[] loadSources(JFileSet fs,
			      JClassLoader parentLoader,
			      JAnnotationLoader annLoader,
			      PrintWriter log,
			      String sourcePath)
    throws IOException, FileNotFoundException
  {
    return loadSources(fs,parentLoader,annLoader,log,sourcePath,null,null);
  }


  /**
   * Note that this method is guaranteed to return a non-empty array;
   * FileNotFoundException is thrown if no classes are found to parse.
   *
   * @param sourcePath A semicolon-separated path on which
   * sources will be located for resolving types that are not included
   * in the filest.  It is ignored if null.
   *
   * @throws FileNotFoundException If no java source files could be
   * located in the given fileset.
   * @throws IOException If an IO error occurred while reading the
   * source files.
   */
  public JClass[] loadSources(JFileSet fs,
                              JClassLoader parentLoader,
                              JAnnotationLoader annLoader,
                              PrintWriter log,
                              String sourcePath,
                              String classPath)
    throws IOException, FileNotFoundException
  {
    return loadSources(fs,parentLoader,annLoader,log,sourcePath,classPath,null);
  }

  /**
   * Note that this method is guaranteed to return a non-empty array;
   * FileNotFoundException is thrown if no classes are found to parse.
   *
   * @param fs the source file set to use
   * @param parentLoader a parent JClassLoader for the loaded JClasses (optional)
   * @param annLoader external annotation loader (optional)
   * @param log Writer to receive logging output (optional)
   * @param sourcePath A semicolon-separated path on which
   * sources will be located for resolving types that are not included
   * in the filest.  (optional)
   * @param classPath A semicolon-separated path on which runtime classes
   * (e.g. javadoc and doclets) will be found. (optional)
   * @param extraJavadocArgs An array of parameter that are passed directly
   * to javadoc.  This is the last parameter in
   * com.com.sun.tools.javadoc.Main.execute.  (optional)
   *
   * @throws FileNotFoundException If no java source files could be
   * located in the given fileset.
   * @throws IOException If an IO error occurred while reading the
   * source files.
   */
  public JClass[] loadSources(JFileSet fs,
                              JClassLoader parentLoader,
                              JAnnotationLoader annLoader,
                              PrintWriter log,
                              String sourcePath,
                              String classPath,
                              String[] extraJavadocArgs)
    throws IOException, FileNotFoundException
  {
    JDClassLoader loader = JDClassLoaderFactory.getInstance().
      create(fs,parentLoader,annLoader,log,sourcePath,classPath,extraJavadocArgs);
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
    return ReflectionClassBuilder.createRClassLoader(cl);//FIXME
  }

  public JClassLoader getSystemClassLoader() { return SYSTEM_CL; }


  /**
   * Serializes a set of JClasses to an XML document.
   *
   * @param out Writer where to write the XML document
   * @param classes JClasses to write.
   *
   * @deprecated
   */
  public void toXML(Writer out, JClass[] classes) throws IOException
  {
    throw new RuntimeException("Temporarily not implemented.");
    /*    XClassLoader xc = XClassLoader.create(classes,null);
	  xc.toXML(out);*/
  }

  /**
   * Returns the JClasses described in an XML document.
   *
   * @param in Reader on the XML document
   * @param parent JClassLoader to use as the parent classloader
   * (i.e. for resolving external classes named in the document).
   * Pass null to load from the system classloader.
   *
   * @deprecated
   */
  public JClass[] fromXML(Reader in, JClassLoader parent)
    throws IOException
  {
    throw new RuntimeException("Temporarily not implemented.");
    /*
    XClassLoader xc = XClassLoader.create(in,parent);
    return xc.getLoadedClasses();
    */
  }



  // ========================================================================
  // main() method

  /**
   * main method is provided for debugging.
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage:\n java org.apache.xmlbeans.impl.jam.JFactory "+
                         "include [include]...");
      System.out.flush();
      System.exit(-1);
    }
    // create the factory
    JFactory factory = new JFactory();
    JFileSet fs = factory.createFileSet(new File("."));
    for(int i=0; i<args.length; i++) {
      if (args[i].endsWith(".java")) fs.include(args[i]);
    }
    PrintWriter log = new PrintWriter(System.out);
    JClass[] classes = factory.loadSources(fs,null,null,log,".","t:/src_141/server/classes",
                                           new String[] {"-verbose"});
    log.flush();
    JamPrinter printer = JamPrinter.newInstance();
    PrintWriter out = new PrintWriter(System.out,true);
    for(int i=0; i<classes.length; i++) {
      printer.print(classes[i],out);
    }
    JClassLoader loader = classes[0].getClassLoader();
    for(int i=0; i<args.length; i++) {
      if (!args[i].endsWith(".java")) {
        JClass clazz = loader.loadClass(args[i]);
        printer.print(clazz,out);
        JMethod[] meths = clazz.getMethods();
        for(int j=0; j<meths.length; j++) {
          System.out.println(meths[j].getSimpleName()+"  "+
                             meths[j].getAnnotation("operation")+"  ----");
        }

      }
    }
    System.out.flush();
  }

  /**
   * @deprecated Don't do this.
   */
  public static void setInstance(JFactory f) {
    if (f == null) throw new IllegalArgumentException("null factory");
    INSTANCE = f;
  }


}
