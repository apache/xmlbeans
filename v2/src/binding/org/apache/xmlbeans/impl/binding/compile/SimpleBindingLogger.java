package org.apache.xmlbeans.impl.binding.compile;

import java.util.logging.Level;
import java.io.PrintWriter;
import org.apache.xmlbeans.impl.jam.JElement;
import org.w3.x2001.xmlSchema.Element;

/**
 * Implementation of BindingLogger that just spews out to some writer.
 */
public class SimpleBindingLogger implements BindingLogger {

  // ========================================================================
  // Variables

  private PrintWriter mOut;
  private Level mThreshold = Level.SEVERE;

  // ========================================================================
  // Constructors

  public SimpleBindingLogger() {
    this(new PrintWriter(System.out));
  }

  public SimpleBindingLogger(PrintWriter out) {
    if (out == null) throw new IllegalArgumentException();
    mOut = out;
  }

  // ========================================================================
  // BindingLogger implementation

  /**
   * Sets the minimum level at which messages will actually be printed out.
   * Anything of a lower level is discarded.  The default is Level.SEVERE.
   *
   * @param thresh the new threshold value.
   */
  public void setThresholdLevel(Level thresh) {
    mThreshold = thresh;
  }

  public void log(Level level, String message, Throwable error) {
    if (level.intValue() < mThreshold.intValue()) return;
    mOut.print(level.toString());
    if (message != null) {
      mOut.print(' ');
      mOut.print(message);
    }
    mOut.println();
    if (error != null) {
      error.printStackTrace(mOut);
    }
    mOut.flush();
  }

  public void log(Level level,
                  String message,
                  Throwable error,
                  JElement javaContext) {
    log(level,"'"+message+"' on "+javaContext.getQualifiedName(),error);
  }

  public void log(Level level,
                  String message,
                  Throwable error,
                  Element schemaContext) {
    log(level,message,error);
  }


}
