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
package org.apache.xmlbeans.impl.jam.annogen.tools;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JamServiceParams;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.annogen.internal.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.jam.annogen.internal.joust.WriterFactory;
import org.apache.xmlbeans.impl.jam.annogen.internal.joust.FileWriterFactory;
import org.apache.xmlbeans.impl.jam.annogen.internal.joust.SourceJavaOutputStream;
import org.apache.xmlbeans.impl.jam.annogen.internal.joust.ExpressionFactory;
import org.apache.xmlbeans.impl.jam.annogen.internal.joust.Variable;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashSet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Annogen {

  // ========================================================================
  // main() method
 
  public static void main(String[] args) {
    try {
    JamServiceFactory jsf = JamServiceFactory.getInstance();
    JamServiceParams params = jsf.createServiceParams();
    Annogen ag = new Annogen();
    for(int i=0; i<args.length; i++) {
      if (args[i].equals("-d")) {
        i++;
        ag.setOutputDir(new File(args[i]));
        i++;
      } else {
        File f = new File(args[i]);
        if (f.isDirectory()) {
          File[] fs = f.listFiles();
          for(int j=0; j<fs.length; j++) params.includeSourceFile(fs[j]);
        } else {
          params.includeSourceFile(f);
        }
      }
    }
      JamService js = jsf.createService(params);
      ag.addAnnotationClasses(js.getAllClasses());
      ag.doCodegen();
    } catch(IOException ioe) {
      ioe.printStackTrace();
      System.out.flush();
      System.exit(-1);
    }
  }


  // ========================================================================
  // Constants

  public static final String SETTER_PREFIX = "set_";
  private static final String FIELD_PREFIX = "_";

  // ========================================================================
  // Variables

  private List mClassesTodo = null;
  private Collection mClassesDone = null;
  private JavaOutputStream mJoust = null;

  // ========================================================================
  // Constructors

  public Annogen() {
    mClassesTodo = new LinkedList();
    mClassesDone = new HashSet();
  }

  // ========================================================================
  // Public methods

//  public void setPackageMapper() {

//  }

  public void addAnnotationClasses(JClass[] classes) {
    for(int i=0; i<classes.length; i++) {
      if (true || classes[i].isAnnotationType()) {
        mClassesTodo.addAll(Arrays.asList(classes));
      } else {
        warn("Ignoring "+classes[i].getQualifiedName()+
             " because it is not an annotation type.");
      }
    }
  }

  public void setOutputDir(File dir) {
    WriterFactory wf = new FileWriterFactory(dir);
    setJavaOutputStream(new SourceJavaOutputStream(wf));
  }

  public void setJavaOutputStream(JavaOutputStream joust) {
    mJoust = joust;
  }

  public void doCodegen() throws IOException {
    while(mClassesTodo.size() > 0) {
      JClass clazz = (JClass)mClassesTodo.get(0);
      mClassesTodo.remove(0);
      mClassesDone.add(clazz);
      doCodegen(clazz);
    }
  }

  // ========================================================================
  // Private methods

  private void doCodegen(JClass clazz) throws IOException {
    ExpressionFactory ef = mJoust.getExpressionFactory();
    JMethod[] methods = clazz.getDeclaredMethods();
    mJoust.startFile(getPackageFor(clazz),clazz.getSimpleName());
    mJoust.startClass(Modifier.PUBLIC,
                      null,
                      new String[] {clazz.getQualifiedName()});
    for(int i=0; i<methods.length; i++) {
      String fieldName = FIELD_PREFIX+ methods[i].getSimpleName();
      String typeName = methods[i].getReturnType().getQualifiedName();
      Variable fieldVar =
        mJoust.writeField(Modifier.PRIVATE,typeName,fieldName,null);
      { // write the 'getter' implementation
        mJoust.startMethod(Modifier.PUBLIC,
                           typeName,
                           methods[i].getSimpleName(),
                           null, // no parameters
                           null, // no parameters
                           null // no throws
                           );
        mJoust.writeReturnStatement(fieldVar);
        mJoust.endMethodOrConstructor();
      }
      { // write the 'setter' implementation
        String[] paramTypeNames = new String[] {typeName};
        String[] paramNames = new String[] {"in"};
        Variable[] params = mJoust.startMethod(Modifier.PUBLIC,
                                               "void",
                                               SETTER_PREFIX+ methods[i].getSimpleName(),
                                               paramTypeNames,
                                               paramNames,
                                               null // no throws
        );
        mJoust.writeAssignmentStatement(fieldVar,params[0]);
        mJoust.endMethodOrConstructor();
      }
      { // check to see if we need to also do annogen for the field's type
        JClass c = clazz.forName(typeName);
        if (c.isAnnotationType()) {
          if (!mClassesTodo.contains(c) && !mClassesDone.contains(c)) {
            mClassesTodo.add(c);
          }
        }
      }
    }
    mJoust.endClassOrInterface();
    mJoust.endFile();
  }

  private void warn(String msg) {
    System.out.println("[Warning] "+msg);
  }

  //TODO make this logic pluggable
  private String getPackageFor(JClass clazz) {
    return clazz.getContainingPackage().getQualifiedName()+".impl";
  }

  public static String getImplClassFor(Class clazz) {
    String shortName = clazz.getName();
    shortName = shortName.substring(shortName.lastIndexOf('.')+1);
    return clazz.getPackage().getName()+".impl."+shortName;
  }


}