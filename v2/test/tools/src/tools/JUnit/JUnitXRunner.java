package tools.JUnit;

import junit.runner.BaseTestRunner;
import junit.runner.TestRunListener;
import junit.framework.*;

import java.io.*;
import java.util.*;

/**
 * User: rajus
 * Date: May 25, 2004
 */

public class JUnitXRunner extends BaseTestRunner
                          implements JUnitXResultFormatter
{
    public static void main(String args[])
        throws Exception
    {
        // TODO: A good clean way to pass arguments would be using something
        // like GetOpt, and make it flexible to change the order of the
        // arguments. right now we impose a rigid sequence on the arguments

        // TODO: allow specifying a single JUnit class to run.. would ideally
        // follow the above TODO.

        File file = new File(args[0]);
        String resListener = null;
        String outFile = null;
        boolean showOutput = false;
        if (args.length > 1)
        {
            if (args[1].equalsIgnoreCase("showoutput"))
                showOutput = true;
            else
            {
                resListener = args[1];
                if (args.length < 3)
                    throw new RuntimeException("No output file specified");
                outFile = args[2];
            }
        }

        // do we have more?
        if (args.length > 3)
        {
            if (args[3].equalsIgnoreCase("showoutput"))
                showOutput = true;
        }

        ArrayList files = new ArrayList();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            while ((line = in.readLine()) != null)
                files.add(line);
            in.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        JUnitXRunner runner;
        if (resListener != null)
        {
            // Try to instantiate a class of resListener
            Object obj;
            try
            {
                Class c = Class.forName(resListener);
                obj = c.newInstance();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            JUnitXResultFormatter fmt = (JUnitXResultFormatter) obj;
            runner = new JUnitXRunner(files, fmt, outFile, showOutput);
        }
        else
            runner = new JUnitXRunner(files, showOutput);

        int nFailureCount=runner.runTests();
        System.exit(nFailureCount);
    }

    /////////////////////////////////////////////////////////////////////////
    // Runner implementation

    ArrayList classes;
    ArrayList tests;
    int testCount = 0;
    JUnitXResultFormatter _listener = null;
    String outFile = null;
    boolean showOutput = false;

    public JUnitXRunner(ArrayList classes, boolean showOutput)
    {
        this.classes = classes;
        tests = new ArrayList();
        _listener = this;
        this.showOutput = showOutput;
    }

    public JUnitXRunner(ArrayList classes,
                        JUnitXResultFormatter listener,
                        String outFile,
                        boolean showOutput)
    {
        this.classes = classes;
        tests = new ArrayList();
        _listener = listener;
        this.outFile = outFile;
        this.showOutput = showOutput;
    }


    public int runTests()
    {
        collectTests();
        Iterator itr = tests.iterator();

        TestResult res = new TestResult();
        res.addListener(_listener);

        try
        {
            if (outFile != null)
            {
                FileOutputStream fos = new FileOutputStream(new File(outFile));
                _listener.setOutput(fos);
            }
        } catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException("Unable to initialize output to file "
                                       + outFile + "\n" + fnfe.getMessage());
        }
        _listener.showTestOutput(showOutput);
        _listener.startRun();
        while (itr.hasNext())
        {
            Test test = (Test) itr.next();
            test.run(res);
        }
        _listener.endRun();
        return res.failureCount();
    }

    private void collectTests()
    {
        Iterator itr = classes.iterator();

        while (itr.hasNext())
        {
            Test suite = null;
            String className = (String) itr.next();
            suite = getTest(className);

            if (suite != null && suite.countTestCases() > 0)
            {
                tests.addAll(getSubTests(suite));
            } else
            {
                //System.out.println("No tests found in " + testClassName);
                // Ignore files which are not Junit tests.
            }
        }
    }

    private Collection getSubTests(Test test)
    {
        Collection ret = new ArrayList();

        if (TestSuite.class.isAssignableFrom(test.getClass()))
        {
            Enumeration e = ((TestSuite) test).tests();
            while (e.hasMoreElements())
            {
                ret.addAll(getSubTests((Test)e.nextElement()));
            }
        }
        else if (TestCase.class.isAssignableFrom(test.getClass()))
        {
            ret.add(((TestCase)test));
        }
        else
        {
            System.out.println("Could not find any tests in " + test.toString());
        }

        return ret;
    }

    // JUnitXResultFormatter Implementation
    public void startRun()
    {

    }

    public void endRun()
    {

    }

    public void setOutput(OutputStream out)
    {
        // Ignored. Custom ResultFormatters will use
    }

    public void showTestOutput(boolean show)
    {
        // Ignore. We don't capture stdout or stderr.
    }

    // TestRunListener implementation
	public void testStarted(String testName)
    {
        System.out.println("\nStarted: " + testName);
    }
	public void testEnded(String testName)
    {
        System.out.println("Ended: " + testName);
    }

	public void testFailed(int status, Test test, Throwable t)
    {
        if (status == TestRunListener.STATUS_FAILURE)
            System.out.println("Failure: ");
        else
            System.out.println("Error: ");

        System.out.println(getFilteredTrace(t));
    }

    protected void runFailed(String message)
    {
        //System.out.println("RUN had failures");
    }

    /* This is important - not setting this to false expilcitly
     * will cause Junit to create a new classloader instance for
     * every class it loads, causing a OOM sooner or later.
     *
     * @see junit.runner.BaseTestRunner#useReloadingTestSuiteLoader()
     */
    protected boolean useReloadingTestSuiteLoader()
    {
        return false;
    }

    private String getStackTraceAsString(Throwable t)
    {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));

        return sw.toString();
    }
}
