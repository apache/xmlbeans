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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.impl.common.IOUtil;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.Collection;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;

public class CommandLine
{
    public CommandLine(String[] args, Collection scheme)
    {
        _options = new LinkedHashMap();
        ArrayList endargs = new ArrayList();

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].indexOf('-') == 0)
            {
                String opt = args[i].substring(1);
                String val = null;
                if (scheme != null && scheme.contains(opt) && i < args.length)
                    val = args[++i];
                else
                    val = "";

                _options.put(opt, val);
            }
            else
            {
                endargs.add(args[i]);
            }
        }

        _args = (String[])endargs.toArray(new String[endargs.size()]);
    }
    
    public static void printLicense()
    {
        try
        {
            IOUtil.copyCompletely(CommandLine.class.getClassLoader().getResourceAsStream("license.txt"), System.out);
        }
        catch (Exception e)
        {
            System.out.println("License available in this JAR in license.txt");
        }
    }

    public static void printVersion()
    {
        System.out.println(XmlBeans.getVendor() + ", " + XmlBeans.getTitle() + ".XmlBeans version " + XmlBeans.getVersion());
    }

    private Map _options;
    private String[] _args;

    public String[] args()
    {
        String[] result = new String[_args.length];
        System.arraycopy(_args, 0, result, 0, _args.length);
        return result;
    }

    public String getOpt(String opt)
    {
        return (String)_options.get(opt);
    }

    private static List collectFiles(File[] dirs)
    {
        List files = new ArrayList();
        for (int i = 0; i < dirs.length; i++)
        {
            File f = dirs[i];
            if (!f.isDirectory())
            {
                files.add(f);
            }
            else
            {
                files.addAll(collectFiles(f.listFiles()));
            }
        }
        return files;
    }

    private List _files;
    private File _baseDir;
    private static final File[] EMPTY_FILEARRAY = new File[0];

    private List getFileList()
    {
        if (_files == null)
        {
            String[] args = args();
            File[] files = new File[args.length];
            boolean noBaseDir = false;
            for (int i = 0; i < args.length; i++)
            {
                files[i] = new File(args[i]);
                if (!noBaseDir && (_baseDir == null)) 
                {
                    if (files[i].isDirectory())
                        _baseDir = files[i];
                    else
                        _baseDir = files[i].getParentFile();
                }
                else
                {
                    URI currUri = files[i].toURI();
                    
                    // Give up on the basedir. There may be none
                    if (_baseDir != null && _baseDir.toURI().relativize(currUri).equals(currUri))
                    {
                        _baseDir = null;
                        noBaseDir = true;
                    }
                }
            }
            _files = Collections.unmodifiableList(collectFiles(files));
        }
        return _files;
    }

    public File[] getFiles()
    {
        int size = getFileList().size();
        if (size == 0)
            return EMPTY_FILEARRAY;
        return (File[])getFileList().toArray(new File[size]);
    }

    public File getBaseDir()
    {
        return _baseDir;
    }

    public File[] filesEndingWith(String ext)
    {
        List result = new ArrayList();
        for (Iterator i = getFileList().iterator(); i.hasNext(); )
        {
            File f = (File)i.next();
            if (f.getName().endsWith(ext))
                result.add(f);
        }
        return (File[])result.toArray(new File[result.size()]);
    }
}
