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
package org.apache.xmlbeans.test.jam;

import org.apache.xmlbeans.impl.jam.*;

import java.util.*;
import java.io.PrintWriter;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ImplComparisonTest {

  // ========================================================================
  // Variables

  private JService mService1;
  private JService mService2;
  private boolean mCompareAnnotations = false;
  private boolean mCompareComments = false;
  private PrintWriter mOut = new PrintWriter(System.out);
  private int mComparisons = 0;
  private int mFailures = 0;

  // ========================================================================
  // Public methods

  public void compare(JService service1, JService service2) {
    List list1 = getSortedClassList(service1);
    //FIXME should also compare that the same classes are in each service
    //  List list2 = getSortedClassList(service2);
    for(int i=0; i<list1.size(); i++) {
      JClass c1 = (JClass)list1.get(i);
      compare(c1, service2.getClassLoader().loadClass(c1.getQualifiedName()));
    }
  }

  public void compare(JClass class1, JClass class2) {
    compare(class1.getConstructors(),class2.getConstructors());
    compare(class1.getDeclaredFields(),class2.getDeclaredFields());
    compare(class1.getDeclaredMethods(),class2.getDeclaredMethods());
  }

  public void compare(JField[] fields1, JField[] fields2) {
    for(int i=0; i<fields1.length; i++) {
      compare(fields1[i],fields2[i]); //FIXME this is dumb and fragile
    }
  }

  public void compare(JField f1, JField f2) {
    if (!f1.getSimpleName().equals(f2.getSimpleName())) {
      failure("field names don't match",f1,f2);
    }
    if (!f1.getType().getQualifiedName().
            equals(f2.getType().getQualifiedName())) {
      failure("field types don't match",f1,f2);
    }
  }

  public void compare(JConstructor[] constrs1, JConstructor[] constrs2) {
    for(int i=0; i<constrs1.length; i++) {
      compare(constrs1[i],constrs2[i]); //FIXME this is dumb and fragile
    }
  }

  public void compare(JMethod[] methods1, JMethod[] methods2) {
    for(int i=0; i<methods1.length; i++) {
      compare(methods1[i],methods2[i]); //FIXME this is dumb and fragile
    }
  }

  public void compare(JMethod m1, JMethod m2) {
    compare((JInvokable)m1,(JInvokable)m2);
    if (!m1.getReturnType().getQualifiedName().
            equals(m2.getReturnType().getQualifiedName())) {
      failure("return types don't match",m1,m2);
    }

  }

  public void compare(JInvokable method1, JInvokable method2) {
    mComparisons++;
    if (!method1.getSimpleName().equals(method2.getSimpleName())) {
      failure("invokables have different names",method1,method2);
    }
//    compare(method1.getParameters(),method2.getParameters());
  }


  // ========================================================================
  // Private methods

  private void failure(String msg, JElement e1, JElement e2) {
    mOut.println(msg+"  ["+e1.getQualifiedName()+
                 "]["+e2.getQualifiedName()+"]");
    mFailures++;
  }

  private Map createName2Element(JElement[] elements) {
    Map out = new HashMap();
    for(int i=0; i<elements.length; i++) {
      out.put(elements[i].getSimpleName(),elements[i]);
    }
    return out;
  }

  private List getSortedClassList(JService service) {
    JClass[] classes = service.getAllClasses();
    Arrays.sort(classes,JElementComparator.getInstance());
    return Arrays.asList(classes);
  }
}
