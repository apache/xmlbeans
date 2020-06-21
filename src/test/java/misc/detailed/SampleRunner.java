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
package misc.detailed;

import org.apache.tools.ant.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Date: Feb 8, 2005
 * Time: 11:30:24 AM
 */
public class SampleRunner {

    private ArrayList samples;
    private Project proj;
    private Target target;
    private String XMLBEANS_HOME;
    private SamplesBuildFileTest runSampleTest;


    @Before
    protected void setUp() throws Exception {
        proj = new Project();
        proj.setName("Samples Task Tests");
        XMLBEANS_HOME = proj.getBaseDir().getAbsolutePath();
        samples = new ArrayList();
        runSampleTest = new SamplesBuildFileTest();
    }

    @Test
    public void testSamples() throws Exception {
        loadSampleDirs(new File("./samples"));
        ArrayList exceptions = new ArrayList();
        for (int i = 0; i < samples.size(); i++)
        {

            runSampleTest.call_samples_task(
                ((File) samples.get(i)).getAbsolutePath()
                , "test");
            BuildException e;
            if ((e = runSampleTest.getAnyExceptions()) != null)
            {
                exceptions.add(((File) samples.get(i)).getAbsolutePath());
                exceptions.add(e.getException());
            }
        }
        if (exceptions.size() != 0)
            throw new RuntimeException(getMessageFromExceptions(exceptions));

    }

    private String getMessageFromExceptions(ArrayList ex)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ex.size(); i += 2)
        {
            sb.append("\n\nFILE:" + (String) ex.get(i));
            sb.append(
                "\n **Error: " + ((BuildException) ex.get(i + 1)).getMessage());
        }
        return sb.toString();
    }

    private void loadSampleDirs(File dir)
    {
        assert dir != null && dir.exists();
        File[] files = dir.listFiles(new BuildFilter());
        assert files.length == 1;
        samples.add(files[0]);

    }

    private class BuildFilter
        implements FilenameFilter
    {
        public boolean accept(File file, String name)
        {
            return name.equals("build.xml");
        }
    }

    private class SamplesBuildFileTest extends BuildFileTest {
        public void call_samples_task(String projectPath, String taskName)
        {
            configureProject(projectPath);
            Project proj = getProject();
            proj.setProperty("xmlbeans.home", XMLBEANS_HOME);
            executeTarget(proj.getDefaultTarget());
        }

        public BuildException getAnyExceptions()
            throws Exception
        {
            return this.getBuildException();
        }
    }
}

abstract class BuildFileTest {

    protected Project project;

    private StringBuilder logBuffer;
    private StringBuilder fullLogBuffer;
    private StringBuilder outBuffer;
    private StringBuilder errBuffer;
    private BuildException buildException;

    protected String getOutput() {
        return cleanBuffer(outBuffer);
    }

    protected String getError() {
        return cleanBuffer(errBuffer);
    }

    protected BuildException getBuildException() {
        return buildException;
    }

    private String cleanBuffer(StringBuilder buffer) {
        StringBuilder cleanedBuffer = new StringBuilder();
        boolean cr = false;
        for (int i = 0; i < buffer.length(); i++) {
            char ch = buffer.charAt(i);
            if (ch == '\r') {
                cr = true;
                continue;
            }

            if (!cr) {
                cleanedBuffer.append(ch);
            } else {
                if (ch == '\n') {
                    cleanedBuffer.append(ch);
                } else {
                    cleanedBuffer.append('\r').append(ch);
                }
            }
        }
        return cleanedBuffer.toString();
    }

    /**
     *  set up to run the named project
     *
     * @param  filename name of project file to run
     */
    protected void configureProject(String filename) throws BuildException {
        logBuffer = new StringBuilder();
        fullLogBuffer = new StringBuilder();
        project = new Project();
        project.init();
        project.setUserProperty( "ant.file" , new File(filename).getAbsolutePath() );
        project.addBuildListener(new BuildFileTest.AntTestListener());
        //ProjectHelper.configureProject(project, new File(filename));
        ProjectHelper.getProjectHelper().parse(project, new File(filename));
    }

    /**
     *  execute a target we have set up
     * @pre configureProject has been called
     * @param  targetName  target to run
     */
    protected void executeTarget(String targetName) {
        PrintStream sysOut = System.out;
        PrintStream sysErr = System.err;
        try {
            sysOut.flush();
            sysErr.flush();
            outBuffer = new StringBuilder();
            PrintStream out = new PrintStream(new BuildFileTest.AntOutputStream());
            System.setOut(out);
            errBuffer = new StringBuilder();
            PrintStream err = new PrintStream(new BuildFileTest.AntOutputStream());
            System.setErr(err);
            logBuffer = new StringBuilder();
            fullLogBuffer = new StringBuilder();
            buildException = null;
            project.executeTarget(targetName);
        } finally {
            System.setOut(sysOut);
            System.setErr(sysErr);
            // rajus: 2004/04/07
            System.out.println("STDOUT+STDERR:\n" + getOutput() + getError());
            System.out.println("END STDOUT+STDERR:");
        }

    }

    /**
     * Get the project which has been configured for a test.
     *
     * @return the Project instance for this test.
     */
    protected Project getProject() {
        return project;
    }

    /**
     * an output stream which saves stuff to our buffer.
     */
    private class AntOutputStream extends java.io.OutputStream {
        public void write(int b) {
            outBuffer.append((char)b);
        }
    }

    /**
     * our own personal build listener
     */
    private class AntTestListener implements BuildListener {
        /**
         *  Fired before any targets are started.
         */
        public void buildStarted(BuildEvent event) {
        }

        /**
         *  Fired after the last target has finished. This event
         *  will still be thrown if an error occured during the build.
         *
         *  @see BuildEvent#getException()
         */
        public void buildFinished(BuildEvent event) {
        }

        /**
         *  Fired when a target is started.
         *
         *  @see BuildEvent#getTarget()
         */
        public void targetStarted(BuildEvent event) {
            //System.out.println("targetStarted " + event.getTarget().getName());
        }

        /**
         *  Fired when a target has finished. This event will
         *  still be thrown if an error occured during the build.
         *
         *  @see BuildEvent#getException()
         */
        public void targetFinished(BuildEvent event) {
            //System.out.println("targetFinished " + event.getTarget().getName());
        }

        /**
         *  Fired when a task is started.
         *
         *  @see BuildEvent#getTask()
         */
        public void taskStarted(BuildEvent event) {
            //System.out.println("taskStarted " + event.getTask().getTaskName());
        }

        /**
         *  Fired when a task has finished. This event will still
         *  be throw if an error occured during the build.
         *
         *  @see BuildEvent#getException()
         */
        public void taskFinished(BuildEvent event) {
            //System.out.println("taskFinished " + event.getTask().getTaskName());
        }

        /**
         *  Fired whenever a message is logged.
         *
         *  @see BuildEvent#getMessage()
         *  @see BuildEvent#getPriority()
         */
        public void messageLogged(BuildEvent event) {
            if (event.getPriority() == Project.MSG_INFO ||
                event.getPriority() == Project.MSG_WARN ||
                event.getPriority() == Project.MSG_ERR) {
                logBuffer.append(event.getMessage());
            }
            fullLogBuffer.append(event.getMessage());

        }
    }


}
