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
import org.apache.xmlbeans.impl.jam_old.JElement;

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
