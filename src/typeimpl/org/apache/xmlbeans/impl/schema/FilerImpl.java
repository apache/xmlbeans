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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.impl.common.IOUtil;

import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import repackage.Repackager;

/**
 * This implementation of Filer writes to disk.
 */
public class FilerImpl implements Filer
{
    private File classdir;
    private File srcdir;
    private Repackager repackager;
    private boolean verbose;
    private List sourceFiles;
    private boolean incrSrcGen;
    private Set seenTypes;

    public FilerImpl(File classdir, File srcdir, Repackager repackager, boolean verbose, boolean incrSrcGen)
    {
        this.classdir = classdir;
        this.srcdir = srcdir;
        this.repackager = repackager;
        this.verbose = verbose;
        this.sourceFiles = (sourceFiles != null ? sourceFiles : new ArrayList());
        this.incrSrcGen = incrSrcGen;
        if (this.incrSrcGen)
            seenTypes = new HashSet();
    }

    /**
     * Creates a new schema binary file (.xsb) and returns a stream for writing to it.
     *
     * @param typename fully qualified type name
     * @return a stream to write the type to
     * @throws java.io.IOException
     */
    public OutputStream createBinaryFile(String typename) throws IOException
    {
        if (verbose)
            System.err.println("created binary: " + typename);
        // KHK: for now the typename will already be a relative filename for the binary
        //String filename = typename.replace('.', File.separatorChar) + ".xsb";
        File source = new File(classdir, typename);
        source.getParentFile().mkdirs();

        return new FileOutputStream( source );
    }

    /**
     * Creates a new binding source file (.java) and returns a writer for it.
     *
     * @param typename fully qualified type name
     * @return a stream to write the type to
     * @throws java.io.IOException
     */
    public Writer createSourceFile(String typename) throws IOException
    {
        if (incrSrcGen)
            seenTypes.add(typename);

        if (typename.indexOf('$') > 0)
        {
            typename =
                typename.substring( 0, typename.lastIndexOf( '.' ) ) + "." +
                typename.substring( typename.indexOf( '$' ) + 1 );
        }

        String filename = typename.replace('.', File.separatorChar) + ".java";

        File sourcefile = new File(srcdir, filename);
        sourcefile.getParentFile().mkdirs();
        if (verbose)
            System.err.println("created source: " + sourcefile.getAbsolutePath());

        if (incrSrcGen && sourcefile.exists())
        {
            // KHK: ?
        }

        sourceFiles.add(sourcefile);

        return repackager == null
            ? (Writer) new FileWriter( sourcefile )
            : (Writer) new RepackagingWriter( sourcefile, repackager );
    }

    public List getSourceFiles()
    {
        return new ArrayList(sourceFiles);
    }

    public Repackager getRepackager()
    {
        return repackager;
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
