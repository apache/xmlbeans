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
package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.apache.xmlbeans.impl.jam.internal.JamClassLoaderImpl;
import org.apache.xmlbeans.impl.jam.internal.JamServiceContextImpl;
import org.apache.xmlbeans.impl.jam.internal.JamServiceImpl;
import org.apache.xmlbeans.impl.jam.internal.reflect.ReflectClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JavadocClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.parser.ParserClassBuilder;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Default implementation of the JamServiceFactory singleton.  Custom
 * JAM providers need to extend this class and override whatever methods
 * they need to.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamServiceFactoryImpl extends JamServiceFactory {

  // ========================================================================
  // Constants

  /**
   * <p>Service context property which turns on the javadoc killer.
   * This will be removed when javadoc has been phased out.</p>
   */
  public static final String USE_NEW_PARSER =
    "JamServiceFactoryImpl.use-new-parser";

  private static final String PREFIX = "[JamServiceFactoryImpl]";
  
  // ========================================================================
  // Constructors

  public JamServiceFactoryImpl() {}

  // ========================================================================
  // JamServiceFactory implementation

  public JamServiceParams createServiceParams() {
    return new JamServiceContextImpl();
  }

  public JamService createService(JamServiceParams jsps) throws IOException {
    //assert that they aren't implementing JamServiceParams themselves or
    //getting them from somewhere else
    if (!(jsps instanceof JamServiceContextImpl)) {
      throw new IllegalArgumentException
              ("JamServiceParams must be instantiated by this JamServiceFactory.");
    }
    //create and return the service
    JamClassLoader clToUse = createClassLoader((JamServiceContextImpl)jsps);

    //this is a nasty way to shoehorn it in there, should do better
    ((JamServiceContextImpl)jsps).setClassLoader(clToUse);

    return new JamServiceImpl((ElementContext)jsps,
      getSpecifiedClasses((JamServiceContextImpl)jsps));
  }

  public JamClassLoader createSystemJamClassLoader() {
    JamServiceParams params = createServiceParams();
    params.setUseSystemClasspath(true);
    try {
      JamService service = createService(params);
      return service.getClassLoader();
    } catch(IOException reallyUnexpected) {
      reallyUnexpected.printStackTrace();
      throw new IllegalStateException(reallyUnexpected.getMessage());
    }
  }

  public JamClassLoader createJamClassLoader(ClassLoader cl) {
    JamServiceParams params = createServiceParams();
    params.setUseSystemClasspath(true); //?
    try {
      JamService service = createService(params);
      return service.getClassLoader();
    } catch(IOException reallyUnexpected) {
      reallyUnexpected.printStackTrace();
      throw new IllegalStateException(reallyUnexpected.getMessage());
    }
  }


  // ========================================================================
  // Protected methods - override these at your own risk

  /**
   * <p>Returns the set of classes to be included in a JamService to be
   * created by the given params.  You should not override this
   * unless you really know what you're doing.</p>
   */
  protected String[] getSpecifiedClasses(JamServiceContext params)
          throws IOException
  {
    return params.getAllClassnames();
  }

  /**
   * <p>Creates the main classloader to be used given the input ctx.
   * This is usually a composite of the source classloader and a
   * classfile classloader.  Subclasses may override to change the behavior.
   * </p>
   */
  protected JamClassLoader createClassLoader(JamServiceContext ctx)
          throws IOException
  {
    JamClassBuilder builder = createBuilder(ctx);
    return new JamClassLoaderImpl((ElementContext)ctx,//eww
      builder,ctx.getInitializer());
  }

  /**
   * <p>Creates the JamClassBuilder for the given context.  This will be
   * a composite that may include custom source and class-based classbuilders.
   * It usually includes the system classbuilders and always contains the
   * builtin classbuilder.</b>
   */
  protected JamClassBuilder createBuilder(JamServiceContext ctx)
    throws IOException

  {
    List builders = new ArrayList();  // make a list of the builders we want
    JamClassBuilder b = createSourceBuilder(ctx);
    if (b != null) builders.add(b);   // prefer first source
    b = createClassfileBuilder(ctx);  // then custom classpath
    if (b != null) builders.add(b);
    ClassLoader[] cls = ctx.getReflectionClassLoaders();
    for(int i=0; i<cls.length; i++) {
      builders.add(new ReflectClassBuilder(cls[i],ctx));
    }
    JamClassBuilder[] barray = new JamClassBuilder[builders.size()];
    builders.toArray(barray);
    JamClassBuilder out = new CompositeJamClassBuilder(barray);
    out.init((ElementContext)ctx);
    return out;

  }


  /**
   * <p>Creates the source-based classbuilder for the given context.
   * If no source files or paths are specified in the context,
   * just returns null.</p>
   */
  protected JamClassBuilder createSourceBuilder(JamServiceContext ctx)
    throws IOException
  {
    File[] sources = ctx.getSourceFiles();
    if (sources == null || sources.length == 0) {
      if (ctx.isVerbose()) {
        ctx.verbose(PREFIX+ "no source files present, "+
                    "skipping source ClassBuilder");
      }
      return null;
    }
    if(ctx.getProperty(USE_NEW_PARSER) == null) {
      return new JavadocClassBuilder(ctx);
    } else {
      return new ParserClassBuilder(ctx);
    }
  }


  /**
   * <p>Creates the class-based classbuilder for the given context.
   * If no class files or classloaders are specified in the params,
   * just returns null.</p>
   */
  protected JamClassBuilder createClassfileBuilder(JamServiceContext jp)
    throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    ResourcePath cp = jp.getInputClasspath();
    if (cp == null) {
      return null;
    } else {
      URL[] urls = cp.toUrlPath();
      ClassLoader cl = new URLClassLoader(urls);
      return new ReflectClassBuilder(cl,jp);
    }
  }

  /**
   * <p>Creates a source class loader that is based on javadoc.  This
   * is currently the default, but someday that will change.</p>

  protected JamClassBuilder createJavadocSourceBuilder(JamServiceContext ctx)
    throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    File[] files = ctx.getSourceFiles();
    if (files == null || files.length == 0) return null;
    String sourcePath = (ctx.getInputSourcepath() == null) ? null :
            ctx.getInputSourcepath().toString();
    String classPath = (ctx.getInputClasspath() == null) ? null :
            ctx.getInputClasspath().toString();
    return JavadocRunner.getInstance().
            create(files,
                   parent,
                   ctx.getOut(),
                   sourcePath,
                   classPath,
                   null);//FIXME get javadoc args from param props
  }
   */

}
