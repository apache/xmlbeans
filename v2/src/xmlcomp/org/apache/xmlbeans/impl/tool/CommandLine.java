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
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.tool;

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
        return (File[])getFileList().toArray(EMPTY_FILEARRAY);
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
        return (File[])result.toArray(EMPTY_FILEARRAY);
    }
}
