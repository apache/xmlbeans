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
package org.apache.xmlbeans.impl.jam.internal.javadoc;

import com.sun.javadoc.*;
import org.apache.xmlbeans.impl.jam.mutable.*;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.internal.elements.PrimitiveClassImpl;
import org.apache.xmlbeans.impl.jam.internal.JamServiceContextImpl;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.provider.JamClassPopulator;
import org.apache.xmlbeans.impl.jam.annotation.JavadocTagParser;

import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocClassBuilder extends JamClassBuilder implements JamClassPopulator {

  // ========================================================================
  // Constants

  public static final String ARGS_PROPERTY = "javadoc.args";
  public static final String PARSETAGS_PROPERTY = "javadoc.parsetags";

  private static final String JAVA15_EXTRACTOR =
    "org.apache.xmlbeans.impl.jam.internal.java15.Javadoc15DelegateImpl";

  // ========================================================================
  // Variables

  private RootDoc mRootDoc = null;
  private JamServiceContext mServiceContext;
  private JamLogger mLogger;
  private Javadoc15Delegate mDelegate = null;
  private JavadocTagParser mTagParser = null;

  private boolean mParseTags = true;//FIXME

  // ========================================================================
  // Constructors

  public JavadocClassBuilder(JamServiceContext ctx) {
    if (ctx == null) throw new IllegalArgumentException("null context");
    mServiceContext = ctx;
    mLogger = ctx.getLogger();
    mTagParser = ctx.getTagParser();
    String pct = ctx.getProperty(PARSETAGS_PROPERTY);
    if (pct != null) {
      mParseTags = Boolean.valueOf(pct).booleanValue();
      mLogger.verbose("mParseTags="+mParseTags,this);
    }
    try {
      // See if we can load a 1.5-specific class.  If we can't, don't use
      // the Javadoc15Delegate.
      Class.forName("com.sun.javadoc.AnnotationDesc");
    } catch (ClassNotFoundException e) {
      if (mLogger.isVerbose(this)) {
        mLogger.warning("You are running under a pre-1.5 JDK.  JSR175-style "+
                        "source annotations will not be understood");
        mLogger.verbose(e);
      }
      return;
    }
    // ok, if we could load that, let's new up the extractor delegate
    try {
      mDelegate = (Javadoc15Delegate)
        Class.forName(JAVA15_EXTRACTOR).newInstance();
      // if this fails for any reason, things are in a bad state
    } catch (ClassNotFoundException e) {
      mLogger.error("Internal error, failed to instantiate "+
                    JAVA15_EXTRACTOR);
      mLogger.error(e);
    } catch (IllegalAccessException e) {
      mLogger.error("Internal error, failed to instantiate "+
                    JAVA15_EXTRACTOR);
      mLogger.error(e);
    } catch (InstantiationException e) {
      mLogger.error("Internal error, failed to instantiate "+
                    JAVA15_EXTRACTOR);
      mLogger.error(e);
    }
  }

  // ========================================================================
  // JamClassBuilder implementation

  public void init(ElementContext ctx) {
    super.init(ctx);
    if (mDelegate != null) mDelegate.init(ctx);
    mServiceContext.getLogger().verbose("init()",this);
    File[] files;
    try {
      files = mServiceContext.getSourceFiles();
    } catch(IOException ioe) {
      mLogger.error(ioe);
      return;
      
    }
    if (files == null || files.length == 0) {
      throw new IllegalArgumentException("No source files in context.");
    }
    String sourcePath = (mServiceContext.getInputSourcepath() == null) ? null :
      mServiceContext.getInputSourcepath().toString();
    String classPath = (mServiceContext.getInputClasspath() == null) ? null :
      mServiceContext.getInputClasspath().toString();
    if (mLogger.isVerbose(this)) {
      mLogger.verbose("sourcePath ="+sourcePath);
      mLogger.verbose("classPath ="+classPath);
      for(int i=0; i<files.length; i++) {
        mLogger.verbose("including '"+files[i]+"'");
      }
    }
    JavadocRunner jdr = new JavadocRunner();
    try {
      PrintWriter out = null;
      if (mLogger.isVerbose(this)) {
        out = new PrintWriter(System.out);
      }
      mRootDoc = jdr.run(files,
                         out,
                         sourcePath,
                         classPath,
                         getJavadocArgs(mServiceContext),
                         mLogger);
      if (mRootDoc == null) {
        mLogger.error("Javadoc returned a null root");//FIXME error
      } else {
        if (mLogger.isVerbose(this)) {
          mLogger.verbose(" received "+mRootDoc.classes().length+
                          " ClassDocs from javadoc: ");
        }
        ClassDoc[] classes = mRootDoc.classes();
        // go through and explicitly add all of the class names.  we need to
        // do this in case they passed any 'unstructured' classes.  to the
        // params.  this could use a little TLC.
        for(int i=0; i<classes.length; i++) {
          if (classes[i].containingClass() != null) continue; // skip inners
          if (mLogger.isVerbose(this)) {
            mLogger.verbose("..."+classes[i].qualifiedName());
          }
          ((JamServiceContextImpl)mServiceContext).
            includeClass(getFdFor(classes[i]));
        }
      }
    } catch (FileNotFoundException e) {
      mLogger.error(e);
    } catch (IOException e) {
      mLogger.error(e);
    }
  }

  public MClass build(String packageName, String className) {
    if (getLogger().isVerbose(this)) {
      getLogger().verbose("trying to build '"+packageName+"' '"+className+"'");
    }
    String loadme = (packageName.trim().length() > 0) ?
      (packageName + '.'  + className) :
      className;
    ClassDoc cd = mRootDoc.classNamed(loadme);
    if (cd == null) {
      if (getLogger().isVerbose(this)) {
        getLogger().verbose("no ClassDoc for "+loadme);
      }
      return null;
    }
    List importSpecs = null;
    {
      ClassDoc[] imported = cd.importedClasses();
      if (imported != null) {
        importSpecs = new ArrayList();
        for(int i=0; i<imported.length; i++) {
          importSpecs.add(getFdFor(imported[i]));
        }
      }
    }
    {
      PackageDoc[] imported = cd.importedPackages();
      if (imported != null) {
        if (importSpecs == null) importSpecs = new ArrayList();
        for(int i=0; i<imported.length; i++) {
          importSpecs.add(imported[i].name()+".*");
        }
      }
    }
    String[] importSpecsArray = null;
    if (importSpecs != null) {
      importSpecsArray = new String[importSpecs.size()];
      importSpecs.toArray(importSpecsArray);
    }
    MClass out = createClassToBuild(packageName, className, importSpecsArray, this);
    out.setArtifact(cd);
    return out;
  }

  // ========================================================================
  // JamClassPopulator implementation

  public void populate(MClass dest) {
    if (dest == null) throw new IllegalArgumentException("null dest");
    ClassDoc src = (ClassDoc)dest.getArtifact();
    if (src == null) throw new IllegalStateException("null artifact");
    dest.setModifiers(src.modifierSpecifier());
    dest.setIsInterface(src.isInterface());
    if (mDelegate != null) {
      dest.setIsEnumType(mDelegate.isEnum(src));
    }
    // set the superclass
    ClassDoc s = src.superclass();
    if (s != null) dest.setSuperclass(getFdFor(s));
    // set the interfaces
    ClassDoc[] ints = src.interfaces();
    for(int i=0; i<ints.length; i++) {
      dest.addInterface(getFdFor(ints[i]));
    }
    // add the fields
    FieldDoc[] fields = src.fields();
    for(int i=0; i<fields.length; i++) populate(dest.addNewField(),fields[i]);
    // add the constructors
    ConstructorDoc[] ctors = src.constructors();
    for(int i=0; i<ctors.length; i++) populate(dest.addNewConstructor(),ctors[i]);
    // add the methods
    MethodDoc[] methods = src.methods();
    for(int i=0; i<methods.length; i++) populate(dest.addNewMethod(),methods[i]);
    // add the annotations
    addAnnotations(dest, src);
    // add the source position
    addSourcePosition(dest,src);
    // add any inner classes
    ClassDoc[] inners = src.innerClasses();
    if (inners != null) {
      for(int i=0; i<inners.length; i++) {
        MClass inner = dest.addNewInnerClass(inners[i].typeName());
        inner.setArtifact(inners[i]);
        populate(inner);
      }
    }
  }

  private void populate(MField dest, FieldDoc src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.name());
    dest.setType(getFdFor(src.type()));
    dest.setModifiers(src.modifierSpecifier());
    addAnnotations(dest, src);
    addSourcePosition(dest,src);
  }

  private void populate(MMethod dest, MethodDoc src) {
    populate((MInvokable)dest,(ExecutableMemberDoc)src);
    dest.setReturnType(getFdFor(src.returnType()));
  }

  private void populate(MInvokable dest, ExecutableMemberDoc src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.name());
    dest.setModifiers(src.modifierSpecifier());
    ClassDoc[] exceptions = src.thrownExceptions();
    for(int i=0; i<exceptions.length; i++) {
      dest.addException(getFdFor(exceptions[i]));
    }
    Parameter[] params = src.parameters();
    for(int i=0; i<params.length; i++) {
      populate(dest.addNewParameter(),src,params[i]);
    }
    addAnnotations(dest, src);
    addSourcePosition(dest,src);
  }

  private void populate(MParameter dest, ExecutableMemberDoc method, Parameter src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.name());
    dest.setType(getFdFor(src.type()));
    if (mDelegate != null) mDelegate.extractAnnotations(dest,method,src);
  }


  private String[] getJavadocArgs(JamServiceContext ctx) {
    String prop = ctx.getProperty(ARGS_PROPERTY);
    if (prop == null) return null;
    StringTokenizer t = new StringTokenizer(prop);
    String[] out = new String[t.countTokens()];
    int i = 0;
    while(t.hasMoreTokens()) out[i++] = t.nextToken();
    return out;
  }

  private void addAnnotations(MAnnotatedElement dest, ProgramElementDoc src) {
    String comments = src.commentText();
    if (comments != null) dest.createComment().setText(comments);
    Tag[] tags = src.tags();
    //if (mLogger.isVerbose(this)) {
    //  mLogger.verbose("processing "+tags.length+" javadoc tags on "+dest);
    //}
    for(int i=0; i<tags.length; i++) {
      if (mLogger.isVerbose(this)) {
        mLogger.verbose("...'"+tags[i].name()+"' ' "+tags[i].text());
      }
      //note name() returns the '@', so we strip it here
      mTagParser.parse(dest,tags[i]);
    }
    if (mDelegate != null) mDelegate.extractAnnotations(dest,src);
  }

  // ========================================================================
  // Shared(?) utilities

  /**
   * Returns a classfile-style field descriptor for the given type.
   * This has to be called to get a name for a javadoc type that can
   * be used with Class.forName(), JRootContext.getClass(), or
   * JClass.forName().
   */
  public static String getFdFor(Type t) {
    if (t == null) throw new IllegalArgumentException("null type");
    String dim = t.dimension();
    if (dim == null || dim.length() == 0) {
      ClassDoc cd = t.asClassDoc();
      if (cd != null) {
        ClassDoc outer = cd.containingClass();
        if (outer == null) return cd.qualifiedName();
        String simpleName = cd.name();
        simpleName = simpleName.substring(simpleName.lastIndexOf('.')+1);
        return outer.qualifiedName()+'$'+simpleName;
      } else {
        return t.qualifiedTypeName();
      }
    } else {
      StringWriter out = new StringWriter();
      for(int i=0, iL=dim.length()/2; i<iL; i++) out.write("[");
      String primFd =
              PrimitiveClassImpl.getPrimitiveClassForName(t.qualifiedTypeName());
      if (primFd != null) { //i.e. if primitive
        out.write(primFd);
      } else {
        out.write("L");
        if (t.asClassDoc() != null) {
          out.write(t.asClassDoc().qualifiedName());
        } else {
          out.write(t.qualifiedTypeName());
        }
        out.write(";");
      }
      return out.toString();
    }
  }

  public static void addSourcePosition(MElement dest, Doc src) {
    SourcePosition pos = src.position();
    if (pos != null) addSourcePosition(dest,pos);
  }

  public static void addSourcePosition(MElement dest, SourcePosition pos) {
    MSourcePosition sp = dest.createSourcePosition();
    sp.setColumn(pos.column());
    sp.setLine(pos.line());
    File f = pos.file();
    if (f != null) sp.setSourceURI(f.toURI());
  }

}
