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
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.mutable.*;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.internal.elements.PrimitiveClassImpl;
import org.apache.xmlbeans.impl.jam.internal.JamServiceContextImpl;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.JClass;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocClassBuilder extends JamClassBuilder {

  // ========================================================================
  // Constants

  private static boolean VERBOSE = false;

  // ========================================================================
  // Variables

  private RootDoc mRootDoc = null;
  private JamServiceContext mServiceContext;
  private boolean mIs15 = false;

  // ========================================================================
  // Constructors

  public JavadocClassBuilder(JamServiceContext ctx) {
    mServiceContext = ctx;
    init175getters();
  }

  // ========================================================================
  // JamClassBuilder implementation


  public void init(ElementContext ctx) {
    super.init(ctx);
    File[] files;
    try {
      files = mServiceContext.getSourceFiles();
    } catch(IOException ioe) {
      ctx.error(ioe);
      return;
    }
    if (files == null || files.length == 0) {
      throw new IllegalArgumentException("No source files in context.");
    }
    String sourcePath = (mServiceContext.getInputSourcepath() == null) ? null :
      mServiceContext.getInputSourcepath().toString();
    String classPath = (mServiceContext.getInputClasspath() == null) ? null :
      mServiceContext.getInputClasspath().toString();
    JavadocRunner jdr = new JavadocRunner();
    try {
      PrintWriter out = null;
      if (((JamServiceContextImpl)mServiceContext).isVerbose()) {
        out = new PrintWriter(System.out);
      }
      mRootDoc = jdr.run(files,
                         out,
                         sourcePath,
                         classPath,
                         null);//FIXME get javadoc args from param props
      if (mRootDoc == null) {
        ctx.debug("Javadoc returned a null root");//FIXME error
      }
    } catch (FileNotFoundException e) {
      ctx.error(e);
    } catch (IOException e) {
      ctx.error(e);
    }
  }



  public MClass build(String packageName, String className) {
    if (VERBOSE) {
      System.out.println("[JavadocClassBuilder] building '"+
                         packageName+"' '"+className+"'");
    }
    String cn = packageName.trim();
    if (cn.length() == 0) {
      cn = className;
    } else {
      cn = packageName + "." +className;
    }
    ClassDoc cd = mRootDoc.classNamed(cn);
    if (cd == null) return null;
    MClass out = createClassToBuild(packageName, className, null);
    populate(out,cd);
    return out;
  }

  // ========================================================================
  // Private methods

  private void populate(MClass dest, ClassDoc src) {
    dest.setArtifact(src);
    dest.setModifiers(src.modifierSpecifier());
    dest.setIsInterface(src.isInterface());
    // set the superclass
    ClassDoc s = src.superclass();
    if (s != null) dest.setSuperclass(s.qualifiedName());
    // set the interfaces
    ClassDoc[] ints = src.interfaces();
    for(int i=0; i<ints.length; i++) dest.addInterface(ints[i].qualifiedName());
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
    addSourcePosition(dest,src);
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
      populate(dest.addNewParameter(),params[i]);
    }
    addAnnotations(dest, src);
    addSourcePosition(dest,src);
  }

  private void populate(MParameter dest, Parameter src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.name());
    dest.setType(getFdFor(src.type()));
    if (mIs15) addAnnotations(dest, callGetAnnotations(src));
  }

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
      return t.qualifiedTypeName();
    } else {
      StringWriter out = new StringWriter();
      for(int i=0, iL=dim.length()/2; i<iL; i++) out.write("[");
      String primFd =
              PrimitiveClassImpl.getPrimitiveClassForName(t.qualifiedTypeName());
      if (primFd != null) { //i.e. if primitive
        out.write(primFd);
      } else {
        out.write("L");
        out.write(t.qualifiedTypeName());
        out.write(";");
      }
      return out.toString();
    }
  }

  private void addSourcePosition(MElement dest, Doc src) {
    SourcePosition jds = src.position();
    if (jds == null) return;
    MSourcePosition sp = dest.createSourcePosition();
    sp.setColumn(jds.column());
    sp.setLine(jds.line());
    File f = jds.file();
    if (f != null) sp.setSourceURI(f.toURI());
  }


  private void addAnnotations(MAnnotatedElement dest, ProgramElementDoc src) {
    String comments = src.getRawCommentText();
    if (comments != null) dest.createComment().setText(comments);
    if (mIs15) addAnnotations(dest,callGetAnnotations(src));
  }

  private void addAnnotations(MAnnotatedElement dest, Object[] descs) {
    if (descs == null) return;
    if (!mIs15) return;
    for(int i=0; i<descs.length; i++) {
      MAnnotation ann =
        dest.addAnnotationForType(callGetAnnotationType(descs[i]).qualifiedTypeName());
      ann.setArtifact(descs[i]);
      AnnotationProxy proxy = ann.getMutableProxy();
      Object[] mvps = callGetMemberValues(descs[i]);
      for(int j=0; j<mvps.length; j++) {
        String name = callGetMvpName(mvps[i]);
        Object value = callGetMvpValue(mvps[i]);
        if (name != null && value != null) proxy.setValue(name,value);
      }
    }
  }


  // ========================================================================
  // Goofy reflection stuff to keep us 1.4-safe

  private Method mAnnotationGetter;
  private Method mParameterAnnotationGetter;
  private Method mAnnotationTypeGetter;
  private Method mMemberValuesGetter;
  private Method mMvpName;
  private Method mMvpValue;

  private Object[] callGetAnnotations(ProgramElementDoc pd) {
    if (mAnnotationGetter == null) return null;
    return (Object[])invoke(mAnnotationGetter,pd);
  }

  private Object[] callGetAnnotations(Parameter p) {
    if (mParameterAnnotationGetter == null) return null;
    return (Object[])invoke(mParameterAnnotationGetter,p);
  }

  private ClassDoc callGetAnnotationType(Object desc) {
    if (mAnnotationTypeGetter == null) return null;
    return (ClassDoc)invoke(mAnnotationTypeGetter, desc);
  }

  private Object[] callGetMemberValues(Object desc) {
    if (mMemberValuesGetter == null) return null;
    return (Object[])invoke(mMemberValuesGetter, desc);
  }

  private String callGetMvpName(Object mvp) {
    if (mMvpName == null) return null;
    return (String)invoke(mMvpName, mvp);
  }

  private Object callGetMvpValue(Object mvp) {
    if (mMvpValue == null) return null;
    return invoke(mMvpValue, mvp);
  }

  private void init175getters() {
    mAnnotationGetter = getGetter(ProgramElementDoc.class,"annotations");
    mParameterAnnotationGetter = getGetter(Parameter.class,"annotations");
    try {
      Class annotationDesc = Class.forName("com.sun.javadoc.AnnotationDesc");
      mAnnotationTypeGetter = getGetter(annotationDesc, "annotationType");
      mMemberValuesGetter = getGetter(annotationDesc, "memberValues");
    } catch (ClassNotFoundException e) {
      mServiceContext.debug(e);
    }
    try {
      Class annotationDesc = Class.forName("com.sun.javadoc.AnnotationDesc.");
      mAnnotationTypeGetter = getGetter(annotationDesc, "annotationType");
      mMemberValuesGetter = getGetter(annotationDesc, "memberValues");
    } catch (ClassNotFoundException e) {
      mServiceContext.debug(e);
    }
    try {
      Class mvp = Class.forName("com.sun.javadoc.AnnotationDesc.MemberValuePair");
      mMvpName = getGetter(mvp, "name");
      mMvpValue = getGetter(mvp, "value");
    } catch (ClassNotFoundException e) {
      mServiceContext.debug(e);
    }
    mIs15 = true;
  }

  private Method getGetter(Class c, String name) {
    try {
      return c.getMethod(name, null);
    } catch (NoSuchMethodException e) {
      mServiceContext.debug(e);
    }
    return null;
  }

  private Object invoke(Method m, Object target) {
    try {
      return m.invoke(target,null);
    } catch (IllegalAccessException e) {
      mServiceContext.debug(e);
    } catch (InvocationTargetException e) {
      mServiceContext.debug(e);
    }
    return null;
  }

}
