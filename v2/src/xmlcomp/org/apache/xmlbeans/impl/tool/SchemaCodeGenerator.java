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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.schema.SchemaTypeCodePrinter;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.schema.FilerImpl;
import org.apache.xmlbeans.SchemaCodePrinter;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.Filer;
import repackage.Repackager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Reader;
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
    /**
     * Saves a SchemaTypeSystem to the specified directory.
     *
     * @param system the <code>SchemaTypeSystem</code> to save
     * @param classesDir the destination directory for xsb's
     * @param sourceFile if present, the TypeSystemHolder source will be
     *                   generated in this file for subsequent compilation,
     *                   if null then the source will be generated in a temp
     *                   directory and then compiled to the destination dir
     * @param repackager the repackager to use when generating the holder class
     * @param options options. Can be null
     */
    public static void saveTypeSystem(SchemaTypeSystem system, File classesDir,
        File sourceFile, Repackager repackager, XmlOptions options)
        throws IOException
    {
        if (sourceFile == null)
        {
            // KHK: remove this branch once we can generate bytecode directly

            system.saveToDirectory(classesDir);
            options = XmlOptions.maskNull(options);

            // Now generate the holder class
            File source = sourceFile;
            File tempDir = null;

            String filename = SchemaTypeCodePrinter.indexClassForSystem(system).replace('.',
                File.separatorChar) + ".java";
            tempDir = createTempDir();
            File sourcedir = IOUtil.createDir(tempDir, "src");
            source = new File(sourcedir, filename);
            source.getParentFile().mkdirs();

            Writer writer =
                repackager == null
                ? (Writer) new FileWriter( source )
                : (Writer) new RepackagingWriter( source, repackager );

            SchemaTypeCodePrinter.printLoader(writer, system, options);

            writer.close();

            // now compile the generated file to classesDir
            List srcFiles = new ArrayList(1);
            srcFiles.add(source);
            CodeGenUtil.externalCompile(srcFiles, classesDir, null, false);
            tryHardToDelete(tempDir);
        }
        else
        {
            Filer filer = new FilerImpl(classesDir, null, repackager, false, false);
            SchemaTypeSystemCompiler.saveTypeSystem(system, filer, options);
        }
    }

