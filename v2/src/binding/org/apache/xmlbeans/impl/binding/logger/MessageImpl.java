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

package org.apache.xmlbeans.impl.binding.logger;

import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.jam.JElement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class MessageImpl implements Message {

  // ========================================================================
  // Variables

  private Level mLevel;
  private String mMessage = null;
  private Throwable mException = null;
  private JElement mJavaContext = null;
  private SchemaType mSchemaTypeContext = null;
  private SchemaProperty mSchemaPropertyContext = null;

  // ========================================================================
  // Constructors

  public MessageImpl(Level level,
                     String message,
                     Throwable exception,
                     JElement javaContext,
                     SchemaType schemaTypeContext,
                     SchemaProperty schemaPropertyContext) {
    if (level == null) throw new IllegalArgumentException("null level");
    mLevel = level;
    mMessage = message;
    mException = exception;
    mJavaContext = javaContext;
    mSchemaTypeContext = schemaTypeContext;
    mSchemaPropertyContext = schemaPropertyContext;
  }


  // ========================================================================
  // Message implementation

  public Level getLevel() {
    return mLevel;
  }

  public String getMessage() {
    if (mMessage != null) return mMessage;
    if (mException != null) return mException.getMessage();
    return mLevel.getLocalizedName(); //?
  }

  public Throwable getException() {
    return mException;
  }

  public JElement getJavaContext() {
    return mJavaContext;
  }

  public SchemaProperty getSchemaPropertyContext() {
    return mSchemaPropertyContext;
  }

  public SchemaType getSchemaTypeContext() {
    return mSchemaTypeContext;
  }

  // ========================================================================
  // Object implementation

  public String toString() {
    StringWriter sw = new StringWriter();
    print(new PrintWriter(sw));
    return sw.toString();
  }

  // ========================================================================
  // Private methods

  private void print(PrintWriter out) {
    out.print('[');
    out.print(mLevel.toString());
    out.print("] ");
    if (mMessage != null) {
      out.println(mMessage);
    }
    if (mJavaContext != null) {
      out.print(" on Java element '");
      out.print(mJavaContext.getQualifiedName());
      out.print("'");
    }
    if (mSchemaTypeContext != null) {
      out.print(" on Schema type ");
      out.print(mSchemaTypeContext.getName());//FIXME?
      out.print("'");
    }
    if (mSchemaPropertyContext != null) {
      out.print(" on Schema type ");
      out.print(mSchemaPropertyContext.getName());//FIXME?
      out.print("'");
    }
    if (mException != null) {
      mException.printStackTrace(out);
    }
  }
}