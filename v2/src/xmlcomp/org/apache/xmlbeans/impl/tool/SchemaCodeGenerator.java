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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.schema.SchemaTypeCodePrinter;
import repackage.Repackager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchemaCodeGenerator
{
    // input directory, output dir filename
    // todo: output jar
    public static boolean compileTypeSystem(SchemaTypeSystem saver, File sourcedir, File[] javasrc,
         Map sourcesToCopyMap, File[] classpath, File classesdir, File outputJar, boolean nojavac,
         XmlErrorWatcher errors, String repackage, boolean verbose, List sourcefiles,
         File schemasDir)
    {

        if (sourcedir == null || classesdir == null)
            throw new IllegalArgumentException("Source and class gen dir must not be null.");

        boolean failure = false;

        saver.saveToDirectory(classesdir);

        // Schema files already copyed when they where parsed
//        if ((sourcesToCopyMap != null) && (sourcesToCopyMap.size() > 0))
//        {
//            for (Iterator iter = sourcesToCopyMap.keySet().iterator(); iter.hasNext();)
//            {
//                String key = (String)iter.next();
//
//                try
//                {
//                    String schemalocation = (String)sourcesToCopyMap.get(key);
//
//                    File targetFile = new File(schemasDir, schemalocation);
//                    if (targetFile.exists())
//                        continue;
//
//                    File parentDir = new File(targetFile.getParent());
//                    IOUtil.createDir(parentDir, null);
//
//                    InputStream in = null;
//                    URL url = new URL(key);
//                    // Copy the file from filepath to schema/src/<schemaFile>
//                    in = url.openStream();
//
//                    FileOutputStream out = new FileOutputStream(targetFile);
//                    IOUtil.copyCompletely(in, out);
//                }
//                catch (IOException e)
//                {
//                    System.err.println("IO Error " + e);
//                    // failure = true; - not cause for failure
//                }
//            }
//        }

        Repackager repackager = repackage == null ? null : new Repackager( repackage );

        try
        {
            String filename = SchemaTypeCodePrinter.indexClassForSystem(saver).replace('.', File.separatorChar) + ".java";
            File sourcefile = new File(sourcedir, filename);
            sourcefile.getParentFile().mkdirs();
            
            Writer writer =
                repackager == null
                    ? (Writer) new FileWriter( sourcefile )
                    : (Writer) new RepackagingWriter( sourcefile, repackager );
                            
            SchemaTypeCodePrinter.printLoader(writer, saver);
            
            writer.close();
            
            sourcefiles.add(sourcefile);
        }
        catch (IOException e)
        {
            System.err.println("IO Error " + e);
            failure = true;
        }

        failure &= genTypes(saver, sourcefiles, sourcedir, repackager, verbose);

        if (failure)
            return false;

        return true;
    }

    private static boolean genTypes(SchemaTypeSystem saver, List sourcefiles, File sourcedir, Repackager repackager, boolean verbose)
    {
        boolean failure = false;

        List types = new ArrayList();
        types.addAll(Arrays.asList(saver.globalTypes()));
        types.addAll(Arrays.asList(saver.documentTypes()));
        types.addAll(Arrays.asList(saver.attributeTypes()));

        for (Iterator i = types.iterator(); i.hasNext(); )
        {
            SchemaType type = (SchemaType)i.next();
            if (verbose)
                System.err.println("Compiling type " + type);
            if (type.isBuiltinType())
                continue;
            if (type.getFullJavaName() == null)
                continue;
            
            String fjn = type.getFullJavaName();

            if (fjn.indexOf('$') > 0)
            {
                fjn =
                    fjn.substring( 0, fjn.lastIndexOf( '.' ) ) + "." +
                        fjn.substring( fjn.indexOf( '$' ) + 1 );
            }
            
            String filename = fjn.replace('.', File.separatorChar) + ".java";
            
            Writer writer = null;
            
            try
            {
                File sourcefile = new File(sourcedir, filename);
                sourcefile.getParentFile().mkdirs();
                if (verbose)
                    System.err.println("created " + sourcefile.getAbsolutePath());
                writer =
                    repackager == null
                        ? (Writer) new FileWriter( sourcefile )
                        : (Writer) new RepackagingWriter( sourcefile, repackager );
                

                SchemaTypeCodePrinter.printType(writer, type);
                
                writer.close();
                
                sourcefiles.add(sourcefile);
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
                failure = true;
            }
            finally {
                try { if (writer != null) writer.close(); } catch (IOException e) {}
            }

            try
            {
                // Generate Implementation class
                filename = type.getFullJavaImplName().replace('.', File.separatorChar) + ".java";
                File implFile = new File(sourcedir,  filename);
                if (verbose)
                    System.err.println("created " + implFile.getAbsolutePath());
                implFile.getParentFile().mkdirs();

                
                writer =
                    repackager == null
                        ? (Writer) new FileWriter( implFile )
                        : (Writer) new RepackagingWriter( implFile, repackager );
                
                SchemaTypeCodePrinter.printTypeImpl(writer, type);
                
                writer.close();
                
                sourcefiles.add(implFile);
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
                failure = true;
            }
            finally {
                try { if (writer != null) writer.close(); } catch (IOException e) {}
            }
        }

        return failure;
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

    protected static void tryHardToDelete(File dir)
    {
        tryToDelete(dir);
        if (dir.exists())
            tryToDeleteLater(dir);
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
    
    private static Set deleteFileQueue = new HashSet();
    private static int triesRemaining = 0;
    
    private static boolean tryNowThatItsLater()
    {
        List files;
        
        synchronized (deleteFileQueue)
        {
            files = new ArrayList(deleteFileQueue);
            deleteFileQueue.clear();
        }
        
        List retry = new ArrayList();
        
        for (Iterator i = files.iterator(); i.hasNext(); )
        {
            File file = (File)i.next();
            tryToDelete(file);
            if (file.exists())
                retry.add(file);
        }
        
        synchronized (deleteFileQueue)
        {
            if (triesRemaining > 0)
                triesRemaining -= 1;
                
            if (triesRemaining <= 0 || retry.size() == 0) // done?
                triesRemaining = 0;
            else
                deleteFileQueue.addAll(retry); // try again?
            
            return (triesRemaining <= 0);
        }
    }
    
    private static void giveUp()
    {
        synchronized (deleteFileQueue)
        {
            deleteFileQueue.clear();
            triesRemaining = 0;
        }
    }
    
    private static void tryToDeleteLater(File dir)
    {
        synchronized (deleteFileQueue)
        {
            deleteFileQueue.add(dir);
            if (triesRemaining == 0)
            {
                new Thread()
                {
                    public void run()
                    {
                        // repeats tryNow until triesRemaining == 0
                        try
                        {
                            for (;;)
                            {
                                if (tryNowThatItsLater())
                                    return; // succeeded
                                Thread.sleep(1000 * 3); // wait three seconds
                            }
                        }
                        catch (InterruptedException e)
                        {
                            giveUp();
                        }
                    }
                };
            }
            
            if (triesRemaining < 10)
                triesRemaining = 10;
        }
    }
    
    static class RepackagingWriter extends StringWriter
    {
        public RepackagingWriter ( File file, Repackager repackager )
        {
            _file = file;
            _repackager = repackager;
        }

        public void close ( ) throws IOException
        {
            super.close();
            
            StringBuffer sb = getBuffer();

            _repackager.repackage( sb );

            FileWriter fw = new FileWriter( _file );
            fw.write( sb.toString() );
            fw.close();
        }

        private File _file;
        private Repackager _repackager;
    }
}
