package xmlbeans;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

/**
 * This simple task takes the output of 'svn info' as a file, searches for the
 * line "Last Changed Rev: XXXX" and sets a property to the value of XXXX.
 */
public class SvnRevisionTask extends Task
{
    private static final String LAST_CHANGED_REV = "Last Changed Rev: ";

    private File file;
    private String property;

    public void setFile(File file)
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    public String getProperty()
    {
        return property;
    }

    public void execute() throws BuildException
    {
        if (file == null)
            throw new BuildException("The 'file' attribute is required.");

        if (!file.isFile())
            throw new BuildException("The file '" + file + "' doesn't exist or isn't a file.");

        if (property == null || property.length() == 0)
            throw new BuildException("The 'property' attribute is required.");

        try
        {
            boolean found = false;

            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = null;
            while (null != (line = buf.readLine()))
            {
                if (line.startsWith(LAST_CHANGED_REV))
                {
                    String value = line.substring(LAST_CHANGED_REV.length());
                    getProject().setProperty(property, value);
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                log("Couldn't find revision number.", Project.MSG_ERR);
                getProject().setProperty(property, "unknown");
            }
        }
        catch (IOException e)
        {
            throw new BuildException(e.getMessage());
        }
    }
}
