package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.jam.JElement;
import org.w3.x2001.xmlSchema.Element;
import java.util.logging.Level;

/**
 * Note that the binding compilers should attempt to continue even after
 * errors are encountered so as to identify as many errors as possible in a
 * single pass.
 */
public interface BindingLogger {

  /**
   * Logs a message that was produced while performing binding.
   *
   * @param level         Severity of the message
   * @param message       Text message or null
   * @param error         Error or null
   */
  public void log(Level level,
                  String message,
                  Throwable error);

  /**
   * Logs a message that was produced while performing binding
   * on the given java construct.
   *
   * @param level         Severity of the message
   * @param message       Text message or null
   * @param error         Error or null
   * @param javaContext   JAM context element or null
   */
  public void log(Level level,
                  String message,
                  Throwable error,
                  JElement javaContext);

  /**
   * Logs a message that was produced while performing binding
   * on the given schema construct.
   *
   * @param level         Severity of the message
   * @param schemaContext Schema context element or null
   * @param message       Text message or null
   * @param error         Error or null
   */
  public void log(Level level,
                  String message,
                  Throwable error,
                  Element schemaContext);

}