//    private static boolean genTypes(SchemaTypeSystem saver, List sourcefiles, File sourcedir, Repackager repackager, boolean verbose, XmlOptions opts, boolean incrSrcGen)
//    {
//        boolean failure = false;
//
//        List types = new ArrayList();
//        types.addAll(Arrays.asList(saver.globalTypes()));
//        types.addAll(Arrays.asList(saver.documentTypes()));
//        types.addAll(Arrays.asList(saver.attributeTypes()));
//
//        Set seenFiles = null;
//        if (incrSrcGen)
//        {
//            seenFiles = new HashSet();
//            if (sourcefiles != null)
//                for (int i = 0; i < sourcefiles.size(); i++)
//                    seenFiles.add(sourcefiles.get(i));
//        }
//
//        for (Iterator i = types.iterator(); i.hasNext(); )
//        {
//            SchemaType type = (SchemaType)i.next();
//            if (verbose)
//                System.err.println("Compiling type " + type);
//            if (type.isBuiltinType())
//                continue;
//            if (type.getFullJavaName() == null)
//                continue;
//
//            String fjn = type.getFullJavaName();
//
//            if (fjn.indexOf('$') > 0)
//            {
//                fjn =
//                    fjn.substring( 0, fjn.lastIndexOf( '.' ) ) + "." +
//                        fjn.substring( fjn.indexOf( '$' ) + 1 );
//            }
//
//            String filename = fjn.replace('.', File.separatorChar) + ".java";
//
//            Writer writer = null;
//            Reader reader = null;
//            boolean changed = true;
//
//            try
//            {
//                File sourcefile = new File(sourcedir, filename);
//                sourcefile.getParentFile().mkdirs();
//                if (verbose)
//                    System.err.println("created " + sourcefile.getAbsolutePath());
//                if (incrSrcGen)
//                    seenFiles.add(sourcefile);
//                if (incrSrcGen && sourcefile.exists())
//                {
//                    // Generate the file in a buffer and then compare it to the
//                    // file already on disk
//                    // Generation
//                    StringWriter sw = new StringWriter();
//                    SchemaTypeCodePrinter.printType(sw, type, opts);
//                    StringBuffer buffer = sw.getBuffer();
//                    if (repackager != null)
//                        buffer = repackager.repackage(buffer);
//                    // Comparison
//                    List diffs = new ArrayList();
//                    reader = new java.io.FileReader(sourcefile);
//                    String str = buffer.toString();
//                    Diff.readersAsText(new java.io.StringReader(str), "<generated>",
//                            reader, sourcefile.getName(), diffs);
//                    reader.close();
//                    // Check the list of differences
//                    changed = (diffs.size() > 0);
//                    if (changed)
//                    {
//                        // Diffs encountered, replace the file with the text from
//                        // the buffer
//                        writer = new FileWriter( sourcefile );
//                        writer.write(str);
//                        writer.close();
//                        sourcefiles.add(sourcefile);
//                    }
//                    else
//                        ; // No diffs, don't do anything
//                }
//                else
//                {
//                    writer =
//                        repackager == null
//                            ? (Writer) new FileWriter( sourcefile )
//                            : (Writer) new RepackagingWriter( sourcefile, repackager );
//
//
//                    SchemaTypeCodePrinter.printType(writer, type, opts);
//
//                    writer.close();
//
//                    sourcefiles.add(sourcefile);
//                }
//            }
//            catch (IOException e)
//            {
//                System.err.println("IO Error " + e);
//                failure = true;
//            }
//            finally {
//                try {
//                    if (writer != null) writer.close();
//                    if (reader != null) reader.close();
//                } catch (IOException e) {}
//            }
//
//            try
//            {
//                // Generate Implementation class
//                filename = type.getFullJavaImplName().replace('.', File.separatorChar) + ".java";
//                File implFile = new File(sourcedir,  filename);
//                if (verbose)
//                    System.err.println("created " + implFile.getAbsolutePath());
//                implFile.getParentFile().mkdirs();
//
//                if (incrSrcGen)
//                    seenFiles.add(implFile);
//                // If the interface did not change, the implementation shouldn't either
//                if (changed)
//                {
//                writer =
//                    repackager == null
//                        ? (Writer) new FileWriter( implFile )
//                        : (Writer) new RepackagingWriter( implFile, repackager );
//
//                SchemaTypeCodePrinter.printTypeImpl(writer, type, opts);
//
//                writer.close();
//
//                sourcefiles.add(implFile);
//                }
//            }
//            catch (IOException e)
//            {
//                System.err.println("IO Error " + e);
//                failure = true;
//            }
//            finally {
//                try { if (writer != null) writer.close(); } catch (IOException e) {}
//            }
//        }
//
//        if (incrSrcGen)
//            deleteObsoleteFiles(sourcedir, sourcedir, seenFiles);
//
//        return failure;
//    }

    private static void deleteObsoleteFiles(File rootDir, File srcDir, Set seenFiles)
    {
        if (!(rootDir.isDirectory() && srcDir.isDirectory()))
            throw new IllegalArgumentException();
        // Go recursively starting with srcDir and delete all files that are
        // not in the given Set
        File[] files = srcDir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
                deleteObsoleteFiles(rootDir, files[i], seenFiles);
            else if (seenFiles.contains(files[i]))
                ;
            else
            {
                files[i].delete();
                deleteDirRecursively(rootDir, files[i].getParentFile());
            }
        }
    }

    private static void deleteDirRecursively(File root, File dir)
    {
        while (dir.list().length == 0 && !dir.equals(root))
        {
            dir.delete();
            dir = dir.getParentFile();
        }
    }

    protected static File createTempDir() throws IOException
    {

// Some beta builds of JDK1.5 are having troubles creating temp directories
// if the java.io.tmpdir doesn't exist.  This seems to help.
try {
  File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
  tmpDirFile.mkdirs();
} catch(Exception e) { e.printStackTrace(); }

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

            FileWriter fw = new FileWriter( _file );
            fw.write( _repackager.repackage( getBuffer() ).toString() );
            fw.close();
        }

        private File _file;
        private Repackager _repackager;
    }
}
