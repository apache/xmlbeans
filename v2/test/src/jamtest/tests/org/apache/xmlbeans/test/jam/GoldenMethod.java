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

import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;

import java.util.StringTokenizer;

import junit.framework.Assert;

/**
 * Independent test structure representing a method.  Can be compared
 * to a JMethod.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class GoldenMethod {

  // ========================================================================
  // Factory methods/static utils

  public static GoldenMethod[] createArray(String[][] params) {
    GoldenMethod[] out = new GoldenMethod[params.length];
    for(int i=0; i<out.length; i++) {
      out[i] = new GoldenMethod(params[i]);
    }
    return out;
  }

  public static void doComparison(JMethod[] result,
                                  GoldenMethod[] expected,
                                  boolean compareParamNames,
                                  Assert a) {
    a.assertTrue("different number of methods ["+
                 result.length+","+expected.length+"]",
                 result.length == expected.length);
    for(int i=0; i<result.length; i++) {
      expected[i].compare(result[i],compareParamNames,a);
    }
  }

  // ========================================================================
  // Variables

  private int mModifers;
  private String mReturnType;
  private String mName;
  private String[] mParamTypes;
  private String[] mParamNames;


  // ========================================================================
  // Constructors

  public GoldenMethod(String[] modsTypeNameParams) {
    this(modsTypeNameParams[0],
         modsTypeNameParams[1],
         modsTypeNameParams[2],
         modsTypeNameParams[3]);
  }

  /**
   *
   * @param modiferString
   * @param name
   * @param paramString
   */
  public GoldenMethod(String modiferString,
                      String type,
                      String name,
                      String paramString) {
    mModifers = ModifierHelper.parseModifiers(modiferString);
    mReturnType = type;
    mName = name;
    //parse parameter string
    if (paramString == null) {
      mParamTypes = new String[0];
      mParamNames = new String[0];
    } else {
      StringTokenizer st = new StringTokenizer(paramString,",");
      mParamTypes = new String[st.countTokens()];
      mParamNames = new String[st.countTokens()];
      int i = 0;
      while(st.hasMoreTokens()) {
        String token = st.nextToken().trim();
        int space = token.indexOf(" ");
        mParamNames[i] = token.substring(space).trim();
        mParamTypes[i] = token.substring(0,space).trim();
        i++;
      }
    }
  }

  public void compare(JMethod method, boolean compareParamNames, Assert a) {
    a.assertTrue("method names are different",
                 method.getSimpleName().equals(mName));
    a.assertTrue("return types are different",
                 method.getReturnType().getQualifiedName().equals(mReturnType));
    a.assertTrue("modifiers are different on "+method.getSimpleName()+
                 "["+method.getModifiers()+","+mModifers+"]",
                 method.getModifiers() == mModifers);
    JParameter[] params = method.getParameters();
    a.assertTrue("parameter lists are of different lengths",
                 params.length == mParamTypes.length);
    for(int i=0; i<params.length; i++) {
      a.assertTrue("parameter type is different on "+method.getSimpleName()+
                   "["+params[i].getType().getQualifiedName()+","+mParamTypes[i]+"]",
                   params[i].getType().getQualifiedName().equals(mParamTypes[i]));
      if (compareParamNames) {
        a.assertTrue("parameter names are different on "+method.getSimpleName(),
                     params[i].getSimpleName().equals(mParamNames[i]));
      }
    }
  }

}
