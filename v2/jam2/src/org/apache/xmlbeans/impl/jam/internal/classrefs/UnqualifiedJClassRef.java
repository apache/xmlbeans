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
package org.apache.xmlbeans.impl.jam.internal.classrefs;

import org.apache.xmlbeans.impl.jam.JClass;

import java.io.StringWriter;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class UnqualifiedJClassRef implements JClassRef {

  // ========================================================================
  // Constants

  private static final boolean VERBOSE = false;
  private static final String PREFIX = "[UnqualifiedJClassRef]";

  // ========================================================================
  // Variables

  private String mUnqualifiedClassname;
  private String mQualifiedClassname = null;
  private JClassRefContext mContext;

  // ========================================================================
  // Factory

  /**
   * Creates a new JClassRef for a qualified class or type name.
   */
  public static JClassRef create(String qualifiedClassname,
                                 JClassRefContext ctx) {
    return new UnqualifiedJClassRef(qualifiedClassname,ctx);
  }

  // ========================================================================
  // Constructors

  private UnqualifiedJClassRef(String ucname,
                               JClassRefContext ctx)
  {
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    if (ucname == null) throw new IllegalArgumentException("null ucname");
    mContext = ctx;
    mUnqualifiedClassname = ucname;
    if (VERBOSE) System.out.println("[UnqualifiedJClassRef] created for '"+
                                    ucname+"'");
  }

  // ========================================================================
  // JClassRef implementation

  public JClass getRefClass() {
    //FIXME this needs optimization, keep it simple and lazy for now
    return mContext.getClassLoader().loadClass(getQualifiedName());
  }

  public String getQualifiedName() {
    if (mQualifiedClassname != null) return mQualifiedClassname;
    // ok, check to see if it's an array type.  if so, we want to strip
    // away all the brackets and so we can try to load just the component
    // type.
    String candidateName;
    int arrayDimensions = 0;
    int bracket = mUnqualifiedClassname.indexOf('[');
    if (bracket != -1) {
      candidateName = mUnqualifiedClassname.substring(0,bracket);
      do {
        arrayDimensions++;
        bracket = mUnqualifiedClassname.indexOf('[',bracket+1);
      } while(bracket != -1);
    } else {
      candidateName = mUnqualifiedClassname;
    }
    // ok, try to get the class that they are talking about
    String name = qualifyName(candidateName);
    if (name == null) {
      throw new IllegalStateException("unable to handle unqualified java type "+
                                      "reference '"+candidateName+" ["+
                                      mUnqualifiedClassname+"]'. "+
                                      "This is still partially NYI.");
    }
    // now if it was an array, we need to convert it into a corresponding
    // field descriptor
    if (arrayDimensions > 0) {
      StringWriter out = new StringWriter();
      for(int i=0; i<arrayDimensions; i++) out.write('[');
      out.write('L');
      out.write(name);
      out.write(';');

      mQualifiedClassname = out.toString();
    } else {
      mQualifiedClassname = name;
    }
    return mQualifiedClassname;
  }

  // ========================================================================
  // Private methods

  private String qualifyName(String ucname) {
    String out = null;
    if ((out = checkExplicitImport(ucname)) != null) return out;
    if ((out = checkJavaLang(ucname)) != null) return out;
    if ((out = checkSamePackage(ucname)) != null) return out;
    if ((out = checkAlreadyQualified(ucname)) != null) return out;
    return null;
  }

  /**
   * Check to see if the unqualified name actually is already qualified.
   */
  private String checkSamePackage(String ucname) {
    String name = mContext.getPackageName()+"."+ucname;
    JClass clazz = mContext.getClassLoader().loadClass(name);
    if (VERBOSE) System.out.println(PREFIX+" checkSamePackage '"+name+"'  "+
                                    clazz.isUnresolvedType()+"  "+mContext.getClassLoader().getClass());
    return (clazz.isUnresolvedType()) ? null : clazz.getQualifiedName();
  }

  /**
   * Check to see if the unqualified name is in java.lang.
   */
  private String checkJavaLang(String ucname) {
    String name = "java.lang."+ucname;
    JClass clazz = mContext.getClassLoader().loadClass(name);
    if (VERBOSE) System.out.println(PREFIX+" checkJavaLang '"+name+"'  "+
                                    clazz.isUnresolvedType()+"  "+mContext.getClassLoader().getClass());
    return (clazz.isUnresolvedType()) ? null : clazz.getQualifiedName();
  }

  /**
   * Check to see if the unqualified name actually is already qualified.
   */
  private String checkAlreadyQualified(String ucname) {
    JClass clazz =
            mContext.getClassLoader().loadClass(ucname);
    return (clazz.isUnresolvedType()) ? null : clazz.getQualifiedName();
  }


  /**
   * Run through the list of import specs and see if the class was explicitly
   * (i.e. without '*') imported.
   */
  private String checkExplicitImport(String ucname) {
    String[] imports = mContext.getImportSpecs();
    if (VERBOSE) System.out.println(PREFIX+" checkExplicitImport "+
                                    imports.length);
    for(int i=0; i<imports.length; i++) {
      //FIXME this does not cover inner classes
      String last = lastSegment(imports[i]);
      if (VERBOSE) System.out.println(PREFIX+" checkExplicitImport '"+
                                      imports[i]+"'  '"+last+"'");
      if (last.equals(ucname)) return imports[i];
    }
    return null;
  }

  private static String lastSegment(String s) {
    int lastDot = s.lastIndexOf(".");
    if (lastDot == -1) return s;
    return s.substring(lastDot+1);
  }

  private static String firstSegment(String s) {
    int lastDot = s.indexOf(".");
    if (lastDot == -1) return s;
    return s.substring(0,lastDot);
  }

}

