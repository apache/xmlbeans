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
package xmlobject.detailed;

import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.util.JarUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit Test file to test XmlObject Abstract base class
 */

public class XmlObjectAbstractClassTest {

    /**
     * The test entry point.
     */
    @Test
    void testAbstractBaseClass() throws Exception {
        // create the source file
        //String src = JarUtil.getResourceFromJarasStream(Common.XMLCASES_JAR, "xbean/xmlobject/SimpleXmlObject.java.txt");
        File to = new File("build/SimpleXmlObject.java");
        InputStreamReader r = new InputStreamReader(JarUtil.getResourceFromJarasStream("xbean/xmlobject/SimpleXmlObject.java.txt"));
        Assertions.assertDoesNotThrow(() -> copyTo(r, to));
        Assertions.assertDoesNotThrow(() -> compileFile(to));
        to.deleteOnExit();
    }


    /**
     * Compiles the source file.
     * The destination for the compiled file is the current directory
     */
    private boolean compileFile(File source) {
        // the location for the compiled file
        File dir = new File("build");
        File[] classpath = CodeGenUtil.systemClasspath();
        List<File> srcFiles = new ArrayList<File>();
        srcFiles.add(source);

        return CodeGenUtil.externalCompile(srcFiles, dir, classpath, false,
            CodeGenUtil.DEFAULT_COMPILER, null, CodeGenUtil.DEFAULT_MEM_START,
            CodeGenUtil.DEFAULT_MEM_MAX, false, false, null);
    }

    /**
     * Copies a file. If destination file exists it will be overwritten
     */
    private void copyTo(InputStreamReader src, File to) throws IOException {
        // inputstream to read in the file
        BufferedReader in = new BufferedReader(src);

        // delete the existing file
        to.delete();
        to.createNewFile();
        // outputstream to write out the java file
        OutputStream fos = Files.newOutputStream(to.toPath());
        int b;

        while ((b = in.read()) != -1) {
            fos.write(b);
        }

        in.close();
        fos.close();
    }

    /**
     * Copies a file. If destination file exists it will be overwritten
     */
    private void copyTo(File src, File to) throws IOException {
        // inputstream to read in the file
        FileInputStream fis = new FileInputStream(src);

        // delete the existing file
        to.delete();
        to.createNewFile();
        // outputstream to write out the java file
        OutputStream fos = Files.newOutputStream(to.toPath());
        int b;

        while ((b = fis.read()) != -1) {
            fos.write(b);
        }
        fis.close();
        fos.close();
    }
}
