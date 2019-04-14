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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import org.apache.xmlbeans.impl.jam.annotation.JavadocTagParser;
import org.apache.xmlbeans.impl.jam.internal.JamServiceContextImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.internal.elements.PrimitiveClassImpl;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MElement;
import org.apache.xmlbeans.impl.jam.mutable.MField;
import org.apache.xmlbeans.impl.jam.mutable.MInvokable;
import org.apache.xmlbeans.impl.jam.mutable.MMethod;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;
import org.apache.xmlbeans.impl.jam.mutable.MSourcePosition;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamClassPopulator;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocClassBuilder extends JamClassBuilder implements JamClassPopulator {

  // ========================================================================
  // Constants

  public static final String ARGS_PROPERTY = "javadoc.args";
  public static final String PARSETAGS_PROPERTY = "javadoc.parsetags";

  // ========================================================================
  // Variables

  private RootDoc mRootDoc = null;
  private JavadocTigerDelegate mTigerDelegate = null;
  private JavadocTagParser mTagParser = null;
  private boolean mParseTags = true;//FIXME

  // ========================================================================
  // Constructors

  public JavadocClassBuilder() {}

  // ========================================================================
  // JamClassBuilder implementation

  public void init(ElementContext ctx) {
    if (ctx == null) throw new IllegalArgumentException("null context");
    super.init(ctx);
    getLogger().verbose("init()",this);
    initDelegate(ctx);
    initJavadoc((JamServiceContext)ctx); //dirty cast because we're 'built in'
  }


  public MClass build(String packageName, String className) {
    assertInitialized();
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
    assertInitialized();
    ClassDoc src = (ClassDoc)dest.getArtifact();
    if (src == null) throw new IllegalStateException("null artifact");
    dest.setModifiers(src.modifierSpecifier());
    dest.setIsInterface(src.isInterface());
    if (mTigerDelegate != null) dest.setIsEnumType(mTigerDelegate.isEnum(src));
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

    // add the 'annotation elements' separately.  javadoc used to return them
    // as methods but this has changed recently.
    if (mTigerDelegate != null) {
      mTigerDelegate.populateAnnotationTypeIfNecessary(src,dest,this);
    }

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

  // this is a gross little callback hook for Javadoc15DelegateImpl.
  // kinda hacky but we have little choice
  public MMethod addMethod(MClass dest, MethodDoc doc) {
    MMethod out = dest.addNewMethod();
    populate(out,doc);
    return out;
  }

  // ========================================================================
  // Private methods

  private void initDelegate(ElementContext ctx) {
    mTigerDelegate = JavadocTigerDelegate.create(ctx);
  }

  private void initJavadoc(JamServiceContext serviceContext) {
    // grab some useful stuff
    mTagParser = serviceContext.getTagParser();
    String pct = serviceContext.getProperty(PARSETAGS_PROPERTY);
    if (pct != null) {
      mParseTags = Boolean.valueOf(pct).booleanValue();
      getLogger().verbose("mParseTags="+mParseTags,this);
    }
    // now go run javadoc on the appropriate files
    File[] files;
    try {
      files = serviceContext.getSourceFiles();
    } catch(IOException ioe) {
      getLogger().error(ioe);
      return;
    }
    if (files == null || files.length == 0) {
      throw new IllegalArgumentException("No source files in context.");
    }
    String sourcePath = (serviceContext.getInputSourcepath() == null) ? null :
      serviceContext.getInputSourcepath().toString();
    String classPath = (serviceContext.getInputClasspath() == null) ? null :
      serviceContext.getInputClasspath().toString();
    if (getLogger().isVerbose(this)) {
      getLogger().verbose("sourcePath ="+sourcePath);
      getLogger().verbose("classPath ="+classPath);
      for(int i=0; i<files.length; i++) {
        getLogger().verbose("including '"+files[i]+"'");
      }
    }
    JavadocRunner jdr = JavadocRunner.newInstance();
    try {
      PrintWriter out = null;
      if (getLogger().isVerbose(this)) {
        out = new PrintWriter(System.out);
      }
      mRootDoc = jdr.run(files,
                         out,
                         sourcePath,
                         classPath,
                         getJavadocArgs(serviceContext),
                         getLogger());
      if (mRootDoc == null) {
        getLogger().error("Javadoc returned a null root");//FIXME error
      } else {
        if (getLogger().isVerbose(this)) {
          getLogger().verbose(" received "+mRootDoc.classes().length+
                              " ClassDocs from javadoc: ");
        }
        ClassDoc[] classes = mRootDoc.classes();
        // go through and explicitly add all of the class names.  we need to
        // do this in case they passed any 'unstructured' classes.  to the
        // params.  this could use a little TLC.
        for(int i=0; i<classes.length; i++) {
          if (classes[i].containingClass() != null) continue; // skip inners
          if (getLogger().isVerbose(this)) {
            getLogger().verbose("..."+classes[i].qualifiedName());
          }
          ((JamServiceContextImpl)serviceContext).
            includeClass(getFdFor(classes[i]));
        }
      }
    } catch (FileNotFoundException e) {
      getLogger().error(e);
    } catch (IOException e) {
      getLogger().error(e);
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
    if (dest == null) throw new IllegalArgumentException("null dest");
    if (src == null) throw new IllegalArgumentException("null src");
    populate((MInvokable)dest,(ExecutableMemberDoc)src);
    dest.setReturnType(getFdFor(src.returnType()));
  }

  private void populate(MInvokable dest, ExecutableMemberDoc src) {
    if (dest == null) throw new IllegalArgumentException("null dest");
    if (src == null) throw new IllegalArgumentException("null src");
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
    if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(dest,method,src);
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
      if (getLogger().isVerbose(this)) {
        getLogger().verbose("...'"+tags[i].name()+"' ' "+tags[i].text());
      }
      //note name() returns the '@', so we strip it here
      mTagParser.parse(dest,tags[i]);
    }
    if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(dest,src);
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
