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

import org.apache.xmlbeans.impl.jam.internal.CachedClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.internal.elements.ClassImpl;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MField;
import org.apache.xmlbeans.impl.jam.mutable.MInvokable;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;
import org.apache.xmlbeans.impl.jam.mutable.MMethod;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.mutable.MSourcePosition;
import org.apache.xmlbeans.impl.jam.JClass;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
/*package*/ class JamXmlReader implements JamXmlElements {

  // ========================================================================
  // Variables

  private XMLStreamReader mIn;
  private CachedClassBuilder mCache;
  private ElementContext mContext;

  // ========================================================================
  // Constructors

  public JamXmlReader(CachedClassBuilder cache, 
                      InputStream in, 
                      ElementContext ctx)
    throws XMLStreamException
  {
    this(cache,XMLInputFactory.newInstance().createXMLStreamReader(in),ctx);
  }

  public JamXmlReader(CachedClassBuilder cache,
                      Reader in,
                      ElementContext ctx)
    throws XMLStreamException
  {
    this(cache,XMLInputFactory.newInstance().createXMLStreamReader(in),ctx);
  }

  public JamXmlReader(CachedClassBuilder cache,
                      XMLStreamReader in,
                      ElementContext ctx)
  {
    if (cache == null) throw new IllegalArgumentException("null cache");
    if (in == null) throw new IllegalArgumentException("null cache");
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    mIn = in;
    mCache = cache;
    mContext = ctx;
  }

  // ========================================================================
  // Public methods

  public void read() throws XMLStreamException {
    nextElement();
    assertStart(JAMSERVICE);
    nextElement();
    while(CLASS.equals(getElementName())) readClass();
    assertEnd(JAMSERVICE);
  }

  // ========================================================================
  // Private methods

  private void readClass() throws XMLStreamException {
    assertStart(CLASS);
    nextElement();
    String clazzName = assertCurrentString(CLASS_NAME);
    int dot = clazzName.lastIndexOf('.');
    String pkgName = "";
    if (dot != -1) {
      pkgName = clazzName.substring(0,dot);
      clazzName = clazzName.substring(dot+1);
    }
    MClass clazz = mCache.createClassToBuild(pkgName,clazzName,null);
    //
    clazz.setIsInterface(assertCurrentBoolean(ISINTERFACE));
    clazz.setModifiers(assertCurrentInt(MODIFIERS));
    String supername = checkCurrentString(SUPERCLASS);
    if (supername != null) clazz.setSuperclass(supername);
    while((supername = checkCurrentString(INTERFACE)) != null) {
      clazz.addInterface(supername);
    }
    while(FIELD.equals(getElementName())) readField(clazz);
    while(CONSTRUCTOR.equals(getElementName())) readConstructor(clazz);
    while(METHOD.equals(getElementName())) readMethod(clazz);
    readAnnotatedElement(clazz);
    assertEnd(CLASS);
    ((ClassImpl)clazz).setState(ClassImpl.LOADED);
    nextElement();
  }

  private void readField(MClass clazz) throws XMLStreamException {
    assertStart(FIELD);
    MField field = clazz.addNewField();
    nextElement();
    field.setSimpleName(assertCurrentString(NAME));
    field.setModifiers(assertCurrentInt(MODIFIERS));
    field.setType(assertCurrentString(TYPE));
    readAnnotatedElement(field);
    assertEnd(FIELD);
    nextElement();
  }

  private void readConstructor(MClass clazz) throws XMLStreamException {
    assertStart(CONSTRUCTOR);
    MConstructor ctor = clazz.addNewConstructor();
    nextElement();
    readInvokableContents(ctor);
    assertEnd(CONSTRUCTOR);
    nextElement();
  }

  private void readMethod(MClass clazz) throws XMLStreamException {
    assertStart(METHOD);
    MMethod method = clazz.addNewMethod();
    nextElement();
    method.setSimpleName(assertCurrentString(NAME));
    method.setReturnType(assertCurrentString(RETURNTYPE));
    readInvokableContents(method);
    assertEnd(METHOD);
    nextElement();
  }

  private void readSourcePosition(MAnnotatedElement element)
    throws XMLStreamException
  {
    assertStart(SOURCEPOSITION);
    MSourcePosition pos = element.createSourcePosition();
    nextElement();
    if (LINE.equals(getElementName())) {
      pos.setLine(assertCurrentInt(LINE));
    }
    if (COLUMN.equals(getElementName())) {
      pos.setColumn(assertCurrentInt(COLUMN));
    }
    if (SOURCEURI.equals(getElementName())) {
      try {
        pos.setSourceURI(new URI(assertCurrentString(SOURCEURI)));
      } catch(URISyntaxException use) {
        throw new XMLStreamException(use);
      }
    }
    assertEnd(SOURCEPOSITION);
    nextElement();
  }

  private void readInvokableContents(MInvokable out)
    throws XMLStreamException
  {
    out.setModifiers(assertCurrentInt(MODIFIERS));
    while(PARAMETER.equals(getElementName())) {
      nextElement();
      MParameter param = out.addNewParameter();
      param.setSimpleName(assertCurrentString(NAME));
      param.setType(assertCurrentString(TYPE));
      readAnnotatedElement(param);
      assertEnd(PARAMETER);
      nextElement();
    }
    readAnnotatedElement(out);
  }


  private void readAnnotatedElement(MAnnotatedElement element)
    throws XMLStreamException
  {
    while(ANNOTATION.equals(getElementName())) {
      nextElement();
      //REVIEW creating ann for tag is not the right thing to do here.
      //we may need to store more info about exactly what the annotation was
      MAnnotation ann = element.addLiteralAnnotation(assertCurrentString(NAME));
      while(ANNOTATIONVALUE.equals(getElementName())) {
        nextElement();
        String name = assertCurrentString(NAME);
        String type = assertCurrentString(TYPE);
        JClass jclass = mContext.getClassLoader().loadClass(type);
        if (jclass.isArrayType()) {
          Collection list = new ArrayList();
          while(VALUE.equals(getElementName())) {
            String value = assertCurrentString(VALUE);
            list.add(value);
          }
          String[] vals = new String[list.size()];
          list.toArray(vals);
          ann.setSimpleValue(name,vals,jclass);
        } else {
          String value = assertCurrentString(VALUE);
          ann.setSimpleValue(name,value, jclass);
        }
        assertEnd(ANNOTATIONVALUE);
        nextElement();
      }
      assertEnd(ANNOTATION);
      nextElement();
    }
    if (COMMENT.equals(getElementName())) {
      element.createComment().setText(mIn.getElementText());
      assertEnd(COMMENT);
      nextElement();
    }
    if (SOURCEPOSITION.equals(getElementName())) {
      readSourcePosition(element);
    }
  }


  private void assertStart(String named) throws XMLStreamException {
    if (!mIn.isStartElement() || !named.equals(getElementName())) {
      error("expected to get a <"+named+">, ");
    }
  }

  private void assertEnd(String named) throws XMLStreamException {
    if (!mIn.isEndElement() || !named.equals(getElementName())) {
      error("expected to get a </"+named+">, ");
    }
  }



  private String checkCurrentString(String named) throws XMLStreamException {
    if (named.equals(getElementName())) {
      String val = mIn.getElementText();
      assertEnd(named);
      nextElement();
      return val;
    }
    return null;
  }


  private String assertCurrentString(String named) throws XMLStreamException {
    assertStart(named);
    String val = mIn.getElementText();
    assertEnd(named);
    nextElement();
    return val;
  }

  private int assertCurrentInt(String named) throws XMLStreamException {
    assertStart(named);
    String val = mIn.getElementText();
    assertEnd(named);
    nextElement();
    return Integer.valueOf(val).intValue();
  }

  private boolean assertCurrentBoolean(String named) throws XMLStreamException {
    assertStart(named);
    String val = mIn.getElementText();
    assertEnd(named);
    nextElement();
    return Boolean.valueOf(val).booleanValue();
  }


  private void error(String message) throws XMLStreamException {
    StringWriter out = new StringWriter();
    out.write("<");
    out.write(mIn.getLocalName());
    out.write("> line:");
    out.write(""+mIn.getLocation().getLineNumber());
    out.write(" col:");
    out.write(""+mIn.getLocation().getColumnNumber());
    out.write("]");
    throw new XMLStreamException(message+":\n "+out.toString());
  }

  private void nextElement() throws XMLStreamException {
    do {
      if (mIn.next() == -1) {
        throw new XMLStreamException("Unexpected end of file");
      }
    } while(!mIn.isEndElement() && !mIn.isStartElement());
  }

  private String getElementName() {
    return mIn.getLocalName();
  }

}
