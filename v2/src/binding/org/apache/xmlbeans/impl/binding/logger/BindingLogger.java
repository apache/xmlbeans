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

import java.util.logging.Level;

/**
 * Provides an interface to which binding compilation objects can send
 * log messages.  The various logging methods here simply construct
 * new BindingLoggerMessages and send them on to some MessageSink
 * which actually processes the messsages; this class is primarily
 * responsible for simply providing clients with a 'tighter' interface to
 * that sink.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class BindingLogger {

  // ========================================================================
  // Constants

  private static final MessageSink DEFAULT_SINK =
          new SimpleMessageSink();

  public static final BindingLogger DEFAULT = new BindingLogger();

  // ========================================================================
  // Variables

  private boolean mVerbose = false;
  private MessageSink mSink = DEFAULT_SINK;
  private boolean mAnyErrorsFound = false;
  private boolean mIgnoreErrors = false;

  // ========================================================================
  // Constructors

  protected BindingLogger(MessageSink sink) { mSink = sink; }

  protected BindingLogger() { mSink = DEFAULT_SINK; }

  // ========================================================================
  // Attributes

  public void setVerbose(boolean b) {
    mVerbose = b;
  }

  public void setIgnoreErrors(boolean b) {
    mIgnoreErrors = b;
  }

  public void setMessageSink(MessageSink blp) {
    mSink = blp;
  }

  public boolean isAnyErrorsFound() {
    return mAnyErrorsFound;
  }

  public boolean isIgnoreErrors() {
    return mIgnoreErrors;
  }

  public boolean isVerbose() {
    return mVerbose;
  }

  // ========================================================================
  // Public logging methods

  /**
   * Logs a warning message.
   */
  public void logWarning(String msg) {
    mSink.log(new MessageImpl
            (Level.WARNING, msg, null, null, null, null));
  }

  /**
   * Logs a message that some error occurred while performing binding.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(String msg) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, msg, null, null, null, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that an error occurred.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(Throwable t) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, null, t, null, null, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(Throwable error, JElement javaContext) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, null, error, javaContext, null, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given schema construct.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(Throwable error, SchemaType schemaContext) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, null, error, null, schemaContext, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java and schema constructs.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(Throwable t, JElement jCtx, SchemaType xsdCtx) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, null, t, jCtx, xsdCtx, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(String msg, JElement javaContext) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, msg, null, javaContext, null, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given schema construct.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(String msg, SchemaType xsdCtx) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, msg, null, null, xsdCtx, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given schema construct.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(String msg, JElement javaCtx, SchemaType xsdCtx) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, msg, null, javaCtx, xsdCtx, null));
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given schema construct.
   *
   * @return true if processing should attempt to continue.
   */
  public boolean logError(String msg, JElement jCtx, SchemaProperty xCtx) {
    mAnyErrorsFound = true;
    mSink.log(new MessageImpl
            (Level.SEVERE, msg, null, jCtx, null, xCtx));
    return mIgnoreErrors;
  }

  /**
   * Logs an informative message that should be printed only in 'verbose'
   * mode.
   */
  public void logVerbose(String msg) {
    if (mVerbose) {
      mSink.log(new MessageImpl
              (Level.FINEST, msg, null, null, null, null));
    }
  }

  /**
   * Logs an informative message that should be printed only in 'verbose'
   * mode.
   */
  public void logVerbose(String msg, JElement javaContext) {
    if (mVerbose) {
      mSink.log(new MessageImpl
              (Level.FINEST, msg, null, javaContext, null, null));
    }
  }

  /**
   * Logs an informative message that should be printed only in 'verbose'
   * mode.
   */
  public void logVerbose(String msg, SchemaType xsdType) {
    if (mVerbose) {
      mSink.log(new MessageImpl
              (Level.FINEST, msg, null, null, xsdType, null));
    }
  }

  /**
   * Logs an informative message that should be printed only in 'verbose'
   * mode.
   */
  public void logVerbose(String msg, JElement javaCtx, SchemaType xsdCtx) {
    if (mVerbose) {
      mSink.log(new MessageImpl
              (Level.FINEST, msg, null, javaCtx, xsdCtx, null));
    }
  }

}
