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
package org.apache.xmlbeans.impl.jam.annogen.internal.java15;


/**
 * @deprecated remove this please
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Javadoc175ProxyPopulator {
  /*

    implements ProxyPopulator {

  // ========================================================================
  // Variables

  private ProxyContext mContext;

  // ========================================================================
  // ProxyPopulator implementation

  public boolean hasAnnotation(ElementId id, Class annoType) {
    if (!(id instanceof JElementId)) {
      throw new IllegalArgumentException("This case is NYI");
    }
    JElement element = (JElement)((JElementId)id).getElement();
    ProgramElementDoc ped = (ProgramElementDoc)(element.getArtifact());
    String targetAnnotationTypeName = mContext.getProxyTypeMapping().
      getDeclaredTypeNameForProxyType(annoType);
    return getAnnotationOfType(targetAnnotationTypeName,ped) != null;
  }

  public void populateProxy(ElementId id,
                            Class annoType,
                            AnnotationProxy targetInstance)
  {
    if (!(id instanceof JElementId)) {
      throw new IllegalArgumentException("This case is NYI");
    }
    JElement element = (JElement)((JElementId)id).getElement();
    ProgramElementDoc ped = (ProgramElementDoc)(element.getArtifact());
    AnnotationDesc ad = getAnnotationOfType(annoType.getName(),ped);
    if (ad != null) populateProxy(ad,targetInstance,ped.position());
  }

  public void init(ProxyContext pc) { mContext = pc; }

  // ========================================================================
  // Private methods

  private AnnotationDesc getAnnotationOfType(String typeName, ProgramElementDoc ped) {
    AnnotationDesc[] annos = ped.annotations();
    for(int i=0; i<annos.length; i++) {
      if (annos[i].annotationType().qualifiedName().
        equals(typeName)) {
        return annos[i];
      }
    }
    return null;
  }

  private void populateProxy(AnnotationDesc src,
                             AnnotationProxy dest,
                             SourcePosition sp) {
//    if (sp != null) JavadocClassBuilder.addSourcePosition(dest,sp);
    {
      AnnotationDesc.ElementValuePair[] mvps = src.elementValues();
      for(int i=0; i<mvps.length; i++) {
        Type jmt = mvps[i].element().returnType();
        String name = mvps[i].element().name();
        AnnotationValue aval = mvps[i].value();
        setAnnotationValue(name,jmt,aval,dest,sp);
      }
    }
    { // also set values for the type's defaults
      AnnotationTypeDoc atd = src.annotationType();
      AnnotationTypeElementDoc[] elements = atd.elements();
      for(int i=0; i<elements.length; i++) {
        AnnotationValue value = elements[i].defaultValue();
        if (value != null) {
          String name = elements[i].name();
          System.out.println("default value named '"+name+"'  = "+
                             " "+value+"  "+value.getClass()+" "+dest.getValue(name));
          if (dest.getValue(name) == null) {
            setAnnotationValue(name,elements[i].returnType(),
                               value,dest,sp);
          }
        }
      }
    }
  }

  private void setAnnotationValue(String memberName,
                                  Type returnType,
                                  AnnotationValue aval,
                                  AnnotationProxy dest,
                                  SourcePosition sp) {
    String typeName = getFdFor(returnType);
    Object valueObj;
    try {
      valueObj = aval.value();
    } catch(NullPointerException npe) {
      //FIXME temporary workaround for sun bug
      mContext.getLogger().warning
        ("Encountered a known javadoc bug which usually \n"+
         "indicates a syntax error in an annotation value declaration.\n"+
         "The value is being ignored.\n"+
         "[file="+sp.file()+", line="+sp.line()+"]");
      return;
    }
    if (mContext.getLogger().isVerbose(this)) {
      mContext.getLogger().verbose(memberName+" is a "+typeName+
                                   " with valueObj "+valueObj+
                                   ", class is "+valueObj.getClass());
    }
    // ok, take a look at how what it really is and translate it into an
    // appropriate represenatation
    if (valueObj instanceof AnnotationDesc) {
      AnnotationProxy nested = dest.createNestedValue(memberName,typeName);
      populateProxy((AnnotationDesc)valueObj,nested,sp);
    } else if (valueObj instanceof Number || valueObj instanceof Boolean) {
      String tn = JavadocClassBuilder.getFdFor(returnType);
      try {
        Class type = mContext.getClassLoader().loadClass(tn);
        dest.setValue(memberName,valueObj,type);
      } catch(ClassNotFoundException cnfe) {
        mContext.getLogger().error(cnfe);
      }
    } else if (valueObj instanceof FieldDoc) {
      String tn = JavadocClassBuilder.getFdFor(((FieldDoc)valueObj).containingClass());
      // this means it's an enum constant
      try {
        Class type = mContext.getClassLoader().loadClass(tn);
        String val = ((FieldDoc)valueObj).name(); //REVIEW is this right?
        dest.setValue(memberName,val,type);
      } catch(ClassNotFoundException cnfe) {
        mContext.getLogger().error(cnfe);
      }
    } else if (valueObj instanceof ClassDoc) {
      String tn = JavadocClassBuilder.getFdFor(((FieldDoc)valueObj).containingClass());
      try {
        Class clazz = mContext.getClassLoader().loadClass(tn);
        dest.setValue(memberName,clazz,Class.class);
      } catch(ClassNotFoundException cnfe) {
        mContext.getLogger().error(cnfe);
      }
    } else if (valueObj instanceof String) {
      String v = ((String)valueObj).trim();
      if (v.startsWith("\"") && v.endsWith("\"")) {
        //javadoc gives us the quotes, which seems kinda dumb.  just deal.
        valueObj = v.substring(1,v.length()-1);
      }
      dest.setValue(memberName,valueObj,String.class);
    } else if (valueObj instanceof AnnotationValue[]) {
      populateArrayMember(dest,memberName,returnType,(AnnotationValue[])valueObj,sp);
    } else {
      mContext.getLogger().error
        ("Value of annotation member "+memberName+" is " +
         "of an unexpected type: "+valueObj.getClass()+"   ["+valueObj+"]");
    }
  }

  /**
   *
   * The javadocs for com.sun.javadoc.AnnotationValue.value() read as follows:
   *
   * <p><pre><i>
   *
   * Returns the value. The type of the returned object is one of the
   * following:
   *
   * a wrapper class for a primitive type
   * String
   * ClassDoc
   * FieldDoc (representing an enum constant)
   * AnnotationDesc
   * AnnotationValue[]
   *
   * </i></pre></p>
   *
   * <p>It seems quite broken to me that in the array case, it returns an array
   * of AnnotationValues.  It would be a lot easier to deal with the API
   * if that last line instead read "or an array of any of the above."
   * As it is, it's imposible to get the doclet API to give you a simple
   * array of ints, for example.  It's not at all clear what the extra
   * wrapping buys you.</p>
   *
   * <p>So, this method does a bunch of work so that JAM does the unwrapping
   * for the user.</p>

  private void populateArrayMember(AnnotationProxy dest,
                                   String memberName,
                                   Type returnType,
                                   AnnotationValue[] annValueArray,
                                   SourcePosition sp)
  {
    throw new IllegalStateException("arrays are NYI");
    /*
    if (sp != null) JavadocClassBuilder.addSourcePosition(dest,sp);
    if (annValueArray.length == 0) {
      Object[] value = new Object[0];
      //FIXME this is a little bit busted - we should try to give them
      //more type information than this.  it's just a little bit harder
      //to figure it out from the javadoc objects
      dest.setValue(memberName,value,loadClass(returnType));
      return;
    }
    // unpack the AnnotationValue values into a single array.
    Object[] valueArray = new Object[annValueArray.length];
    for(int i=0; i<valueArray.length; i++) {
      try {
        valueArray[i] = annValueArray[i].value();
        if (valueArray[i] == null) {
          getLogger().error
            ("Javadoc provided an array annotation member value which contains "+
             "[file="+sp.file()+", line="+sp.line()+"]");
          return;
        }
      } catch(NullPointerException npe) {
        //FIXME temporary workaround for sun bug
        getLogger().warning
          ("Encountered a known javadoc bug which usually \n"+
           "indicates a syntax error in an annotation value declaration.\n"+
           "The value is being ignored.\n"+
           "[file="+sp.file()+", line="+sp.line()+"]");
        return;
      }
    }
    // now go do something with them
    if (valueArray[0] instanceof AnnotationDesc) {
      String annType = getFdFor(((AnnotationDesc)valueArray[0]).annotationType());
      MAnnotation[] anns = dest.createNestedValueArray
        (memberName, annType, valueArray.length);
      for(int i=0; i<anns.length; i++) {
        populateAnnotation(anns[i],(AnnotationDesc)valueArray[i],sp);
      }
    } else if (valueArray[0] instanceof Number || valueArray[0] instanceof Boolean) {
      JClass type = loadClass(returnType);
      dest.setValue(memberName,annValueArray,type);
    } else if (valueArray[0] instanceof FieldDoc) {
      // this means it's an array of an enum constants
      String enumTypeName = JavadocClassBuilder.getFdFor(
        ((FieldDoc)valueArray[0]).containingClass());
      JClass memberType = loadClass("[L"+enumTypeName+";");
      String[] value = new String[valueArray.length];
      for(int i=0; i<valueArray.length; i++) {
        value[i] = ((FieldDoc)valueArray[i]).name(); //REVIEW is this right?
      }
      dest.setValue(memberName,value,memberType);
    } else if (valueArray[0] instanceof ClassDoc) {
      JClass[] value = new JClass[valueArray.length];
      for(int i=0; i<value.length; i++) {
        value[i] = loadClass(((ClassDoc)valueArray[0]));
      }
      dest.setValue(memberName,value,loadClass(JClass[].class));
    } else if (valueArray[0] instanceof String) {
      String[] value = new String[valueArray.length];
      for(int i=0; i<value.length; i++) {
        String v = ((String)valueArray[i]).trim();
        if (v.startsWith("\"") && v.endsWith("\"")) {
          //javadoc gives us the quotes, which seems kinda dumb.  just deal.
          v = v.substring(1,v.length()-1);
        }
        value[i] = v;
      }
      dest.setValue(memberName,value,loadClass(String[].class));
    } else {
      getLogger().error("Value of array annotation member "+
                                 memberName+" is of an unexpected type: "+
                                 valueArray[0].getClass()+"   ["+
                                 valueArray[0]+"]");
    }
  }

  private String getFdFor(Type t) { return JavadocClassBuilder.getFdFor(t); }
  */

}



