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
/*package*/ class JamXmlWriter implements JamXmlElements {

  // ========================================================================
  // Variables

  private XMLStreamWriter mOut;
  private boolean mInBody = false;
  private boolean mWriteSourceURI = false;

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


  public void begin() throws XMLStreamException {
    if (mInBody) throw new XMLStreamException("begin() already called");
    mOut.writeStartElement(JAMSERVICE);
    mInBody = true;
  }

  public void end() throws XMLStreamException {
    if (!mInBody) throw new XMLStreamException("begin() never called");
    mOut.writeEndElement();
    mInBody = false;
  }


  /*
  public void write(JPackage pkg) throws XMLStreamException {
    assertStarted();
    mOut.writeStartElement(PACKAGE);
    JClass[] c = pkg.getClasses();
    for(int i=0; i<c.length; i++) write(c[i]);
    writeAnnotatedElement(pkg);
    mOut.writeEndElement();
  }*/

  public void write(JClass clazz) throws XMLStreamException {
    assertStarted();
    mOut.writeStartElement(CLASS);
    writeValueElement(CLASS_NAME,clazz.getQualifiedName());
    writeValueElement(ISINTERFACE,clazz.isInterface());
    writeModifiers(clazz.getModifiers());
    JClass sc = clazz.getSuperclass();
    if (sc != null) writeValueElement(SUPERCLASS,sc.getQualifiedName());
    writeClassList(INTERFACE,clazz.getInterfaces());
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


  // ========================================================================
  // Private methods

  private void write(JMethod method) throws XMLStreamException {
    mOut.writeStartElement(METHOD);
    writeValueElement(NAME,method.getSimpleName());
    writeValueElement(RETURNTYPE,
                      method.getReturnType().getQualifiedName());
    writeInvokable(method);
    mOut.writeEndElement();
  }

  private void write(JConstructor ctor) throws XMLStreamException {
    mOut.writeStartElement(CONSTRUCTOR);
    writeInvokable(ctor);
    mOut.writeEndElement();
  }

  private void write(JField field) throws XMLStreamException {
    mOut.writeStartElement(FIELD);
    writeValueElement(NAME,field.getSimpleName());
    writeModifiers(field.getModifiers());
    writeValueElement(TYPE,field.getType().getQualifiedName());
    writeAnnotatedElement(field);
    mOut.writeEndElement();
  }

  private void writeInvokable(JInvokable ji) throws XMLStreamException {
    writeModifiers(ji.getModifiers());
    JParameter[] params = ji.getParameters();
    for(int i=0; i<params.length; i++) {
      mOut.writeStartElement(PARAMETER);
      writeValueElement(NAME,params[i].getSimpleName());
      writeValueElement(TYPE,params[i].getType().getQualifiedName());
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
    mOut.writeStartElement(MODIFIERS);
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
          mOut.writeStartElement(COMMENT);
          mOut.writeCData(jc.getText());
          mOut.writeEndElement();
        }
      }
    }
    JSourcePosition pos = ae.getSourcePosition();
    if (pos != null) {
      mOut.writeStartElement(SOURCEPOSITION);
      if (pos.getLine() != -1) {
        writeValueElement(LINE,pos.getLine());
      }
      if (pos.getColumn() != -1) {
        writeValueElement(COLUMN,pos.getColumn());
      }
      if (mWriteSourceURI && pos.getSourceURI() != null)
        writeValueElement(SOURCEURI,pos.getSourceURI().toString());
      mOut.writeEndElement();
    }
  }

  private void writeAnnotation(JAnnotation ann) throws XMLStreamException {
    mOut.writeStartElement(ANNOTATION);
    writeValueElement(NAME,ann.getSimpleName());
    JAnnotationValue[] values = ann.getValues();
    for(int i=0; i<values.length; i++) {
      writeAnnotationValue(values[i]);
    }
    mOut.writeEndElement();
  }

  private void writeAnnotationValue(JAnnotationValue val)
    throws XMLStreamException
  {
    mOut.writeStartElement(ANNOTATIONVALUE);
    writeValueElement(NAME,val.getName());
    writeValueElement(VALUE,val.asString());
    mOut.writeEndElement();
    //FIXME what about asAnnotationArray?
/*    JAnnotation nestedAnn = val.asAnnotation();
    if (nestedAnn != null) {
      writeAnnotation(nestedAnn);
    } else {
      writeValueElement(VALUE,val.asString());
    }
    */
  }

  private void assertStarted() throws XMLStreamException {
    if (!mInBody) throw new XMLStreamException("begin() not called");
  }

}