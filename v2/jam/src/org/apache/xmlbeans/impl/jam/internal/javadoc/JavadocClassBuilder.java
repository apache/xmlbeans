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
import org.apache.xmlbeans.impl.jam.editable.*;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.internal.JamServiceContextImpl;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocClassBuilder extends JamClassBuilder {

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

  public EClass build(String packageName, String className) {
    ClassDoc cd = mRootDoc.classNamed(packageName+"."+className);
    if (cd == null) return null;
    EClass out = createClass(packageName, className, null);
    populate(out,cd);
    return out;
  }

  // ========================================================================
  // Private methods

  private void populate(EClass dest, ClassDoc src) {
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
    ConstructorDoc[] ctors = src.constructors(false);
    for(int i=0; i<ctors.length; i++) populate(dest.addNewConstructor(),ctors[i]);
    // add the methods
    MethodDoc[] methods = src.methods(false);
    for(int i=0; i<methods.length; i++) populate(dest.addNewMethod(),methods[i]);
    // add the annotations
    addAnnotations(dest, src);
    addSourcePosition(dest,src);
  }

  private void populate(EField dest, FieldDoc src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.name());
    dest.setType(src.type().typeName());
    dest.setModifiers(src.modifierSpecifier());
    addAnnotations(dest, src);
    addSourcePosition(dest,src);
  }

  private void populate(EMethod dest, MethodDoc src) {
    populate((EInvokable)dest,(ExecutableMemberDoc)src);
    dest.setReturnType(src.returnType().typeName());
  }

  private void populate(EInvokable dest, ExecutableMemberDoc src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.name());
    dest.setModifiers(src.modifierSpecifier());
    ClassDoc[] exceptions = src.thrownExceptions();
    for(int i=0; i<exceptions.length; i++) {
      dest.addException(exceptions[i].typeName());
    }
    Parameter[] params = src.parameters();
    for(int i=0; i<params.length; i++) {
      populate(dest.addNewParameter(),params[i]);
    }
    addAnnotations(dest, src);
    addSourcePosition(dest,src);
  }

  private void populate(EParameter dest, Parameter src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.name());
    dest.setType(src.typeName());
    if (mIs15) addAnnotations(dest, callGetAnnotations(src));
  }

  private void addSourcePosition(EElement dest, Doc src) {
    SourcePosition jds = src.position();
    if (jds == null) return;
    ESourcePosition sp = dest.createSourcePosition();
    sp.setColumn(jds.column());
    sp.setLine(jds.line());
    File f = jds.file();
    if (f != null) sp.setSourceURI(f.toURI());
  }


  private void addAnnotations(EAnnotatedElement dest, ProgramElementDoc src) {
    String comments = src.getRawCommentText();
    if (comments != null) dest.createComment().setText(comments);
    if (mIs15) addAnnotations(dest,callGetAnnotations(src));
  }

  private void addAnnotations(EAnnotatedElement dest, Object[] descs) {
    if (descs == null) return;
    if (!mIs15) return;
    for(int i=0; i<descs.length; i++) {
      EAnnotation ann =
        dest.addAnnotationForType(callGetAnnotationType(descs[i]).typeName());
      ann.setArtifact(descs[i]);
      AnnotationProxy proxy = ann.getEditableProxy();
      Object[] mvps = callGetMemberValues(descs[i]);
      for(int j=0; j<mvps.length; j++) {
        String name = callGetMvpName(mvps[i]);
        Object value = callGetMvpValue(mvps[i]);
        if (name != null && value != null) proxy.setMemberValue(name,value);
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
