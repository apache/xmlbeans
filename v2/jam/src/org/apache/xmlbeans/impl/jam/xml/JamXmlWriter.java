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
package org.apache.xmlbeans.impl.jam.xml;

import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JConstructor;
import org.apache.xmlbeans.impl.jam.JField;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JAnnotatedElement;
import org.apache.xmlbeans.impl.jam.JInvokable;
import org.apache.xmlbeans.impl.jam.JSourcePosition;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLOutputFactory;
import java.io.Writer;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamXmlWriter {

  // ========================================================================
  // Constants

  public static final String PACKAGE_ELEMENT = "package";

  public static final String CLASS_ELEMENT = "class";
  public static final String NAME_ELEMENT = "name";
  public static final String ISINTERFACE_ELEMENT = "is-interface";
  public static final String INTERFACE_ELEMENT = "interface";
  public static final String SUPERCLASS_ELEMENT = "superclass";
  public static final String MODIFIERS_ELEMENT = "modifiers";
  public static final String PARAMETER_ELEMENT = "parameter";
  public static final String TYPE_ELEMENT = "parameter";
  public static final String CONSTRUCTOR_ELEMENT = "constructor";
  public static final String METHOD_ELEMENT = "method";
  public static final String FIELD_ELEMENT = "field";
  public static final String RETURNTYPE_ELEMENT = "return-type";
  public static final String COMMENT_ELEMENT = "comment";

  public static final String SOURCEPOSITION_ELEMENT = "source-position";
  public static final String LINE_ELEMENT = "line";
  public static final String COLUMN_ELEMENT = "column";
  public static final String SOURCEURI_ELEMENT = "source-uri";

  public static final String VALUE_ELEMENT = "value";

  public static final String ANNOTATION_ELEMENT = "annotation";
  public static final String ANNOTATIONVALUE_ELEMENT = "annotation-value";

  // ========================================================================
  // Variables

  private XMLStreamWriter mOut;

  // ========================================================================
  // Constructors

  public JamXmlWriter(Writer out) throws XMLStreamException  {
    if (out == null) throw new IllegalArgumentException("null out");
    mOut = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
  }

  public JamXmlWriter(XMLStreamWriter out) {
    if (out == null) throw new IllegalArgumentException("null out");
    mOut = out;
  }

  // ========================================================================
  // Public methods

  public void write(JPackage pkg) throws XMLStreamException {
    mOut.writeStartElement(PACKAGE_ELEMENT);
    JClass[] c = pkg.getClasses();
    for(int i=0; i<c.length; i++) write(c[i]);
    writeAnnotatedElement(pkg);
    mOut.writeEndElement();
  }

  public void write(JClass clazz) throws XMLStreamException {
    mOut.writeStartElement(CLASS_ELEMENT);
    writeValueElement(NAME_ELEMENT,clazz.getQualifiedName());
    writeValueElement(ISINTERFACE_ELEMENT,clazz.isInterface());
    JClass sc = clazz.getSuperclass();
    if (sc != null) writeValueElement(SUPERCLASS_ELEMENT,sc.getQualifiedName());
    writeClassList(INTERFACE_ELEMENT,clazz.getInterfaces());
    writeModifiers(clazz.getModifiers());
    {
      JField[] f = clazz.getDeclaredFields();
      for(int i=0; i<f.length; i++) write(f[i]);
    }{
      JConstructor[] c = clazz.getConstructors();
      for(int i=0; i<c.length; i++) write(c[i]);
    }{
      JMethod[] m = clazz.getDeclaredMethods();
      for(int i=0; i<m.length; i++) write(m[i]);
    }
    //FIXME inner classes?
    writeAnnotatedElement(clazz);
    mOut.writeEndElement();
  }

  public void write(JMethod method) throws XMLStreamException {
    mOut.writeStartElement(METHOD_ELEMENT);
    writeValueElement(NAME_ELEMENT,method.getSimpleName());
    writeValueElement(RETURNTYPE_ELEMENT,
                      method.getReturnType().getQualifiedName());
    writeInvokable(method);
    mOut.writeEndElement();
  }

  public void write(JConstructor ctor) throws XMLStreamException {
    mOut.writeStartElement(CONSTRUCTOR_ELEMENT);
    writeInvokable(ctor);
    mOut.writeEndElement();
  }

  public void write(JField field) throws XMLStreamException {
    mOut.writeStartElement(FIELD_ELEMENT);
    writeValueElement(NAME_ELEMENT,field.getSimpleName());
    writeModifiers(field.getModifiers());
    writeValueElement(TYPE_ELEMENT,field.getType().getQualifiedName());
    writeAnnotatedElement(field);
    mOut.writeEndElement();
  }

  // ========================================================================
  // Private methods

  private void writeInvokable(JInvokable ji) throws XMLStreamException {
    writeModifiers(ji.getModifiers());
    JParameter[] params = ji.getParameters();
    for(int i=0; i<params.length; i++) {
      mOut.writeStartElement(PARAMETER_ELEMENT);
      writeValueElement(NAME_ELEMENT,params[i].getSimpleName());
      writeValueElement(TYPE_ELEMENT,params[i].getType().getQualifiedName());
      writeAnnotatedElement(params[i]);
      mOut.writeEndElement();
    }
    writeAnnotatedElement(ji);
  }

  private void writeClassList(String elementName, JClass[] clazzes)
    throws XMLStreamException
  {
    for(int i=0; i<clazzes.length; i++) {
      mOut.writeStartElement(elementName);
      mOut.writeCharacters(clazzes[i].getQualifiedName());
      mOut.writeEndElement();
    }
  }

  private void writeModifiers(int mods) throws XMLStreamException {
    mOut.writeStartElement(MODIFIERS_ELEMENT);
    mOut.writeCharacters(String.valueOf(mods));
    mOut.writeEndElement();
  }

  private void writeValueElement(String elementName, boolean b)
    throws XMLStreamException
  {
    mOut.writeStartElement(elementName);
    mOut.writeCharacters(String.valueOf(b));
    mOut.writeEndElement();
  }

  private void writeValueElement(String elementName, int x)
    throws XMLStreamException
  {
    mOut.writeStartElement(elementName);
    mOut.writeCharacters(String.valueOf(x));
    mOut.writeEndElement();
  }

  private void writeValueElement(String elementName, String val)
    throws XMLStreamException
  {
    mOut.writeStartElement(elementName);
    mOut.writeCharacters(val);
    mOut.writeEndElement();
  }

  private void writeAnnotatedElement(JAnnotatedElement ae)
    throws XMLStreamException
  {
    JAnnotation[] anns = ae.getAnnotations();
    for(int i=0; i<anns.length; i++) {
      writeAnnotation(anns[i]);
    }
    JComment jc = ae.getComment();
    if (jc != null) {
      String text = jc.getText();
      if (text != null) {
        text = text.trim();
        if (text.length() > 0) {
          mOut.writeStartElement(COMMENT_ELEMENT);
          mOut.writeCData(jc.getText());
          mOut.writeEndElement();
        }
      }
    }
    JSourcePosition pos = ae.getSourcePosition();
    if (pos != null) {
      mOut.writeStartElement(SOURCEPOSITION_ELEMENT);
      if (pos.getLine() != -1) {
        writeValueElement(LINE_ELEMENT,pos.getLine());
      }
      if (pos.getColumn() != -1) {
        writeValueElement(COLUMN_ELEMENT,pos.getColumn());
      }
      if (pos.getSourceURI() != null)
        writeValueElement(SOURCEURI_ELEMENT,pos.getSourceURI().toString());
      mOut.writeEndElement();
    }
  }

  private void writeAnnotation(JAnnotation ann) throws XMLStreamException {
    mOut.writeStartElement(ANNOTATION_ELEMENT);
    writeValueElement(NAME_ELEMENT,ann.getSimpleName());
    JAnnotationValue[] values = ann.getValues();
    for(int i=0; i<values.length; i++) {
      writeAnnotationValue(values[i]);
    }
  }

  private void writeAnnotationValue(JAnnotationValue val)
    throws XMLStreamException
  {
    mOut.writeStartElement(ANNOTATIONVALUE_ELEMENT);
    writeValueElement(NAME_ELEMENT,val.getName());

writeValueElement(VALUE_ELEMENT,val.asString());
    //FIXME what about asAnnotationArray?
/*    JAnnotation nestedAnn = val.asAnnotation();
    if (nestedAnn != null) {
      writeAnnotation(nestedAnn);
    } else {
      writeValueElement(VALUE_ELEMENT,val.asString());
    }
    */
  }
}