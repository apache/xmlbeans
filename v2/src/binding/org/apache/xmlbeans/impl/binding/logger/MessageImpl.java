/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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