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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.apache.xmlbeans.impl.binding.bts.PathBindingLoader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class SimpleSchemaToJavaResultCompiler
{
    public static class Params
    {
        private File outputJar;

        public File getOutputJar()
        {
            return outputJar;
        }

        public void setOutputJar(File outputJar)
        {
            this.outputJar = outputJar;
        }
    }
    
    public static void compile(SchemaToJavaResult sources, Params params) throws IOException
    {
        // first, pick temp directory
        File tempDir = createTempDir();
        File sourceDir = createDir(tempDir, "sources");
        File classDir = createDir(tempDir, "classes");
        
        // next, output all the .java files to the temp dir
        List javaFileList = new ArrayList();
        JavaCodeGenerator jcg = sources.getJavaCodeGenerator();
        Collection classNames = jcg.getToplevelClasses();
        for (Iterator i = classNames.iterator(); i.hasNext(); )
        {
            String className = (String)i.next();
            File javaFilename = ensureDir(sourceDir, className.replace('.', File.separatorChar) + ".java");
            OutputStream output = new FileOutputStream(javaFilename);
            jcg.printSourceCode(className, output);
            output.close();
            javaFileList.add(javaFilename);
        }
        
        // then compile into .classes
        CodeGenUtil.externalCompile(javaFileList, classDir, null, false,
                CodeGenUtil.DEFAULT_COMPILER, CodeGenUtil.DEFAULT_MEM_START, CodeGenUtil.DEFAULT_MEM_MAX, false, false);
        
        // then also dump the schema binary files into the JAR
        SchemaToJavaInput sourceSet = sources.getSchemaSourceSet();
        sourceSet.compileSchemaToBinaries(classDir);
        
        // then create the binding-config.xml file
        BindingFileGenerator bfg = sources.getBindingFileGenerator();
        OutputStream output = new FileOutputStream(ensureDir(classDir, PathBindingLoader.STANDARD_PATH));
        bfg.printBindingFile(output);
        output.close();
        
        // and jar it up to the target JAR
        CodeGenUtil.externalJar(classDir, params.getOutputJar());
        
        // delete temporary dirs
        tryToDelete(tempDir);
    }
    
    protected static File ensureDir(File rootdir, String filepath)
    {
        File result = new File(rootdir, filepath);
        File newdir = result.getParentFile();
        boolean created = (newdir.exists() && newdir.isDirectory()) || newdir.mkdirs();
        assert(created) : "Could not create " + newdir.getAbsolutePath();
        return result;
    }
    
    protected static File createDir(File rootdir, String subdir)
    {
        File newdir = (subdir == null) ? rootdir : new File(rootdir, subdir);
        boolean created = (newdir.exists() && newdir.isDirectory()) || newdir.mkdirs();
        assert(created) : "Could not create " + newdir.getAbsolutePath();
        return newdir;
    }

    protected static File createTempDir() throws IOException
    {
        File tmpFile = File.createTempFile("xbean", null);
        String path = tmpFile.getAbsolutePath();
        if (!path.endsWith(".tmp"))
            throw new IOException("Error: createTempFile did not create a file ending with .tmp");
        path = path.substring(0, path.length() - 4);
        File tmpSrcDir = null;

        for (int count = 0; count < 100; count++)
        {
            String name = path + ".d" + (count == 0 ? "" : Integer.toString(count++));

            tmpSrcDir = new File(name);

            if (!tmpSrcDir.exists())
            {
                boolean created = tmpSrcDir.mkdirs();
                assert created : "Could not create " + tmpSrcDir.getAbsolutePath();
                break;
            }
        }
        tmpFile.deleteOnExit();

        return tmpSrcDir;
    }

    private static void tryToDelete(File dir)
    {
        if (dir.exists())
        {
            if (dir.isDirectory())
            {
                String[] list = dir.list();
                for (int i = 0; i < list.length; i++)
                    tryToDelete(new File(dir, list[i]));
            }
            if (!dir.delete())
                return; // don't try very hard, because we're just deleting tmp
        }
    }
    
    
}
