package tools.JUnit;

import junit.framework.TestListener;

import java.io.OutputStream;

/**
 * User: rajus
 * Date: May 26, 2004
 */
public interface JUnitXResultFormatter extends TestListener
{
    /** Signals start of run */
    public void startRun();

    /** Signals end of run */
    public void endRun();

    /** Sets an outputstream to output logs to */
    public void setOutput(OutputStream out);

    /** Tells an ResultFormatter to show stdout/stderr if its capturing
     * the streams
     */
    public void showTestOutput(boolean show);
    /* Any class implementing this will automatically have to
     * implement TestListener
     */
}
